package com.peersafe.chainsql.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;
import com.ripple.client.Client.OnLedgerClosed;
import com.ripple.client.Client.OnPathFind;
import com.ripple.client.Client.OnTBMessage;
import com.ripple.client.Client.OnTXMessage;
import com.ripple.client.enums.Message;
import com.ripple.client.pubsub.Publisher.Callback;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.TableListSet;

public class Chainsql extends Submit {
	private String owner;
	private String[] query;
	private String exec;
	public	EventManager event;
	public List<JSONObject> cache = new ArrayList<JSONObject>();
	private boolean strictMode = false;
	private boolean transaction = false;
	private Integer needVerify = 1;
	
	private SignedTransaction signed;
	
	public enum paymentType{
		Account,
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
	    StrictMode
	}

	 public List array(Object val0, Object... vals){
		 	List res = new ArrayList();
		 	if(val0.getClass().isArray()){
		 		String[] a = (String[]) val0; 
		 		for(String s:a){
		 			res.add(s);
		 		}
		 	}else{
		 		  res.add(val0);
			      res.addAll(Arrays.asList(vals));
		 	}
	        return res;
	 }
	 
	public void as(String address, String secret) {
		this.connection.address = address;
		this.connection.secret = secret;
		if (this.connection.scope == null) {
			this.connection.scope = address;
		}
	}

	public void use(String address) {
		this.connection.scope = address;
	}

	public static final Chainsql c = new Chainsql();

	public Connection connect(String url) {
		connection = new Connection().connect(url);
		while (!connection.client.connected) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.event = new EventManager(this.connection);
		return connection;
	}

	public void disconnect() {
		this.connection.disconnect();
	}

	public void setRestrict(boolean falg) {
		this.strictMode = falg;
	}

	public Table table(String name) {
		Table tab = new Table(name);
		 if (this.transaction) {
		   	tab.transaction = this.transaction;
		    tab.cache = this.cache;
		}
		tab.strictMode = this.strictMode;
		tab.event = this.event;
		tab.connection = this.connection;
		return tab;
	}
	
	@Override
	JSONObject doSubmit() {
		return doSubmit(signed);
	}
	
	private boolean mapError(Map map){
		if(map.get("Sequence") == null){
	    	return true;
	    }else{
	    	return false;
	    }
	}
	public Chainsql createTable(String name, List<String> raw) {
		return createTable(name, raw , false);
	}
	
	public Chainsql createTable(String name, List<String> raw ,boolean confidential) {
		use(this.connection.address);

		List<JSONObject> strraw = new ArrayList<JSONObject>();
		for (String s : raw) {
			JSONObject json = Util.StrToJson(s);
			strraw.add(json);
		}
		try {
			Util.checkinsert(strraw);
		} catch (Exception e) {
			System.out.println("Exception:" + e.getLocalizedMessage());
		}
		String tablestr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(name) + "\"}}";
		JSONArray table = new JSONArray();
		table.put(new JSONObject(tablestr));
		
		JSONObject json = new JSONObject();
		json.put("OpType", 1);
		json.put("Tables", table);
//		json.put("Confidential",confidential);
		
		if(this.transaction){
			json.put("Raw", strraw);
			this.cache.add(json);
			return null;
		}
		return create(json,strraw);
	}

	private Chainsql create(JSONObject txjson,List<JSONObject> strraw) {
		txjson.put("Raw", Util.toHexString(strraw.toString()));
		TableListSet payment = toPayment(txjson);
		signed = payment.sign(this.connection.secret);
		return this;
	}

	public Chainsql dropTable(String name) {
		String tablestr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(name) +"\"}}";
		JSONArray table = new JSONArray();
		table.put(new JSONObject(tablestr));
		
		JSONObject json = new JSONObject();
		json.put("OpType", 2);
		json.put("Tables", table);
		if(this.transaction){
			this.cache.add(json);
			return null;
		}
		return drop(json);
	}

	private Chainsql drop(JSONObject txjson) {
		TableListSet payment = toPayment(txjson);
		signed = payment.sign(this.connection.secret);
		return this;
	}

	public Chainsql renameTable(String oldName, String newName) {
		String tablestr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(oldName) + "\",\"TableNewName\":\"" + Util.toHexString(newName) + "\"}}";
		JSONArray table = new JSONArray();
		table.put(new JSONObject(tablestr));
		JSONObject json = new JSONObject();
		json.put("OpType", 3);
		json.put("Tables", table);
		if(this.transaction){
			this.cache.add(json);
			return null;
		}
		return rename(json);
		
	}
	private Chainsql rename(JSONObject txjson) {
		TableListSet payment = toPayment(txjson);
		signed = payment.sign(this.connection.secret);
		return this;
	}

	public Chainsql grant(String name, String user, String flag) {
		List<JSONObject> flags = new ArrayList<JSONObject>();
		JSONObject json = Util.StrToJson(flag);
		flags.add(json);
		String tablestr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(name) + "\"}}";
		JSONArray table = new JSONArray();
		table.put(new JSONObject(tablestr));
		JSONObject txjson = new JSONObject();
		txjson.put("TransactionType",TransactionType.TableListSet);
		txjson.put("Tables", table);
		txjson.put("OpType", 11);
		txjson.put("User", user);
		txjson.put("Raw", Util.toHexString(flags.toString()));
		
		if(this.transaction){
			this.cache.add(txjson);
			return null;
		}
		return grant(name, txjson);
		
	}

	private Chainsql grant(String name, JSONObject txjson) {
		TableListSet payment = toPayment(txjson);
		signed = payment.sign(this.connection.secret);
		return this;
	}

	public void beginTran(){
		 if (this.connection!=null && this.connection.address!=null) {
		    this.transaction = true;
		    return;
		  }
		
	}
	
	public JSONObject commit(Callback cb){
		HashMap secretMap = new HashMap();
		List ary = new ArrayList();
		List<JSONObject> cache = this.cache;
		for(int i = 0;i<cache.size();i++){
			if(cache.get(i).get("OpType").toString().indexOf("2,3,5,7") == -1){
				if(cache.get(i).getInt("OpType")== 1 && cache.get(i).getBoolean("confidential")==true){
					
				}
				if (this.cache.get(i).get("OpType").toString().indexOf("6,8,9,10") != -1) {
			        this.needVerify = 0;
			    }
			    if (cache.get(i).getInt("OpType") != 1) {
			        //ary.add(Validate.getUserToken(this, cache.get(i).getString("TableName")));
			    }
				
			}
		}
		JSONObject payment = new JSONObject();
		payment.put("TransactionType",TransactionType.SQLTransaction);
		payment.put( "Account", this.connection.address);
		payment.put("Statements", new JSONArray());
		payment.put("StrictMode",this.strictMode);
		payment.put("NeedVerify",this.needVerify);
		
		
        for (int i = 0; i < cache.size(); i++) {
        	/*if(secretMap.get(cache.get(i).get("TableName"))!=null){
        		
        	}*/
        	payment.getJSONArray("Statements").put(cache.get(i));
        }
        JSONObject tx_json = Validate.getTxJson(this.connection.client, payment);
        if("success".equals(tx_json.getString("status"))){
    		tx_json = tx_json.getJSONObject("result");
 		}
        System.out.println(tx_json);
        AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		if(mapError(map)){
			return null;
		}else{
			Transaction paymentTS  = new Transaction(TransactionType.SQLTransaction);
			paymentTS.as(AccountID.Account, tx_json.get("Account"));
			paymentTS.as(UInt16.TransactionType,TransactionType.SQLTransaction);
			paymentTS.as(UInt32.NeedVerify,1);
			paymentTS.as(UInt32.Sequence, map.get("Sequence"));
			paymentTS.as(Amount.Fee, fee);
			paymentTS.as(Blob.Statements,Util.toHexString(tx_json.get("Statements").toString()));
			
			signed = paymentTS.sign(this.connection.secret);
			return submit(cb);
		}
	}
	
    private TableListSet toPayment(JSONObject json){
    	json.put("Account",this.connection.address);
    	JSONObject tx_json = Validate.getTxJson(this.connection.client, json);
    	if("success".equals(tx_json.getString("status"))){
    		tx_json = tx_json.getJSONObject("result");
 		}
    	TableListSet payment = new TableListSet();
    	 try {  
             Iterator it = tx_json.keys();  
             while (it.hasNext()) {  
                 String key = (String) it.next();  
                 Object value = tx_json.get(key);  
                 enumPayment(payment,key,value);
             }  
         } catch (JSONException e) {  
             e.printStackTrace();  
         }
    	 
    
    	String fee = this.connection.client.serverInfo.fee_ref + "";
 		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account);
		if(mapError(map)){
			return null;
		}else{
	 		enumPayment(payment,"Sequence",map.get("Sequence"));
	 		enumPayment(payment,"Fee",fee);
	    	return payment;
		}
 	
    	
    }
    
	private void enumPayment(TableListSet payment,String str,Object value){
		paymentType type = paymentType.valueOf(str);
        switch (type) {
            case Account:
            	payment.as(AccountID.Account, value);
                break;
            case Tables:
            	payment.as(STArray.Tables, Validate.fromJSONArray(((JSONArray)value).get(0).toString()));
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
            case TransactionType:
                break;
            case TableNewName:
                break;
            case Owner:
                break;
            case Flags:
                break;
            case AutoFillField:
            	break;
            case Token:
            	break;
            case StrictMode:
            	break;
            default:
                break;
        }
        
	}
	public void getUserToken(String name){
		Validate.getUserToken(this, name);
	}

	public void getLedger(JSONObject option,Callback cb){
		this.connection.client.getLedger(option,cb);
	}
	
	public void getLedgerVersion(Callback cb){
		this.connection.client.getLedgerVersion(cb);
	}
	
	public void getTransactions(String address,Callback cb){
		this.connection.client.getTransactions(address,cb);	
	}

}
