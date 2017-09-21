package com.peersafe.chainsql.core;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.math.BigInteger;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.client.Client.OnReconnected;
import com.peersafe.base.client.Client.OnReconnecting;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.config.Config;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.fields.Field;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.core.types.known.tx.signed.SignedTransaction;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.encodings.B58IdentiferCodecs;
import com.peersafe.chainsql.crypto.EncryptCommon;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.resources.Constant;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.GenericPair;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;

public class Chainsql extends Submit {
	public	EventManager event;

	private JSONObject mTxJson;
	
	private static final int PASSWORD_LENGTH = 16;  
	private static final int DEFAULT_TX_LIMIT = 20;
	
	private JSONObject mRetJson;
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
	 * @param url Websocket url to connect,e.g.:"ws://127.0.0.1:5006".
	 * @return Connection object after connected.
	 */
	@SuppressWarnings("resource")
	public Connection connect(String url) {
		connection = new Connection().connect(url);
		doWhenConnect();
		return connection;
	}
	/**
	 * Connect to a secure websocket url.
	 * @param wss url,e.g.:"ws://127.0.0.1:5006".
	 * @param serverCertPath
	 * @param storePass
	 * @return
	 */
	@SuppressWarnings("resource")
	public Connection connect(String url,String serverCertPath,String storePass) {
		connection = new Connection().connect(url,serverCertPath,storePass);
		doWhenConnect();
		return connection;
	}
	
	private void doWhenConnect(){
		while (!connection.client.connected) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("connect success");
		this.event = new EventManager(this.connection);
		//jdk1.8
//		this.connection.client.onReconnecting(this::onReconnecting);
//		this.connection.client.onReconnected(this::onReconnected);
		this.connection.client.onReconnecting(new OnReconnecting(){
			@Override
			public void called(JSONObject args) {
				onReconnecting(args);
			}
		});
		this.connection.client.onReconnected(new OnReconnected(){
			@Override
			public void called(JSONObject args) {
				onReconnected(args);
			}			
		});
	}
	
	/**
	 * Transfer variable number of Strings to List of String
	 * @param val0 Parameter
	 * @param vals Parameter
	 * @return List of String
	 */
	public static List<String> array(String val0, String... vals){
	 	List<String> res = new ArrayList<String>();
	 	res.add(val0);
	 	res.addAll(Arrays.asList(vals));

        return res;
	}

	/**
	 * Subscribe 'reconnected' event,cb.called trigger when connection lost and reconnect succeed.
	 * @param cb Callback
	 */
	public void onReconnected(Callback<JSONObject> cb){
		this.reconnectedCB = cb;
	}
	/**
	 * Subscribe 'reconnecting' event,cb.called trigger when connection lost and reconnecting started.
	 * @param cb Callback
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
	 * Create a Table object by giving a table name.
	 * @param name Name of a table.
	 * @return Table object.
	 */
	public Table table(String name) {
		Table tab = new Table(name);
		 if (this.transaction) {
		   	tab.transaction = this.transaction;
		    tab.cache = this.cache;
		    tab.mapToken = this.mapToken;
		}
		tab.strictMode = this.strictMode;
		tab.event = this.event;
		tab.connection = this.connection;
		tab.setCrossChainArgs(this.crossChainArgs);
		return tab;
	}
	
	/**
	 * use guomi algorithm
	 * @param useGM 
	 * @param bNewKeyPair 是否生成新的公私钥对
	 * @param pin default is '666666'
	 * @throws Exception throws exception if failed.
	 */
	public void setUseGM(boolean useGM,boolean bNewKeyPair,String pin) throws Exception{
		boolean isSuccess =  Config.setUseGM(useGM,bNewKeyPair,pin);
		if(!isSuccess){
			throw new Exception("设置使用国密失败!");
		}
	}
	
	public boolean isUseGM(){
		return Config.isUseGM();
	}
	/**
	 * Sign a transaction.
	 * @param tx transaction Json.
	 * @param secret 
	 * @return tx_blob and hash:
	 * {
	 * 	  "tx_blob":"xxxxx",
	 *	  "hash":"xxx"
	 * }
	 */
	public JSONObject sign(JSONObject tx,String secret){
		JSONObject tx_json = tx.getJSONObject("tx_json");
		TransactionType type = TransactionType.valueOf(tx_json.getString("TransactionType"));
		Transaction transaction = new Transaction(type);
		try {
			transaction.parseFromJson(tx_json);
			//Fee
			checkFee(transaction,tx_json);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SignedTransaction signed = transaction.sign(secret);
		
		JSONObject obj = new JSONObject();
		obj.put("tx_blob", signed.tx_blob);
		obj.put("hash", signed.hash.toString());
		return obj;
	}
	/**
	 * sign for 
	 * @param tx transaction Json.
	 * @param secret
	 * @return sign result form:
	 {
	    "Signer":{
	        "Account":"rDsFXt1KRDNNckSh3exyTqkQeBKQCXawb2",
	        "SigningPubKey":"02E37D565DF377D0C30D93163CF40F41BB81B966B11757821F25FBCDCFEA18E8A9",
	        "TxnSignature":"3044022050903320FF924BCD7F55D3BE095A457BF2421E805C5B39DA77F006BB217D6398022024C51DECA25018D80CB16AB65674B71BFD20789D63EC47FD5EAD7FC75B880055"
	    },
	    "hash":""
	 }
	 */
	public JSONObject sign_for(JSONObject tx,String secret){
		if(!tx.has("secret")){
			return Util.errorObject("no secret supplied");
		}
			;
		if(!tx.has("account")){
			return Util.errorObject("no account supplied");
		}

		JSONObject tx_json = tx.getJSONObject("tx_json");
		TransactionType type = TransactionType.valueOf(tx_json.getString("TransactionType"));
		Transaction transaction = new Transaction(type);
		try {
			transaction.parseFromJson(tx_json);
			//Fee
			checkFee(transaction,tx_json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SignedTransaction signed = transaction.multiSign(secret);
		
		String sJson = signed.txn.prettyJSON();
//		System.out.println(sJson);
		
		IKeyPair keyPair = Seed.fromBase58(secret).keyPair();
		String publicKey = Util.bytesToHex(keyPair.canonicalPubBytes());
		
		JSONObject json = new JSONObject(sJson);
		JSONObject signer = new JSONObject();
		signer.put("Account",tx.getString("account"));
		signer.put("SigningPubKey", publicKey);
		signer.put("TxnSignature", json.getString("TxnSignature"));
		JSONObject ret = new JSONObject();
		ret.put("Signer", signer);
		ret.put("hash", signed.hash.toString());
		return ret;
	}
	
	private void checkFee(Transaction transaction,JSONObject tx_json){
		if(!tx_json.has("Fee")){
			if(connection != null && connection.client != null && connection.client.serverInfo != null){
				Amount fee = connection.client.serverInfo.transactionFee(transaction);
				transaction.as(Amount.Fee, fee);
			}else{
				transaction.as(Amount.Fee, "50");
			}
		}
	}
	@Override
	JSONObject prepareSigned() {
		try {
			mTxJson.put("Account",this.connection.address);

			//for cross chain
			if(crossChainArgs != null){
				mTxJson.put("TxnLgrSeq", crossChainArgs.txnLedgerSeq);
				mTxJson.put("OriginalAddress", crossChainArgs.originalAddress);
				mTxJson.put("CurTxHash", crossChainArgs.curTxHash);
				mTxJson.put("FutureTxHash", crossChainArgs.futureHash);
				crossChainArgs = null;
			}
			
	    	JSONObject tx_json = Validate.tablePrepare(this.connection.client, mTxJson);
	    	if(tx_json.getString("status").equals("error")){
	    		//throw new Exception(tx_json.getString("error_message"));
	    		return tx_json;
	    	}else{
	    		tx_json = tx_json.getJSONObject("tx_json");	    			
	    	}
	    	
	    	Transaction payment;
	    	if(this.transaction){
	    		tx_json.put("Statements", Util.toHexString(tx_json.getJSONArray("Statements").toString()));
	    		payment = toTransaction(tx_json,TransactionType.SQLTransaction);
	    	}else{
	    		payment = toTransaction(tx_json,TransactionType.TableListSet);
	    	}
			
			signed = payment.sign(this.connection.secret);
			
			return Util.successObject();
		} catch (Exception e) {
//			e.printStackTrace();
			return Util.errorObject(e.getMessage());
		}
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
	 * Create table with operation-rule
	 * @param name Table name
	 * @param raw Option or conditions to create a table.
	 * @param operationRule Conditions to operate this table,this is a json-string like:
	 * "{
			'Insert':{
				'Condition':{'account':'$account','txid':'$tx_hash'},
				'Count':{'AccountField':'account','CountLimit':5}
			},
			'Update':{
				'Condition':{'$or':[{'age':{'$le':28}},{'id':2}]},
				'Fields':['age']
			},
			'Delete':{
				'Condition':{'age':'$lt18'}
			},
			'Get':{
				'Condition':{'id':{'$ge':3}}
			}
		}"
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql createTable(String name, List<String> raw,JSONObject operationRule) {
		return createTable(name, raw ,operationRule, false);
	}
	
	/**
	 * A create table operation.
	 * @param name Table name.
	 * @param rawList Option or conditions to create a table.
	 * @param confidential Table will be confidential or not.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql createTable(String name, List<String> rawList ,boolean confidential) {
		return createTable(name,rawList,null,confidential);
	}
	
	private Chainsql createTable(String name, List<String> rawList, JSONObject operationRule,boolean confidential) {
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
		String token = "";
		if(confidential){
			byte[] password = Util.getRandomBytes(PASSWORD_LENGTH);
			token = generateUserToken(this.connection.secret,password);
			if(token.length() == 0){
				System.out.println("generateUserToken failed");
				return null;
			}
			json.put("Token", token);
			byte[] rawBytes = EncryptCommon.symEncrypt(strRaw.getBytes(),password );
			strRaw = Util.bytesToHex(rawBytes);
		}else{
			strRaw = Util.toHexString(strRaw);
		}
		
		json.put(Field.Raw.toString(), strRaw);
		if(operationRule != null){
			json.put(Field.OperationRule.toString(), Util.toHexString(operationRule.toString()));
		}
		if(this.transaction){
			//有加密则不验证
			if(confidential){
				this.mapToken.put(new GenericPair<String,String>(this.connection.address,name),token);
				this.needVerify = 0;
			}
			this.cache.add(json);
			return null;
		}
		this.mTxJson = json;
		return this;
	}
	
	private String generateUserToken(String seed,byte[] password){
		IKeyPair keyPair = Seed.getKeyPair(seed);
		byte[] tokenBytes = null;
		if(Config.isUseGM())
			tokenBytes = EncryptCommon.asymEncrypt(password, null);
		else
			tokenBytes = EncryptCommon.asymEncrypt(password, keyPair.canonicalPubBytes());
		return tokenBytes == null ? "" :Util.bytesToHex(tokenBytes);
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
		this.mTxJson = json;
		return this;
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
		this.mTxJson = json;
		return this;
	}

	/**
	 * Rename a table.
	 * @param oldName Old table name.
	 * @param newName New table name.
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
		this.mTxJson = json;
		return this;
		
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
				byte[] seedBytes = null;
				if(!this.connection.secret.isEmpty()){
					seedBytes = getB58IdentiferCodecs().decodeFamilySeed(this.connection.secret);
				}
				byte[] password = EncryptCommon.asymDecrypt(Util.hexToBytes(token), seedBytes) ;
				if(password == null){
					return null;
				}
				byte [] pubBytes = getB58IdentiferCodecs().decode(userPublicKey, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
				byte[] newBytes = EncryptCommon.asymEncrypt(password, pubBytes);
				newToken = Util.bytesToHex(newBytes);
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
		flags.add(Util.StrToJson(flag));
		
		JSONObject json = new JSONObject();
		json.put("Tables", getTableArray(name));
		json.put("OpType", Constant.opType.get("t_grant"));
		json.put("User", user);
		json.put("Raw", Util.toHexString(flags.toString()));
		if(token.length() > 0){
			json.put("Token", token);
		}
		
		if(this.transaction){
			this.cache.add(json);
			return null;
		}
		this.mTxJson = json;
		return this;
	}
	/**
	 * Start a payment transaction, can be used to activate account 
	 * @param accountId The Address of an account.
	 * @param count		Count of coins to transfer,max value:1e11.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public JSONObject pay(String accountId,String count){
		JSONObject obj = new JSONObject();
		obj.put("Account", this.connection.address);
		obj.put("Destination", accountId);
		BigInteger bigCount = new BigInteger(count);
		BigInteger amount = bigCount.multiply(BigInteger.valueOf(1000000));
		obj.put("Amount", amount.toString());
		
		Transaction payment;
		try {
			payment = toTransaction(obj,TransactionType.Payment);
			signed = payment.sign(this.connection.secret);
			return doSubmitNoPrepare();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Begin a sql-transaction type operation.
	 * Sql-transaction is like the transaction in db. Transactions in it will all success or all rollback. 
	 */
	public void beginTran(){
		this.transaction = true;
	}
	/**
	 * End a sql-transaction type operation.
	 */
	public void endTran(){
		this.transaction = false;
		this.mapToken.clear();
	}
	/**
	 * Commit a sql-transaction type operation.
	 * @return Commit result.
	 */
	public JSONObject commit(){
		return doCommit("");
	}
	
	public Chainsql report(){
		this.mTxJson = new JSONObject();
		this.mTxJson.put("OpType", Constant.opType.get("t_report"));
//		this.txJson.put("Tables", new JSONArray());
		this.mTxJson.put("Tables", getTableArray("t_report_tablename_xxx_xxx"));
		return this;
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
			if(ledger_range.equals("empty")){
				return obj;
			}
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
	 * @return Ledger data.
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
		
		mRetJson = null;
		this.connection.client.getLedger(option,new Callback<JSONObject>(){
			@Override
			public void called(JSONObject data) {
				if(data == null){
					mRetJson = new JSONObject();
				}else{
					mRetJson = (JSONObject) data;
				}
			}
		});
		while(mRetJson == null){
			Util.waiting();
		}
		
		if(mRetJson.has("ledger")){
			return mRetJson;
		}else{
			return null;
		}
		
	}
	/**
	 * An asynchronous api to get the ledger identified by ledger_index.
	 * @param cb Callback.
	 */
	public void getLedger(Callback<JSONObject> cb){
		JSONObject option = new JSONObject();
		option.put("ledger_index",  "validated");
		this.connection.client.getLedger(option,cb);
	}
	/**
	 * Get the ledger identified by ledger_index.
	 * @param ledger_index Ledger index.
	 * @param cb Callback.
	 */
	public void getLedger(Integer ledger_index,Callback<JSONObject> cb){
		JSONObject option = new JSONObject();
		option.put("ledger_index", ledger_index);
		this.connection.client.getLedger(option,cb);
		
	}
	
	/**
	 * Get newest validated ledger index
	 * @return LedgerVersion data
	 */
	public JSONObject getLedgerVersion(){
		
		mRetJson = null;
		this.connection.client.getLedgerVersion(new Callback<JSONObject>(){
			@Override
			public void called(JSONObject data) {
				if(data == null){
					mRetJson = new JSONObject();
				}else{
					mRetJson = (JSONObject) data;
				}
			}
		});
		while(mRetJson == null){
			Util.waiting();
		}
		
		if(mRetJson.has("ledger_current_index")){
			return mRetJson;
		}else{
			return null;
		}
		
	}
	/**
	 * Get newest validated ledger index,asynchronous.
	 * @param cb Callback.
	 */
	public void getLedgerVersion(Callback<JSONObject> cb){
		this.connection.client.getLedgerVersion(cb);	
	}

	/**
	 * Get trasactions submitted by notified account,asynchronous.
	 * @param address Account address.
	 * @param limit Max transaction count to get.
	 * @return Result.
	 */
	public JSONObject getTransactions(String address,int limit){
		mRetJson = null;
		this.connection.client.getTransactions(address,limit,new Callback<JSONObject>(){
			@Override
			public void called(JSONObject data) {
				if(data == null){
					mRetJson = new JSONObject();
				}else{
					mRetJson = (JSONObject) data;
				}
			}
		});
		while(mRetJson == null){
			Util.waiting();
		}
		
		if(mRetJson.has("transactions")){
			return mRetJson;
		}else{
			return null;
		}
	}
	/**
	 * Get transactions from chain
	 * @param hash Start tx hash(can be tx_hash,ledger_seq,or "",if "",get from start tx).
	 * @param limit Count to get.
	 * @param include If the return should include the tx corresponding to hash we identified.
	 * @return A JSONObject including transactions.
	 */
	public JSONObject getCrossChainTxs(String hash,int limit,boolean include){
		if(hash == null){
			return Util.errorObject("hash cannot be null");
		}
		mRetJson = null;
		this.connection.client.getCrossChainTxs(hash, limit,include,new Callback<JSONObject>(){
			@Override
			public void called(JSONObject data) {
				if(data == null){
					mRetJson = new JSONObject();
				}else{
					mRetJson = (JSONObject) data;
				}
			}
		});
		while(mRetJson == null){
			Util.waiting();
		}
		
		if(mRetJson.has("transactions")){
			return mRetJson;
		}else{
			return null;
		}
	}
	/**
	 * Get trasactions submitted by notified account.
	 * @param address Account address.
	 * @return Result.
	 */
	public JSONObject getTransactions(String address){
		return getTransactions(address,DEFAULT_TX_LIMIT);
	}
	/**
	 * Get trasactions submitted by notified account,asynchronous.
	 * @param address Account address.
	 * @param cb Callback.
	 */
	public void getTransactions(String address,Callback<JSONObject> cb){
		this.connection.client.getTransactions(address,DEFAULT_TX_LIMIT,cb);	
	}
	/**
	 * Get trasactions submitted by notified account,asynchronous.
	 * @param address Account address.
	 * @param limit Max transaction count to get.
	 * @param cb Callback.
	 */
	public void getTransactions(String address,int limit,Callback<JSONObject> cb){
		getTransactions(address,limit,cb);	
	}
	/**
	 * Get transaction identified by hash.
	 * @param hash Transaction hash.
	 * @return Transaction information.
	 */
	public JSONObject getTransaction(String hash){
		mRetJson = null;
		this.connection.client.getTransaction(hash,new Callback<JSONObject>(){
			@Override
			public void called(JSONObject data) {
				if(data == null){
					mRetJson = new JSONObject();
				}else{
					mRetJson = (JSONObject) data;
				}
			}			
		});
		while(mRetJson == null){
			Util.waiting();
		}
		
		if(mRetJson.has("ledger_index")){
			return mRetJson;
		}else{
			return null;
		}
	}
	/**
	 * Get transaction by hash asynrhonously.
	 * @param hash Transaction hash.
	 * @param cb Callback.
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
		return generateAddress(seed);
	}
	
	public JSONObject generateAddress(String secret){
		Security.addProvider(new BouncyCastleProvider());
		Seed seed = Seed.fromBase58(secret);
		
		return generateAddress(seed);
	}
	
	private JSONObject generateAddress(Seed seed){
		if(Config.isUseGM()){
			seed.setGM();
		}
		IKeyPair keyPair = seed.keyPair();
		byte[] pubBytes = keyPair.canonicalPubBytes();
		byte[] o;
		{
			SHA256Digest sha = new SHA256Digest();
			sha.update(pubBytes, 0, pubBytes.length);
		    byte[] result = new byte[sha.getDigestSize()];
		    sha.doFinal(result, 0);
		    
			RIPEMD160Digest d = new RIPEMD160Digest();
		    d.update (result, 0, result.length);
		    o = new byte[d.getDigestSize()];
		    d.doFinal (o, 0);
		}

		String secretKey = getB58IdentiferCodecs().encodeFamilySeed(seed.bytes());
		String publicKey = getB58IdentiferCodecs().encode(pubBytes, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
		String address = getB58IdentiferCodecs().encodeAddress(o);
		
		JSONObject obj = new JSONObject();
		if(!Config.isUseGM()){
			obj.put("secret", secretKey);
		}
		obj.put("account_id", address);
		obj.put("public_key", publicKey);
		return obj;
	}
	/**
	 * Create validation key
	 * @param count 
	 * @return JSONArray contains validation keys, each with a structue of {"seed":xxx,"publickey":xxx}
	 */
	public JSONArray validationCreate(int count){
		JSONArray ret = new JSONArray();
		for(int i=0; i<count; i++){
			JSONObject obj = validationCreate();
			ret.put(obj);
		}
		return ret;
	}
	/**
	 * Create validation keys.
	 * @return JSONObject with field "seed" and "publickey".
	 */
	public JSONObject validationCreate(){
		Security.addProvider(new BouncyCastleProvider());
		JSONObject ret = new JSONObject();
		Seed seed = Seed.randomSeed();
		
//		byte[] bytes = getB58IdentiferCodecs().decodeFamilySeed("snEqBjWd2NWZK3VgiosJbfwCiLPPZ");
//		Seed seed = new Seed(bytes);
		
		IKeyPair keyPair = seed.keyPair(-1);
		byte[] pubBytes = keyPair.canonicalPubBytes();
		
		String secretKey = getB58IdentiferCodecs().encodeFamilySeed(seed.bytes());
		String validation_publickey = getB58IdentiferCodecs().encodeNodePublic(pubBytes);
		
		ret.put("seed", secretKey);
		ret.put("publickey", validation_publickey);
		
		return ret;
	}
	/**
	 * Get validation publickey list
	 * @return validation publickey list
	 */
	public JSONObject getUnlList(){
		return connection.client.getUnlList();
	}
	
	public Connection getConnection() {
		return connection;
	}

	public String getAccountBalance(String address){
		try{
			AccountID account = AccountID.fromAddress(address);
			Request request = connection.client.accountInfo(account);
			if(request.response.result!=null){
				String balance = request.response.result.optJSONObject("account_data").getString("Balance");
				BigInteger bal = new BigInteger(balance);
				BigInteger xrp = bal.divide(BigInteger.valueOf(1000000));
				return xrp.toString();
			}else {
				return null;
			}
		}catch(Exception e){
			return null;
		}
	}
	/**
	 * sqlTransaction commit
	 * @param commitType Commit type.
	 * @return Commit result.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject doCommit(Object  commitType){
		List<JSONObject> cache = this.cache;	
		
		JSONArray statements = new JSONArray();
        for (int i = 0; i < cache.size(); i++) {
        	statements.put(cache.get(i));
        }
        
		JSONObject json = new JSONObject();
		//this line must add here
		json.put("TransactionType",TransactionType.SQLTransaction);
		json.put( "Account", this.connection.address);
		json.put("Statements", statements);
		json.put("NeedVerify",this.needVerify);
		this.mTxJson = json;
		
		try {
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
}
