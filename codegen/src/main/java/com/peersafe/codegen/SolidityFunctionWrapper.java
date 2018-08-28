package com.peersafe.codegen;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.utils.Collection;
import org.web3j.utils.Strings;
import org.web3j.utils.Version;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peersafe.abi.EventValues;
import com.peersafe.abi.FunctionEncoder;
import com.peersafe.abi.TypeReference;
import com.peersafe.abi.datatypes.Address;
import com.peersafe.abi.datatypes.Bool;
import com.peersafe.abi.datatypes.DynamicArray;
import com.peersafe.abi.datatypes.DynamicBytes;
import com.peersafe.abi.datatypes.Event;
import com.peersafe.abi.datatypes.Function;
import com.peersafe.abi.datatypes.StaticArray;
import com.peersafe.abi.datatypes.Type;
import com.peersafe.abi.datatypes.Utf8String;
import com.peersafe.abi.datatypes.generated.AbiTypes;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.exception.ContractCallException;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import rx.functions.Func1;

/**
 * Generate Java Classes based on generated Solidity bin and abi files.
 */
public class SolidityFunctionWrapper extends Generator {

    private static final String BINARY = "BINARY";
    private static final String INITIAL_VALUE = "initialDropsValue";
    private static final String CONTRACT_ADDRESS = "contractAddress";
    private static final String GAS_LIMIT = "gasLimit";
    private static final String DROP_VALUE = "dropValue";
    private static final String FUNC_NAME_PREFIX = "FUNC_";
    private static final String CHAINSQL = "chainsql";
    private static final String CALLBACK = "cb";

//    private static final ClassName LOG = ClassName.get(Log.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(SolidityFunctionWrapper.class);

    private static final String CODEGEN_WARNING = "<p>Auto generated code.\n"
            + "<p><strong>Do not modify!</strong>\n"
            + "<p>Please use the "
            + "<a href=\"https://docs.web3j.io/command_line.html\">web3j command line tools</a>,\n"
            + "or the " + SolidityFunctionWrapperGenerator.class.getName() + " in the \n"
            + "<a href=\"https://github.com/web3j/web3j/tree/master/codegen\">"
            + "codegen module</a> to update.\n";

    private final boolean useNativeJavaTypes;
    private static final String regex = "(\\w+)(?:\\[(.*?)\\])(?:\\[(.*?)\\])?";
    private static final Pattern pattern = Pattern.compile(regex);
    private final GenerationReporter reporter;

    public SolidityFunctionWrapper(boolean useNativeJavaTypes) {
        this(useNativeJavaTypes, new LogGenerationReporter(LOGGER));
    }

    public SolidityFunctionWrapper(boolean useNativeJavaTypes, GenerationReporter reporter) {
        this.useNativeJavaTypes = useNativeJavaTypes;
        this.reporter = reporter;
    }

    @SuppressWarnings("unchecked")
    public void generateJavaFiles(
            String contractName, String bin, String abi, String destinationDir,
            String basePackageName)
            throws IOException, ClassNotFoundException {
        generateJavaFiles(contractName, bin,
                loadContractDefinition(abi),
                destinationDir, basePackageName,
                null);
    }

    void generateJavaFiles(
            String contractName, String bin, List<AbiDefinition> abi, String destinationDir,
            String basePackageName, Map<String, String> addresses)
            throws IOException, ClassNotFoundException {
        String className = Strings.capitaliseFirstLetter(contractName);

        TypeSpec.Builder classBuilder = createClassBuilder(className, bin);

//        classBuilder.addMethod(buildConstructor(Credentials.class, CREDENTIALS));
//        classBuilder.addMethod(buildConstructor(TransactionManager.class,
//                TRANSACTION_MANAGER));
        classBuilder.addMethod(buildConstructor());
        
        classBuilder.addFields(buildFuncNameConstants(abi));
        classBuilder.addMethods(
                buildFunctionDefinitions(className, classBuilder, abi));
        
        classBuilder.addMethod(buildLoad(className));
//        classBuilder.addMethod(buildLoad(className, Credentials.class, CREDENTIALS));
//        classBuilder.addMethod(buildLoad(className, TransactionManager.class,
//                TRANSACTION_MANAGER));

        addAddressesSupport(classBuilder, addresses);

        write(basePackageName, classBuilder.build(), destinationDir);
    }

    private void addAddressesSupport(TypeSpec.Builder classBuilder,
                                     Map<String, String> addresses) {
        if (addresses != null) {

            ClassName stringType = ClassName.get(String.class);
            ClassName mapType = ClassName.get(HashMap.class);
            TypeName mapStringString = ParameterizedTypeName.get(mapType, stringType, stringType);
            FieldSpec addressesStaticField = FieldSpec
                    .builder(mapStringString, "_addresses",
                            Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL)
                    .build();
            classBuilder.addField(addressesStaticField);

            final CodeBlock.Builder staticInit = CodeBlock.builder();
            staticInit.addStatement("_addresses = new HashMap<String, String>()");
            addresses.forEach((k, v) ->
                    staticInit.addStatement(String.format("_addresses.put(\"%1s\", \"%2s\")",
                            k, v))
            );
            classBuilder.addStaticBlock(staticInit.build());

            // See org.web3j.tx.Contract#getStaticDeployedAddress(String)
            MethodSpec getAddress = MethodSpec
                    .methodBuilder("getStaticDeployedAddress")
                    .addModifiers(Modifier.PROTECTED)
                    .returns(stringType)
                    .addParameter(stringType, "networkId")
                    .addCode(
                            CodeBlock
                                    .builder()
                                    .addStatement("return _addresses.get(networkId)")
                                    .build())
                    .build();
            classBuilder.addMethod(getAddress);

            MethodSpec getPreviousAddress = MethodSpec
                    .methodBuilder("getPreviouslyDeployedAddress")
                    .addModifiers(Modifier.PUBLIC)
                    .addModifiers(Modifier.STATIC)
                    .returns(stringType)
                    .addParameter(stringType, "networkId")
                    .addCode(
                            CodeBlock
                                    .builder()
                                    .addStatement("return _addresses.get(networkId)")
                                    .build())
                    .build();
            classBuilder.addMethod(getPreviousAddress);

        }
    }


    private TypeSpec.Builder createClassBuilder(String className, String binary) {

        String javadoc = CODEGEN_WARNING + getWeb3jVersion();

        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(javadoc)
                .superclass(Contract.class)
                .addField(createBinaryDefinition(binary));
    }

    private String getWeb3jVersion() {
        String version;

        try {
            // This only works if run as part of the web3j command line tools which contains
            // a version.properties file
            version = Version.getVersion();
        } catch (IOException | NullPointerException e) {
            version = Version.DEFAULT;
        }
        return "\n<p>Generated with web3j version " + version + ".\n";
    }

    private FieldSpec createBinaryDefinition(String binary) {
        return FieldSpec.builder(String.class, BINARY)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", binary)
                .build();
    }

    private FieldSpec createEventDefinition(
            String name,
            List<NamedTypeName> indexedParameters,
            List<NamedTypeName> nonIndexedParameters) {

        CodeBlock initializer = buildVariableLengthEventInitializer(
                name, indexedParameters, nonIndexedParameters);

        return FieldSpec.builder(Event.class, buildEventDefinitionName(name))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(initializer)
                .build();
    }

    private String buildEventDefinitionName(String eventName) {
        return eventName.toUpperCase() + "_EVENT";
    }

    private List<MethodSpec> buildFunctionDefinitions(
            String className,
            TypeSpec.Builder classBuilder,
            List<AbiDefinition> functionDefinitions) throws ClassNotFoundException {

        List<MethodSpec> methodSpecs = new ArrayList<>();
        boolean constructor = false;

        for (AbiDefinition functionDefinition : functionDefinitions) {
            if (functionDefinition.getType().equals("function")) {
                methodSpecs.add(buildFunction(functionDefinition));

            } else if (functionDefinition.getType().equals("event")) {
                methodSpecs.addAll(buildEventFunctions(functionDefinition, classBuilder));

            } else if (functionDefinition.getType().equals("constructor")) {
                constructor = true;
                methodSpecs.add(buildDeploy(
                        className, functionDefinition));
                methodSpecs.add(buildDeployAsync(
                		className, functionDefinition));
            }
        }

        // constructor will not be specified in ABI file if its empty
        if (!constructor) {
//            MethodSpec.Builder credentialsMethodBuilder =
//                    getDeployMethodSpec(className, Credentials.class, CREDENTIALS, false);
//            methodSpecs.add(buildDeployNoParams(
//                    credentialsMethodBuilder, className, CREDENTIALS, false));
//
//            MethodSpec.Builder transactionManagerMethodBuilder =
//                    getDeployMethodSpec(
//                            className, TransactionManager.class, TRANSACTION_MANAGER, false);
//            methodSpecs.add(buildDeployNoParams(
//                    transactionManagerMethodBuilder, className, TRANSACTION_MANAGER, false));
            
            MethodSpec.Builder deployBuilder =
                    getDeployMethodSpec(
                            className, false);
            methodSpecs.add(buildDeployNoParams(
            		deployBuilder, className, false));
            methodSpecs.add(buildDeployNoParamsAsync(
            		deployBuilder, className, false));
        }

        return methodSpecs;
    }

    public Iterable<FieldSpec> buildFuncNameConstants(List<AbiDefinition> functionDefinitions) {
        List<FieldSpec> fields = new ArrayList<>();
        Set<String> fieldNames = new HashSet<>();
        fieldNames.add(Contract.FUNC_DEPLOY);

        for (AbiDefinition functionDefinition : functionDefinitions) {
            if (functionDefinition.getType().equals("function")) {
                String funcName = functionDefinition.getName();

                if (!fieldNames.contains(funcName)) {
                    FieldSpec field = FieldSpec.builder(String.class,
                            funcNameToConst(funcName),
                            Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$S", funcName)
                            .build();
                    fields.add(field);
                    fieldNames.add(funcName);
                }
            }
        }
        return fields;
    }
    
    private static MethodSpec buildConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(Chainsql.class, CHAINSQL)
                .addParameter(String.class, CONTRACT_ADDRESS)
                .addParameter(BigInteger.class, GAS_LIMIT)
                .addStatement("super($N,$N, $N, $N)",
                        CHAINSQL,BINARY, CONTRACT_ADDRESS, GAS_LIMIT)
                .build();
    }

    private MethodSpec buildDeploy(
            String className, AbiDefinition functionDefinition) {

        boolean isPayable = functionDefinition.isPayable();

        MethodSpec.Builder methodBuilder = getDeployMethodSpec(
                className, isPayable);
        String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());

        if (!inputParams.isEmpty()) {
            return buildDeployWithParams(
                    methodBuilder, className, inputParams, isPayable);
        } else {
            return buildDeployNoParams(methodBuilder, className, isPayable);
        }
    }

    private static MethodSpec buildDeployWithParams(
            MethodSpec.Builder methodBuilder, String className, String inputParams,
             boolean isPayable) {

        methodBuilder.addStatement("$T encodedConstructor = $T.encodeConstructor("
                        + "$T.<$T>asList($L)"
                        + ")",
                String.class, FunctionEncoder.class, Arrays.class, Type.class, inputParams);
        if (isPayable) {
            methodBuilder.addStatement(
                    "return deployRemoteCall("
                            + "$L.class,$L, $L, $L, encodedConstructor, $L)",
                    className,CHAINSQL, GAS_LIMIT, BINARY, INITIAL_VALUE);
        } else {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class,$L, $L, $L, encodedConstructor)",
                    className,CHAINSQL, GAS_LIMIT, BINARY);
        }

        return methodBuilder.build();
    }

    private static MethodSpec buildDeployNoParams(
            MethodSpec.Builder methodBuilder, String className,
            boolean isPayable) {
        if (isPayable) {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class,$L, $L, $L, \"\", $L)",
                    className,CHAINSQL, GAS_LIMIT, BINARY, INITIAL_VALUE);
        } else {
            methodBuilder.addStatement(
                    "return deployRemoteCall($L.class, $L, $L, $L, \"\")",
                    className,CHAINSQL, GAS_LIMIT, BINARY);
        }

        return methodBuilder.build();
    }

    private static MethodSpec.Builder getDeployMethodSpec(
            String className, boolean isPayable) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("deploy")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeVariableName.get(className, Type.class))
                .addException(TransactionException.class)
                .addParameter(Chainsql.class,CHAINSQL)
                .addParameter(BigInteger.class, GAS_LIMIT);
        if (isPayable) {
            return builder.addParameter(BigInteger.class, INITIAL_VALUE);
        } else {
            return builder;
        }
    }
    
    private MethodSpec buildDeployAsync(
            String className, AbiDefinition functionDefinition) {

        boolean isPayable = functionDefinition.isPayable();

        MethodSpec.Builder methodBuilder = getDeployMethodSpecAsync(
                className, isPayable);
        String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
        
        // add callback parameter
        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get(Callback.class), ClassName.get("", className));
        methodBuilder.addParameter(typeName,CALLBACK);
        
        if (!inputParams.isEmpty()) {
            return buildDeployWithParamsAsync(
                    methodBuilder, className, inputParams, isPayable);
        } else {
            return buildDeployNoParamsAsync(methodBuilder, className, isPayable);
        }
    }

    private static MethodSpec buildDeployWithParamsAsync(
            MethodSpec.Builder methodBuilder, String className, String inputParams,
             boolean isPayable) {

        methodBuilder.addStatement("$T encodedConstructor = $T.encodeConstructor("
                        + "$T.<$T>asList($L)"
                        + ")",
                String.class, FunctionEncoder.class, Arrays.class, Type.class, inputParams);
        if (isPayable) {
            methodBuilder.addStatement(
                    "deployRemoteCall("
                            + "$L.class,$L, $L, $L, encodedConstructor, $L, $L)",
                    className,CHAINSQL, GAS_LIMIT, BINARY, INITIAL_VALUE,CALLBACK);
        } else {
            methodBuilder.addStatement(
                    "deployRemoteCall($L.class,$L, $L, $L, encodedConstructor, $L)",
                    className,CHAINSQL, GAS_LIMIT, BINARY,CALLBACK);
        }

        return methodBuilder.build();
    }

    private static MethodSpec buildDeployNoParamsAsync(
            MethodSpec.Builder methodBuilder, String className,
            boolean isPayable) {
        if (isPayable) {
            methodBuilder.addStatement(
                    "deployRemoteCall($L.class,$L, $L, $L, \"\", $L, $L)",
                    className,CHAINSQL, GAS_LIMIT, BINARY, INITIAL_VALUE,CALLBACK);
        } else {
            methodBuilder.addStatement(
                    "deployRemoteCall($L.class, $L, $L, $L, \"\", $L)",
                    className,CHAINSQL, GAS_LIMIT, BINARY,CALLBACK);
        }

        return methodBuilder.build();
    }

    private static MethodSpec.Builder getDeployMethodSpecAsync(
            String className, boolean isPayable) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("deploy")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Void.TYPE)
                .addException(TransactionException.class)
                .addParameter(Chainsql.class,CHAINSQL)
                .addParameter(BigInteger.class, GAS_LIMIT);
        if (isPayable) {
            builder.addParameter(BigInteger.class, INITIAL_VALUE);
        }
        return builder;

    }
    private static MethodSpec buildLoad(String className) {
        return MethodSpec.methodBuilder("load")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeVariableName.get(className, Type.class))
                .addParameter(Chainsql.class,CHAINSQL)
                .addParameter(String.class, CONTRACT_ADDRESS)
                .addParameter(BigInteger.class, GAS_LIMIT)
                .addStatement("return new $L($L,$L, $L)", className,
                        CHAINSQL,CONTRACT_ADDRESS, GAS_LIMIT)
                .build();
    }

    String addParameters(
            MethodSpec.Builder methodBuilder, List<AbiDefinition.NamedType> namedTypes) {

        List<ParameterSpec> inputParameterTypes = buildParameterTypes(namedTypes);

        List<ParameterSpec> nativeInputParameterTypes =
                new ArrayList<>(inputParameterTypes.size());
        for (ParameterSpec parameterSpec : inputParameterTypes) {
            TypeName typeName = getWrapperType(parameterSpec.type);
            nativeInputParameterTypes.add(
                    ParameterSpec.builder(typeName, parameterSpec.name).build());
        }

        methodBuilder.addParameters(nativeInputParameterTypes);

        if (useNativeJavaTypes) {
            return Collection.join(
                    inputParameterTypes,
                    ", \n",
                    // this results in fully qualified names being generated
                    this::createMappedParameterTypes);
        } else {
            return Collection.join(
                    inputParameterTypes,
                    ", ",
                    parameterSpec -> parameterSpec.name);
        }
    }

    private String createMappedParameterTypes(ParameterSpec parameterSpec) {
        if (parameterSpec.type instanceof ParameterizedTypeName) {
            List<TypeName> typeNames =
                    ((ParameterizedTypeName) parameterSpec.type).typeArguments;
            if (typeNames.size() != 1) {
                throw new UnsupportedOperationException(
                        "Only a single parameterized type is supported");
            } else {
                String parameterSpecType = parameterSpec.type.toString();
                TypeName typeName = typeNames.get(0);
                String typeMapInput = typeName + ".class";
                if (typeName instanceof ParameterizedTypeName) {
                    List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
                    if (typeArguments.size() != 1) {
                        throw new UnsupportedOperationException(
                                "Only a single parameterized type is supported");
                    }
                    TypeName innerTypeName = typeArguments.get(0);
                    parameterSpecType = ((ParameterizedTypeName) parameterSpec.type)
                            .rawType.toString();
                    typeMapInput = ((ParameterizedTypeName) typeName).rawType + ".class, "
                            + innerTypeName + ".class";
                }
                return "new " + parameterSpecType + "(\n"
                        + "        com.peersafe.abi.Utils.typeMap("
                        + parameterSpec.name + ", " + typeMapInput + "))";
            }
        } else {
            return "new " + parameterSpec.type + "(" + parameterSpec.name + ")";
        }
    }

    private TypeName getWrapperType(TypeName typeName) {
        if (useNativeJavaTypes) {
            return getNativeType(typeName);
        } else {
            return typeName;
        }
    }

    private TypeName getWrapperRawType(TypeName typeName) {
        if (useNativeJavaTypes) {
            if (typeName instanceof ParameterizedTypeName) {
                return ClassName.get(List.class);
            }
            return getNativeType(typeName);
        } else {
            return typeName;
        }
    }

    private TypeName getIndexedEventWrapperType(TypeName typeName) {
        if (useNativeJavaTypes) {
            return getEventNativeType(typeName);
        } else {
            return typeName;
        }
    }

    public static TypeName getNativeType(TypeName typeName) {

        if (typeName instanceof ParameterizedTypeName) {
            return getNativeType((ParameterizedTypeName) typeName);
        }

        String simpleName = ((ClassName) typeName).simpleName();

        if (simpleName.equals(Address.class.getSimpleName())) {
            return TypeName.get(String.class);
        } else if (simpleName.startsWith("Uint")) {
            return TypeName.get(BigInteger.class);
        } else if (simpleName.startsWith("Int")) {
            return TypeName.get(BigInteger.class);
        } else if (simpleName.equals(Utf8String.class.getSimpleName())) {
            return TypeName.get(String.class);
        } else if (simpleName.startsWith("Bytes")) {
            return TypeName.get(byte[].class);
        } else if (simpleName.equals(DynamicBytes.class.getSimpleName())) {
            return TypeName.get(byte[].class);
        } else if (simpleName.equals(Bool.class.getSimpleName())) {
            return TypeName.get(Boolean.class);  // boolean cannot be a parameterized type
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported type: " + typeName
                            + ", no native type mapping exists.");
        }
    }

    public static TypeName getNativeType(ParameterizedTypeName parameterizedTypeName) {
        List<TypeName> typeNames = parameterizedTypeName.typeArguments;
        List<TypeName> nativeTypeNames = new ArrayList<>(typeNames.size());
        for (TypeName enclosedTypeName : typeNames) {
            nativeTypeNames.add(getNativeType(enclosedTypeName));
        }
        return ParameterizedTypeName.get(
                ClassName.get(List.class),
                nativeTypeNames.toArray(new TypeName[nativeTypeNames.size()]));
    }

    public static TypeName getEventNativeType(TypeName typeName) {
        if (typeName instanceof ParameterizedTypeName) {
            return TypeName.get(byte[].class);
        }

        String simpleName = ((ClassName) typeName).simpleName();
        if (simpleName.equals(Utf8String.class.getSimpleName())) {
            return TypeName.get(byte[].class);
        } else {
            return getNativeType(typeName);
        }
    }

    static List<ParameterSpec> buildParameterTypes(List<AbiDefinition.NamedType> namedTypes) {
        List<ParameterSpec> result = new ArrayList<>(namedTypes.size());
        for (int i = 0; i < namedTypes.size(); i++) {
            AbiDefinition.NamedType namedType = namedTypes.get(i);

            String name = createValidParamName(namedType.getName(), i);
            String type = namedTypes.get(i).getType();

            result.add(ParameterSpec.builder(buildTypeName(type), name).build());
        }
        return result;
    }

    /**
     * Public Solidity arrays and maps require an unnamed input parameter - multiple if they
     * require a struct type.
     *
     * @param name parameter name
     * @param idx  parameter index
     * @return non-empty parameter name
     */
    public static String createValidParamName(String name, int idx) {
        if (name.equals("")) {
            return "param" + idx;
        } else {
            return name;
        }
    }

    public static List<TypeName> buildTypeNames(List<AbiDefinition.NamedType> namedTypes) {
        List<TypeName> result = new ArrayList<>(namedTypes.size());
        for (AbiDefinition.NamedType namedType : namedTypes) {
            result.add(buildTypeName(namedType.getType()));
        }
        return result;
    }

    public MethodSpec buildFunction(
            AbiDefinition functionDefinition) throws ClassNotFoundException {
        String functionName = functionDefinition.getName();

        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder(functionName)
                        .addModifiers(Modifier.PUBLIC);

        String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());

        List<TypeName> outputParameterTypes = buildTypeNames(functionDefinition.getOutputs());
        if (functionDefinition.isConstant()) {
            buildConstantFunction(
                    functionDefinition, methodBuilder, outputParameterTypes, inputParams);
        } else {
            buildTransactionFunction(
                    functionDefinition, methodBuilder, inputParams);
        }

        return methodBuilder.build();
    }

    private void buildConstantFunction(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            List<TypeName> outputParameterTypes,
            String inputParams) throws ClassNotFoundException {

        String functionName = functionDefinition.getName();

        methodBuilder.addException(ContractCallException.class);
        if (outputParameterTypes.isEmpty()) {
            throw new RuntimeException("Only transactional methods should have void return types");
        } else if (outputParameterTypes.size() == 1) {

            TypeName typeName = outputParameterTypes.get(0);
            TypeName nativeReturnTypeName;
            if (useNativeJavaTypes) {
                nativeReturnTypeName = getWrapperRawType(typeName);
            } else {
                nativeReturnTypeName = getWrapperType(typeName);
            }
            methodBuilder.returns(nativeReturnTypeName);

            methodBuilder.addStatement("final $T function = "
                            + "new $T($N, \n$T.<$T>asList($L), "
                            + "\n$T.<$T<?>>asList(new $T<$T>() {}))",
                    Function.class, Function.class, funcNameToConst(functionName),
                    Arrays.class, Type.class, inputParams,
                    Arrays.class, TypeReference.class,
                    TypeReference.class, typeName);

            if (useNativeJavaTypes) {
                if (nativeReturnTypeName.equals(ClassName.get(List.class))) {
                    // We return list. So all the list elements should
                    // also be converted to native types
                    TypeName listType = ParameterizedTypeName.get(List.class, Type.class);

                    CodeBlock.Builder callCode = CodeBlock.builder();
                    callCode.addStatement(
                            "$T result = "
                            + "($T) executeCallSingleValueReturn(function, $T.class)",
                            listType, listType, nativeReturnTypeName);
                    callCode.addStatement("return convertToNative(result)");

                    TypeSpec callableType = TypeSpec.anonymousClassBuilder("")
                            .addSuperinterface(ParameterizedTypeName.get(
                                    ClassName.get(Callable.class), nativeReturnTypeName))
                            .addMethod(MethodSpec.methodBuilder("call")
                                    .addAnnotation(Override.class)
                                    .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                            .addMember("value", "$S", "unchecked")
                                            .build())
                                    .addModifiers(Modifier.PUBLIC)
                                    .addException(Exception.class)
                                    .returns(nativeReturnTypeName)
                                    .addCode(callCode.build())
                                    .build())
                            .build();

//                    methodBuilder.addStatement("return new $T(\n$L)",
//                            buildRemoteCall(nativeReturnTypeName), callableType);
                    methodBuilder.addStatement("return new $T(\n$L)",
                            nativeReturnTypeName, callableType);
                } else {
                    methodBuilder.addStatement(
                            "return executeRemoteCallSingleValueReturn(function, $T.class)",
                            nativeReturnTypeName);
                }
            } else {
                methodBuilder.addStatement("return executeRemoteCallSingleValueReturn(function)");
            }
        } else {
            List<TypeName> returnTypes = buildReturnTypes(outputParameterTypes);

            ParameterizedTypeName parameterizedTupleType = ParameterizedTypeName.get(
                    ClassName.get(
                            "org.web3j.tuples.generated",
                            "Tuple" + returnTypes.size()),
                    returnTypes.toArray(
                            new TypeName[returnTypes.size()]));

//            methodBuilder.returns(buildRemoteCall(parameterizedTupleType));
            methodBuilder.returns(parameterizedTupleType);

            buildVariableLengthReturnFunctionConstructor(
                    methodBuilder, functionName, inputParams, outputParameterTypes);

            buildTupleResultContainer(methodBuilder, parameterizedTupleType, outputParameterTypes);
        }
    }

//    private static ParameterizedTypeName buildRemoteCall(TypeName typeName) {
//        return ParameterizedTypeName.get(
//                ClassName.get(RemoteCall.class), typeName);
//    }

    private void buildTransactionFunction(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            String inputParams) throws ClassNotFoundException {

//    	methodBuilder.addException(TransactionException.class);
    	
        if (functionDefinition.hasOutputs()) {
            //CHECKSTYLE:OFF
            reporter.report(String.format(
                    "Definition of the function %s returns a value but is not defined as a view function. "
                            + "Please ensure it contains the view modifier if you want to read the return value",
                    functionDefinition.getName()));
            //CHECKSTYLE:ON
        }

        if (functionDefinition.isPayable()) {
            methodBuilder.addParameter(BigInteger.class, DROP_VALUE);
        }

        String functionName = functionDefinition.getName();

//        methodBuilder.returns(buildRemoteCall(TypeName.get(TransactionReceipt.class)));
        methodBuilder.returns(TypeName.get(Contract.class));

        methodBuilder.addStatement("final $T function = new $T(\n$N, \n$T.<$T>asList($L), \n$T"
                        + ".<$T<?>>emptyList())",
                Function.class, Function.class, funcNameToConst(functionName),
                Arrays.class, Type.class, inputParams, Collections.class,
                TypeReference.class);
        if (functionDefinition.isPayable()) {
            methodBuilder.addStatement(
                    "return executeRemoteCallTransaction(function, $N)", DROP_VALUE);
        } else {
            methodBuilder.addStatement("return executeRemoteCallTransaction(function)");
        }
    }

    TypeSpec buildEventResponseObject(
            String className,
            List<com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters) {

        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

//        builder.addField(LOG, "log", Modifier.PUBLIC);
        for (com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName
                namedType : indexedParameters) {
            TypeName typeName = getIndexedEventWrapperType(namedType.typeName);
            builder.addField(typeName, namedType.getName(), Modifier.PUBLIC);
        }

        for (com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName
                namedType : nonIndexedParameters) {
            TypeName typeName = getWrapperType(namedType.typeName);
            builder.addField(typeName, namedType.getName(), Modifier.PUBLIC);
        }

        return builder.build();
    }

    MethodSpec buildEventObservableFunction(
            String responseClassName,
            String functionName,
            List<com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters)
            throws ClassNotFoundException {

        String generatedFunctionName =
                Strings.lowercaseFirstLetter(functionName) + "EventObservable";
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(rx
                .Observable.class), ClassName.get("", responseClassName));

        MethodSpec.Builder observableMethodBuilder =
                MethodSpec.methodBuilder(generatedFunctionName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(parameterizedTypeName);

        TypeSpec converter = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(Func1.class),
//                        ClassName.get(Log.class),
                        ClassName.get("", responseClassName)))
                .addMethod(MethodSpec.methodBuilder("call")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
//                        .addParameter(Log.class, "log")
                        .returns(ClassName.get("", responseClassName))
                        .addStatement("$T eventValues = extractEventParametersWithLog("
                                        + buildEventDefinitionName(functionName) + ", log)",
                                Contract.EventValuesWithLog.class)
                        .addStatement("$1T typedResponse = new $1T()",
                                ClassName.get("", responseClassName))
                        .addCode(buildTypedResponse("typedResponse", indexedParameters,
                                nonIndexedParameters, true))
                        .addStatement("return typedResponse")
                        .build())
                .build();

        observableMethodBuilder
                .addStatement("return web3j.ethLogObservable(filter).map($L)", converter);

        return observableMethodBuilder
                .build();
    }

    MethodSpec buildDefaultEventObservableFunction(
            String responseClassName,
            String functionName) {

        String generatedFunctionName =
                Strings.lowercaseFirstLetter(functionName) + "EventObservable";
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(rx
                .Observable.class), ClassName.get("", responseClassName));

        MethodSpec.Builder observableMethodBuilder =
                MethodSpec.methodBuilder(generatedFunctionName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(parameterizedTypeName);

//        observableMethodBuilder.addStatement("$1T filter = new $1T($2L, $3L, "
//                + "getContractAddress())", EthFilter.class, START_BLOCK, END_BLOCK)
//                .addStatement("filter.addSingleTopic($T.encode("
//                        + buildEventDefinitionName(functionName) + "))", EventEncoder.class)
//                .addStatement("return " + generatedFunctionName + "(filter)");

        return observableMethodBuilder
                .build();
    }

/*    public MethodSpec buildEventTransactionReceiptFunction(
            String responseClassName,
            String functionName,
            List<NamedTypeName> indexedParameters,
            List<NamedTypeName> nonIndexedParameters) {

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(List.class), ClassName.get("", responseClassName));

        String generatedFunctionName = "get" + Strings.capitaliseFirstLetter(functionName)
                + "Events";
        MethodSpec.Builder transactionMethodBuilder = MethodSpec
                .methodBuilder(generatedFunctionName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TransactionReceipt.class, "transactionReceipt")
                .returns(parameterizedTypeName);

        transactionMethodBuilder.addStatement("$T valueList = extractEventParametersWithLog("
                + buildEventDefinitionName(functionName) + ", "
                + "transactionReceipt)", ParameterizedTypeName.get(List.class,
                        Contract.EventValuesWithLog.class))
                .addStatement("$1T responses = new $1T(valueList.size())",
                        ParameterizedTypeName.get(ClassName.get(ArrayList.class),
                                ClassName.get("", responseClassName)))
                .beginControlFlow("for ($T eventValues : valueList)",
                        Contract.EventValuesWithLog.class)
                .addStatement("$1T typedResponse = new $1T()",
                        ClassName.get("", responseClassName))
                .addCode(buildTypedResponse("typedResponse", indexedParameters,
                        nonIndexedParameters, false))
                .addStatement("responses.add(typedResponse)")
                .endControlFlow();


        transactionMethodBuilder.addStatement("return responses");
        return transactionMethodBuilder.build();
    }
    */
    public MethodSpec buildEventFunction(
            String responseClassName,
            String functionName,
            List<NamedTypeName> indexedParameters,
            List<NamedTypeName> nonIndexedParameters) {

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(Callback.class), ClassName.get("", responseClassName));

        String generatedFunctionName = "on" + Strings.capitaliseFirstLetter(functionName)
                + "Events";
        
        
        MethodSpec.Builder transactionMethodBuilder = MethodSpec
                .methodBuilder(generatedFunctionName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterizedTypeName, "cb")
                .returns(TypeName.VOID);

		TypeSpec callback = TypeSpec.anonymousClassBuilder("")
			    .addSuperinterface(ParameterizedTypeName.get(Callback.class, EventValues.class))
			    .addMethod(MethodSpec.methodBuilder("called")
			        .addAnnotation(Override.class)
			        .addModifiers(Modifier.PUBLIC)
			        .addParameter(EventValues.class, "eventValues")
			        .addStatement("$1T typedResponse = new $1T()",
	                        ClassName.get("", responseClassName))
	                .addCode(buildTypedResponse("typedResponse", indexedParameters,
	                        nonIndexedParameters, false))
	                .addStatement("cb.called(typedResponse)")
			        .build())
			    .build();
        transactionMethodBuilder.addStatement("super.on(" 
        		+ buildEventDefinitionName(functionName) + ", $L)",callback);

        return transactionMethodBuilder.build();
    }

    public List<MethodSpec> buildEventFunctions(
            AbiDefinition functionDefinition,
            TypeSpec.Builder classBuilder) throws ClassNotFoundException {
        String functionName = functionDefinition.getName();
        List<AbiDefinition.NamedType> inputs = functionDefinition.getInputs();
        String responseClassName = Strings.capitaliseFirstLetter(functionName) + "EventResponse";

        List<NamedTypeName> indexedParameters = new ArrayList<>();
        List<NamedTypeName> nonIndexedParameters = new ArrayList<>();

        for (AbiDefinition.NamedType namedType : inputs) {

            if (namedType.isIndexed()) {
                indexedParameters.add(
                        new com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName(
                                namedType.getName(),
                                buildTypeName(namedType.getType())));
            } else {
                nonIndexedParameters.add(
                        new com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName(
                                namedType.getName(),
                                buildTypeName(namedType.getType())));
            }
        }

        classBuilder.addField(createEventDefinition(functionName,
                indexedParameters, nonIndexedParameters));

        classBuilder.addType(buildEventResponseObject(responseClassName, indexedParameters,
                nonIndexedParameters));

        List<MethodSpec> methods = new ArrayList<>();
        methods.add(buildEventFunction(responseClassName,
              functionName, indexedParameters, nonIndexedParameters));
//        methods.add(buildEventTransactionReceiptFunction(responseClassName,
//                functionName, indexedParameters, nonIndexedParameters));
//
//        methods.add(buildEventObservableFunction(responseClassName, functionName,
//                indexedParameters, nonIndexedParameters));
//        methods.add(buildDefaultEventObservableFunction(responseClassName,
//                functionName));
        return methods;
    }

    public CodeBlock buildTypedResponse(
            String objectName,
            List<com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<com.peersafe.codegen.SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters,
            boolean observable) {
        String nativeConversion;

        if (useNativeJavaTypes) {
            nativeConversion = ".getValue()";
        } else {
            nativeConversion = "";
        }

        CodeBlock.Builder builder = CodeBlock.builder();
//        if (observable) {
//            builder.addStatement("$L.log = log", objectName);
//        } else {
//            builder.addStatement(
//                    "$L.log = eventValues.getLog()",
//                    objectName);
//        }
        for (int i = 0; i < indexedParameters.size(); i++) {
            builder.addStatement(
                    "$L.$L = ($T) eventValues.getIndexedValues().get($L)" + nativeConversion,
                    objectName,
                    indexedParameters.get(i).getName(),
                    getIndexedEventWrapperType(indexedParameters.get(i).getTypeName()),
                    i);
        }

        for (int i = 0; i < nonIndexedParameters.size(); i++) {
            builder.addStatement(
                    "$L.$L = ($T) eventValues.getNonIndexedValues().get($L)" + nativeConversion,
                    objectName,
                    nonIndexedParameters.get(i).getName(),
                    getWrapperType(nonIndexedParameters.get(i).getTypeName()),
                    i);
        }
        return builder.build();
    }

    public static TypeName buildTypeName(String typeDeclaration) {
        String type = trimStorageDeclaration(typeDeclaration);
        Matcher matcher = pattern.matcher(type);
        if (matcher.find()) {
            Class<?> baseType = AbiTypes.getType(matcher.group(1));
            String firstArrayDimension = matcher.group(2);
            String secondArrayDimension = matcher.group(3);

            TypeName typeName;

            if ("".equals(firstArrayDimension)) {
                typeName = ParameterizedTypeName.get(DynamicArray.class, baseType);
            } else {
                Class<?> rawType = getStaticArrayTypeReferenceClass(firstArrayDimension);
                typeName = ParameterizedTypeName.get(rawType, baseType);
            }

            if (secondArrayDimension != null) {
                if ("".equals(secondArrayDimension)) {
                    return ParameterizedTypeName.get(ClassName.get(DynamicArray.class), typeName);
                } else {
                    Class<?> rawType = getStaticArrayTypeReferenceClass(secondArrayDimension);
                    return ParameterizedTypeName.get(ClassName.get(rawType), typeName);
                }
            }

            return typeName;
        } else {
            Class<?> cls = AbiTypes.getType(type);
            return ClassName.get(cls);
        }
    }

    private static Class<?> getStaticArrayTypeReferenceClass(String type) {
        try {
            return Class.forName("com.peersafe.abi.datatypes.generated.StaticArray" + type);
        } catch (ClassNotFoundException e) {
            // Unfortunately we can't encode it's length as a type if it's > 32.
            return StaticArray.class;
        }
    }

    private static String trimStorageDeclaration(String type) {
        if (type.endsWith(" storage") || type.endsWith(" memory")) {
            return type.split(" ")[0];
        } else {
            return type;
        }
    }

    private List<TypeName> buildReturnTypes(List<TypeName> outputParameterTypes) {
        List<TypeName> result = new ArrayList<>(outputParameterTypes.size());
        for (TypeName typeName : outputParameterTypes) {
            result.add(getWrapperType(typeName));
        }
        return result;
    }

    private static void buildVariableLengthReturnFunctionConstructor(
            MethodSpec.Builder methodBuilder, String functionName, String inputParameters,
            List<TypeName> outputParameterTypes) throws ClassNotFoundException {

        List<Object> objects = new ArrayList<>();
        objects.add(Function.class);
        objects.add(Function.class);
        objects.add(funcNameToConst(functionName));

        objects.add(Arrays.class);
        objects.add(Type.class);
        objects.add(inputParameters);

        objects.add(Arrays.class);
        objects.add(TypeReference.class);
        for (TypeName outputParameterType : outputParameterTypes) {
            objects.add(TypeReference.class);
            objects.add(outputParameterType);
        }

        String asListParams = Collection.join(
                outputParameterTypes,
                ", ",
                typeName -> "new $T<$T>() {}");

        methodBuilder.addStatement("final $T function = new $T($N, \n$T.<$T>asList($L), \n$T"
                + ".<$T<?>>asList("
                + asListParams + "))", objects.toArray());
    }

    private void buildTupleResultContainer(
            MethodSpec.Builder methodBuilder, ParameterizedTypeName tupleType,
            List<TypeName> outputParameterTypes)
            throws ClassNotFoundException {

        List<TypeName> typeArguments = tupleType.typeArguments;

        CodeBlock.Builder tupleConstructor = CodeBlock.builder();
        tupleConstructor.addStatement(
                "$T results = executeCallMultipleValueReturn(function)",
                ParameterizedTypeName.get(List.class, Type.class))
                .add("return new $T(", tupleType)
                .add("$>$>");

        String resultStringSimple = "\n($T) results.get($L)";
        if (useNativeJavaTypes) {
            resultStringSimple += ".getValue()";
        }

        String resultStringNativeList =
                "\nconvertToNative(($T) results.get($L).getValue())";

        int size = typeArguments.size();
        ClassName classList = ClassName.get(List.class);

        for (int i = 0; i < size; i++) {
            TypeName param = outputParameterTypes.get(i);
            TypeName convertTo = typeArguments.get(i);

            String resultString = resultStringSimple;

            // If we use native java types we need to convert
            // elements of arrays to native java types too
            if (useNativeJavaTypes && param instanceof ParameterizedTypeName) {
                ParameterizedTypeName oldContainer = (ParameterizedTypeName)param;
                ParameterizedTypeName newContainer = (ParameterizedTypeName)convertTo;
                if (newContainer.rawType.compareTo(classList) == 0
                        && newContainer.typeArguments.size() == 1) {
                    convertTo = ParameterizedTypeName.get(classList,
                            oldContainer.typeArguments.get(0));
                    resultString = resultStringNativeList;
                }
            }

            tupleConstructor
                .add(resultString, convertTo, i);
            tupleConstructor.add(i < size - 1 ? ", " : ");\n");
        }
        tupleConstructor.add("$<$<");

        TypeSpec callableType = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(Callable.class), tupleType))
                .addMethod(MethodSpec.methodBuilder("call")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addException(Exception.class)
                        .returns(tupleType)
                        .addCode(tupleConstructor.build())
                        .build())
                .build();

//        methodBuilder.addStatement(
//                "return new $T(\n$L)", buildRemoteCall(tupleType), callableType);
        methodBuilder.addStatement(
                "return new $T(\n$L)", tupleType, callableType);
    }

    private static CodeBlock buildVariableLengthEventInitializer(
            String eventName,
            List<NamedTypeName> indexedParameterTypes,
            List<NamedTypeName> nonIndexedParameterTypes) {

        List<Object> objects = new ArrayList<>();
        objects.add(Event.class);
        objects.add(eventName);

        objects.add(Arrays.class);
        objects.add(TypeReference.class);
        for (NamedTypeName indexedParameterType : indexedParameterTypes) {
            objects.add(TypeReference.class);
            objects.add(indexedParameterType.getTypeName());
        }

        objects.add(Arrays.class);
        objects.add(TypeReference.class);
        for (NamedTypeName indexedParameterType : nonIndexedParameterTypes) {
            objects.add(TypeReference.class);
            objects.add(indexedParameterType.getTypeName());
        }

        String indexedAsListParams = Collection.join(
                indexedParameterTypes,
                ", ",
                typeName -> "new $T<$T>() {}");

        String nonIndexedAsListParams = Collection.join(
                nonIndexedParameterTypes,
                ", ",
                typeName -> "new $T<$T>() {}");

        return CodeBlock.builder()
                .addStatement("new $T($S, \n"
                        + "$T.<$T<?>>asList(" + indexedAsListParams + "),\n"
                        + "$T.<$T<?>>asList(" + nonIndexedAsListParams + "))", objects.toArray())
                .build();
    }

    private List<AbiDefinition> loadContractDefinition(String abi) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(abi, AbiDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    private static String funcNameToConst(String funcName) {
        return FUNC_NAME_PREFIX + funcName.toUpperCase();
    }

    private static class NamedTypeName {
        private final TypeName typeName;
        private final String name;

        NamedTypeName(String name, TypeName typeName) {
            this.name = name;
            this.typeName = typeName;
        }

        public String getName() {
            return name;
        }

        public TypeName getTypeName() {
            return typeName;
        }
    }

}
