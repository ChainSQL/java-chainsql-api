package com.peersafe.example.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import com.peersafe.abi.EventValues;
import com.peersafe.abi.FunctionEncoder;
import com.peersafe.abi.TypeReference;
import com.peersafe.abi.datatypes.Event;
import com.peersafe.abi.datatypes.Function;
import com.peersafe.abi.datatypes.Type;
import com.peersafe.abi.datatypes.Utf8String;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.RemoteCall;
import com.peersafe.chainsql.contract.TransactionReceipt;
import com.peersafe.chainsql.core.Chainsql;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.chainsql.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.4.0.
 */
public class Greeter extends Contract {
    private static final String BINARY = "60806040526040516106b53803806106b583398101806040528101908080518201929190505050336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550806001908051906020019061007c929190610083565b5050610128565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100c457805160ff19168380011785556100f2565b828001600101855582156100f2579182015b828111156100f15782518255916020019190600101906100d6565b5b5090506100ff9190610103565b5090565b61012591905b80821115610121576000816000905550600101610109565b5090565b90565b61057e806101376000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806341c0e1b51461005c5780634ac0d66e14610073578063cfae3217146100dc575b600080fd5b34801561006857600080fd5b5061007161016c565b005b34801561007f57600080fd5b506100da600480360381019080803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192905050506101fd565b005b3480156100e857600080fd5b506100f161040b565b6040518080602001828103825283818151815260200191508051906020019080838360005b83811015610131578082015181840152602081019050610116565b50505050905090810190601f16801561015e5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614156101fb576000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16ff5b565b806040518082805190602001908083835b602083101515610233578051825260208201915060208101905060208303925061020e565b6001836020036101000a0380198251168184511680821785525050505050509050019150506040518091039020600160405180828054600181600116156101000203166002900480156102bd5780601f1061029b5761010080835404028352918201916102bd565b820191906000526020600020905b8154815290600101906020018083116102a9575b505091505060405180910390207f047dcd1aa8b77b0b943642129c767533eeacd700c7c1eab092b8ce05d2b2faf560018460405180806020018060200183810383528581815460018160011615610100020316600290048152602001915080546001816001161561010002031660029004801561037b5780601f106103505761010080835404028352916020019161037b565b820191906000526020600020905b81548152906001019060200180831161035e57829003601f168201915b5050838103825284818151815260200191508051906020019080838360005b838110156103b557808201518184015260208101905061039a565b50505050905090810190601f1680156103e25780820380516001836020036101000a031916815260200191505b5094505050505060405180910390a380600190805190602001906104079291906104ad565b5050565b606060018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156104a35780601f10610478576101008083540402835291602001916104a3565b820191906000526020600020905b81548152906001019060200180831161048657829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106104ee57805160ff191683800117855561051c565b8280016001018555821561051c579182015b8281111561051b578251825591602001919060010190610500565b5b509050610529919061052d565b5090565b61054f91905b8082111561054b576000816000905550600101610533565b5090565b905600a165627a7a7230582046f2bfde971e97e7746b11b4a9fc91dad8b679c52935889d011d9a1ba1a0f75e0029";

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

    public RemoteCall<TransactionReceipt> kill() {
        final Function function = new Function(
                FUNC_KILL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> newGreeting(String _greeting) {
        final Function function = new Function(
                FUNC_NEWGREETING, 
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(_greeting)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> greet() {
        final Function function = new Function(FUNC_GREET, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public static RemoteCall<Greeter> deploy(Chainsql chainsql, BigInteger gasLimit, BigInteger initialDropsValue, String _greeting) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(_greeting)));
        return deployRemoteCall(Greeter.class,chainsql, gasLimit, BINARY, encodedConstructor, initialDropsValue);
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
