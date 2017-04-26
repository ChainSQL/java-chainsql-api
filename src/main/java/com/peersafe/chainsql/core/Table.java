package com.peersafe.chainsql.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
//import net.sf.json.JSONObject;
import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.SQLStatement;

public class Table extends Submit{
	private String name;
	private String owner;
	private List<String> query = new ArrayList<String>();
	private String exec;
	private Object data;
	public String message;
	
	public List<JSONObject> cache = new ArrayList<JSONObject>();
	public boolean strictMode = false;
	public boolean transaction = false;
	public	EventManager event;

	public Table insert(List<String> orgs){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = Util.StrToJsonStr(s);
				this.query.add(json);
			}
		}
	    this.exec = "r_insert";
	    if(this.transaction){
	    	JSONObject json = Tjson();
			this.cache.add(json);
			return null;
		}else{
			return this;
		}
		
	}
	
	public Table update(String orgs) {
		
		if(!"".equals(orgs)&&orgs!=null){
			String json = Util.StrToJsonStr(orgs);
			this.query.add(0, json);
		}
			
	    this.exec = "r_update";
	    if(this.transaction){
	    	JSONObject json = Tjson();
			this.cache.add(json);
			return null;
		}else{
			return this;
		}
		
	}
	
	public Table delete() {
		this.exec = "r_delete";
		if(this.transaction){
			JSONObject json = Tjson();
			this.cache.add(json);
			return null;
		}else{
			return this;
		}
		
	}
	public Table get(List<String> orgs){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = Util.StrToJsonStr(s);
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
	public void sqlAssert(){
		if (!this.transaction)
			try {
				throw new Exception("you must begin the transaction first");
			} catch (Exception e) {
				e.printStackTrace();
			}
		if (this.name==null)
			try {
				throw new Exception("you must appoint the table name");
			} catch (Exception e) {
				e.printStackTrace();
			}
		if(this.transaction){
			JSONObject json = Tjson();
			this.cache.add(json);
		}
	}
	
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
		List<JSONObject> orderarr = new ArrayList<JSONObject>();
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				JSONObject json = Util.StrToJson(s);
				orderarr.add(json);
			}
		}
		JSONObject json = new JSONObject();
		json.put("$order", orderarr);
		this.query.add(json.toString());
    	return this;
	}

	private SignedTransaction prepareTransaction(){
	    AccountID account = AccountID.fromAddress(connection.scope);
	    Map map = Validate.rippleRes(connection.client, account);
	    
	    if(map.get("Sequence") == null){
	    	return null;
	    }
	    
        return prepareSQLStatement(map);
	}

	
	private JSONObject Tjson(){
		JSONObject json = new JSONObject();
		String str ="{\"Table\":{\"TableName\":\""+Util.toHexString(name)+"\"}}";
		
		JSONArray table = new JSONArray();
		table.put(new JSONObject(str));
		json.put("Tables", table);
		json.put("Owner",  connection.scope);
//		json.put("TableName", name);
		json.put("Raw", this.query);
		json.put("OpType",Validate.toOpType(this.exec));
		return json;
	}
	
	private SignedTransaction prepareSQLStatement(Map map) {

		JSONObject txjson = Tjson();
		txjson.put("Account", this.connection.address);
		JSONObject tx_json = Validate.getTxJson(this.connection.client, txjson);
		String tebles ="";
		if("success".equals(tx_json.getString("status"))){
			tebles = tx_json.getJSONObject("result").getJSONArray("Tables").get(0).toString();
			System.out.println(tebles);
			
		}
		
		String fee = connection.client.serverInfo.fee_ref+"";
		SQLStatement payment = new SQLStatement();
        payment.as(AccountID.Owner,   connection.scope);
        payment.as(AccountID.Account, connection.address);
        payment.as(STArray.Tables, Validate.fromJSONArray(tebles));
        payment.as(UInt16.OpType, Validate.toOpType(exec));
        payment.as(Blob.Raw, Util.toHexString(query.toString()));
        payment.as(UInt32.Sequence, map.get("Sequence"));
        payment.as(Amount.Fee, fee);
        SignedTransaction signed = payment.sign(connection.secret);
        
        return signed;
	};

	@Override
	JSONObject doSubmit() {
		if(this.exec == "r_get"){
			return select();
		}else{
			return doSubmit(prepareTransaction());
		}
	}

	private JSONObject select(){
		if(query.size()==0||!query.get(0).substring(0, 1).contains("[")){
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
	
	public Table(String name) {
		super();
		this.name = name;
	}

	public Table() {
		super();
	
	}

}
