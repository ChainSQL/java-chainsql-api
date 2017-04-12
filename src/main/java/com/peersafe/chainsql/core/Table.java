package com.peersafe.chainsql.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import net.sf.json.JSONObject;
import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.JSONUtil;
import com.peersafe.chainsql.util.Validate;
import com.ripple.client.Account;
import com.ripple.client.pubsub.Publisher.Callback;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.SQLStatement;

public class Table {
	private String name;
	private String owner;
	private List<String> query = new ArrayList<String>();
	private String exec;
	private Object data;
	public String message;
	public Connection connection;
	public Callback cb;
	
	public SubmitState submit_state;
	public SyncState sync_state;
	
	public JSONObject submitRes;
	public JSONObject syncRes;
	
	public boolean sync = false;
	public SyncCond condition;
	
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

	public Table insert(List<String> orgs){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = JSONUtil.StrToJsonStr(s);
				this.query.add(json);
			}
		}
	    this.exec = "r_insert";
		return this;
		
	}
	
	public Table update(List<String> orgs) {
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = JSONUtil.StrToJsonStr(s);
				this.query.add(0, json);
			}
			
		}
	    this.exec = "r_update";
		return this;
		
	}
	
	public Table delete() {
		this.exec = "r_delete";
		return this;
		
	}
	public Table get(List<String> orgs){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = JSONUtil.StrToJsonStr(s);
				this.query.add(json);
			}
			
		}
		
	    this.exec = "r_get";
		return this;
		
	}

	public Table withFields(String  orgs){
		if(!"".equals(orgs)&&orgs!=null){
			String ss = orgs.replace("\'", "\"");
			this.query.add(0, ss);
		}	
		return this;
		
	}
	/*public Table sqlAssert(String orgs){
		String ss = "";
		if(!"".equals(orgs)&&orgs!=null){
			 ss= orgs.replace("\'", "\"");
			
		}	
		JSONObject json = new JSONObject();
		json.put("$limit", ss);
		this.query.add(json.toString());
		return this;
	}
	*/
	public Table limit(String orgs){
		String ss = "";
		if(!"".equals(orgs)&&orgs!=null){
			 ss= orgs.replace("\'", "\"");
			
		}	
		JSONObject json = new JSONObject();
		json.put("$limit", ss);
		this.query.add(json.toString());
		return this;
	}

	public Table order(List<String> orgs){
		List<String> orderarr = new ArrayList<String>();
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = JSONUtil.StrToJsonStr(s);
				orderarr.add(json);
			}
		}
		JSONObject json = new JSONObject();
		json.put("$order", orderarr);
		this.query.add(json.toString());
    	return this;
	}
	
	
//	public Table submit(Callback cb){
//		if(this.exec=="r_get"){
//			select(this,this.connection,cb);
//		}else{
//			prepareSQLStatement(this,this.connection,cb);
//		}
//		return this;
//		
//	}
	
	/**
	 * asynchronous,callback trigger with all possible status
	 * @param cb
	 * @return submit result
	 */
	public JSONObject submit(Callback cb){
		this.cb = cb;
		if(this.exec == "r_get"){
			return select();
		}else{
			return prepareSQLStatement();
		}
	}
	
	/**
	 * synchronous,return when condition satisfied or submit failed
	 * @param cond,return condition
	 * @return
	 */
	public JSONObject submit(SyncCond cond){
		sync = true;
		condition = cond;
		
		if(this.exec=="r_get"){
			return select();
		}else{
			return prepareSQLStatement();
		}		
	}

	/**
	 * submit a transaction,return immediately
	 * @return submit result
	 */
	public JSONObject submit(){
		if(this.exec=="r_get"){
			return select();
		}else{
			return prepareSQLStatement();
		}
	}
	

	private JSONObject prepareSQLStatement(){
	    AccountID account = AccountID.fromAddress(connection.scope);
	    Map map = Validate.rippleRes(connection.client, account, name);
	    
	    if(map.get("Sequence") == null || map.get("NameInDB") == null){
	    	JSONObject obj = new JSONObject();
	    	obj.put("status","error");
	    	obj.put("error_message", "Command account_info failed.");
	    	return obj;
	    }
	    
        return prepareSQLStatement(map);
	}
	private JSONObject prepareSQLStatement(Map map) {
		Account account = connection.client.accountFromSeed(connection.secret);
	    TransactionManager tm = account.transactionManager();
		String str ="{\"Table\":{\"TableName\":\""+JSONUtil.toHexString(name)+"\",\"NameInDB\":\""+map.get("NameInDB")+"\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = connection.client.serverInfo.fee_ref+"";
		SQLStatement payment = new SQLStatement();
        payment.as(AccountID.Owner,      connection.scope);
        payment.as(AccountID.Account, connection.address);
        payment.as(STArray.Tables, arr);
        payment.as(UInt16.OpType, Validate.toOpType(exec));
        payment.as(Blob.Raw, JSONUtil.toHexString(query.toString()));
        payment.as(UInt32.Sequence, map.get("Sequence"));
        payment.as(Amount.Fee, fee);
        SignedTransaction signed = payment.sign(connection.secret);
        
        ManagedTxn tx = tm.manage(signed.txn);
        tm.queue(tx.onSubmitSuccess(this::onSubmitSuccess)
                   .onError(this::onSubmitError));
        
        //subscribe tx
        if(sync || cb != null){
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
	};
	
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
	
	private void waiting(){
      	try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	private JSONObject select(){
		if(query.size()==0||!query.get(0).contains("[")){
			query.add(0, "[]");
			
		}
		AccountID account = AccountID.fromAddress(connection.scope);
		String tables ="{\"Table\":{\"TableName\":\""+ name + "\"}}";
		JSONObject tabjson = new JSONObject(tables);

		JSONObject[] tabarr ={tabjson};
		Request req = connection.client.select(account,tabarr,query.toString(),(data)->{
			if(cb != null){
				Response response = (Response) data;
				cb.called(getSelectRes(response));
			}
		});
		
		return getSelectRes(req.response);
	}
	
	private JSONObject getSelectRes(Response response){
		JSONObject obj = new JSONObject();
		obj.put("status", response.status);
		if( !"error".equals(response.status)){
			this.data = response.result.get("lines");
			obj.put("lines", response.result.get("lines"));
		}else{
			obj.put("error_message", response.error);
		}
		return obj;
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
	
	public Table(String name) {
		super();
		this.name = name;
	}

	public Table() {
		super();
	
	}


	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setQuery(List<String> query) {
		this.query = query;
	}
	public List getQuery() {
		return query;
	}
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getExec() {
		return exec;
	}
	public void setExec(String exec) {
		this.exec = exec;
	}


	
}
