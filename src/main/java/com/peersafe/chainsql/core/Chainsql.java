package com.peersafe.chainsql.core;

import static com.ripple.config.Config.getB58IdentiferCodecs;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.crypto.Aes;
import com.peersafe.chainsql.crypto.Ecies;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;
import com.ripple.client.pubsub.Publisher.Callback;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
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
	private JSONObject retJson;
	//reconnect callback when disconnected
	private Callback<JSONObject> reconnectCb = null;
	private Callback<JSONObject> reconnectedCB = null;
	
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
		System.out.println("connect success");
		this.event = new EventManager(this.connection);
		this.connection.client.onReconnecting(this::onReconnecting);
		this.connection.client.onReconnected(this::onReconnected);
		
		return connection;
	}

	public void onReconnected(Callback<JSONObject> cb){
		this.reconnectedCB = cb;
	}
	public void onReconnecting(Callback<JSONObject> cb){
		this.reconnectCb = cb;
	}
	
	private void onReconnecting(JSONObject cb){
		if(reconnectCb != null){
			reconnectCb.called(cb);
		}
	}
	private void onReconnected(JSONObject cb){
		if(reconnectedCB != null){
			reconnectedCB.called(cb);
		}
		event.reSubscribe();
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
		Transaction payment;
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
		Transaction payment;
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
		Transaction payment;
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
		Transaction payment;
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
	
	public JSONObject getServerInfo(){
		return connection.client.getServerInfo();
	}
	public JSONObject getChainInfo(){
		JSONObject obj = new JSONObject();
		try{
			JSONObject serverInfo = getServerInfo();		
			String ledger_range = serverInfo.getJSONObject("info").getString("complete_ledgers");
			int startIndex = ledger_range.indexOf(',') == -1 ? 0 : ledger_range.lastIndexOf(',') + 1;
			int endIndex = ledger_range.lastIndexOf('-');
			int startLedger = Integer.parseInt(ledger_range.substring(startIndex,endIndex));
			int skipedTime = (startLedger - 1) * 3;
			startLedger = Math.max(startLedger,2);
			
			JSONObject firstLedger = getLedger(startLedger);
			JSONObject lastLedger = getLedger();
			if(firstLedger == null){
				System.out.println("error_message:" + "get first ledger failed ,please ensure connecting to a full-history server");
			}else{
				obj.put("tx_count", getTransactionCount());
				int seconds1 = firstLedger.getJSONObject("ledger").getInt("close_time");
				int seconds2 = lastLedger.getJSONObject("ledger").getInt("close_time");
				obj.put("chain_time", seconds2 - seconds1 + skipedTime);
			}
			return obj;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private JSONObject getTransactionCount(){
		return connection.client.getTransactionCount();
	}
	
	public JSONObject getLedger(){
		return getLedger(-1);
	}
	
	public JSONObject getLedger(Integer ledger_index){
		JSONObject option = new JSONObject();
		if(ledger_index == -1){
			option.put("ledger_index",  "validated");
		}else{
			option.put("ledger_index",  ledger_index);
		}
		
		retJson = null;
		this.connection.client.getLedger(option,(data)->{
			if(data == null){
				retJson = new JSONObject();
			}else{
				retJson = (JSONObject) data;
			}
		});
		while(retJson == null){
			Util.waiting();
		}
		
		if(retJson.has("ledger")){
			return retJson;
		}else{
			return null;
		}
		
	}
	
	public void getLedger(Callback<JSONObject> cb){
		JSONObject option = new JSONObject();
		option.put("ledger_index",  "validated");
		this.connection.client.getLedger(option,cb);
	}
	
	public void getLedger(Integer ledger_index,Callback<JSONObject> cb){
		JSONObject option = new JSONObject();
		option.put("ledger_index", ledger_index);
		this.connection.client.getLedger(option,cb);
		
	}
	
	public void getLedger(JSONObject option,Callback<JSONObject> cb){
		this.connection.client.getLedger(option,cb);
	}
	
	public JSONObject getLedgerVersion(){
		
		retJson = null;
		this.connection.client.getLedgerVersion((data)->{
			if(data == null){
				retJson = new JSONObject();
			}else{
				retJson = (JSONObject) data;
			}
		});
		while(retJson == null){
			Util.waiting();
		}
		
		if(retJson.has("ledger_current_index")){
			return retJson;
		}else{
			return null;
		}
		
	}
	public void getLedgerVersion(Callback<JSONObject> cb){
		this.connection.client.getLedgerVersion(cb);	
	}
	public JSONObject getTransactions(String address){
		retJson = null;
		this.connection.client.getTransactions(address,(data)->{
			if(data == null){
				retJson = new JSONObject();
			}else{
				retJson = (JSONObject) data;
			}
		});
		while(retJson == null){
			Util.waiting();
		}
		
		if(retJson.has("transactions")){
			return retJson;
		}else{
			return null;
		}
		
	}
	public void getTransactions(String address,Callback<JSONObject> cb){
		this.connection.client.getTransactions(address,cb);	
	}
	
	public JSONObject getTransaction(String hash){
		retJson = null;
		this.connection.client.getTransaction(hash,(data)->{
			if(data == null){
				retJson = new JSONObject();
			}else{
				retJson = (JSONObject) data;
			}
		});
		while(retJson == null){
			Util.waiting();
		}
		
		if(retJson.has("ledger_index")){
			return retJson;
		}else{
			return null;
		}
	}
	public void getTransaction(String hash,Callback<JSONObject> cb){
		this.connection.client.getTransaction(hash, cb);
	}
	
	public JSONObject generateAccount(){
		Security.addProvider(new BouncyCastleProvider());
		Seed seed = Seed.randomSeed();
		IKeyPair keyPair = seed.keyPair();
		byte[] pubBytes = keyPair.canonicalPubBytes();
		String secretKey = getB58IdentiferCodecs().encodeFamilySeed(seed.bytes());
		String publicKey = getB58IdentiferCodecs().encode(pubBytes, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
		String address = getB58IdentiferCodecs().encodeAddress(pubBytes);
		
		JSONObject obj = new JSONObject();
		obj.put("secret", secretKey);
		obj.put("account_id", address);
		obj.put("public_key", publicKey);
		return obj;
	}
    
	public Connection getConnection() {
		return connection;
	}
	public JSONObject commit(SyncCond cond){
		return doCommit(cond);
	}
	public JSONObject commit(Callback<?> cb){
		return doCommit(cb);
	}
	
	/**
	 * sqlTransaction commit
	 * @param commitType
	 * @return
	 */
	public JSONObject doCommit(Object  commitType){
		List<JSONObject> cache = this.cache;	
		
		JSONArray statements = new JSONArray();
        for (int i = 0; i < cache.size(); i++) {
        	statements.put(cache.get(i));
        }
        
		JSONObject json = new JSONObject();
		json.put("TransactionType",TransactionType.SQLTransaction);
		json.put( "Account", this.connection.address);
		json.put("Statements", statements);
		json.put("NeedVerify",this.needVerify);
		
        JSONObject result = Validate.getTxJson(this.connection.client, json);
		if(result.getString("status").equals("error")){
			return  new JSONObject(){{
				put("Error:",result.getString("error_message"));
			}};
		}
		JSONObject tx_json = result.getJSONObject("tx_json");
		Transaction paymentTS;
		try {
			paymentTS = toPayment(tx_json,TransactionType.SQLTransaction);
			signed = paymentTS.sign(this.connection.secret);
			if(commitType instanceof SyncCond ){
				return submit((SyncCond)commitType);
			}else if(commitType instanceof Callback){
				return submit((Callback<JSONObject>) commitType);
			}else{
				return submit();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private Transaction toPayment(JSONObject json) throws Exception{
		json.put("Account",this.connection.address);
    	JSONObject tx_json = Validate.getTxJson(this.connection.client, json);
    	if(tx_json.getString("status").equals("error")){
    		throw new Exception(tx_json.getString("error_message"));
    	}else{
    		tx_json = tx_json.getJSONObject("tx_json");
    	}
		return toPayment(tx_json,TransactionType.TableListSet);
	}
}
