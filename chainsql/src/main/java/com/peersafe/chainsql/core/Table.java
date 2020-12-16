package com.peersafe.chainsql.core;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.util.ArrayList;
import java.util.List;

import com.peersafe.base.utils.Utils;
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
	private List<String> query = new ArrayList<>();
	private String  exec;
	private String  autoFillField;
	private String  txsHashFillField;
	private String  nameInDB;
	private boolean confidential = false;  // 标志是否为加密表

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
	 * @param orgs  Insert parameters.
	 * @param autoFillField AutoFillField filed.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table insert(List<String> orgs,String autoFillField){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = Util.StrToJsonStr(s);
				this.query.add(json);
			}
		}
		this.autoFillField = autoFillField;
		this.exec = "r_insert";
		return dealWithTransaction();

	}


	/**
	 * Insert data to a table.
	 * @param orgs  Insert parameters.
	 * @param autoFillField AutoFillField filed.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table insert(List<String> orgs,String autoFillField,String txsHashFillField){
		for(String s: orgs){
			if(!"".equals(s)&&s!=null){
				String json = Util.StrToJsonStr(s);
				this.query.add(json);
			}
		}

		if(!autoFillField.isEmpty()){
			this.autoFillField = autoFillField;
		}

		if(!txsHashFillField.isEmpty()){
			this.txsHashFillField = txsHashFillField;
		}

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
	 * Update data to a table.
	 * @param orgs  Update parameters.
	 * @param autoFillField AutoFillField filed.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table update(String orgs,String autoFillField){


		String json = Util.StrToJsonStr(orgs);
		this.query.add(0, json);

		this.autoFillField = autoFillField;
		this.exec = "r_update";
		return dealWithTransaction();

	}


	/**
	 * Update data to a table.
	 * @param orgs  Update parameters.
	 * @param autoFillField AutoFillField filed.
	 * @param txsHashFillField AutoFillField filed.
	 * @return Table object,can be used to operate Table continually.
	 */
	public Table update(String orgs,String autoFillField,String txsHashFillField){


		String json = Util.StrToJsonStr(orgs);
		this.query.add(0, json);

		if(!autoFillField.isEmpty()){
			this.autoFillField = autoFillField;
		}

		if(!txsHashFillField.isEmpty()){
			this.txsHashFillField = txsHashFillField;
		}

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
		JSONObject json = new JSONObject();
		json.put("Tables", getTableArray(name));
		json.put("Owner",  connection.scope);
		json.put("Raw", tryEncryptRaw(this.query.toString()));
		json.put("OpType",Validate.toOpType(this.exec));
		json.put("StrictMode", this.strictMode);
		return json;
	}
	
	private String tryEncryptRaw(String strRaw) throws Exception{

		if( !this.confidential ){
			strRaw = Util.toHexString(strRaw);
			return strRaw;
		}

		// 处理加密表
		String token = "";
		boolean bFound = false;
		if(this.transaction){
			GenericPair<String,String> pair = new GenericPair<>(this.connection.address,name);
			if(mapToken.containsKey(pair)){
				token = mapToken.get(pair);
				bFound = true;
			}
		}

		if(token.equals("") && !bFound){
			JSONObject res = this.connection.client.getUserToken(this.connection.scope,connection.address,name);
			if(res.has("error")){
				if(!this.transaction)
					throw new Exception(res.getString("error_message"));
			}else if(res.has("token")) {
				token = res.getString("token");
			}
		}

		//有加密则不验证
		if(this.transaction){
			this.needVerify = 0;
		}
		try {
			byte[] seedBytes = null;

			boolean bSoftGM = Utils.getAlgType(this.connection.secret).equals("softGMAlg");
			if(!this.connection.secret.isEmpty()){

				if(bSoftGM){
					seedBytes   = getB58IdentiferCodecs().decodeAccountPrivate(this.connection.secret);
				}else{
					seedBytes = getB58IdentiferCodecs().decodeFamilySeed(this.connection.secret);
				}

			}

			byte[] password = EncryptCommon.asymDecrypt(Util.hexToBytes(token), seedBytes,bSoftGM) ;
			if(password == null){
				System.out.println("Exception: decrypt token failed");
			}
			byte[] rawBytes = EncryptCommon.symEncrypt( strRaw.getBytes(),password,bSoftGM);
			strRaw = Util.bytesToHex(rawBytes);
		} catch (Exception e) {
			e.printStackTrace();
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

		if (this.connection.userCert != null) {
			String sCert = Util.toHexString(this.connection.userCert);
			txjson.put("Certificate", sCert);
		}

		if(this.autoFillField != null){
			txjson.put("AutoFillField", Util.toHexString(this.autoFillField));
		}

		if(this.txsHashFillField != null){
			txjson.put("TxsHashFillField", Util.toHexString(this.txsHashFillField));
		}

		//for cross chain
		if(crossChainArgs != null){
			txjson.put("TxnLgrSeq", crossChainArgs.txnLedgerSeq);
			txjson.put("OriginalAddress", crossChainArgs.originalAddress);
			txjson.put("CurTxHash", crossChainArgs.curTxHash);
			txjson.put("FutureTxHash", crossChainArgs.futureHash);
			crossChainArgs = null;
		}

		JSONObject tx_json = txjson;
		if(this.nameInDB != null){
			tx_json.put("NameInDB",this.nameInDB);

		}else{

			JSONObject result = this.connection.client.tablePrepare(txjson);
			if(result.has("error")){
				return result;
			}
			tx_json = result.getJSONObject("tx_json");
		}

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
	protected
	JSONObject prepareSigned() {
		if(this.exec.equals("r_get")){
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
		return connection.client.select(this.connection.secret,account,owner,name,query.toString(),cb);
	}

	/**
	 *  设置表的属性，包括 nameInDB; 是否为加密表
	 * @param tableProperties {"nameInDB":"AAAA","confidential":false}
	 * @return Table
	 */
	public Table tableSet(JSONObject tableProperties){

		if(tableProperties.has("nameInDB")){
			this.nameInDB = tableProperties.getString("nameInDB");
		}

		if(tableProperties.has("confidential")){
			this.confidential = tableProperties.getBoolean("confidential");
		}

		return this;
	}


	@Override
	public JSONArray getTableArray(String tableName){

		String tableStr;
		if(this.nameInDB != null){
			tableStr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(tableName) + "\",\"NameInDB\":\"" + this.nameInDB + "\"}}";
		}else{
			tableStr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(tableName) + "\"}}";
		}

		return Util.strToJSONArray(tableStr);
	}
}
