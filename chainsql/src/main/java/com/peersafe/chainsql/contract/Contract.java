package com.peersafe.chainsql.contract;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.abi.EventEncoder;
import com.peersafe.abi.EventValues;
import com.peersafe.abi.FunctionEncoder;
import com.peersafe.abi.FunctionReturnDecoder;
import com.peersafe.abi.TypeReference;
import com.peersafe.abi.datatypes.Address;
import com.peersafe.abi.datatypes.Event;
import com.peersafe.abi.datatypes.Function;
import com.peersafe.abi.datatypes.Type;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.chainsql.contract.exception.ContractCallException;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.ContractOp;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.Util;

/**
 * Solidity contract type abstraction for interacting with smart contracts via native Java types.
 */
public abstract class Contract{

	public enum ContractOpType {
		ContractCreation(1),			///< Transaction to create contracts - receiveAddress() is ignored.
		MessageSend(2),					///< Transaction to invoke a message call - receiveAddress() is used.
		ContractDeletion(3),			///
		MessageCall(4);
		
		private int nType;
		private ContractOpType(int type) {
			nType = type;
		}
		public int value() {
			return nType;
		}
	};
    //https://www.reddit.com/r/ethereum/comments/5g8ia6/attention_miners_we_recommend_raising_gas_limit/
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(30_000_000);
    public static final BigInteger INITIAL_DROPS = BigInteger.valueOf(5_000_000);

    public static final String FUNC_DEPLOY = "deploy";

    protected final String contractBinary;
    protected String contractAddress;
    protected Map<String, String> deployedAddresses;
    protected BigInteger gasLimit;
    protected Chainsql chainsql;

    protected Contract(Chainsql chainsql,String contractBinary, String contractAddress,BigInteger gasLimit) {
    	this.chainsql = chainsql;
    	
        this.contractAddress = contractAddress;

        this.contractBinary = contractBinary;
        
        this.gasLimit = gasLimit;
    }


    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getContractBinary() {
        return contractBinary;
    }

    /**
     * Allow {@code gasPrice} to be set.
     * @param newPrice gas price to use for subsequent transactions
     * @deprecated use ContractGasProvider
     */
    public void setGasPrice(BigInteger newPrice) {
//        this.gasProvider = new StaticGasProvider(newPrice, gasProvider.getGasLimit());
    }

    /**
     * Get the current {@code gasPrice} value this contract uses when executing transactions.
     * @return the gas price set on this contract
     * @deprecated use ContractGasProvider
     */
    public BigInteger getGasPrice() {
		return null;
//        return gasProvider.getGasPrice();
    }
    
    public BigInteger getGasLimit() {
    	return gasLimit;
    }

    public Chainsql getChainsql() {
    	return chainsql;
    }
    
    /**
     * Execute constant function call - i.e. a call that does not change state of the contract
     *
     * @param function to call
     * @return {@link List} of values returned by function call
     */
    private List<Type> executeCall(
            Function function) throws IOException {
        String encodedFunction = FunctionEncoder.encode(function);
        String data = encodedFunction.substring(2, encodedFunction.length());
        JSONObject objTx = new JSONObject();
        objTx.put("account", chainsql.connection.address);
        objTx.put("contract_data", data);
        objTx.put("contract_address", contractAddress);
        
        JSONObject ret = this.chainsql.connection.client.contractCall(objTx);
        if(ret.has("error")){
        	if(ret.has("error_code"))
        		throw new ContractCallException(ret.getString("error_message"));
        	else {
        		List<Type> error = FunctionReturnDecoder.decode(ret.getString("error_message"), function.getOutputParameters());
        		String str = (String)(Object)error.get(0);
        		throw new ContractCallException(str);
        	}
        }
        return FunctionReturnDecoder.decode(ret.getString("contract_call_result"), function.getOutputParameters());
    }

    @SuppressWarnings("unchecked")
    protected <T extends Type> T executeCallSingleValueReturn(
            Function function) throws IOException {
        List<Type> values = executeCall(function);
        if (!values.isEmpty()) {
            return (T) values.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends Type, R> R executeCallSingleValueReturn(
            Function function, Class<R> returnType) throws IOException {
        T result = executeCallSingleValueReturn(function);
        if (result == null) {
            throw new ContractCallException("Empty value (0x) returned from contract");
        }

        Object value = result.getValue();
        if (returnType.isAssignableFrom(value.getClass())) {
            return (R) value;
        } else if (result.getClass().equals(Address.class) && returnType.equals(String.class)) {
            return (R) result.toString();  // cast isn't necessary
        } else {
            throw new ContractCallException(
                    "Unable to convert response: " + value
                            + " to expected type: " + returnType.getSimpleName());
        }
    }

    protected List<Type> executeCallMultipleValueReturn(
            Function function) throws IOException {
        return executeCall(function);
    }

    protected TransactionReceipt executeTransaction(
            Function function)
            throws IOException, TransactionException {
        return executeTransaction(function, BigInteger.ZERO);
    }

    private TransactionReceipt executeTransaction(
            Function function, BigInteger weiValue)
            throws IOException, TransactionException {
        return executeTransaction(FunctionEncoder.encode(function), weiValue, function.getName());
    }

    /**
     * Given the duration required to execute a transaction.
     *
     * @param data  to send in transaction
     * @param weiValue in Wei to send in transaction
     * @return {@link Optional} containing our transaction receipt
     * @throws IOException                 if the call to the node fails
     * @throws TransactionException if the transaction was not mined while waiting
     */
    TransactionReceipt executeTransaction(
            String data, BigInteger weiValue, String funcName)
            throws TransactionException, IOException {

        JSONObject objTx = new JSONObject();
        objTx.put("Account", chainsql.connection.address);
        objTx.put("ContractOpType", ContractOpType.MessageSend.value());
        objTx.put("ContractData", data.substring(2, data.length()));
        objTx.put("Gas", gasLimit.intValue());
        if(weiValue.intValue() > 0)
        	objTx.put("ContractValue", Amount.fromString(weiValue.toString()));
        objTx.put("ContractAddress", contractAddress);
        
        ContractOp op = new ContractOp(objTx,this.chainsql);
        op.connection = this.chainsql.connection;
        
        JSONObject obj = op.submit(SyncCond.validate_success);
        if(obj.has("error_message")){
        	throw new RuntimeException(obj.getString("error_message"));
        }
        TransactionReceipt receipt = new TransactionReceipt(this.contractAddress,obj);

        return receipt;
    }

    protected <T extends Type> RemoteCall<T> executeRemoteCallSingleValueReturn(Function function) {
        return new RemoteCall<>(() -> executeCallSingleValueReturn(function));
    }

    protected <T> RemoteCall<T> executeRemoteCallSingleValueReturn(
            Function function, Class<T> returnType) {
        return new RemoteCall<>(() -> executeCallSingleValueReturn(function, returnType));
    }

    protected RemoteCall<List<Type>> executeRemoteCallMultipleValueReturn(Function function) {
        return new RemoteCall<>(() -> executeCallMultipleValueReturn(function));
    }

    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(Function function) {
        return new RemoteCall<>(() -> executeTransaction(function));
    }

    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(
            Function function, BigInteger weiValue) {
        return new RemoteCall<>(() -> executeTransaction(function, weiValue));
    }

    private static <T extends Contract> T create(
            T contract, String binary, String encodedConstructor, BigInteger value)
            throws IOException, TransactionException {
        
        JSONObject objTx = new JSONObject();
        Chainsql c = contract.getChainsql();
        objTx.put("Account", c.connection.address);
        objTx.put("ContractOpType", ContractOpType.ContractCreation.value());
        objTx.put("ContractData", binary + encodedConstructor);
        objTx.put("Gas", contract.getGasLimit().intValue());
        objTx.put("ContractValue", Amount.fromString(value.toString()));
        
        ContractOp op = new ContractOp(objTx,contract.getChainsql());
        JSONObject obj = op.submit(SyncCond.validate_success);
        String contractAddress = null;
        try {
            if(obj.getString("status").equals("validate_success")) {
            	JSONObject tx = c.connection.client.getTransaction(obj.getString("tx_hash"));
            	contractAddress = Util.getNewAccountFromTx(tx);
            }else
            {
                if(obj.has("error_message")){
                	throw new RuntimeException(obj.getString("error_message"));
                }
            }
        }catch(Exception e) {
        	throw new RuntimeException(e.getMessage());
        }
     
        if (contractAddress == null) {
            throw new RuntimeException("Empty contract address returned");
        }
        contract.setContractAddress(contractAddress);

        return contract;
    }

    protected static <T extends Contract> T deploy(
            Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor, BigInteger value) throws
            IOException, TransactionException {

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(
            		Chainsql.class,
                    String.class,
                    BigInteger.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            T contract = constructor.newInstance(chainsql, binary, gasLimit);

            return create(contract, binary, encodedConstructor, value);
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor, BigInteger value) {
        return new RemoteCall<>(() -> deploy(
                type,chainsql, gasLimit, binary,
                encodedConstructor, value));
    }

    public static <T extends Contract> RemoteCall<T> deployRemoteCall(
            Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor) {
        return deployRemoteCall(
                type,chainsql, gasLimit,
                binary, encodedConstructor, BigInteger.ZERO);
    }



    public EventValues extractEventParameters(
            Event event,List<String> topics,String logData) {
    	
        String encodedEventSignature = EventEncoder.encode(event);
        encodedEventSignature = encodedEventSignature.substring(2, encodedEventSignature.length());
        //remove 0x,to lowercase
        if (!topics.get(0).equals(encodedEventSignature.toUpperCase())) {
            return null;
        }

        List<Type> indexedValues = new ArrayList<>();
        List<Type> nonIndexedValues = FunctionReturnDecoder.decode(
        		logData, event.getNonIndexedParameters());

        List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
        for (int i = 0; i < indexedParameters.size(); i++) {
            Type value = FunctionReturnDecoder.decodeIndexedValue(
                    topics.get(i + 1), indexedParameters.get(i));
            indexedValues.add(value);
        }
        return new EventValues(indexedValues, nonIndexedValues);
    }
    
    protected void on(Event event,Callback<EventValues> cb) {
    	EventManager.instance().subscribeContract(contractAddress, event, new Callback<JSONObject>() {

			@Override
			public void called(JSONObject args) {
				JSONArray arr = args.getJSONArray("ContractEventTopics");
				List<String> list = new ArrayList<String>();
				for(int i=0; i<arr.length(); i++) {
					list.add(arr.getString(i));
				}
				EventValues ev = extractEventParameters(event,list,args.getString("ContractEventInfo"));
				cb.called(ev);
			}
    		
    	});
    }

    /**
     * Subclasses should implement this method to return pre-existing addresses for deployed
     * contracts.
     *
     * @param networkId the network id, for example "1" for the main-net, "3" for ropsten, etc.
     * @return the deployed address of the contract, if known, and null otherwise.
     */
    protected String getStaticDeployedAddress(String networkId) {
        return null;
    }

    public final void setDeployedAddress(String networkId, String address) {
        if (deployedAddresses == null) {
            deployedAddresses = new HashMap<>();
        }
        deployedAddresses.put(networkId, address);
    }

    public final String getDeployedAddress(String networkId) {
        String addr = null;
        if (deployedAddresses != null) {
            addr = deployedAddresses.get(networkId);
        }
        return addr == null ? getStaticDeployedAddress(networkId) : addr;
    }

    /**
     * Adds a log field to {@link EventValues}.
     */
    public static class EventValuesWithLog {
        private final EventValues eventValues;
//        private final Log log;

        private EventValuesWithLog(EventValues eventValues) {
            this.eventValues = eventValues;
        }

        public List<Type> getIndexedValues() {
            return eventValues.getIndexedValues();
        }

        public List<Type> getNonIndexedValues() {
            return eventValues.getNonIndexedValues();
        }
    }

    @SuppressWarnings("unchecked")
    protected static <S extends Type, T> 
            List<T> convertToNative(List<S> arr) {
        List<T> out = new ArrayList<T>();
        for (Iterator<S> it = arr.iterator(); it.hasNext(); ) {
            out.add((T)it.next().getValue());
        }
        return out;
    }
}
