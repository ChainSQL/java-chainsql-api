package com.peersafe.chainsql.core;

import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.EventManager;
import com.ripple.client.Account;
import com.ripple.client.pubsub.Publisher.Callback;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.core.types.known.tx.signed.SignedTransaction;

public abstract class Submit {
	public Connection connection;
	protected Callback cb;
	private SubmitState submit_state;
	private SyncState sync_state;
	
	private JSONObject submitRes;
	private JSONObject syncRes;
	
	private boolean sync = false;
	private SyncCond condition;
	
	public enum SyncCond {
        validate_success,	
        db_success,
	}
	
	public enum SubmitState{
		waiting_submit,
		submit_success,
		submit_error,
	}
	
	public enum SyncState{
		waiting_sync,
		sync_response,
	}

	
	/**
	 * asynchronous,callback trigger with all possible status
	 * @param cb
	 * @return submit result
	 */
	public JSONObject submit(Callback cb){
		this.cb = cb;

		return doSubmit();
	}
	
	/**
	 * synchronous,return when condition satisfied or submit failed
	 * @param cond,return condition
	 * @return
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
		return doSubmit();
	}
	
	abstract JSONObject doSubmit();
	
	private void waiting(){
      	try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private JSONObject getError(String err){
		JSONObject obj = new JSONObject();
		obj.put("status", "error");
		obj.put("error_message", err);
		return obj;
	}

	protected JSONObject doSubmit(SignedTransaction signed){
		if(signed == null){
			return getError("Signing failed,maybe ripple node error");
		}
		
		Account account = connection.client.accountFromSeed(connection.secret);
	    TransactionManager tm = account.transactionManager();
        ManagedTxn tx = tm.manage(signed.txn);
        tm.queue(tx.onSubmitSuccess(this::onSubmitSuccess)
                   .onError(this::onSubmitError));
        
        for(int i=0; i<2; i++){
        	 waiting();
        }
       
        //subscribe tx
        if(sync || cb != null){
        	if(tx == null || tx.hash == null){
        		System.out.println(" hash :"+tx.hash.toString());
    			return getError("Submit failed,transaction hash is null.");
        	}
        	System.out.println(tx.hash.toString());
        	subscribeTx(tx.hash.toString());
        }
        
        //wait until submit return
        submit_state = SubmitState.waiting_submit;
        while(submit_state == SubmitState.waiting_submit){
        	waiting();
        }        
        
        if(sync){
        	if(submit_state == SubmitState.submit_error){
        		return submitRes;
        	}else{
            	while(sync_state != SyncState.sync_response){
            		waiting();
            	}
            	return syncRes;
        	}        	
        }else{
        	return submitRes;
        }
	}
	
	private void subscribeTx(String txId){
    	EventManager manager = new EventManager(connection);
    	manager.subTx(txId,(data)->{
    		//System.out.println(data);
    		if(cb != null){
    			cb.called(data);
    		}else if(sync){
    			JSONObject obj = (JSONObject)data;
    			JSONObject res = new JSONObject();
    			JSONObject tx = (JSONObject) obj.get("transaction");
    			res.put("tx_hash", tx.get("hash").toString());
    			
    			if(condition == SyncCond.validate_success && obj.get("status").equals("validate_success")){
    				res.put("status", "success");
    			}else if(condition == SyncCond.db_success && obj.get("status").equals("db_success")){
    				res.put("status", "success");
    			}else if(!obj.get("status").equals("validate_success")){
    				res.put("status", "error");
    				res.put("error_message", obj.get("error_message"));
    			}
    			if(!res.isNull("status")){
        			syncRes = res;
        			sync_state = SyncState.sync_response;
    			}
    		}
        });
	}
	
	private void onSubmitSuccess(Response res){
        JSONObject obj = new JSONObject();
        obj.put("status", "success");
        JSONObject tx_json = (JSONObject) res.result.get("tx_json");
        obj.put("tx_hash", tx_json.get("hash").toString());
        
		submitRes = obj;
		submit_state = SubmitState.submit_success;
	}

    private void onSubmitError(ManagedTxn managed) {
        JSONObject obj = new JSONObject();
        obj.put("status", "error");
        obj.put("error_message", managed.result.engineResult.human);
        //JSONObject tx_json = (JSONObject) managed.result.get("tx_json");
        obj.put("tx_hash", managed.result.hash.toString());
        
        submitRes = obj;
        submit_state = SubmitState.submit_error;
    }
	
}
