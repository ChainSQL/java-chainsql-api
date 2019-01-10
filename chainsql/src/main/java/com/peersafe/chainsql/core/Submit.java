package com.peersafe.chainsql.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.peersafe.base.client.Account;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.transactions.ManagedTxn;
import com.peersafe.base.client.transactions.ManagedTxn.OnSubmitSuccess;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.core.types.known.tx.signed.SignedTransaction;
import com.peersafe.chainsql.manager.EventManager;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.GenericPair;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;

public abstract class Submit {
	public Connection connection;
	protected Callback<JSONObject> cb;
	private SubmitState submit_state;
	private SyncState sync_state;
	private JSONObject submitRes;
	private JSONObject syncRes;
	
	private boolean sync = false;
	protected SyncCond condition;
	protected SignedTransaction signed;
	
	protected CrossChainArgs crossChainArgs = null;
	
	protected EventManager eventManager = new EventManager();
	
	//事务相关
	protected List<JSONObject> cache = new ArrayList<JSONObject>();	
	protected Map<GenericPair<String,String>,String> mapToken = 
			new HashMap<GenericPair<String,String>,String>();
	protected boolean transaction = false;
	protected Integer needVerify = 1;
	//严格模式
	protected boolean strictMode = false;
	
	public enum SyncCond {
		send_success,
        validate_success,	
        db_success,
	}
	
	public enum SubmitState{
		waiting_submit,
		send_success,
		submit_error,
	}
	
	public enum SyncState{
		waiting_sync,
		sync_response,
	}
	
	public class CrossChainArgs{
		public String originalAddress;
		public int 	  txnLedgerSeq;
		public String curTxHash;
		public String futureHash;
	}
	private static final int wait_milli = 50;
	private static final int submit_wait = 10000;
	private static final int sync_maxtime = 30000;
	
	/**
	 * Set restrict mode.
	 * If restrict mode enabled,transaction will fail when user executing a consecutive operation 
	 * to a table and some other user interrupts this by making an operation to this identical table.  
	 * @param falg True to enable restrict mode and false to disable restrict mode.
	 */
	public void setRestrict(boolean falg) {
		this.strictMode = falg;
	}
	
	public void setNeedVerify(boolean flag){
		this.needVerify = flag ? 1 : 0;
	}

	/**
	 * asynchronous,callback trigger with all possible status
	 * @param cb Callback.
	 * @return submit result
	 */
	public JSONObject submit(Callback<JSONObject> cb){
		this.cb = cb;

		return doSubmit();
	}
	
	/**
	 * synchronous,return when condition satisfied or submit failed
	 * @param cond,return condition
	 * @return Submit result.
	 */
	public JSONObject submit(SyncCond cond){
		sync = true;
		condition = cond;
		
		return doSubmit();
	}


	/**
	 * submit a transaction,return immediately
	 * @return submit result
	 */
	public JSONObject submit(){
		sync = false;
		cb = null;
		return doSubmit();
	}
	
	public void setCrossChainArgs(String originalAddress,int txnLedgerSeq,String curTxHash,String futureHash){
		crossChainArgs = new CrossChainArgs();
		crossChainArgs.originalAddress = originalAddress;
		crossChainArgs.txnLedgerSeq = txnLedgerSeq;
		crossChainArgs.curTxHash = curTxHash;
		crossChainArgs.futureHash = futureHash;
	}
	
	public void setCrossChainArgs(CrossChainArgs args){
		this.crossChainArgs = args;
	}
	public boolean isCrossChainArgsSet(){
		return this.crossChainArgs != null;
	}

	abstract protected JSONObject prepareSigned();
//	protected JSONObject prepareSigned() {
//		return null;
//	}

	private JSONObject getError(String err){
		JSONObject obj = new JSONObject();
		obj.put("status", "error");
		obj.put("error_message", err);
		return obj;
	}
	
	public EventManager eventManager() {
		return eventManager;
	}
	
	protected JSONObject doSubmit(){
		JSONObject obj = prepareSigned();
		if(obj.getString("status").equals("error") || obj.has("final_result")){
			return obj;
		}		
		return doSubmitNoPrepare();
	}
	
	protected JSONObject doSubmitNoPrepare(){
		if(signed == null){
			return getError("Signing failed,maybe ripple node error");
		}
		
        submit_state = SubmitState.waiting_submit;
        sync_state = SyncState.waiting_sync;
        
		Account account = connection.client.accountFromSeed(connection.secret);
	    TransactionManager tm = account.transactionManager();
	    ManagedTxn tx = new ManagedTxn(signed);
        
        //subscribe tx
        if(sync || cb != null){
        	if(tx == null || tx.hash == null){
    			return getError("Submit failed,transaction hash is null.");
        	}
        	subscribeTx(tx.hash.toString());
        }
        
        tm.submitSigned(tx.onSubmitSuccess(new OnSubmitSuccess(){
			@Override
			public void called(Response args) {
				onSubmitSuccess(args);
			}    	  
        }).onError(new Callback<Response>(){
			@Override
			public void called(Response args) {
				onSubmitError(args);
			}
        })); 
        
        //wait until submit return
        int count = submit_wait / wait_milli;
        while(submit_state == SubmitState.waiting_submit){
        	Util.waiting();
        	if(--count <= 0){
        		submit_state = SubmitState.submit_error;
        		submitRes = getError("waiting submit result timeout");
        		break;
        	}
        }        
        
        if(sync){
        	if(submit_state == SubmitState.submit_error || 
        		(submit_state == SubmitState.send_success && condition == SyncCond.send_success)){
        		return submitRes;
        	}else{
        		count = sync_maxtime / wait_milli;
            	while(sync_state != SyncState.sync_response){
            		Util.waiting();
            		if(--count <= 0){
            			syncRes = getError("waiting sync result timeout");
            			break;
            		}
            	}
            	return syncRes;
        	}        	
        }else{
        	return submitRes;
        }
	}
	
	private void subscribeTx(String txId){
    	this.eventManager.subscribeTx(txId,new Callback<JSONObject>(){
			@Override
			public void called(JSONObject data) {
	    		if(cb != null){
	    			if(!data.getString("status").equals("success"))
	    				cb.called((JSONObject)data);
	    		}else if(sync){
	    			if(!data.has("transaction"))
	    				return;
	    			JSONObject obj = (JSONObject)data;
	    			JSONObject res = new JSONObject();
	    			JSONObject tx = (JSONObject) obj.get("transaction");
	    			res.put("tx_hash", tx.get("hash").toString());
	    			
	    			if(condition == SyncCond.validate_success && obj.get("status").equals("validate_success")){
	    				res.put("status", "validate_success");
	    			}else if(condition == SyncCond.db_success && obj.get("status").equals("db_success")){
	    				res.put("status", "db_success");
	    			}else if(!obj.get("status").equals("validate_success") && !obj.get("status").equals("db_success")){
	    				res.put("status", obj.get("status"));
	    				if(obj.has("error_message"))
	    					res.put("error_message", obj.get("error_message"));
	    			}
	    			if(!res.isNull("status")){
	        			syncRes = res;
	        			sync_state = SyncState.sync_response;
	    			}
	    		}
			}
    	});
	}
	
	private void unSubscribeTx(String txId) {
		this.eventManager.unsubscribeTx(txId,null);
	}
	
	private void onSubmitSuccess(Response res){
        JSONObject obj = new JSONObject();
        obj.put("status", "send_success");
        JSONObject tx_json = (JSONObject) res.result.get("tx_json");
        obj.put("tx_hash", tx_json.get("hash").toString());
        
		submitRes = obj;
		submit_state = SubmitState.send_success;
	}

    private void onSubmitError(Response res) {
        JSONObject obj = new JSONObject();
        
        obj.put("status", "error");
        if(res.result.has("engine_result_message"))
        	obj.put("error_message", res.result.getString("engine_result_message"));
        if(res.result.has("engine_result_message_detail"))
        	obj.put("error_message",res.result.getString("engine_result_message_detail"));
        
        if(res.result.has("engine_result_code")){
        	obj.put("error_code", res.result.getInt("engine_result_code"));
        }
        if(res.result.has("tx_json")){
        	obj.put("tx_json", res.result.getJSONObject("tx_json"));
        }
        if(cb != null) {
        	cb.called(obj);
        	unSubscribeTx(signed.hash.toString());
        }
        submitRes = obj;
        submit_state = SubmitState.submit_error;
    }
  
	protected JSONArray getTableArray(String tableName){
		String tablestr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(tableName) + "\"}}";
		return Util.strToJSONArray(tablestr);
	}
	
	protected boolean mapError(Map<String,Object> map){
		if(map.get("Sequence") == null){
	    	return true;
	    }else{
	    	return false;
	    }
	}
	
	/**
	 * Translate to transaction type.
	 * @param json tx_json.
	 * @param type Transaction type.
	 * @return Transaction object.
	 * @throws Exception Exception to be throws.
	 */
	protected Transaction toTransaction(JSONObject json,TransactionType type) throws Exception{
    	Transaction tx = new Transaction(type);
    	Amount fee;
    	if(connection.client.serverInfo.primed()) {
    		fee = connection.client.serverInfo.transactionFee(tx);
    		if(!json.has(UInt32.LastLedgerSequence.toString())) {
    			tx.put(UInt32.LastLedgerSequence, new UInt32(connection.client.serverInfo.ledger_index + 5));
    		}
    	}else {
    		fee = Amount.fromString("50");
    		JSONObject ledger = connection.client.getLedgerVersion();
    		if(ledger.has("ledger_current_index")) {
    			tx.put(UInt32.LastLedgerSequence, new UInt32(ledger.getInt("ledger_current_index") + 5));
    		}
    	}    		
    	
    	//chainsql type tx needs higher fee
    	Amount extraFee = Util.getExtraFee(json,type);
    	fee = fee.add(extraFee);
    	
		tx.as(Amount.Fee, fee);
		
  		AccountID account = AccountID.fromAddress(this.connection.address);
 		Map<String,Object> map = Validate.rippleRes(this.connection.client, account);
 		if(mapError(map)){
 			throw new Exception((String)map.get("error_message"));
 		}else{
 			tx.as(UInt32.Sequence, map.get("Sequence"));
 		}
		try {  
		   tx.parseFromJson(json);
		} catch (JSONException e) {  
		   e.printStackTrace();  
		}  
	  	return tx;
    }	
}
