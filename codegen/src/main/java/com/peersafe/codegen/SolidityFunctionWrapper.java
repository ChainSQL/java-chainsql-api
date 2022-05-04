package com.peersafe.codegen;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.primitive.Char;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.AbiDefinition.NamedType;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Collection;
import org.web3j.utils.Strings;
import org.web3j.utils.Version;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peersafe.abi.EventEncoder;
import com.peersafe.abi.EventValues;
import com.peersafe.abi.FunctionEncoder;
import com.peersafe.abi.TypeReference;
import com.peersafe.abi.datatypes.Address;
import com.peersafe.abi.datatypes.DynamicArray;
import com.peersafe.abi.datatypes.DynamicStruct;
import com.peersafe.abi.datatypes.Event;
import com.peersafe.abi.datatypes.Function;
import com.peersafe.abi.datatypes.Int;
import com.peersafe.abi.datatypes.StaticArray;
import com.peersafe.abi.datatypes.StaticStruct;
import com.peersafe.abi.datatypes.Type;
import com.peersafe.abi.datatypes.Utf8String;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.exception.ContractCallException;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import io.reactivex.Flowable;
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
    private static final String FILTER = "filter";
    private static final String START_BLOCK = "startBlock";
    private static final String END_BLOCK = "endBlock";
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
    private final HashMap<Integer, ClassName> structClassNameMap = new HashMap<>();
    private final List<AbiDefinition.NamedType> structsNamedTypeList = new ArrayList<>();
    public SolidityFunctionWrapper(boolean useNativeJavaTypes) {
        this(useNativeJavaTypes, new LogGenerationReporter(LOGGER));
    }

    public SolidityFunctionWrapper(boolean useNativeJavaTypes, GenerationReporter reporter) {
        this.useNativeJavaTypes = useNativeJavaTypes;
        this.reporter = reporter;
    }
    
    private void buildStructsNamedTypesList(List<AbiDefinition> abi) {
        structsNamedTypeList.addAll(
                abi.stream()
                        .flatMap(
                                definition -> {
                                    List<AbiDefinition.NamedType> parameters = new ArrayList<>();
                                    parameters.addAll(definition.getInputs());
                                    parameters.addAll(definition.getOutputs());
                                    return parameters.stream()
                                            .filter(
                                                    namedType ->
                                                            namedType.getType().equals("tuple"));
                                })
                        .collect(Collectors.toList()));
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
        classBuilder.addTypes(buildStructTypes(abi));
        classBuilder.addMethods(
                buildFunctionDefinitions(className, classBuilder, abi));
       
        classBuilder.addMethod(buildLoad(className));
//        classBuilder.addMethod(buildLoad(className, Credentials.class, CREDENTIALS));
//        classBuilder.addMethod(buildLoad(className, TransactionManager.class,
//                TRANSACTION_MANAGER));

        addAddressesSupport(classBuilder, addresses);

        write(basePackageName, classBuilder.build(), destinationDir);
    }

    /**
     * Verifies if the two structs are the same. Equal structs means: - They have the same field
     * names - They have the same field types The order of declaring the fields does not matter.
     *
     * @return True if they are the same fields
     */
    private boolean isSameStruct(NamedType base, NamedType target) {
        for (NamedType baseField : base.getComponents()) {
            if (!target.getComponents().stream()
                    .anyMatch(
                            targetField ->
                                    baseField.getType().equals(targetField.getType())
                                            && baseField.getName().equals(targetField.getName())))
                return false;
        }
        return true;
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

    private FieldSpec createEventDefinition(String name, List<NamedTypeName> parameters) {

        CodeBlock initializer = buildVariableLengthEventInitializer(name, parameters);

        return FieldSpec.builder(Event.class, buildEventDefinitionName(name))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(initializer)
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
        	if (functionDefinition.getType().equals("event")) {
                methodSpecs.addAll(buildEventFunctions(functionDefinition, classBuilder));
            }else {
            	if(functionDefinition.getStateMutability().trim().equals("view") || functionDefinition.getStateMutability().trim().equals("pure"))
            		functionDefinition.setConstant(true);
            	if(functionDefinition.getStateMutability().trim().equals("payable"))
            		functionDefinition.setPayable(true);
                if (functionDefinition.getType().equals("function")) {
                	 methodSpecs.addAll(buildFunctions(functionDefinition));
                    if(functionDefinition.isConstant()) {
                    	methodSpecs.add(buildConstantFunctionAsync(functionDefinition));
                    }
                } else if (functionDefinition.getType().equals("constructor")) {
                    constructor = true;
                    methodSpecs.add(buildDeploy(
                            className, functionDefinition));
                    methodSpecs.add(buildDeployAsync(
                    		className, functionDefinition));
                }
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
            
            MethodSpec.Builder deployBuilderAsync = getDeployMethodSpecAsync(
                    className, false);
            methodSpecs.add(buildDeployNoParamsAsync(
            		deployBuilderAsync, className, false));
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
            String className, AbiDefinition functionDefinition) throws ClassNotFoundException {

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
            String className, AbiDefinition functionDefinition) throws ClassNotFoundException {

        boolean isPayable = functionDefinition.isPayable();

        MethodSpec.Builder methodBuilder = getDeployMethodSpecAsync(
                className, isPayable);
        String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
        
//        // add callback parameter
//        ParameterizedTypeName typeName = ParameterizedTypeName.get(
//                ClassName.get(Callback.class), ClassName.get("", className));
//        methodBuilder.addParameter(typeName,CALLBACK);
        
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
        
        // add callback parameter
        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get(Callback.class), ClassName.get("", className));
        builder.addParameter(typeName,CALLBACK);
        
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
            MethodSpec.Builder methodBuilder, List<AbiDefinition.NamedType> namedTypes) throws ClassNotFoundException {

        List<ParameterSpec> inputParameterTypes = buildParameterTypes(namedTypes);

        List<ParameterSpec> nativeInputParameterTypes =
                new ArrayList<>(inputParameterTypes.size());
        for (int i = 0; i < inputParameterTypes.size(); ++i) {
            final TypeName typeName;
            if (namedTypes.get(i).getType().startsWith("tuple")
                    && namedTypes.get(i).getType().contains("[")) {
                typeName = buildStructArrayTypeName(namedTypes.get(i), true);
            }else if (namedTypes.get(i).getType().equals("tuple")) {
                typeName = structClassNameMap.get(namedTypes.get(i).structIdentifier());
            } else {
                typeName = getWrapperType(inputParameterTypes.get(i).type);
            }
            nativeInputParameterTypes.add(
                    ParameterSpec.builder(typeName, inputParameterTypes.get(i).name).build());
        }

        methodBuilder.addParameters(nativeInputParameterTypes);

        if (useNativeJavaTypes) {
            return Collection.join(
                    inputParameterTypes,
                    ", \n",
                    // this results in fully qualified names being generated
                    this::createMappedParameterTypes);
        } else {
            return Collection.join(inputParameterTypes, ", ", parameterSpec -> parameterSpec.name);
        }
    }
    

    private java.util.Collection<? extends AbiDefinition.NamedType> extractNested(
            final AbiDefinition.NamedType namedType) {
        if (namedType.getComponents().size() == 0) {
            return new ArrayList<>();
        } else {
            List<AbiDefinition.NamedType> nestedStructs = new ArrayList<>();
            namedType
                    .getComponents()
                    .forEach(
                            nestedNamedStruct -> {
                                nestedStructs.add(nestedNamedStruct);
                                nestedStructs.addAll(extractNested(nestedNamedStruct));
                            });
            return nestedStructs;
        }
    }
    
    private NamedType normalizeNamedType(NamedType namedType) {
        if (namedType.getType().endsWith("[]") && namedType.getInternalType().endsWith("[]")) {
            return new NamedType(
                    namedType.getName(),
                    namedType.getType().substring(0, namedType.getType().length() - 2),
                    namedType.getComponents(),
                    namedType
                            .getInternalType()
                            .substring(0, namedType.getInternalType().length() - 2),
                    namedType.isIndexed());
        } else {
            return namedType;
        }
    }
    
    @NotNull
    private List<AbiDefinition.NamedType> extractStructs(
            final List<AbiDefinition> functionDefinitions) {

    	final HashMap<Integer, AbiDefinition.NamedType> structMap = new LinkedHashMap<>();
        functionDefinitions.stream()
                .flatMap(
                        definition -> {
                            List<AbiDefinition.NamedType> parameters = new ArrayList<>();
                            parameters.addAll(definition.getInputs());
                            parameters.addAll(definition.getOutputs());
                            return parameters.stream()
                            		.map(this::normalizeNamedType)
                            		.filter(namedType -> namedType.getType().equals("tuple"));
                        })
                .forEach(
                        namedType -> {
                            structMap.put(namedType.structIdentifier(), namedType);
                            extractNested(namedType).stream()
                                    .filter(
                                            nestedNamedStruct ->
                                                    nestedNamedStruct.getType().equals("tuple"))
                                    .forEach(
                                            nestedNamedType ->
                                                    structMap.put(
                                                            nestedNamedType.structIdentifier(),
                                                            nestedNamedType));
                        });

        return structMap.values().stream()
                .sorted(Comparator.comparingInt(AbiDefinition.NamedType::nestedness))
                .collect(Collectors.toList());
    }
    private List<TypeSpec> buildStructTypes(final List<AbiDefinition> functionDefinitions)
            throws ClassNotFoundException {
        final List<AbiDefinition.NamedType> orderedKeys = extractStructs(functionDefinitions);
        int structCounter = 0;
        final List<TypeSpec> structs = new ArrayList<>();
        for (final AbiDefinition.NamedType namedType : orderedKeys) {
            final String internalType = namedType.getInternalType();
            final String structName;
            if (internalType == null || internalType.isEmpty()) {
                structName = "Struct" + structCounter;
            } else {
                structName = internalType.substring(internalType.lastIndexOf(".") + 1);
            }

            final TypeSpec.Builder builder =
                    TypeSpec.classBuilder(structName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            final MethodSpec.Builder constructorBuilder =
                    MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement(
                                    "super("
                                            + buildStructConstructorParameterDefinition(
                                                    namedType.getComponents(), useNativeJavaTypes)
                                            + ")");

            final MethodSpec.Builder nativeConstructorBuilder =
                    MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement(
                                    "super("
                                            + buildStructConstructorParameterDefinition(
                                                    namedType.getComponents(), false)
                                            + ")");

            for (AbiDefinition.NamedType component : namedType.getComponents()) {
                if (component.getType().equals("tuple")) {
                    final ClassName typeName = structClassNameMap.get(component.structIdentifier());
                    builder.addField(typeName, component.getName(), Modifier.PUBLIC);
                    constructorBuilder.addParameter(typeName, component.getName());
                    nativeConstructorBuilder.addParameter(typeName, component.getName());
                    constructorBuilder.addStatement(
                            "this." + component.getName() + " = "  + component.getName());
                } else {
                    final TypeName nativeTypeName =
                            buildTypeName(component.getType(), false);
                    final TypeName wrappedTypeName = getWrapperType(nativeTypeName);
                    // TODO
                  //  builder.addField(wrappedTypeName, component.getName(), Modifier.PUBLIC);
                    builder.addField(nativeTypeName, component.getName(), Modifier.PUBLIC);
                    constructorBuilder.addParameter(wrappedTypeName, component.getName());
                    nativeConstructorBuilder.addParameter(nativeTypeName, component.getName());
                    constructorBuilder.addStatement(
                            "this." + component.getName() + " = new "  + nativeTypeName.toString() +"("+ component.getName() + ")"
                            );
                }
              
                nativeConstructorBuilder.addStatement(
                        "this."
                                + component.getName()
                                + " = "
                                + component.getName());
                              /*  + (useNativeJavaTypes
                                                && structClassNameMap.keySet().stream()
                                                        .noneMatch(
                                                                i ->
                                                                        i
                                                                                == component
                                                                                        .structIdentifier())
                                        ? ".getValue()"
                                        : ""));*/
            }

            builder.superclass(namedType.isDynamic() ? DynamicStruct.class : StaticStruct.class);
            builder.addMethod(constructorBuilder.build());
            if (useNativeJavaTypes
                    && !namedType.getComponents().isEmpty()
                    && namedType.getComponents().stream()
                            .anyMatch(
                                    component ->
                                            structClassNameMap.keySet().stream()
                                                    .noneMatch(
                                                            i ->
                                                                    i
                                                                            == component
                                                                                    .structIdentifier()))) {
                builder.addMethod(nativeConstructorBuilder.build());
            }
            structClassNameMap.put(namedType.structIdentifier(), ClassName.get("", structName));
            structs.add(builder.build());
            structCounter++;
        }
        return structs;
    }
    
    
    private String createMappedParameterTypes(ParameterSpec parameterSpec) {
        if (parameterSpec.type instanceof ParameterizedTypeName) {
            List<TypeName> typeNames = ((ParameterizedTypeName) parameterSpec.type).typeArguments;
            if (typeNames.size() != 1) {
                throw new UnsupportedOperationException(
                        "Only a single parameterized type is supported");
            } else if (structClassNameMap.values().stream()
                    .map(ClassName::simpleName)
                    .anyMatch(
                            name ->
                                    name.equals(
                                            ((ClassName)
                                                            ((ParameterizedTypeName)
                                                                            parameterSpec.type)
                                                                    .typeArguments.get(0))
                                                    .simpleName()))) {
                String structName =
                        structClassNameMap.values().stream()
                                .map(ClassName::simpleName)
                                .filter(
                                        name ->
                                                name.equals(
                                                        ((ClassName)
                                                                        ((ParameterizedTypeName)
                                                                                        parameterSpec
                                                                                                .type)
                                                                                .typeArguments.get(
                                                                                        0))
                                                                .simpleName()))
                                .collect(Collectors.toList())
                                .get(0);
                return "new "
                        + parameterSpec.type
                        + "("
                        + structName
                        + ".class, "
                        + parameterSpec.name
                        + ")";
            } else {
                String parameterSpecType = parameterSpec.type.toString();
                TypeName typeName = typeNames.get(0);
                String typeMapInput = typeName + ".class";
                String componentType = typeName.toString();
                if (typeName instanceof ParameterizedTypeName) {
                    List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
                    if (typeArguments.size() != 1) {
                        throw new UnsupportedOperationException(
                                "Only a single parameterized type is supported");
                    }
                    TypeName innerTypeName = typeArguments.get(0);
                    componentType = ((ParameterizedTypeName) typeName).rawType.toString();
                    parameterSpecType =
                            ((ParameterizedTypeName) parameterSpec.type).rawType
                                    + "<"
                                    + componentType
                                    + ">";
                    typeMapInput = componentType + ".class,\n" + innerTypeName + ".class";
                }
                return "new "
                        + parameterSpecType
                        + "(\n"
                        + "        "
                        + componentType
                        + ".class,\n"
                        + "        com.peersafe.abi.Utils.typeMap("
                        + parameterSpec.name
                        + ", "
                        + typeMapInput
                        + "))";
            }
        } else if (structClassNameMap.values().stream()
                .map(ClassName::simpleName)
                .noneMatch(name -> name.equals(parameterSpec.type.toString()))) {
            String constructor = "new " + parameterSpec.type + "(";
            // TODO
            /*if (Address.class.getCanonicalName().equals(parameterSpec.type.toString())
                    && addressLength != Address.DEFAULT_LENGTH) {

                constructor += (addressLength * java.lang.Byte.SIZE) + ", ";
            }*/
            return constructor + parameterSpec.name + ")";
        } else {
            return parameterSpec.name;
        }
    }
    
   /* private String createMappedParameterTypes(ParameterSpec parameterSpec) {
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
*/
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

    static TypeName getNativeType(TypeName typeName) {

        if (typeName instanceof ParameterizedTypeName) {
            return getNativeType((ParameterizedTypeName) typeName);
        }

        String simpleName = ((ClassName) typeName).simpleName();

        if (simpleName.equals(Address.class.getSimpleName())) {
            return TypeName.get(String.class);
        } else if (simpleName.startsWith("Uint")) {
            return TypeName.get(BigInteger.class);
        } else if (simpleName.equals(Utf8String.class.getSimpleName())) {
            return TypeName.get(String.class);
        } else if (simpleName.startsWith("Bytes") || simpleName.equals("DynamicBytes")) {
            return TypeName.get(byte[].class);
        } else if (simpleName.startsWith("Bool")) {
            return TypeName.get(java.lang.Boolean.class);
            // boolean cannot be a parameterized type
        } else if (simpleName.equals(Byte.class.getSimpleName())) {
            return TypeName.get(java.lang.Byte.class);
        } else if (simpleName.equals(Char.class.getSimpleName())) {
            return TypeName.get(Character.class);
        } else if (simpleName.equals(Double.class.getSimpleName())) {
            return TypeName.get(java.lang.Double.class);
        } else if (simpleName.equals(Float.class.getSimpleName())) {
            return TypeName.get(java.lang.Float.class);
        } else if (simpleName.equals(Int.class.getSimpleName())) {
            return TypeName.get(Integer.class);
        } else if (simpleName.equals(Long.class.getSimpleName())) {
            return TypeName.get(java.lang.Long.class);
        } else if (simpleName.equals(Short.class.getSimpleName())) {
            return TypeName.get(java.lang.Short.class);
        } else if (simpleName.startsWith("Int")) {
            return TypeName.get(BigInteger.class);
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported type: " + typeName + ", no native type mapping exists.");
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

    private List<ParameterSpec> buildParameterTypes(List<AbiDefinition.NamedType> namedTypes) throws ClassNotFoundException {
        List<ParameterSpec> result = new ArrayList<>(namedTypes.size());
        for (int i = 0; i < namedTypes.size(); i++) {
            AbiDefinition.NamedType namedType = namedTypes.get(i);

            String name = createValidParamName(namedType.getName(), i);
            String type = namedTypes.get(i).getType();

           if (type.startsWith("tuple") && type.contains("[")) {
                result.add(
                        ParameterSpec.builder(buildStructArrayTypeName(namedType, false), name)
                                .build());
            }else if (type.equals("tuple")) {
                result.add(
                        ParameterSpec.builder(
                                        structClassNameMap.get(namedType.structIdentifier()), name)
                                .build());
            } else {
                result.add(ParameterSpec.builder(buildTypeName(type), name).build());
            }
            
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

    public List<TypeName> buildTypeNames(List<AbiDefinition.NamedType> namedTypes) throws ClassNotFoundException {
        List<TypeName> result = new ArrayList<>(namedTypes.size());
        for (AbiDefinition.NamedType namedType : namedTypes) {
        	if (namedType.getType().startsWith("tuple")
                     && namedType.getType().contains("[")) 
        		result.add(buildStructArrayTypeName(namedType, false));
             else if (namedType.getType().equals("tuple")) {
                result.add(structClassNameMap.get(namedType.structIdentifier()));
            } else {
                result.add(buildTypeName(namedType.getType()));
            }
        }
        return result;
    }
    
/*    private List<TypeName> buildTypeNames(
            List<AbiDefinition.NamedType> namedTypes, boolean primitives)
            throws ClassNotFoundException {

        List<TypeName> result = new ArrayList<>(namedTypes.size());
        for (AbiDefinition.NamedType namedType : namedTypes) {
            if (namedType.getType().equals("tuple")) {
                result.add(structClassNameMap.get(namedType.structIdentifier()));
            } else {
                result.add(buildTypeName(namedType.getType(), primitives));
            }
        }
        return result;
    }*/

    /**
     * Builds the array of struct type name. In case of using the Java native types, we return the
     * <code>List<struct></code> class Else, we return the Web3j generated types.
     *
     * @param namedType Array of structs namedType
     * @param useNativeJavaTypes Set to true for java native types
     * @return ParametrizedTypeName of the array of structs, eg, <code>StaticArray3<StructName>
     *     </code>
     */
    private TypeName buildStructArrayTypeName(AbiDefinition.NamedType namedType, Boolean useNativeJavaTypes) {
        String structName;
        if (namedType.getInternalType().isEmpty()) {
            structName =
                    structClassNameMap
                            .get(
                                    structsNamedTypeList.stream()
                                            .filter(struct -> isSameStruct(namedType, struct))
                                            .collect(Collectors.toList())
                                            .get(0)
                                            .structIdentifier())
                            .simpleName();

        } else {
            structName =
                    namedType
                            .getInternalType()
                            .substring(
                                    namedType.getInternalType().lastIndexOf(".") + 1,
                                    namedType.getInternalType().indexOf("["));
        }

        if (useNativeJavaTypes)
            return ParameterizedTypeName.get(
                    ClassName.get(List.class), ClassName.get("", structName));

        String arrayLength =
                namedType
                        .getType()
                        .substring(
                                namedType.getType().indexOf('[') + 1,
                                namedType.getType().indexOf(']'));
        if (!arrayLength.isEmpty() && Integer.parseInt(arrayLength) > 0) {
            return ParameterizedTypeName.get(
                    ClassName.get("com.peersafe.abi.datatypes.generated", "StaticArray" + arrayLength),
                    ClassName.get("", structName));
        } else {
            return ParameterizedTypeName.get(
                    ClassName.get(DynamicArray.class), ClassName.get("", structName));
        }
    }
    
    private String buildStructConstructorParameterDefinition(
            final List<AbiDefinition.NamedType> components, final boolean useNativeJavaTypes)
            throws ClassNotFoundException {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < components.size(); i++) {
            final AbiDefinition.NamedType component = components.get(i);
            stringBuilder.append(i > 0 ? "," : "");
            stringBuilder.append(
                    (!component.getType().equals("tuple") && useNativeJavaTypes
                            ? "new " + buildTypeName(component.getType(), false) + "("
                            : ""));
            stringBuilder.append(
                    (component.getType().equals("tuple")
                            ? component.getName()
                            : useNativeJavaTypes
                                    ? component.getName() + ")"
                                    : component.getName()));
        }
        return stringBuilder.toString();
    }

    private TypeName buildStructArrayTypeNameForEvent(
    		final List<AbiDefinition.NamedType> components, final boolean useNativeJavaTypes) 
    				throws ClassNotFoundException{
    	 final StringBuilder stringBuilder = new StringBuilder();
         for (int i = 0; i < components.size(); i++) {
             final AbiDefinition.NamedType component = components.get(i);
             stringBuilder.append(i > 0 ? "," : "");
             stringBuilder.append(
                     (!component.getType().equals("tuple") && useNativeJavaTypes
                             ? "new " + buildTypeName(component.getType(), false) + "("
                             : ""));
             stringBuilder.append(
                     (component.getType().equals("tuple")
                             ? component.getName()
                             : useNativeJavaTypes
                                     ? component.getName() + ")"
                                     : component.getName()));
         }
		return null;
    }
    
    List<MethodSpec> buildFunctions(AbiDefinition functionDefinition)
            throws ClassNotFoundException {

        List<MethodSpec> results = new ArrayList<>(2);
        String functionName = functionDefinition.getName();

        String stateMutability = functionDefinition.getStateMutability();
        boolean pureOrView = "pure".equals(stateMutability) || "view".equals(stateMutability);
        boolean isFunctionDefinitionConstant = functionDefinition.isConstant() || pureOrView;

            // If the solidity function name is a reserved word
            // in the current java version prepend it with "_"
            if (!SourceVersion.isName(functionName)) {
                functionName = "_" + functionName;
            }

        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder(functionName).addModifiers(Modifier.PUBLIC);

        final String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
        final List<TypeName> outputParameterTypes =
                buildTypeNames(functionDefinition.getOutputs());

        if (isFunctionDefinitionConstant) {
            // Avoid generating runtime exception call
            if (functionDefinition.hasOutputs()) {
                buildConstantFunction(
                        functionDefinition,
                        methodBuilder,
                        outputParameterTypes,
                        inputParams);

                results.add(methodBuilder.build());
            }
            /*if (generateSendTxForCalls) {
                AbiDefinition sendFuncDefinition = new AbiDefinition(functionDefinition);
                sendFuncDefinition.setConstant(false);
                results.addAll(buildFunctions(sendFuncDefinition));
            }*/
        }

        if (!isFunctionDefinitionConstant) {
            buildTransactionFunction(functionDefinition, methodBuilder, inputParams);
            results.add(methodBuilder.build());
        }

        return results;
    }

    
    private MethodSpec buildConstantFunctionAsync(
            AbiDefinition functionDefinition) throws ClassNotFoundException{
    	String functionName = functionDefinition.getName();

        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder(functionName)
                        .addModifiers(Modifier.PUBLIC);

        String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
        List<TypeName> outputParameterTypes = buildTypeNames(functionDefinition.getOutputs());
        
        buildConstantFunctionAsync(
        		functionDefinition, methodBuilder, outputParameterTypes, inputParams);
        
        return methodBuilder.build();
    }

/*    private static ParameterizedTypeName buildRemoteFunctionCall(TypeName typeName) {
        return ParameterizedTypeName.get(ClassName.get(RemoteFunctionCall.class), typeName);
    }
    */
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
            if (functionDefinition.getOutputs().get(0).getType().equals("tuple")) 
                nativeReturnTypeName =
                        structClassNameMap.get(
                                functionDefinition.getOutputs().get(0).structIdentifier());
            else if (useNativeJavaTypes) {
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
                           "return executeCallSingleValueReturn(function, $T.class)",nativeReturnTypeName);
                    //callCode.addStatement("return convertToNative(result)");

//                    TypeSpec callableType = TypeSpec.anonymousClassBuilder("")
//                            .addSuperinterface(ParameterizedTypeName.get(
//                                    ClassName.get(Callable.class), nativeReturnTypeName))
//                            .addMethod(MethodSpec.methodBuilder("call")
//                                    .addAnnotation(Override.class)
//                                    .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
//                                            .addMember("value", "$S", "unchecked")
//                                            .build())
//                                    .addModifiers(Modifier.PUBLIC)
//                                    .addException(Exception.class)
//                                    .returns(nativeReturnTypeName)
//                                    .addCode(callCode.build())
//                                    .build())
//                            .build();

//                    methodBuilder.addStatement("return new $T(\n$L)",
//                            buildRemoteCall(nativeReturnTypeName), callableType);
//                    methodBuilder.addStatement("return new $T(\n$L)",
//                            nativeReturnTypeName, callableType);
                    methodBuilder.addCode(callCode.build());
                } else {
                	String simpleName = ((ClassName) typeName).simpleName();
                    if (simpleName.equals(Address.class.getSimpleName())) {
                    	methodBuilder.addStatement(
                    					"String address = executeRemoteCallSingleValueReturn(function, $T.class)",
                    						nativeReturnTypeName)
                    				.addStatement("return $T.fromString(address.substring(2)).toString()",TypeName.get(AccountID.class));

                    }else {
                        methodBuilder.addStatement(
                                "return executeRemoteCallSingleValueReturn(function, $T.class)",
                                nativeReturnTypeName);
                    }
                }
            } else {
                methodBuilder.addStatement("return executeRemoteCallSingleValueReturn(function)");
            }
        } else {
        	
        	final List<TypeName> returnTypes = new ArrayList<>();
            for (int i = 0; i < functionDefinition.getOutputs().size(); ++i) {
                if (functionDefinition.getOutputs().get(i).getType().equals("tuple")) {
                    returnTypes.add(
                            structClassNameMap.get(
                                    functionDefinition.getOutputs().get(i).structIdentifier()));
                } else {
                    returnTypes.add(getWrapperType(outputParameterTypes.get(i)));
                }
            }
            //List<TypeName> returnTypes = buildReturnTypes(outputParameterTypes);

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

    private void buildConstantFunctionAsync(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            List<TypeName> outputParameterTypes,
            String inputParams) throws ClassNotFoundException {

        String functionName = functionDefinition.getName();
        
        methodBuilder.returns(TypeName.VOID);
        methodBuilder.addException(ContractCallException.class);
        if (outputParameterTypes.isEmpty()) {
            throw new RuntimeException("Only transactional methods should have void return types");
        } else if (outputParameterTypes.size() == 1) {

            TypeName typeName0 = outputParameterTypes.get(0);
            TypeName nativeReturnTypeName;
            if (functionDefinition.getOutputs().get(0).getType().equals("tuple")) 
                nativeReturnTypeName =
                        structClassNameMap.get(
                                functionDefinition.getOutputs().get(0).structIdentifier());
            else if (useNativeJavaTypes) {
                nativeReturnTypeName = getWrapperRawType(typeName0);
            } else {
                nativeReturnTypeName = getWrapperType(typeName0);
            }
            
        	ParameterizedTypeName typeName = ParameterizedTypeName.get(
                    ClassName.get(Callback.class), nativeReturnTypeName);
        	
        	methodBuilder.addParameter(typeName,CALLBACK,Modifier.FINAL);
        	
            methodBuilder.addStatement("final $T function = "
                            + "new $T($N, \n$T.<$T>asList($L), "
                            + "\n$T.<$T<?>>asList(new $T<$T>() {}))",
                    Function.class, Function.class, funcNameToConst(functionName),
                    Arrays.class, Type.class, inputParams,
                    Arrays.class, TypeReference.class,
                    TypeReference.class, typeName0);

            if (useNativeJavaTypes) {
            	TypeSpec callback = null;
                if (nativeReturnTypeName.equals(ClassName.get(List.class))) {
                    // We return list. So all the list elements should
                    // also be converted to native types
                	TypeName listType = ParameterizedTypeName.get(List.class, Type.class);
                    callback = TypeSpec.anonymousClassBuilder("")
            			    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Publisher.Callback.class),
            			    		nativeReturnTypeName))
            			    .addMethod(MethodSpec.methodBuilder("called")
            				        .addAnnotation(Override.class)
            				        .addModifiers(Modifier.PUBLIC)
            				        .addParameter(nativeReturnTypeName, "args")
            		                .addStatement("cb.called(args)")
            				        .build())
            				    .build();
                    
                } else {
                	String simpleName = ((ClassName) typeName0).simpleName();
                    if (simpleName.equals(Address.class.getSimpleName())) {
//                    	methodBuilder.addStatement(
//                    					"String address = executeRemoteCallSingleValueReturn(function, $T.class)",
//                    						nativeReturnTypeName)
//                    				.addStatement("return $T.fromString(address.substring(2)).toString()",TypeName.get(AccountID.class));
                    	callback = TypeSpec.anonymousClassBuilder("")
                			    .addSuperinterface(ParameterizedTypeName.get(Callback.class, String.class))
                			    .addMethod(MethodSpec.methodBuilder("called")
                				        .addAnnotation(Override.class)
                				        .addModifiers(Modifier.PUBLIC)
                				        .addParameter(nativeReturnTypeName, "args")
                		                .addStatement("cb.called($T.fromString(args.substring(2)).toString())",TypeName.get(AccountID.class))
                				        .build())
                				    .build();

                    }else {
//                        methodBuilder.addStatement(
//                                "return executeRemoteCallSingleValueReturn(function, $T.class)",
//                                nativeReturnTypeName);
                    	callback = TypeSpec.anonymousClassBuilder("")
                			    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Callback.class), nativeReturnTypeName))
                			    .addMethod(MethodSpec.methodBuilder("called")
                				        .addAnnotation(Override.class)
                				        .addModifiers(Modifier.PUBLIC)
                				        .addParameter(nativeReturnTypeName, "args")
                		                .addStatement("cb.called(args)")
                				        .build())
                				    .build();
                    }
                }
                methodBuilder.addStatement("executeCallSingleValueReturn(function, $T.class,$L)",nativeReturnTypeName,callback);
            } else {
//                methodBuilder.addStatement("return executeRemoteCallSingleValueReturn(function)");
            	TypeSpec callback = TypeSpec.anonymousClassBuilder("")
        			    .addSuperinterface(ParameterizedTypeName.get(Callback.class, nativeReturnTypeName.getClass()))
        			    .addMethod(MethodSpec.methodBuilder("called")
        				        .addAnnotation(Override.class)
        				        .addModifiers(Modifier.PUBLIC)
        				        .addParameter(nativeReturnTypeName, "args")
        		                .addStatement("cb.called(args)")
        				        .build())
        				    .build();
            	methodBuilder.addStatement("executeCallSingleValueReturn(function, $T.class,$L)",nativeReturnTypeName,callback);
            }
        } else {
        	final List<TypeName> returnTypes = new ArrayList<>();
            for (int i = 0; i < functionDefinition.getOutputs().size(); ++i) {
                if (functionDefinition.getOutputs().get(i).getType().equals("tuple")) {
                    returnTypes.add(
                            structClassNameMap.get(
                                    functionDefinition.getOutputs().get(i).structIdentifier()));
                } else {
                    returnTypes.add(getWrapperType(outputParameterTypes.get(i)));
                }
            }
        	
        	ParameterizedTypeName typeName = ParameterizedTypeName.get(
                    ClassName.get(
                            "org.web3j.tuples.generated",
                            "Tuple" + outputParameterTypes.size()),
                    outputParameterTypes.toArray(
                            new TypeName[outputParameterTypes.size()]));
        	
           // List<TypeName> returnTypes = buildReturnTypes(outputParameterTypes);

            ParameterizedTypeName parameterizedTupleType = ParameterizedTypeName.get(
                    ClassName.get(
                            "org.web3j.tuples.generated",
                            "Tuple" + returnTypes.size()),
                    returnTypes.toArray(
                            new TypeName[returnTypes.size()]));

//            methodBuilder.returns(parameterizedTupleType);

            buildVariableLengthReturnFunctionConstructor(
                    methodBuilder, functionName, inputParams, outputParameterTypes);
            
            methodBuilder.addParameter(ParameterizedTypeName.get(
                    ClassName.get(Callback.class), parameterizedTupleType),CALLBACK,Modifier.FINAL);

            buildTupleResultContainerAsync(methodBuilder, parameterizedTupleType, outputParameterTypes);
        }
    }
//    private static ParameterizedTypeName buildRemoteCall(TypeName typeName) {
//        return ParameterizedTypeName.get(
//                ClassName.get(RemoteCall.class), typeName);
//    }

   /* private static ParameterizedTypeName buildRemoteFunctionCall(TypeName typeName) {
        return ParameterizedTypeName.get(ClassName.get(RemoteFunctionCall.class), typeName);
    }*/
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
      // methodBuilder.returns(buildRemoteFunctionCall(TypeName.get(TransactionReceipt.class)));
        methodBuilder.addStatement(
                "final $T function = new $T(\n$N, \n$T.<$T>asList($L), \n$T"
                        + ".<$T<?>>emptyList())",
                Function.class,
                Function.class,
                funcNameToConst(functionName),
                Arrays.class,
                Type.class,
                inputParams,
                Collections.class,
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
            List<SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters) {

        TypeSpec.Builder builder =
                TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.STATIC);

       // builder.superclass(BaseEventResponse.class);
        for (SolidityFunctionWrapper.NamedTypeName namedType :
                indexedParameters) {
            final TypeName typeName;
            if (namedType.getType().startsWith("tuple")
                    && namedType.getType().contains("[")) {
                typeName = buildStructArrayTypeName(namedType.namedType, false);
            }else if (namedType.getType().equals("tuple")) {
                typeName = structClassNameMap.get(namedType.structIdentifier());
            } else {
                typeName = getIndexedEventWrapperType(namedType.typeName);
            }
            builder.addField(typeName, namedType.getName(), Modifier.PUBLIC);
        }

        for (SolidityFunctionWrapper.NamedTypeName namedType :
                nonIndexedParameters) {
            final TypeName typeName;
            if (namedType.getType().startsWith("tuple")
                    && namedType.getType().contains("[")) {
                typeName = buildStructArrayTypeName(namedType.namedType, true);
            }else if (namedType.getType().equals("tuple")) {
                typeName = structClassNameMap.get(namedType.structIdentifier());
            }  else {
                typeName = getWrapperType(namedType.typeName);
            }
            builder.addField(typeName, namedType.getName(), Modifier.PUBLIC);
        }

        return builder.build();
    }

    MethodSpec buildEventFlowableFunction(
            String responseClassName,
            String functionName,
            List<SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters)
            throws ClassNotFoundException {

        String generatedFunctionName = Strings.lowercaseFirstLetter(functionName) + "EventFlowable";
        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(
                        ClassName.get(Flowable.class), ClassName.get("", responseClassName));

        MethodSpec.Builder flowableMethodBuilder =
                MethodSpec.methodBuilder(generatedFunctionName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(EthFilter.class, FILTER)
                        .returns(parameterizedTypeName);

        TypeSpec converter =
                TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(
                                ParameterizedTypeName.get(
                                        ClassName.get(io.reactivex.functions.Function.class),
                                        ClassName.get(Log.class),
                                        ClassName.get("", responseClassName)))
                        .addMethod(
                                MethodSpec.methodBuilder("apply")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(Log.class, "log")
                                        .returns(ClassName.get("", responseClassName))
                                        .addStatement(
                                                "$T eventValues = extractEventParametersWithLog("
                                                        + buildEventDefinitionName(functionName)
                                                        + ", log)",
                                                Contract.EventValuesWithLog.class)
                                        .addStatement(
                                                "$1T typedResponse = new $1T()",
                                                ClassName.get("", responseClassName))
                                        .addCode(
                                                buildTypedResponse(
                                                        "typedResponse",
                                                        indexedParameters,
                                                        nonIndexedParameters,
                                                        true))
                                        .addStatement("return typedResponse")
                                        .build())
                        .build();

        flowableMethodBuilder.addStatement(
                "return web3j.ethLogFlowable(filter).map($L)", converter);

        return flowableMethodBuilder.build();
    }

    MethodSpec buildDefaultEventFlowableFunction(String responseClassName, String functionName) {

        String generatedFunctionName = Strings.lowercaseFirstLetter(functionName) + "EventFlowable";
        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(
                        ClassName.get(Flowable.class), ClassName.get("", responseClassName));

        MethodSpec.Builder flowableMethodBuilder =
                MethodSpec.methodBuilder(generatedFunctionName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(DefaultBlockParameter.class, START_BLOCK)
                        .addParameter(DefaultBlockParameter.class, END_BLOCK)
                        .returns(parameterizedTypeName);

        flowableMethodBuilder
                .addStatement(
                        "$1T filter = new $1T($2L, $3L, " + "getContractAddress())",
                        EthFilter.class,
                        START_BLOCK,
                        END_BLOCK)
                .addStatement(
                        "filter.addSingleTopic($T.encode("
                                + buildEventDefinitionName(functionName)
                                + "))",
                        EventEncoder.class)
                .addStatement("return " + generatedFunctionName + "(filter)");

        return flowableMethodBuilder.build();
    }

    MethodSpec buildEventTransactionReceiptFunction(
            String responseClassName,
            String functionName,
            List<NamedTypeName> indexedParameters,
            List<NamedTypeName> nonIndexedParameters) {

        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(
                        ClassName.get(List.class), ClassName.get("", responseClassName));

        String generatedFunctionName =
                "get" + Strings.capitaliseFirstLetter(functionName) + "Events";
        MethodSpec.Builder transactionMethodBuilder =
                MethodSpec.methodBuilder(generatedFunctionName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TransactionReceipt.class, "transactionReceipt")
                        .returns(parameterizedTypeName);

        transactionMethodBuilder
                .addStatement(
                        "$T valueList = extractEventParametersWithLog("
                                + buildEventDefinitionName(functionName)
                                + ", "
                                + "transactionReceipt)",
                        ParameterizedTypeName.get(List.class, Contract.EventValuesWithLog.class))
                .addStatement(
                        "$1T responses = new $1T(valueList.size())",
                        ParameterizedTypeName.get(
                                ClassName.get(ArrayList.class),
                                ClassName.get("", responseClassName)))
                .beginControlFlow(
                        "for ($T eventValues : valueList)", Contract.EventValuesWithLog.class)
                .addStatement("$1T typedResponse = new $1T()", ClassName.get("", responseClassName))
                .addCode(
                        buildTypedResponse(
                                "typedResponse", indexedParameters, nonIndexedParameters, false))
                .addStatement("responses.add(typedResponse)")
                .endControlFlow();

        transactionMethodBuilder.addStatement("return responses");
        return transactionMethodBuilder.build();
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

    List<MethodSpec> buildEventFunctions(
            AbiDefinition functionDefinition, TypeSpec.Builder classBuilder)
            throws ClassNotFoundException {
        String functionName = functionDefinition.getName();
        List<AbiDefinition.NamedType> inputs = functionDefinition.getInputs();
        String responseClassName = Strings.capitaliseFirstLetter(functionName) + "EventResponse";

        List<NamedTypeName> parameters = new ArrayList<>();
        List<NamedTypeName> indexedParameters = new ArrayList<>();
        List<NamedTypeName> nonIndexedParameters = new ArrayList<>();

        for (AbiDefinition.NamedType namedType : inputs) {
            final TypeName typeName;
            if (namedType.getType().startsWith("tuple")
                    && namedType.getType().contains("[")) {
               typeName = buildStructArrayTypeName(namedType, false);
            }else if (namedType.getType().equals("tuple")) {
                typeName = structClassNameMap.get(namedType.structIdentifier());
            } else {
                typeName = buildTypeName(namedType.getType(), false);
            }
            NamedTypeName parameter = new NamedTypeName(namedType, typeName);
            if (namedType.isIndexed()) {
                indexedParameters.add(parameter);
            } else {
                nonIndexedParameters.add(parameter);
            }
            parameters.add(parameter);
        }

        classBuilder.addField(createEventDefinition(functionName, parameters));

        classBuilder.addType(
                buildEventResponseObject(
                        responseClassName, indexedParameters, nonIndexedParameters));


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
      /*  methods.add(
                buildEventTransactionReceiptFunction(
                        responseClassName, functionName, indexedParameters, nonIndexedParameters));

        methods.add(
                buildEventFlowableFunction(
                        responseClassName, functionName, indexedParameters, nonIndexedParameters));
        methods.add(buildDefaultEventFlowableFunction(responseClassName, functionName));*/
        
        return methods;
    }

    CodeBlock buildTypedResponse(
            String objectName,
            List<SolidityFunctionWrapper.NamedTypeName> indexedParameters,
            List<SolidityFunctionWrapper.NamedTypeName> nonIndexedParameters,
            boolean flowable) {
        CodeBlock.Builder builder = CodeBlock.builder();
        if (flowable) {
            builder.addStatement("$L.log = log", objectName);
        } else {
            //builder.addStatement("$L.log = eventValues.getLog()", objectName);
        }
        for (int i = 0; i < indexedParameters.size(); i++) {
            final NamedTypeName namedTypeName = indexedParameters.get(i);
            final String nativeConversion;
            if (useNativeJavaTypes
                    && structClassNameMap.values().stream()
                            .map(ClassName::simpleName)
                            .noneMatch(
                                    name -> name.equals(namedTypeName.getTypeName().toString()))) {
                nativeConversion = ".getValue()";
            } else {
                nativeConversion = "";
            }
            final TypeName indexedEventWrapperType;
            if (namedTypeName.getType().startsWith("tuple")
                    && namedTypeName.getType().contains("[")) {
                indexedEventWrapperType = buildStructArrayTypeName(namedTypeName.namedType, true);
            }else if (namedTypeName.getType().equals("tuple")) {
                indexedEventWrapperType = structClassNameMap.get(namedTypeName.structIdentifier());
            }else {
                indexedEventWrapperType = getIndexedEventWrapperType(namedTypeName.getTypeName());
            }
            builder.addStatement(
                    "$L.$L = ($T) eventValues.getIndexedValues().get($L)" + nativeConversion,
                    objectName,
                    namedTypeName.getName(),
                    indexedEventWrapperType,
                    i);
        }

        for (int i = 0; i < nonIndexedParameters.size(); i++) {
            final NamedTypeName namedTypeName = nonIndexedParameters.get(i);
            final String nativeConversion;
            if (useNativeJavaTypes
                    && structClassNameMap.values().stream()
                            .map(ClassName::simpleName)
                            .noneMatch(
                                    name -> name.equals(namedTypeName.getTypeName().toString()))) {
                nativeConversion = ".getValue()";
            } else {
                nativeConversion = "";
            }
            final TypeName nonIndexedEventWrapperType;
            if (nonIndexedParameters.get(i).getType().startsWith("tuple")
                    && nonIndexedParameters.get(i).getType().contains("[")) {
                nonIndexedEventWrapperType =
                        buildStructArrayTypeName(namedTypeName.namedType, true);
            }else if (nonIndexedParameters.get(i).getType().equals("tuple")) {
                nonIndexedEventWrapperType =
                        structClassNameMap.get(namedTypeName.structIdentifier());
            } else {
                nonIndexedEventWrapperType =
                        getWrapperType(nonIndexedParameters.get(i).getTypeName());
            }
            builder.addStatement(
                    "$L.$L = ($T) eventValues.getNonIndexedValues().get($L)" + nativeConversion,
                    objectName,
                    namedTypeName.getName(),
                    nonIndexedEventWrapperType,
                    i);
        }
        return builder.build();
    }


    static TypeName buildTypeName(String typeDeclaration) throws ClassNotFoundException {
        return buildTypeName(typeDeclaration, false);
    }
    

    static TypeName buildTypeName(String typeDeclaration, boolean primitives)
            throws ClassNotFoundException {

        final String solidityType = trimStorageDeclaration(typeDeclaration);

        final TypeReference typeReference =
                TypeReference.makeTypeReference(solidityType, false, primitives);

        return TypeName.get(typeReference.getType());
    }
    
  /*  public static TypeName buildTypeName(String typeDeclaration) {
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
*/
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
            boolean bAddress = ((ClassName) param).simpleName().equals(Address.class.getSimpleName());
            if (bAddress) {
            	resultString = "$T.fromString((" + resultStringSimple + ").substring(2)).toString()";
            }

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
            if(bAddress) {
            	tupleConstructor
                	.add(resultString, TypeName.get(AccountID.class),convertTo, i);
            }else {
            	tupleConstructor
                	.add(resultString, convertTo, i);
            }
            
            tupleConstructor.add(i < size - 1 ? ", " : ");\n");
        }
        tupleConstructor.add("$<$<");

        methodBuilder.addCode(tupleConstructor.build());
    }

    private void buildTupleResultContainerAsync(
            MethodSpec.Builder methodBuilder, ParameterizedTypeName tupleType,
            List<TypeName> outputParameterTypes)
            throws ClassNotFoundException {

        List<TypeName> typeArguments = tupleType.typeArguments;
    	
        CodeBlock.Builder tupleConstructor = CodeBlock.builder();
        tupleConstructor.add(
                "$T ret = new $T(", tupleType,tupleType)
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
            boolean bAddress = ((ClassName) param).simpleName().equals(Address.class.getSimpleName());
            if (bAddress) {
            	resultString = "$T.fromString((" + resultStringSimple + ").substring(2)).toString()";
            }

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
            if(bAddress) {
            	tupleConstructor
                	.add(resultString, TypeName.get(AccountID.class),convertTo, i);
            }else {
            	tupleConstructor
                	.add(resultString, convertTo, i);
            }
            
            tupleConstructor.add(i < size - 1 ? ", " : ");\n");
        }
        tupleConstructor.add("$<$<");

        ParameterizedTypeName listType = ParameterizedTypeName.get(List.class, Type.class);
    	TypeSpec callback = TypeSpec.anonymousClassBuilder("")
			    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Callback.class), listType))
			    .addMethod(MethodSpec.methodBuilder("called")
				        .addAnnotation(Override.class)
				        .addModifiers(Modifier.PUBLIC)
				        .addParameter(listType, "results")
				        .addCode(tupleConstructor.build())
		                .addStatement("cb.called(ret)")
				        .build())
				    .build();
    	methodBuilder.addStatement("executeCallMultipleValueReturn(function, $L)",callback);
    }
    private static CodeBlock buildVariableLengthEventInitializer(
            String eventName, List<NamedTypeName> parameterTypes) {

        List<Object> objects = new ArrayList<>();
        objects.add(Event.class);
        objects.add(eventName);

        objects.add(Arrays.class);
        objects.add(TypeReference.class);
        for (NamedTypeName parameterType : parameterTypes) {
            objects.add(TypeReference.class);
            objects.add(parameterType.getTypeName());
        }

        String asListParams =
                parameterTypes.stream()
                        .map(
                                type -> {
                                    if (type.isIndexed()) {
                                        return "new $T<$T>(true) {}";
                                    } else {
                                        return "new $T<$T>() {}";
                                    }
                                })
                        .collect(Collectors.joining(", "));

        return CodeBlock.builder()
                .addStatement(
                        "new $T($S, \n" + "$T.<$T<?>>asList(" + asListParams + "))",
                        objects.toArray())
                .build();
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
        private final AbiDefinition.NamedType namedType;

        NamedTypeName(AbiDefinition.NamedType namedType, TypeName typeName) {
            this.namedType = namedType;
            this.typeName = typeName;
        }

        public String getName() {
            return namedType.getName();
        }

        public String getType() {
            return namedType.getType();
        }

        public TypeName getTypeName() {
            return typeName;
        }

        public boolean isIndexed() {
            return namedType.isIndexed();
        }

        public int structIdentifier() {
            return namedType.structIdentifier();
        }
    }
  /*  private static class NamedTypeName {
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
    }*/

}
