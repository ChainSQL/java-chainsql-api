package com.peersafe.chainsql.core;

import static com.ripple.java8.utils.Print.print;
import static com.ripple.java8.utils.Print.printErr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.peersafe.chainsql.net.Connection;
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

import net.sf.json.JSONObject;

public class Table {
	private String name;
	private String owner;
	private List<String> query = new ArrayList<String>();
	private String exec;
	private Object data;
	public String message;
	public Connection connection;
	public Callback cb;
	
	public Table insert(List<String> orgs){
		for(String s: orgs){
			String json = JSONUtil.StrToJsonStr(s);
			this.query.add(json);
		}
	    this.exec = "r_insert";
		return this;
		
	}
	
	public Table update(List<String> orgs) {
		for(String s: orgs){
			String json = JSONUtil.StrToJsonStr(s);
			this.query.add(0, json);
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
			if(!"".equals(orgs)&&orgs!=null){
				String json = JSONUtil.StrToJsonStr(s);
				this.query.add(json);
			}
			
		}
	    this.exec = "r_get";
		return this;
		
	}
	public Table select(List<String> orgs){
		for(String s: orgs){
			if(!"".equals(orgs)&&orgs!=null){
				String json = JSONUtil.StrToJsonStr(s);
				this.query.add(json);
			}
		}
		this.query.add(0, "[]");
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
			if(!"".equals(orgs)&&orgs!=null){
				String json = JSONUtil.StrToJsonStr(s);
				orderarr.add(json);
			}
		}
		JSONObject json = new JSONObject();
		json.put("$order", orderarr);
		this.query.add(json.toString());
    	return this;
	}
	
	
	public Table submit(Callback cb){
		if(this.exec=="r_get"){
			select(this,this.connection,cb);
		}else{
			prepareSQLStatement(this,this.connection,cb);
		}
		return this;
		
	}
	
	public void prepareSQLStatement(Table scope, Connection connect,Callback cb){
	    AccountID account = AccountID.fromAddress(connect.scope);
	    Map map = Validate.rippleRes(connect.client, account, scope.name);
        prepareSQLStatement( scope,connect,map,cb);

	}
	public void prepareSQLStatement(Table scope, Connection connect,Map map,Callback cb) {
		Account account = connect.client.accountFromSeed(connect.secret);
	    TransactionManager tm = account.transactionManager();
		String str ="{\"Table\":{\"TableName\":\""+JSONUtil.toHexString(scope.name)+"\",\"NameInDB\":\""+map.get("NameInDB")+"\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = connect.client.serverInfo.fee_ref+"";
		SQLStatement payment = new SQLStatement();
        payment.as(AccountID.Owner,      connect.scope);
        payment.as(AccountID.Account, connect.address);
        payment.as(STArray.Tables, arr);
        payment.as(UInt16.OpType, Validate.toOpType(scope.exec));
        payment.as(Blob.Raw, JSONUtil.toHexString(scope.query.toString()));
        payment.as(UInt32.Sequence, map.get("Sequence"));
        payment.as(Amount.Fee, fee);
        SignedTransaction signed = payment.sign(connect.secret);
      
        this.cb = cb;
        tm.queue(tm.manage(signed.txn)
 	            .onValidated(this::onValidated)
 	                .onError(this::onError));
    
		};
	

	public Table select(Table table,Connection connect,Callback cb){
		AccountID account = AccountID.fromAddress(connect.scope);
		String tables ="{\"Table\":{\"TableName\":\""+table.name+"\"}}";
		JSONObject tabjson = JSONObject.fromObject(tables);
		JSONObject[] tabarr ={tabjson};
		Request req = connect.client.select(account,tabarr,table.query.toString(),(data)->{
			Response response = (Response) data;
			
			if( !"error".equals(response.status)){
				this.data = response.result.get("lines");
				cb.called(response.result.get("lines"));
			}else{
				this.message = response.error;
				System.out.println("error_message :" +this.message);
				cb.called(response.error);
			}
		});
		
		
		return this;
	}
	private void onValidated(ManagedTxn managed) {
        TransactionResult tr = managed.result;
        cb.called(tr.toJSON());
       // print("Result:\n{0}", tr.toJSON().toString(2));
       // print("Transaction result was: {0}", tr.engineResult);
        //System.exit(0);
    }

    private void onError(ManagedTxn managed) {
        printErr("Transaction failed!");
        managed.submissions.forEach(sub ->
                printErr("{0}", sub.hash) );
        //System.exit(1);
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
