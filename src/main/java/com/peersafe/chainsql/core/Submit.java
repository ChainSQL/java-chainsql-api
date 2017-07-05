package com.peersafe.chainsql.core;

import java.util.Iterator;
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
import com.peersafe.base.core.coretypes.Blob;
import com.peersafe.base.core.coretypes.STArray;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.uint.UInt16;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.core.types.known.tx.signed.SignedTransaction;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.EventManager;
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
	
	public enum EPaymentType{
		Account,
		Destination,
		Amount,
		Tables,
		OpType,
		User,
		Raw,
		Sequence,
		Fee,
		TransactionType,
	    TableNewName,
	    Owner,
	    Flags,
	    AutoFillField,
	    Token,
	    StrictMode,
	    NeedVerify,
	    Statements,
	    TxCheckHash,
	    CurTxHash,
	    FutureTxHash,
	    TxnLgrSeq,
	    OriginalAddress,
	}

	public class CrossChainArgs{
		public String originalAddress;
		public int 	  txnLedgerSeq;
		public String curTxHash;
		public String futureHash;
	}
	private static final int wait_milli = 50; 
	private static final int account_wait = 5000;
	private static final int submit_wait = 5000;
	private static final int sync_maxtime = 200000;
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
	
	abstract JSONObject prepareSigned();

	private JSONObject getError(String err){
		JSONObject obj = new JSONObject();
		obj.put("status", "error");
		obj.put("error_message", err);
		return obj;
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
        ManagedTxn tx = tm.manage(signed.txn);
        int count = account_wait/wait_milli;
        while(!account.getAccountRoot().primed()){
        	Util.waiting();
        	if(--count <= 0){
        		break;
        	}
        }
//        tm.queue(tx.onSubmitSuccess(this::onSubmitSuccess)
//                   .onError(this::onSubmitError));
        
        tm.queue(tx.onSubmitSuccess(new OnSubmitSuccess(){
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
       
        //subscribe tx
        if(sync || cb != null){
        	if(tx == null || tx.hash == null){
    			return getError("Submit failed,transaction hash is null.");
        	}
        	subscribeTx(tx.hash.toString());
        }
        
        //wait until submit return
        count = submit_wait / wait_milli;
        while(submit_state == SubmitState.waiting_submit){
        	Util.waiting();
        	if(--count <= 0){
        		submit_state = SubmitState.submit_error;
        		submitRes = getError("waiting submit result timeout");
        		break;
        	}
        }        
        
        if(sync){
        	if(submit_state == SubmitState.submit_error){
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
    	EventManager manager = new EventManager(connection);
    	manager.subTx(txId,new Callback<JSONObject>(){
			@Override
			public void called(JSONObject data) {
	    		//System.out.println(data);
	    		if(cb != null){
	    			cb.called((JSONObject)data);
	    		}else if(sync){
	    			JSONObject obj = (JSONObject)data;
	    			JSONObject res = new JSONObject();
	    			JSONObject tx = (JSONObject) obj.get("transaction");
	    			res.put("tx_hash", tx.get("hash").toString());
	    			
	    			if(condition == SyncCond.validate_success && obj.get("status").equals("validate_success")){
	    				res.put("status", "success");
	    			}else if(condition == SyncCond.db_success && obj.get("status").equals("db_success")){
	    				res.put("status", "success");
	    			}else if(!obj.get("status").equals("validate_success") && !obj.get("status").equals("db_success")){
	    				res.put("status", "error");
	    				if(res.has("error_message"))
	    					res.put("error_message", obj.get("error_message"));
	    				else
	    					res.put("error_message", obj.get("status"));
	    			}
	    			if(!res.isNull("status")){
	        			syncRes = res;
	        			sync_state = SyncState.sync_response;
	    			}
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

    private void onSubmitError(Response res) {
        JSONObject obj = new JSONObject();
        
        obj.put("status", "error");
        if(res.result.has("engine_result_message"))
        	obj.put("error_message", res.result.getString("engine_result_message"));
        if(res.result.has("engine_result_code")){
        	obj.put("error_code", res.result.getInt("engine_result_code"));
        }
        if(res.result.has("")){
        	JSONObject tx_json = (JSONObject) res.result.get("tx_json");
        	obj.put("tx_hash", tx_json.getString("hash"));
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
	protected Transaction toPayment(JSONObject json,TransactionType type) throws Exception{
    	Transaction payment = new Transaction(type);
    	 try {  
             Iterator<String> it = json.keys();  
             while (it.hasNext()) {  
                 String key = (String) it.next();  
                 Object value = json.get(key);  
                 enumPayment(payment,key,value);
             }
         } catch (JSONException e) {  
             e.printStackTrace();  
         }  
     	String fee = this.connection.client.serverInfo.fee_ref + "";
  		AccountID account = AccountID.fromAddress(this.connection.address);
 		Map<String,Object> map = Validate.rippleRes(this.connection.client, account);
 		if(mapError(map)){
 			throw new Exception((String)map.get("error_message"));
 		}else{
 	 		enumPayment(payment,"Sequence",map.get("Sequence"));
 	 		enumPayment(payment,"Fee",fee);
 	    	return payment;
 		}
    }
	
	private void enumPayment(Transaction payment,String strType,Object value){
		EPaymentType type = EPaymentType.valueOf(strType);
        switch (type) {
            case Account:
            	payment.as(AccountID.Account, value);
                break;
            case TxnLgrSeq:
            	payment.as(UInt32.TxnLgrSeq, value);
            	break;
            case OriginalAddress:
            	payment.as(AccountID.OriginalAddress, value);
            	break;
            case CurTxHash:
            	payment.as(Hash256.CurTxHash, value);
            	break;
            case FutureTxHash:
                payment.as(Hash256.FutureTxHash, value);
            	break;
            case Destination:
            	payment.as(AccountID.Destination, value);
            	break;
            case Amount:
            	payment.as(Amount.Amount, value);
            	break;
            case Tables:
//            	payment.as(STArray.Tables, Validate.fromJSONArray(((JSONArray)value).get(0).toString()));
            	payment.as(STArray.Tables, Validate.fromJSONArray((JSONArray)value));
                break;
            case OpType:
            	payment.as(UInt16.OpType, value);
                break;
            case User:
            	payment.as(AccountID.User, value);
                break;
            case Sequence:
            	payment.as(UInt32.Sequence, value);
            	break;
            case Raw:
            	payment.as(Blob.Raw,  value);
            	break;
            case Fee:
            	payment.as(Amount.Fee, value);
            	break;
            case NeedVerify:
            	payment.as(UInt32.NeedVerify,value);
            	break;
            case Statements:
            	payment.as(Blob.Statements,Util.toHexString(value.toString()));
            	break;
            case Owner:
            	payment.as(AccountID.Owner, value);
                break;
            case Flags:
            	payment.as(UInt32.Flags,value);
                break;
            case AutoFillField:
            	break;
            case TxCheckHash:
            	payment.as(Hash256.TxCheckHash, value);
            	break;
            case Token:
            	payment.as(Blob.Token, value);
            	break;
            default:
                break;
        } 
	}
	
}
