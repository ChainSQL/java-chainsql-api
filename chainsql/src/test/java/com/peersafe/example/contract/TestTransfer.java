package com.peersafe.example.contract;

import com.peersafe.abi.TypeReference;
import com.peersafe.abi.datatypes.Address;
import com.peersafe.abi.datatypes.Function;
import com.peersafe.abi.datatypes.Type;
import com.peersafe.abi.datatypes.generated.Uint256;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.exception.ContractCallException;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.tuples.generated.Tuple2;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the com.peersafe.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.4.0.
 */
public class TestTransfer extends Contract {
    private static final String BINARY = "6080604052336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550610375806100536000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806347734892146100675780635f7807a4146100be578063893d20e8146100fe5780638da5cb5b1461015c575b600080fd5b34801561007357600080fd5b506100a8600480360381019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506101b3565b6040518082815260200191505060405180910390f35b6100fc600480360381019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019092919050505061022f565b005b34801561010a57600080fd5b506101136102f2565b604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019250505060405180910390f35b34801561016857600080fd5b50610171610324565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561021057600080fd5b8173ffffffffffffffffffffffffffffffffffffffff16319050919050565b600a811015156102a7576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260088152602001807f706565727361666500000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b8173ffffffffffffffffffffffffffffffffffffffff166108fc829081150290604051600060405180830381858888f193505050501580156102ed573d6000803e3d6000fd5b505050565b6000806000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff166001809050915091509091565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff16815600a165627a7a72305820e13be05a6508c98e6f0c702a7addc06ec990c847ae07d9e2a298331c66ebfb880029";

    public static final String FUNC_GETUSERBALANCE = "getUserBalance";

    public static final String FUNC_TRANSFERTOUSER = "transferToUser";

    public static final String FUNC_GETOWNER = "getOwner";

    public static final String FUNC_OWNER = "owner";

    protected TestTransfer(Chainsql chainsql, String contractAddress, BigInteger gasLimit) {
        super(chainsql,BINARY, contractAddress, gasLimit);
    }

    public BigInteger getUserBalance(String to) throws ContractCallException {
        final Function function = new Function(FUNC_GETUSERBALANCE, 
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(to)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public void getUserBalance(String to, Publisher.Callback<BigInteger> cb) throws ContractCallException {
        final Function function = new Function(FUNC_GETUSERBALANCE, 
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(to)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        executeCallSingleValueReturn(function, BigInteger.class,new Publisher.Callback<BigInteger>() {
            @Override
            public void called(BigInteger args) {
                cb.called(args);
            }
        });
    }

    public Contract transferToUser(String to, BigInteger amount, BigInteger dropValue) {
        final Function function = new Function(
                FUNC_TRANSFERTOUSER, 
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(to), 
                new com.peersafe.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, dropValue);
    }

    public Tuple2<String, BigInteger> getOwner() throws ContractCallException {
        final Function function = new Function(FUNC_GETOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = executeCallMultipleValueReturn(function);
        return new Tuple2<String, BigInteger>(AccountID.fromString((
                (String) results.get(0).getValue()).substring(2)).toString(), 
                (BigInteger) results.get(1).getValue());
    }

    public void getOwner(Publisher.Callback<Tuple2<String, BigInteger>> cb) throws ContractCallException {
        final Function function = new Function(FUNC_GETOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        executeCallMultipleValueReturn(function, new Publisher.Callback<List<Type>>() {
            @Override
            public void called(List<Type> results) {
                Tuple2<String, BigInteger> ret = new Tuple2<String, BigInteger>(AccountID.fromString((
                        (String) results.get(0).getValue()).substring(2)).toString(), 
                        (BigInteger) results.get(1).getValue());
                cb.called(ret);
            }
        });
    }

    public String owner() throws ContractCallException {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        String address = executeRemoteCallSingleValueReturn(function, String.class);
        return AccountID.fromString(address.substring(2)).toString();
    }

    public void owner(Publisher.Callback<String> cb) throws ContractCallException {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        executeCallSingleValueReturn(function, String.class,new Publisher.Callback<String>() {
            @Override
            public void called(String args) {
                cb.called(AccountID.fromString(args.substring(2)).toString());
            }
        });
    }

    public static TestTransfer deploy(Chainsql chainsql, BigInteger gasLimit, BigInteger initialDropsValue) throws TransactionException {
        return deployRemoteCall(TestTransfer.class,chainsql, gasLimit, BINARY, "", initialDropsValue);
    }

    public static void deploy(Chainsql chainsql, BigInteger gasLimit, BigInteger initialDropsValue, Publisher.Callback<TestTransfer> cb) throws TransactionException {
        deployRemoteCall(TestTransfer.class,chainsql, gasLimit, BINARY, "", initialDropsValue, cb);
    }

    public static TestTransfer load(Chainsql chainsql, String contractAddress, BigInteger gasLimit) {
        return new TestTransfer(chainsql,contractAddress, gasLimit);
    }
}
