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
 * <p>Generated with web3j version 4.8.1.
 */
public class Greeter extends Contract {
    private static final String BINARY = "608060405260405161070f38038061070f833981016040819052610022916100e7565b600080546001600160a01b03191633179055805161004790600190602084019061004e565b5050610207565b82805461005a906101b6565b90600052602060002090601f01602090048101928261007c57600085556100c2565b82601f1061009557805160ff19168380011785556100c2565b828001600101855582156100c2579182015b828111156100c25782518255916020019190600101906100a7565b506100ce9291506100d2565b5090565b5b808211156100ce57600081556001016100d3565b600060208083850312156100fa57600080fd5b82516001600160401b038082111561011157600080fd5b818501915085601f83011261012557600080fd5b815181811115610137576101376101f1565b604051601f8201601f19908116603f0116810190838211818310171561015f5761015f6101f1565b81604052828152888684870101111561017757600080fd5b600093505b82841015610199578484018601518185018701529285019261017c565b828411156101aa5760008684830101525b98975050505050505050565b600181811c908216806101ca57607f821691505b602082108114156101eb57634e487b7160e01b600052602260045260246000fd5b50919050565b634e487b7160e01b600052604160045260246000fd5b6104f9806102166000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c806341c0e1b5146100465780634ac0d66e14610050578063cfae321714610063575b600080fd5b61004e610081565b005b61004e61005e36600461024d565b6100a4565b61006b610122565b60405161007891906103b8565b60405180910390f35b6000546001600160a01b03163314156100a2576000546001600160a01b0316ff5b565b806040516100b2919061032a565b604051809103902060016040516100c99190610346565b60405180910390207f047dcd1aa8b77b0b943642129c767533eeacd700c7c1eab092b8ce05d2b2faf56001846040516101039291906103d2565b60405180910390a3805161011e9060019060208401906101b4565b5050565b6060600180546101319061049b565b80601f016020809104026020016040519081016040528092919081815260200182805461015d9061049b565b80156101aa5780601f1061017f576101008083540402835291602001916101aa565b820191906000526020600020905b81548152906001019060200180831161018d57829003601f168201915b5050505050905090565b8280546101c09061049b565b90600052602060002090601f0160209004810192826101e25760008555610228565b82601f106101fb57805160ff1916838001178555610228565b82800160010185558215610228579182015b8281111561022857825182559160200191906001019061020d565b50610234929150610238565b5090565b5b808211156102345760008155600101610239565b60006020828403121561025f57600080fd5b813567ffffffffffffffff8082111561027757600080fd5b818401915084601f83011261028b57600080fd5b81358181111561029d5761029d6104d6565b604051601f8201601f19908116603f011681019083821181831017156102c5576102c56104d6565b816040528281528760208487010111156102de57600080fd5b826020860160208301376000928101602001929092525095945050505050565b6000815180845261031681602086016020860161046b565b601f01601f19169290920160200192915050565b6000825161033c81846020870161046b565b9190910192915050565b60008083546103548161049b565b6001828116801561036c576001811461037d576103ac565b60ff198416875282870194506103ac565b8760005260208060002060005b858110156103a35781548a82015290840190820161038a565b50505082870194505b50929695505050505050565b6020815260006103cb60208301846102fe565b9392505050565b6040815260008084546103e48161049b565b8060408601526060600180841660008114610406576001811461041a5761044b565b60ff1985168884015260808801955061044b565b8960005260208060002060005b868110156104425781548b8201870152908401908201610427565b8a018501975050505b5050505050828103602084015261046281856102fe565b95945050505050565b60005b8381101561048657818101518382015260200161046e565b83811115610495576000848401525b50505050565b600181811c908216806104af57607f821691505b602082108114156104d057634e487b7160e01b600052602260045260246000fd5b50919050565b634e487b7160e01b600052604160045260246000fdfea164736f6c6343000805000a";

    public static final String FUNC_GREET = "greet";

    public static final String FUNC_KILL = "kill";

    public static final String FUNC_NEWGREETING = "newGreeting";

    public static final Event MODIFIED_EVENT = new Event("Modified", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>(true) {}, new TypeReference<Utf8String>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
    ;

    protected Greeter(Chainsql chainsql, String contractAddress, BigInteger gasLimit) {
        super(chainsql,BINARY, contractAddress, gasLimit);
    }

    public static Greeter deploy(Chainsql chainsql, BigInteger gasLimit, BigInteger initialDropsValue, String _greeting) throws TransactionException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(_greeting)));
        return deployRemoteCall(Greeter.class,chainsql, gasLimit, BINARY, encodedConstructor, initialDropsValue);
    }

    public static void deploy(Chainsql chainsql, BigInteger gasLimit, BigInteger initialDropsValue, Publisher.Callback<Greeter> cb, String _greeting) throws TransactionException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(_greeting)));
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

    public String greet() throws ContractCallException {
        final Function function = new Function(FUNC_GREET, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
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
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(_greeting)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
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
