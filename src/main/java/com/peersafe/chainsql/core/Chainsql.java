package com.peersafe.chainsql.core;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.math.BigInteger;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.core.types.known.tx.signed.SignedTransaction;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.encodings.B58IdentiferCodecs;
import com.peersafe.chainsql.crypto.Aes;
import com.peersafe.chainsql.crypto.Ecies;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.resources.Constant;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;

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
	
	/**
	 * Assigning the operating user.
	 * @param address Account address,start with a lower case 'r'.
	 * @param secret  Account secret,start with a lower case 's'.
	 */
	public void as(String address, String secret) {
		this.connection.address = address;
		this.connection.secret = secret;
		if (this.connection.scope == null) {
			this.connection.scope = address;
		}
	}

	/**
	 * Assigning table owner.
	 * @param address Address of table owner.
	 */
	public void use(String address) {
		this.connection.scope = address;
	}

	public static final Chainsql c = new Chainsql();

	/**
	 * Connect to a websocket url.
	 * @param url Websocket url to connect,eg:"ws://127.0.0.1:5006".
	 * @return Connection object after connected.
	 */
	@SuppressWarnings("resource")
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
	
	/**
	 * Transfer variable number of Strings to List<String>
	 * @param val0
	 * @param vals
	 * @return
	 */
	public static List<String> array(String val0, String... vals){
	 	List<String> res = new ArrayList<String>();
	 	res.add(val0);
	 	res.addAll(Arrays.asList(vals));

        return res;
	}

	/**
	 * Subscribe 'reconnected' event,cb.called trigger when connection lost and reconnect succeed.
	 * @param cb
	 */
	public void onReconnected(Callback<JSONObject> cb){
		this.reconnectedCB = cb;
	}
	/**
	 * Subscribe 'reconnecting' event,cb.called trigger when connection lost and reconnecting started.
	 * @param cb
	 */
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
	/**
	 * Disconnect the websocket connection.
	 */
	public void disconnect() {
		this.connection.disconnect();
	}

	/**
	 * Set restrict mode.
	 * If restrict mode enabled,transaction will fail when user executing a consecutive operation 
	 * to a table and some other user interrupts this by making an operation to this identical table.  
	 * @param falg True to enable restrict mode and false to disable restrict mode.
	 */
	public void setRestrict(boolean falg) {
		this.strictMode = falg;
	}

	/**
	 * Create a Table object by giving a table name.
	 * @param name Name of a table.
	 * @return Table object.
	 */
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
	
	/**
	 * A create table operation.
	 * @param name Table name
	 * @param raw  Option or conditions to create a table.
	 * @return	You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql createTable(String name, List<String> raw) {
		return createTable(name, raw , false);
	}
	
	/**
	 * A create table operation.
	 * @param name Table name.
	 * @param rawList Option or conditions to create a table.
	 * @param confidential Table will be confidential or not.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql createTable(String name, List<String> rawList ,boolean confidential) {
		List<JSONObject> listRaw = Util.ListToJsonList(rawList);
		try {
			Util.checkinsert(listRaw);
		} catch (Exception e) {
			System.out.println("Exception:" + e.getLocalizedMessage());
		}
		
		JSONObject json = new JSONObject();
		json.put("OpType", Constant.opType.get("t_create"));
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
		return prepare(json);
	}
	
	private String generateUserToken(String seed,byte[] password){
		IKeyPair keyPair = Seed.getKeyPair(seed);
		return Ecies.eciesEncrypt(password, keyPair.canonicalPubBytes());
	}
	/**
	 * Recreate a table, for slimming the chain.
	 * @param name Table name.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql recreateTable(String name){
		JSONObject json = new JSONObject();
		json.put("OpType", Constant.opType.get("t_recreate"));
		json.put("Tables", getTableArray(name));
		if(this.transaction){
			this.cache.add(json);
			return null;
		}
		return prepare(json);
	}
	/**
	 * A drop table operation.
	 * @param name Table name.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql dropTable(String name) {
		JSONObject json = new JSONObject();
		json.put("OpType", Constant.opType.get("t_drop"));
		json.put("Tables", getTableArray(name));
		if(this.transaction){
			this.cache.add(json);
			return null;
		}
		return prepare(json);
	}

	/**
	 * Rename a table.
	 * @param oldName
	 * @param newName
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql renameTable(String oldName, String newName) {
		String tablestr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(oldName) + "\",\"TableNewName\":\"" + Util.toHexString(newName) + "\"}}";
		JSONArray table = new JSONArray();
		table.put(new JSONObject(tablestr));
		JSONObject json = new JSONObject();
		json.put("OpType", Constant.opType.get("t_rename"));
		json.put("Tables", table);
		if(this.transaction){
			this.cache.add(json);
			return null;
		}
		return prepare(json);
		
	}

	/**
	 * Grant a user with authorities to operate a table.
	 * @param name Table name
	 * @param user User address,start with a lower case 'r'.
	 * @param userPublicKey User's public key,start with a lower case 'a'.
	 * 						Will be used if the table is confidential.
	 * @param flag Options to notify the authorities.eg:"{insert:true,delete:false}" means 
	 * 			   the user can insert to this table,but cannot delete from this table.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
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
	/**
	 * Grant a user with authorities to operate a table.
	 * @param name Table name
	 * @param user User address,start with a lower case 'r'.
	 * @param flag Options to notify the authorities.eg:"{insert:true,delete:false}" means 
	 * 			   the user can insert to this table,but cannot delete from this table.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql grant(String name, String user,String flag) {
		return grant_inner(name,user,flag,"");
	}

	private Chainsql grant_inner(String name, String user,String flag,String token) {
		List<JSONObject> flags = new ArrayList<JSONObject>();
		JSONObject json = Util.StrToJson(flag);
		flags.add(json);
		JSONObject txJson = new JSONObject();
		txJson.put("Tables", getTableArray(name));
		txJson.put("OpType", Constant.opType.get("t_grant"));
		txJson.put("User", user);
		txJson.put("Raw", Util.toHexString(flags.toString()));
		if(token.length() > 0){
			txJson.put("Token", token);
		}
		
		if(this.transaction){
			this.cache.add(txJson);
			return null;
		}
		return prepare(txJson);
	}
	/**
	 * Start a payment transaction, can be used to activate account 
	 * @param accountId The Address of an account.
	 * @param count		Count of coins to transfer.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql pay(String accountId,int count){
		JSONObject obj = new JSONObject();
		obj.put("Account", this.connection.address);
		obj.put("Destination", accountId);
		BigInteger amount = BigInteger.valueOf(count * 1000000);
		obj.put("Amount", amount.toString());
		Transaction payment;
		try {
			payment = toPayment(obj,TransactionType.Payment);
			signed = payment.sign(this.connection.secret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Begin a sql-transaction type operation.
	 * Sql-transaction is like the transaction in db. Transactions in it will all success or all rollback. 
	 */
	public void beginTran(){
		 if (this.connection!=null && this.connection.address!=null) {
		    this.transaction = true;
		    return;
		  }
		
	}
	/**
	 * Commit a sql-transaction type operation.
	 * @return
	 */
	public JSONObject commit(){
		return doCommit("");
	}
	
	/**
	 * Get server info.
	 * @return Server's informations.
	 */
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
	
	/**
	 * Get the newest generated ledger.
	 * @return
	 */
	public JSONObject getLedger(){
		return getLedger(-1);
	}
	
	/**
	 * Get the ledger identified by ledger_index.
	 * @param ledger_index Index of a ledger.
	 * @return Ledger informations.
	 */
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
	/**
	 * An asynchronous api to get the ledger identified by ledger_index.
	 * @return Ledger informations.
	 */
	public void getLedger(Callback<JSONObject> cb){
		JSONObject option = new JSONObject();
		option.put("ledger_index",  "validated");
		this.connection.client.getLedger(option,cb);
	}
	/**
	 * Get the ledger identified by ledger_index.
	 * @param ledger_index
	 * @param cb
	 */
	public void getLedger(Integer ledger_index,Callback<JSONObject> cb){
		JSONObject option = new JSONObject();
		option.put("ledger_index", ledger_index);
		this.connection.client.getLedger(option,cb);
		
	}
	
	/**
	 * Get newest validated ledger index
	 * @return
	 */
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
	/**
	 * Get newest validated ledger index,asynchronous.
	 * @return
	 */
	public void getLedgerVersion(Callback<JSONObject> cb){
		this.connection.client.getLedgerVersion(cb);	
	}
	/**
	 * Get trasactions submitted by notified account.
	 * @param address Account address.
	 * @return Result.
	 */
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
	/**
	 * Get trasactions submitted by notified account,asynchronous.
	 * @param address Account address.
	 * @return Result.
	 */
	public void getTransactions(String address,Callback<JSONObject> cb){
		this.connection.client.getTransactions(address,cb);	
	}
	
	/**
	 * Get transaction identified by hash.
	 * @param hash Transaction hash.
	 * @return Transaction information.
	 */
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
	/**
	 * Get transaction by hash asynrhonously.
	 * @param hash Transaction hash.
	 * @param cb
	 */
	public void getTransaction(String hash,Callback<JSONObject> cb){
		this.connection.client.getTransaction(hash, cb);
	}
	
	/**
	 * Generate a new account.
	 * @return Contains folling fields:
	 * 		   secret:Account secret.
	 * 		   account_id:Account address.
	 * 		   public_key:Account publickey. 
	 */
	public JSONObject generateAddress(){
		Security.addProvider(new BouncyCastleProvider());
		Seed seed = Seed.randomSeed();
		IKeyPair keyPair = seed.keyPair();
		byte[] pubBytes = keyPair.canonicalPubBytes();
		byte[] o;
		{
			RIPEMD160Digest d = new RIPEMD160Digest();
		    d.update (pubBytes, 0, pubBytes.length);
		    o = new byte[d.getDigestSize()];
		    d.doFinal (o, 0);
		}

		String secretKey = getB58IdentiferCodecs().encodeFamilySeed(seed.bytes());
		String publicKey = getB58IdentiferCodecs().encode(pubBytes, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
		String address = getB58IdentiferCodecs().encodeAddress(o);
		
		JSONObject obj = new JSONObject();
		obj.put("secret", secretKey);
		obj.put("account_id", address);
		obj.put("public_key", publicKey);
		return obj;
	}
	
	public Connection getConnection() {
		return connection;
	}

	/**
	 * sqlTransaction commit
	 * @param commitType
	 * @return
	 */
	@SuppressWarnings("unchecked")
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
	/**
	 * Commit sql-transactoin synchronously.
	 * @param cond Return condition.
	 * @return Commit result.
	 */
	public JSONObject commit(SyncCond cond){
		return doCommit(cond);
	}
	/**
	 * Commit sql-transactoin asynchronously.
	 * @param cb Callback object.
	 * @return Commit result.
	 */
	public JSONObject commit(Callback<?> cb){
		return doCommit(cb);
	}
	
	private Chainsql prepare(JSONObject txjson){
		Transaction payment;
		try {
			payment = toPayment(txjson);
			signed = payment.sign(this.connection.secret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
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
