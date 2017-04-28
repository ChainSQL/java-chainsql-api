package com.peersafe.chainsql.core;

import static com.ripple.config.Config.getB58IdentiferCodecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.crypto.Aes;
import com.peersafe.chainsql.crypto.Ecies;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;
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
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.encodings.B58IdentiferCodecs;

public class Chainsql extends Submit {
	public	EventManager event;
	public List<JSONObject> cache = new ArrayList<JSONObject>();
	private boolean strictMode = false;
	private boolean transaction = false;
	private Integer needVerify = 1;
	
	private static final int PASSWORD_LENGTH = 16;  
	
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
	
	private boolean mapError(Map<String,Object> map){
		if(map.get("Sequence") == null){
	    	return true;
	    }else{
	    	return false;
	    }
	}
	public Chainsql createTable(String name, List<String> raw) {
		return createTable(name, raw , false);
	}
	
	public Chainsql createTable(String name, List<String> rawList ,boolean confidential) {
		List<JSONObject> listRaw = Util.ListToJsonList(rawList);
		try {
			Util.checkinsert(listRaw);
		} catch (Exception e) {
			System.out.println("Exception:" + e.getLocalizedMessage());
		}
		
		JSONObject json = new JSONObject();
		json.put("OpType", 1);
		json.put("Tables", getTableArray(name));
		
		String strRaw = listRaw.toString();
		if(confidential){
			byte[] password = Util.getRandomBytes(PASSWORD_LENGTH);
			String token = generateUserToken(this.connection.secret,password);
			if(token.length() == 0){
				System.out.println("generateUserToken failed");
				return null;
			}
			json.put("Token", token);
			strRaw = Aes.aesEncrypt(password, strRaw);
		}else{
			strRaw = Util.toHexString(strRaw);
		}
		json.put("Raw", strRaw);
		
		if(this.transaction){
			this.cache.add(json);
			return null;
		}
		return create(json);
	}

	private Chainsql create(JSONObject txjson) {
		TableListSet payment;
		try {
			payment = toPayment(txjson);
			signed = payment.sign(this.connection.secret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	
	private String generateUserToken(String seed,byte[] password){
		IKeyPair keyPair = Seed.getKeyPair(seed);
		return Ecies.eciesEncrypt(password, keyPair.canonicalPubBytes());
	}

	public Chainsql dropTable(String name) {
		JSONObject json = new JSONObject();
		json.put("OpType", 2);
		json.put("Tables", getTableArray(name));
		if(this.transaction){
			this.cache.add(json);
			return null;
		}
		return drop(json);
	}

	private Chainsql drop(JSONObject txjson) {
		TableListSet payment;
		try {
			payment = toPayment(txjson);
		signed = payment.sign(this.connection.secret);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		TableListSet payment;
		try {
			payment = toPayment(txjson);
			signed = payment.sign(this.connection.secret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public Chainsql grant(String name, String user,String userPublicKey,String flag){
		JSONObject res = Validate.getUserToken(connection,this.connection.address,name);
		if(res.get("status").equals("error")){
			System.out.println(res.getString("error_message"));
			return null;
		}
		String token = res.getString("token");
		String newToken = "";
		if(token.length() != 0){
			try {
				byte[] password = Ecies.eciesDecrypt(token, this.connection.secret);
				if(password == null){
					return null;
				}
				byte [] pubBytes = getB58IdentiferCodecs().decode(userPublicKey, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
				newToken = Ecies.eciesEncrypt(password, pubBytes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return grant_inner(name,user,flag,newToken);
		
	}
	
	public Chainsql grant(String name, String user,String flag) {
		return grant_inner(name,user,flag,"");
	}

	private Chainsql grant_inner(String name, String user,String flag,String token) {
		List<JSONObject> flags = new ArrayList<JSONObject>();
		JSONObject json = Util.StrToJson(flag);
		flags.add(json);
		JSONObject txJson = new JSONObject();
		txJson.put("Tables", getTableArray(name));
		txJson.put("OpType", 11);
		txJson.put("User", user);
		txJson.put("Raw", Util.toHexString(flags.toString()));
		if(token.length() > 0){
			txJson.put("Token", token);
		}
		
		if(this.transaction){
			this.cache.add(txJson);
			return null;
		}
		return grant(name, txJson);
	}
	private Chainsql grant(String name, JSONObject txJson) {
		TableListSet payment;
		try {
			payment = toPayment(txJson);
		signed = payment.sign(this.connection.secret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public void beginTran(){
		 if (this.connection!=null && this.connection.address!=null) {
		    this.transaction = true;
		    return;
		  }
		
	}
	public JSONObject commit(){
		return doCommit("");
	}
	public JSONObject commit(SyncCond cond){
		return doCommit(cond);
	}
	public JSONObject commit(Callback cb){
		return doCommit(cb);
	}
	
	public JSONObject doCommit(Object  commitType){
		List<JSONObject> cache = this.cache;
		JSONObject payment = new JSONObject();
		payment.put("TransactionType",TransactionType.SQLTransaction);
		payment.put( "Account", this.connection.address);
		payment.put("Statements", new JSONArray());
		payment.put("NeedVerify",this.needVerify);
		
		
        for (int i = 0; i < cache.size(); i++) {
        	payment.getJSONArray("Statements").put(cache.get(i));
        }

        JSONObject result = Validate.getTxJson(this.connection.client, payment);
		if(result.getString("status").equals("error")){
//			System.out.println("Error:" + result.getString("error_message"));
			return  new JSONObject(){{
				put("Error:",result.getString("error_message"));
			}};
		}
		JSONObject tx_json = result.getJSONObject("tx_json");
		
        AccountID account = AccountID.fromAddress(this.connection.address);
		Map<String,Object> map = Validate.rippleRes(this.connection.client, account);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		if(mapError(map)){
			//System.out.println("Error:" +"This Sequence is null");
			return new JSONObject(){{
				put("Error:","This Sequence is null");
			}};
		}else{
			Transaction paymentTS  = new Transaction(TransactionType.SQLTransaction);
			paymentTS.as(AccountID.Account, tx_json.get("Account"));
			paymentTS.as(UInt16.TransactionType,TransactionType.SQLTransaction);
			paymentTS.as(UInt32.NeedVerify,tx_json.getInt("NeedVerify"));
			paymentTS.as(UInt32.Sequence, map.get("Sequence"));
			paymentTS.as(Amount.Fee, fee);
			paymentTS.as(Blob.Statements,Util.toHexString(tx_json.get("Statements").toString()));
			
			signed = paymentTS.sign(this.connection.secret);
			if(commitType instanceof SyncCond ){
				return submit((SyncCond)commitType);
			}else if(commitType instanceof Callback){
				return submit((Callback<JSONObject>) commitType);
			}else{
				return submit();
			}
			
		}
	}
	
    private TableListSet toPayment(JSONObject json)throws Exception{
    	json.put("Account",this.connection.address);
    	JSONObject tx_json = Validate.getTxJson(this.connection.client, json);
    	if(tx_json.getString("status").equals("error")){
    		throw new Exception(tx_json.getString("error_message"));
    	}else{
    		tx_json = tx_json.getJSONObject("tx_json");
    	}

    	TableListSet payment = new TableListSet();
    	 try {  
             Iterator<String> it = tx_json.keys();  
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
		Map<String,Object> map = Validate.rippleRes(this.connection.client, account);
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
            	payment.as(Blob.Token, value);
            	break;
            case StrictMode:
            	break;
            default:
                break;
        }
        
	}

	public void getLedger(JSONObject option,Callback<JSONObject> cb){
		this.connection.client.getLedger(option,cb);
	}
	
	public void getLedgerVersion(Callback<JSONObject>  cb){
		this.connection.client.getLedgerVersion(cb);
	}
	
	public void getTransactions(String address,Callback<JSONObject>  cb){
		this.connection.client.getTransactions(address,cb);	
	}

}
