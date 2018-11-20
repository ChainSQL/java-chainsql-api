package com.peersafe.chainsql.core;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
//import net.sf.json.JSONObject;
import org.json.JSONObject;

import com.peersafe.base.client.requests.Request;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.chainsql.crypto.EncryptCommon;
import com.peersafe.chainsql.util.GenericPair;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;

public class Table extends Submit{
	private String name;
	private List<String> query = new ArrayList<String>();
	private String autoFillField = "";
	private String exec;

	/**
	 * Constructor for Table.
	 * @param name Tablename.
	 */
	public Table(String name) {
		super();
		this.name = name;
	}
/*
	public Table() {
		super();
	}
*/
	/**
	 * Insert data to a table.
	 * @param orgs Insert parameters.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table insert(List<String> orgs){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = Util.StrToJsonStr(s);
				this.query.add(json);
			}
		}
	    this.exec = "r_insert";
	    return dealWithTransaction();
		
	}
	
	/**
	 * Insert data to a table.
	 * @param orgs Insert parameters.
	 * @param sAutoFillField set auto fill Field such as txHash
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table insert(List<String> orgs, String sAutoFillField){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = Util.StrToJsonStr(s);
				this.query.add(json);
			}
		}
		if(!sAutoFillField.isEmpty())
			this.autoFillField = sAutoFillField;
	    this.exec = "r_insert";
	    return dealWithTransaction();
	}

	/**
	 * Update table data.
	 * @param orgs Update parameters.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table update(String orgs) {
		String json = Util.StrToJsonStr(orgs);
		this.query.add(0, json);

	    this.exec = "r_update";
	    return dealWithTransaction();
		
	}
	/**
	 * Delete data from a table.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table delete() {
		this.exec = "r_delete";
		return dealWithTransaction();
		
	}
	
	private Table dealWithTransaction(){
		if(this.transaction){
			JSONObject json;
			try {
				json = txJson();
				this.cache.add(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	/**
	 * Select data from a table.
	 * @param args Select parameters.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table get(List<String> args){
		if(args != null){
			for(String s: args){
				if(!"".equals(s)&&s!=null){
					String json = Util.StrToJsonStr(s);
					this.query.add(json);
				}
			}
		}
		
	    this.exec = "r_get";
		return this;
	}
	
	/**
	 * Select data from a table.
	 * @param arg Select parameters.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table get(String arg){
		if(!"".equals(arg) && arg !=null){
			String json = Util.StrToJsonStr(arg);
			this.query.add(json);
		}
		
	    this.exec = "r_get";
		return this;
		
	}
	
	public Table get(){		
	    this.exec = "r_get";
		return this;
	}
	/**
	 * Filter conditions when select.
	 * @param orgs Select conditions.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table withFields(String  orgs){
		if(!"".equals(orgs)&&orgs!=null){
			String ss = orgs.replace("\'", "\"");
			this.query.add(0, ss);
		}	
		return this;
		
	}
	
	/**
	 * Assertion when sql-transaction begins.
	 * @param orgs assert conditions.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table sqlAssert(List<String> orgs){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = Util.StrToJsonStr(s);
				this.query.add(json);
			}
			
		}
		this.exec = "t_assert";
		if (!this.transaction)
			System.out.println("Exception: you must begin the transaction first");
		if (this.name==null)
			System.out.println("Exception: you must appoint the table name");

		return dealWithTransaction();
	}
	
	/**
	 * Filter condition for select result.
	 * @param orgs parameters.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table limit(String orgs){
		JSONObject json = new JSONObject();
	    json.put("$limit", Util.StrToJson(orgs));
	    this.query.add(json.toString());
		return this;
	}

	/**
	 * Sort for a select result.
	 * @param orgs Sort conditions.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table order(List<String> orgs){
		JSONArray orderarr = new JSONArray();
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				JSONObject json = Util.StrToJson(s);
				orderarr.put(json);
			}
		}
		JSONObject json = new JSONObject();
		json.put("$order", orderarr);
		this.query.add(json.toString());
    	return this;
	}
	
	private JSONObject txJson() throws Exception{
		//System.out.println(this.query.toString());
		JSONObject json = new JSONObject();
		json.put("Tables", getTableArray(name));
		json.put("Owner",  connection.scope);
		json.put("Raw", tryEncryptRaw(this.query.toString()));
		json.put("OpType",Validate.toOpType(this.exec));
		json.put("StrictMode", this.strictMode);
		if(!this.autoFillField.isEmpty())
			json.put("AutoFillField", Util.toHexString(this.autoFillField));
		return json;
	}
	
	private String tryEncryptRaw(String strRaw) throws Exception{
		String token = "";
		boolean bFound = false;
		if(this.transaction){
			GenericPair<String,String> pair = new GenericPair<String,String>(this.connection.address,name);
			if(mapToken.containsKey(pair)){
				token = mapToken.get(pair);
				bFound = true;
			}
		}
		if(token.equals("") && !bFound){
			JSONObject res = this.connection.client.getUserToken(this.connection.scope,connection.address,name);
			if(res.has("status") && res.get("status").equals("error")){
				if(!this.transaction)
					System.out.println("Exception: "+res.getString("error_message"));
			}else if(res.has("token")) {
				token = res.getString("token");
			}
		}

		if(token.equals("")){
			strRaw = Util.toHexString(strRaw);
		}else{
			//有加密则不验证
			if(this.transaction){
				this.needVerify = 0;
			}
			try {
				byte[] seedBytes = null;
				if(!this.connection.secret.isEmpty()){
					seedBytes = getB58IdentiferCodecs().decodeFamilySeed(this.connection.secret);
				}
				byte[] password = EncryptCommon.asymDecrypt(Util.hexToBytes(token), seedBytes) ;
				if(password == null){
					System.out.println("Exception: decrypt token failed");
				}
				byte[] rawBytes = EncryptCommon.symEncrypt( strRaw.getBytes(),password);
				strRaw = Util.bytesToHex(rawBytes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	


		return strRaw;
	}
	
	private JSONObject prepareSQLStatement() {
		JSONObject txjson;
		try {
			txjson = txJson();
		} catch (Exception e) {
			e.printStackTrace();
			return Util.errorObject(e.getMessage());
		}
		
		txjson.put("Account", this.connection.address);
		
		//for cross chain
		if(crossChainArgs != null){
			txjson.put("TxnLgrSeq", crossChainArgs.txnLedgerSeq);
			txjson.put("OriginalAddress", crossChainArgs.originalAddress);
			txjson.put("CurTxHash", crossChainArgs.curTxHash);
			txjson.put("FutureTxHash", crossChainArgs.futureHash);
			crossChainArgs = null;
		}
		
		JSONObject result = Validate.tablePrepare(this.connection.client, txjson);
		if(result.getString("status").equals("error")){
			return result;
		}
		JSONObject tx_json = result.getJSONObject("tx_json");
		Transaction payment;
		
		try {
			payment = toTransaction(tx_json,TransactionType.SQLStatement);
	        signed = payment.sign(connection.secret);
	        return Util.successObject();
		} catch (Exception e) {
			e.printStackTrace();
			return Util.errorObject(e.getMessage());
		}   
	}

	@Override
	JSONObject prepareSigned() {
		if(this.exec == "r_get"){
			return select();
		}else{
			try {
				return prepareSQLStatement();
			} catch (Exception e) {
				e.printStackTrace();
				return new JSONObject(e.getLocalizedMessage());
			}
		}
	}

	private JSONObject select(){
		if(query.size()==0 || !query.get(0).substring(0, 1).contains("[")){
			query.add(0, "[]");
		}
		AccountID account = AccountID.fromAddress(connection.address);
		AccountID owner = AccountID.fromAddress(connection.scope);
		Request req = connection.client.select(account,owner,name,query.toString());
		
		return getSelectRes(req.response);
	}
	
	private JSONObject getSelectRes(Response response){
		JSONObject obj = new JSONObject();
		obj.put("status", response.status);
		if( !"error".equals(response.status)){
			//this.data = response.result.get("lines");
			obj.put("final_result", true);
			obj.put("lines", response.result.get("lines"));
			obj.put("diff", response.result.getInt("diff"));
		}else{
			obj.put("error_message", response.error);
		}
		return obj;
	}
}
