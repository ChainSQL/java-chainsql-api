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
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.chainsql.contract.exception.ContractCallException;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit;
import com.peersafe.chainsql.manager.EventManager;
import com.peersafe.chainsql.util.Util;

/**
 * Solidity contract type abstraction for interacting with smart contracts via native Java types.
 */
public abstract class Contract extends Submit{

	public enum ContractOpType {
		ContractCreation(1),			///< Transaction to create contracts - receiveAddress() is ignored.
		MessageSend(2);					///< Transaction to invoke a message call - receiveAddress() is used.;
		
		private int nType;
		private ContractOpType(int type) {
			nType = type;
		}
		public int value() {
			return nType;
		}
	};
    //https://www.reddit.com/r/ethereum/comments/5g8ia6/attention_miners_we_recommend_raising_gas_limit/
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(3_000_000);
    public static final BigInteger INITIAL_DROPS = BigInteger.valueOf(5_000_000);

    public static final String FUNC_DEPLOY = "deploy";

    protected final String contractBinary;
    protected String contractAddress;
    protected Map<String, String> deployedAddresses;
    protected BigInteger gasLimit;
    protected Chainsql chainsql;

    private JSONObject mTxJson;
    
    protected Contract(Chainsql chainsql,String contractBinary, String contractAddress,BigInteger gasLimit) {
    	this.chainsql = chainsql;
    	
        this.contractAddress = contractAddress;

        this.contractBinary = contractBinary;
        
        this.gasLimit = gasLimit;
        
        this.connection = chainsql.connection;
        
        this.eventManager = chainsql.eventManager();
    }


    public void setTxJson(JSONObject json) {
    	mTxJson = json;
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
            Function function) throws ContractCallException {
    	JSONObject objTx = prepareCallParam(function);
        
        JSONObject ret = this.chainsql.connection.client.contractCall(objTx);
        if(ret.has("error") && ret.has("error_message")){
            throw new ContractCallException(ret.getString("error"),ret.getString("error_message"));
        }else if(ret.has("error")){
            throw new ContractCallException(ret.getString("error"));
        }

        return FunctionReturnDecoder.decode(ret.getString("contract_call_result"), function.getOutputParameters());
    }
    
    private void executeCall(Function function,Callback<List<Type>> cb) throws ContractCallException{
        JSONObject objTx = prepareCallParam(function);
        
        this.chainsql.connection.client.contractCall(objTx,new Callback<JSONObject>() {

			@Override
			public void called(JSONObject ret) {
				if(ret.has("error")){
		        	throw new ContractCallException(ret.getString("error"));
		        }
				cb.called(FunctionReturnDecoder.decode(ret.getString("contract_call_result"), function.getOutputParameters()));
			}        	
        });
    }

    private JSONObject prepareCallParam(Function function) {
    	String encodedFunction = FunctionEncoder.encode(function);
        String data = encodedFunction.substring(2, encodedFunction.length());
        JSONObject objTx = new JSONObject();
        objTx.put("account", chainsql.connection.address);
        objTx.put("contract_data", data);
        objTx.put("contract_address", contractAddress);
        return objTx;
    }
    @SuppressWarnings("unchecked")
    protected <T extends Type> T executeCallSingleValueReturn(
            Function function) throws ContractCallException {
        List<Type> values = executeCall(function);
        if (!values.isEmpty()) {
            return (T) values.get(0);
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends Type> void executeCallSingleValueReturn(
            Function function,final Callback<T> cb) throws ContractCallException {
    	executeCall(function,new Callback<List<Type>>() {

			@Override
			public void called(List<Type> values) {
				if (!values.isEmpty()) {
		            cb.called((T)values.get(0));
		        } else {
		            cb.called(null);
		        }
			}
    		
    	});
        
    }

    @SuppressWarnings("unchecked")
    protected <T extends Type, R> R executeCallSingleValueReturn(
            Function function, Class<R> returnType) throws ContractCallException {
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
    protected <T extends Type,R> void executeCallSingleValueReturn(
            Function function, Class<R> returnType,final Callback<R> cb) throws ContractCallException {
    	
    	executeCallSingleValueReturn(function,new Callback<T>() {

			@Override
			public void called(T result) {
				if (result == null) {
		            throw new ContractCallException("Empty value (0x) returned from contract");
		        }

		        Object value = result.getValue();
		        if (returnType.isAssignableFrom(value.getClass())) {
		            cb.called((R) value);
		        } else if (result.getClass().equals(Address.class) && returnType.equals(String.class)) {
		        	cb.called((R) result.toString());  // cast isn't necessary
		        } else {
		            throw new ContractCallException(
		                    "Unable to convert response: " + value
		                            + " to expected type: " + returnType.getSimpleName());
		        }
			}    		
    	});
    }


    protected List<Type> executeCallMultipleValueReturn(
            Function function) throws ContractCallException {
        return executeCall(function);
    }
    
    protected void executeCallMultipleValueReturn(Function function,Callback<List<Type>> cb)
    		throws ContractCallException {
        executeCall(function,cb);
    }

    protected Contract executeTransaction(
            Function function) {
        return executeTransaction(function, BigInteger.ZERO);
    }

    private Contract executeTransaction(
            Function function, BigInteger dropValue){
        return executeTransaction(FunctionEncoder.encode(function), dropValue, function.getName());
    }

    /**
     * Given the duration required to execute a transaction.
     *
     * @param data  to send in transaction
     * @param dropValue in Wei to send in transaction
     * @return {@link Optional} containing our transaction receipt
     * @throws IOException                 if the call to the node fails
     * @throws TransactionException if the transaction was not mined while waiting
     */
    Contract executeTransaction(
            String data, BigInteger dropValue, String funcName)
            /*throws TransactionException, IOException*/ {

        JSONObject objTx = new JSONObject();
        objTx.put("Account", chainsql.connection.address);
        objTx.put("ContractOpType", ContractOpType.MessageSend.value());
        if(data.length() > 2)
        	objTx.put("ContractData", data.substring(2, data.length()));
        else
        	objTx.put("ContractData", "");
        objTx.put("Gas", gasLimit.intValue());
        if(dropValue.intValue() > 0)
        	objTx.put("ContractValue", Amount.fromString(dropValue.toString()));
        objTx.put("ContractAddress", contractAddress);
        
        mTxJson = objTx;
        
        return this;
    }
    @Override
	protected
	JSONObject prepareSigned() {
		try {
			
			if(mTxJson.toString().equals("{}")) {
				return Util.errorObject("Exception occured:Json not prepared");
			}
			mTxJson.put("Account",this.connection.address);

            if (this.connection.userCert != null) {
                String sCert = Util.toHexString(this.connection.userCert);
                mTxJson.put("Certificate", sCert);
            }
	    	
	    	Transaction tx = toTransaction(mTxJson,TransactionType.Contract);
			signed = tx.sign(this.connection.secret);
			
			return Util.successObject();
		} catch (Exception e) {
			if(!e.getMessage().equals("")) {
				return Util.errorObject(e.getMessage());
			}else {
				e.printStackTrace();
				return Util.errorObject("Exception occured");
			}
		}
	}
	
    protected <T extends Type> T executeRemoteCallSingleValueReturn(Function function) throws ContractCallException {
        return executeCallSingleValueReturn(function);
    }
	
    protected <T> T executeRemoteCallSingleValueReturn(
            Function function, Class<T> returnType) throws ContractCallException {
        return executeCallSingleValueReturn(function, returnType);
    }

    protected <T> void executeRemoteCallSingleValueReturn(
            Function function,Class<T> returnType,final Callback<T> cb) throws ContractCallException {
        executeCallSingleValueReturn(function,returnType, cb);
    }
    
    protected List<Type> executeRemoteCallMultipleValueReturn(Function function) throws ContractCallException {
        return executeCallMultipleValueReturn(function);
    }
    
    protected void executeRemoteCallMultipleValueReturn(Function function,Callback<List<Type>> cb) throws ContractCallException {
        executeCallMultipleValueReturn(function,cb);
    }
    
    protected Contract executeRemoteCallTransaction(Function function) {
        return executeTransaction(function);
    }

    protected Contract executeRemoteCallTransaction(
            Function function, BigInteger dropValue) {
        return executeTransaction(function, dropValue);
    }

    private static <T extends Contract> T create(
            T contract, String binary, String encodedConstructor, BigInteger value,final Callback<T> cb)
            throws TransactionException {
        
        JSONObject objTx = new JSONObject();
        Chainsql c = contract.getChainsql();
        objTx.put("Account", c.connection.address);
        objTx.put("ContractOpType", ContractOpType.ContractCreation.value());
        objTx.put("ContractData", binary + encodedConstructor);
        objTx.put("Gas", contract.getGasLimit().intValue());
        objTx.put("ContractValue", Amount.fromString(value.toString()));
        contract.setTxJson(objTx);
        
        if(cb == null) {
            JSONObject obj = contract.submit(SyncCond.validate_success);
            String contractAddress = null;

            if(obj.has("status") && obj.getString("status").equals("validate_success")) {
            	JSONObject tx = c.connection.client.getTransaction(obj.getString("tx_hash"));
            	contractAddress = Util.getNewAccountFromTx(tx);
                contract.setContractAddress(contractAddress);
            }else if(obj.has("status") && obj.getString("status").equals("validate_timeout")){

                System.out.println(obj);
                throw new TransactionException("deploy validate_timeout");
            }
            else{
                if(obj.has("error_message")){
                	if(obj.has("error_code"))
                		throw new TransactionException(obj.getString("error_message"),obj.getInt("error_code"));
                	else
                		throw new TransactionException(obj.getString("error_message"));
                }else {
                	throw new TransactionException("deploy failed,unknown error");
                }
            }
     
            return contract;
        }else {
        	contract.submit(new Callback<JSONObject>() {

				@Override
				public void called(JSONObject obj){
					if(!obj.has("transaction"))
						return;
					String hash = obj.getJSONObject("transaction").getString("hash");
					if(obj.has("status") && obj.getString("status").equals("validate_success")) {
						try {
							c.connection.client.getTransaction(hash,new Callback<JSONObject>(){
								@Override
								public void called(JSONObject tx) {
									if(tx == null){
										cb.called(null);
									}else{
										String contractAddress = Util.getNewAccountFromTx(tx);
						            	contract.setContractAddress(contractAddress);
						            	contract.cb = null;
						            	cb.called(contract);
									}
								}			
							});
						}catch(Exception e){
							e.printStackTrace();
							cb.called(null);
						}		            	
		            }else{
		                if(obj.has("error_message")){
		                	System.err.println(obj);
		                }
		                cb.called(null);
		            }
				}
        		
        	});
        	return null;
        }        
    }

    protected static <T extends Contract> T deploy(
            Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor, BigInteger value) throws
            TransactionException {

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(
            		Chainsql.class,
                    String.class,
                    BigInteger.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            T contract = constructor.newInstance(chainsql, "", gasLimit);

            return create(contract, binary, encodedConstructor, value,null);
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected static <T extends Contract> void deploy(
            Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor, BigInteger value,Callback<T> cb) throws
            TransactionException {

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(
            		Chainsql.class,
                    String.class,
                    BigInteger.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            T contract = constructor.newInstance(chainsql, binary, gasLimit);

            create(contract, binary, encodedConstructor, value,cb);
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T extends Contract> T deployRemoteCall(
            Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor, BigInteger value) throws TransactionException {
        return deploy(
                type,chainsql, gasLimit, binary,
                encodedConstructor, value);
    }

    public static <T extends Contract> void deployRemoteCall(
            Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor, BigInteger value,Publisher.Callback<T> cb) throws TransactionException {
        deploy(
                type,chainsql, gasLimit, binary,
                encodedConstructor, value,cb);
    }
    
    public static <T extends Contract> T deployRemoteCall(
            Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor) throws TransactionException {
        return deployRemoteCall(
                type,chainsql, gasLimit,
                binary, encodedConstructor, BigInteger.ZERO);
    }

    public static <T extends Contract> void deployRemoteCall(
    		Class<T> type, Chainsql chainsql,BigInteger gasLimit,
            String binary, String encodedConstructor,Publisher.Callback<T> cb) throws TransactionException {
    	deployRemoteCall(
                type,chainsql, gasLimit, binary,
                encodedConstructor, BigInteger.ZERO,cb);
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
    	this.eventManager.subscribeContract(contractAddress, event, new Callback<JSONObject>() {

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
