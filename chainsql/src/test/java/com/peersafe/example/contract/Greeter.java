package com.peersafe.example.contract;

import com.peersafe.abi.EventValues;
import com.peersafe.abi.FunctionEncoder;
import com.peersafe.abi.TypeReference;
import com.peersafe.abi.datatypes.Event;
import com.peersafe.abi.datatypes.Function;
import com.peersafe.abi.datatypes.Type;
import com.peersafe.abi.datatypes.Utf8String;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.exception.ContractCallException;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the com.peersafe.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.4.0.
 */
public class Greeter extends Contract {
    private static final String BINARY = "60806040526040516107673803806107678339810180604052602081101561002657600080fd5b81019080805164010000000081111561003e57600080fd5b8281019050602081018481111561005457600080fd5b815185600182028301116401000000008211171561007157600080fd5b5050929190505050336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600190805190602001906100cf9291906100d6565b505061017b565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061011757805160ff1916838001178555610145565b82800160010185558215610145579182015b82811115610144578251825591602001919060010190610129565b5b5090506101529190610156565b5090565b61017891905b8082111561017457600081600090555060010161015c565b5090565b90565b6105dd8061018a6000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806341c0e1b51461005c5780634ac0d66e14610073578063cfae32171461013b575b600080fd5b34801561006857600080fd5b506100716101cb565b005b34801561007f57600080fd5b506101396004803603602081101561009657600080fd5b81019080803590602001906401000000008111156100b357600080fd5b8201836020820111156100c557600080fd5b803590602001918460018302840111640100000000831117156100e757600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929050505061025c565b005b34801561014757600080fd5b5061015061046a565b6040518080602001828103825283818151815260200191508051906020019080838360005b83811015610190578082015181840152602081019050610175565b50505050905090810190601f1680156101bd5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141561025a576000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16ff5b565b806040518082805190602001908083835b602083101515610292578051825260208201915060208101905060208303925061026d565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405180910390206001604051808280546001816001161561010002031660029004801561031c5780601f106102fa57610100808354040283529182019161031c565b820191906000526020600020905b815481529060010190602001808311610308575b505091505060405180910390207f047dcd1aa8b77b0b943642129c767533eeacd700c7c1eab092b8ce05d2b2faf56001846040518080602001806020018381038352858181546001816001161561010002031660029004815260200191508054600181600116156101000203166002900480156103da5780601f106103af576101008083540402835291602001916103da565b820191906000526020600020905b8154815290600101906020018083116103bd57829003601f168201915b5050838103825284818151815260200191508051906020019080838360005b838110156104145780820151818401526020810190506103f9565b50505050905090810190601f1680156104415780820380516001836020036101000a031916815260200191505b5094505050505060405180910390a3806001908051906020019061046692919061050c565b5050565b606060018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105025780601f106104d757610100808354040283529160200191610502565b820191906000526020600020905b8154815290600101906020018083116104e557829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061054d57805160ff191683800117855561057b565b8280016001018555821561057b579182015b8281111561057a57825182559160200191906001019061055f565b5b509050610588919061058c565b5090565b6105ae91905b808211156105aa576000816000905550600101610592565b5090565b905600a165627a7a7230582012398d6d6dd7343add7ffa3d517e22314098bd6d5dd880926bcc39064b80175f0029";

    public static final String FUNC_KILL = "kill";

    public static final String FUNC_NEWGREETING = "newGreeting";

    public static final String FUNC_GREET = "greet";

    public static final Event MODIFIED_EVENT = new Event("Modified", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
    ;

    protected Greeter(Chainsql chainsql, String contractAddress, BigInteger gasLimit) {
        super(chainsql,BINARY, contractAddress, gasLimit);
    }

    public Contract kill() {
        final Function function = new Function(
                FUNC_KILL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public Contract newGreeting(String _greeting) {
        final Function function = new Function(
                FUNC_NEWGREETING, 
                Arrays.<Type>asList(new Utf8String(_greeting)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public String greet() throws ContractCallException {
        final Function function = new Function(FUNC_GREET, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public void greet(Publisher.Callback<String> cb) throws ContractCallException {
        final Function function = new Function(FUNC_GREET, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        executeCallSingleValueReturn(function, String.class,new Publisher.Callback<String>() {
            @Override
            public void called(String args) {
                cb.called(args);
            }
        });
    }

    public static Greeter deploy(Chainsql chainsql, BigInteger gasLimit, BigInteger initialDropsValue, String _greeting) throws TransactionException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Utf8String(_greeting)));
        return deployRemoteCall(Greeter.class,chainsql, gasLimit, BINARY, encodedConstructor, initialDropsValue);
    }

    public static void deploy(Chainsql chainsql, BigInteger gasLimit, BigInteger initialDropsValue, Publisher.Callback<Greeter> cb, String _greeting) throws TransactionException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Utf8String(_greeting)));
        deployRemoteCall(Greeter.class,chainsql, gasLimit, BINARY, encodedConstructor, initialDropsValue, cb);
    }

    public void onModifiedEvents(Publisher.Callback<ModifiedEventResponse> cb) {
        super.on(MODIFIED_EVENT, new Publisher.Callback<EventValues>() {
            @Override
            public void called(EventValues eventValues) {
                ModifiedEventResponse typedResponse = new ModifiedEventResponse();
                typedResponse.oldGreetingIdx = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newGreetingIdx = (byte[]) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.oldGreeting = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.newGreeting = (String) eventValues.getNonIndexedValues().get(1).getValue();
                cb.called(typedResponse);
            }
        });
    }

    public static Greeter load(Chainsql chainsql, String contractAddress, BigInteger gasLimit) {
        return new Greeter(chainsql,contractAddress, gasLimit);
    }

    public static class ModifiedEventResponse {
        public byte[] oldGreetingIdx;

        public byte[] newGreetingIdx;

        public String oldGreeting;

        public String newGreeting;
    }
}
