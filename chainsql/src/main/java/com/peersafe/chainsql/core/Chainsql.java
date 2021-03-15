package com.peersafe.chainsql.core;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.peersafe.base.core.coretypes.Currency;
import com.peersafe.base.core.formats.Format;
import com.peersafe.base.core.formats.TxFormat;
import com.peersafe.chainsql.pool.ChainsqlPool;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.client.Client;
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
import com.peersafe.base.crypto.ecdsa.K256KeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.encodings.B58IdentiferCodecs;
import com.peersafe.base.utils.Utils;
import com.peersafe.chainsql.crypto.Ecies;
import com.peersafe.chainsql.crypto.EncryptCommon;
import com.peersafe.chainsql.manager.EventManager;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.resources.Constant;
import com.peersafe.chainsql.util.GenericPair;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;

public class Chainsql extends Submit {

	private JSONObject mTxJson;
	
	private static final int PASSWORD_LENGTH = 32;  
	private static final int DEFAULT_TX_LIMIT = 20;
	
	// Logger
    public static final Logger logger = Logger.getLogger(Chainsql.class.getName());


    
	private JSONObject mRetJson;
	//reconnect callback when disconnected
	private Callback<JSONObject> reconnectCb = null;
	private Callback<JSONObject> reconnectedCB = null;

	public static String MAIN_SCHEMA = "";
	
	/**
	 * Assigning the operating user.
	 * @param address Account address,start with a lower case 'z'.
	 * @param secret  Account secret,start with a lower case 'x'.
	 */
	public void as(String address, String secret) {


		JSONObject retAddress = generateAddress(secret);
		if(retAddress.has("address") && !address.equals( retAddress.getString("address") )){
			System.err.println("Exception: address and secret not match !");
		}

		this.connection.address = address;
		this.connection.secret  = secret;

		if(this.connection.scope == null){
			this.connection.scope   = address;
		}
	}


	public void useCert(String userCert) {
		this.connection.userCert = userCert;
	}

	/**
	 * 设置操作链的ID
	 * @param schemaID schemaID="" 代表操作的是主链;
	 */
	public void setSchema(String schemaID) {
		if(!this.connection.client.schemaID.equals(schemaID)) 
		{
			this.connection.client.unsubscribeStreams();
			this.connection.client.schemaID = schemaID;	
			this.connection.client.resubscribeStreams();
		}
	}

	/**
	 * Assigning table owner.
	 * @param address Address of table owner.
	 */
	public void use(String address) {
		this.connection.scope = address;
	}

	public final EventManager event = eventManager;

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
	 * @param url url,e.g.:"ws://127.0.0.1:5006".
	 * @param serverCertPath server certificate path
	 * @param storePass password
	 * @return Connection
	 */
	@SuppressWarnings("resource")
	public Connection connect(String url,String serverCertPath,String storePass) {
		connection = new Connection().connect(url,serverCertPath,storePass);
		doWhenConnect();
		return connection;
	}
	/**
	 * Connect to a websocket url.
	 * @param url Websocket url to connect,e.g.:"ws://127.0.0.1:5006".
	 * @param connectCb callback when connected
	 * @return Connection object after connected.
	 */
	public Connection connect(String url,final Callback<Client> connectCb) {
		return connect(url,connectCb,null);
	}
	/**
	 * Connect to a websocket url.
	 * @param url Websocket url to connect,e.g.:"ws://127.0.0.1:5006".
	 * @param connectCb callback when connected
	 * @param disconnectCb callback when disconnected
	 * @return Connection object after connected.
	 */
	@SuppressWarnings("resource")
	public Connection connect(String url,final Callback<Client> connectCb,final Callback<Client> disconnectCb) {
		connection = new Connection().connect(url);
		this.eventManager.init(this.connection);
		connection.client.onConnected(connectCb::called);
		if(disconnectCb != null) {
			connection.client.onDisconnected(disconnectCb::called);
		}
		
		return connection;
	}
	/**
	 * Connect to a secure websocket url.
	 * @param url url,e.g.:"ws://127.0.0.1:5006".
	 * @param serverCertPath server certificate path
	 * @param storePass password
	 * @param connectCb callback when connected
	 * @return Connection
	 */
	public Connection connect(String url,String serverCertPath,String storePass,final Callback<Client> connectCb) {
		return connect(url,serverCertPath,storePass,connectCb,null);
	}
	/**
	 * Connect to a secure websocket url.
	 * @param url url,e.g.:"ws://127.0.0.1:5006".
	 * @param serverCertPath server certificate path
	 * @param storePass password
	 * @param connectCb callback when connected
	 * @param disconnectCb callback when disconnected
	 * @return Connection
	 */
	@SuppressWarnings("resource")
	public Connection connect(String url,String serverCertPath,String storePass,final Callback<Client> connectCb,final Callback<Client> disconnectCb) {
		connection = new Connection().connect(url,serverCertPath,storePass);
		this.eventManager.init(this.connection);

		connection.client.onConnected(connectCb::called);
		if(disconnectCb != null) {
			connection.client.onDisconnected(disconnectCb::called);
		}

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
		this.eventManager.init(this.connection);
		//jdk1.8
//		this.connection.client.onReconnecting(this::onReconnecting);
//		this.connection.client.onReconnected(this::onReconnected);
		this.connection.client.onReconnecting(this::onReconnecting);
		this.connection.client.onReconnected(this::onReconnected);
	}


	public static void shutdown() {
		ChainsqlPool.instance().shutdown();
		Client.shutdown();
	}
	
	/**
	 * Transfer variable number of Strings to List of String
	 * @param val0 Parameter
	 * @param vals Parameter
	 * @return List of String
	 */
	public static List<String> array(String val0, String... vals){
	 	List<String> res = new ArrayList<>();
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
		this.eventManager.reSubscribe();
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
		tab.connection = this.connection;
		tab.setCrossChainArgs(this.crossChainArgs);
		tab.eventManager = this.eventManager;
		tab.extraDrop    = this.extraDrop;
		return tab;
	}
	
	/**
	 * use guomi algorithm
	 * @param useGM 是否使用国密
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
	 * @param secret Secret used to sign
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
	 * @param secret Secret used to sign
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
	public JSONObject signFor(JSONObject tx,String secret){
		if(!tx.has("secret")){
			return Util.errorObject("no secret supplied");
		}

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
	protected
	JSONObject prepareSigned() {
		try {

			if (this.connection.userCert != null) {
				String sCert = Util.toHexString(this.connection.userCert);
				mTxJson.put("Certificate", sCert);
			}

			if(schemaCreateTx){

				Transaction payment;
				payment = toTransaction(mTxJson,TransactionType.SchemaCreate);
				signed  = payment.sign(this.connection.secret);

				schemaCreateTx = false;
				return Util.successObject();
			}

			if(schemaModifyTx){

				Transaction payment;
				payment = toTransaction(mTxJson,TransactionType.SchemaModify);
				signed  = payment.sign(this.connection.secret);

				schemaModifyTx = false;
				return Util.successObject();
			}


			if(mTxJson.toString().equals("{}")) {
				return Util.errorObject("Exception occured");
			}
			mTxJson.put("Account",this.connection.address);

			if(mTxJson.has("OpType") && mTxJson.getInt("OpType") == Constant.opType.get("t_grant") &&
					!this.connection.address.equals(connection.scope)){
				mTxJson.put("Owner",  connection.scope);
			}

			//for cross chain
			if(crossChainArgs != null){
				mTxJson.put("TxnLgrSeq", crossChainArgs.txnLedgerSeq);
				mTxJson.put("OriginalAddress", crossChainArgs.originalAddress);
				mTxJson.put("CurTxHash", crossChainArgs.curTxHash);
				mTxJson.put("FutureTxHash", crossChainArgs.futureHash);
				crossChainArgs = null;
			}





			
	    	JSONObject tx_json = this.connection.client.tablePrepare(mTxJson);
	    	if(tx_json.has("error")){
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
			e.printStackTrace();
			return Util.errorObject(e.getMessage());
		}
	}

	/**
	 * @param extraDrop 额外的费用,单位为drop
	 * @throws Exception
	 */
	public void setExtraFee(int extraDrop) throws Exception {
		if ((extraDrop <= 1000000) && (extraDrop > 0)) {
			this.extraDrop = extraDrop;
		} else {
			throw new Exception("设置的额外费用超过1ZXC或低于0drop");
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


	/**
	 * SchemaName String
	 *
	 * withState  ture
	 *
	 * 锚定区块
	 *
	 * @return
	 */
	public Chainsql createSchema(JSONObject schemaInfo) throws Exception{

		boolean bValid = schemaInfo.has("SchemaName") && schemaInfo.has("WithState") &&
				 schemaInfo.has("Validators") && schemaInfo.has("PeerList");

		if(!bValid){
			throw new Exception("Invalid schemaInfo parameter");
		}

		JSONObject params = new JSONObject();
		params.put("Account", this.connection.address);
		params.put("SchemaName",Util.toHexString(schemaInfo.getString("SchemaName")));


		if( schemaInfo.has("SchemaAdmin")){
			params.put("SchemaAdmin",schemaInfo.getString("SchemaAdmin"));
		}

		if( schemaInfo.getBoolean("WithState")){

			//继承主链的节点状态
			if(! schemaInfo.has("AnchorLedgerHash")){
				throw new Exception("Missing field AnchorLedgerHash");
			}
			params.put("AnchorLedgerHash",schemaInfo.getString("AnchorLedgerHash"));
			params.put("SchemaStrategy",2);
		}else{
			// 不继承主链的节点状态
			params.put("SchemaStrategy",1);
			if(schemaInfo.has("AnchorLedgerHash")){
				throw new Exception("Field 'AnchorLedgerHash' is unnecessary");
			}
		}

		this.schemaCreateTx = true;

		JSONArray validatorsArr = schemaInfo.getJSONArray("Validators");
		JSONArray peerListArr   = schemaInfo.getJSONArray("PeerList");


		JSONArray jsonValidators = new JSONArray();
		for(int i=0; i<validatorsArr.length(); i++){
			String validator = (String)validatorsArr.get(i);

			//System.out.println(validator);
			JSONObject subItem = new JSONObject();
			subItem.put("PublicKey",validatorsArr.get(i));

			JSONObject item = new JSONObject();
			item.put("Validator",subItem);
			jsonValidators.put(item);
		}

		JSONArray jsonPeerList = new JSONArray();

		for(int i=0; i<peerListArr.length(); i++){
			String Endpoint = (String)peerListArr.get(i);
			JSONObject subItem = new JSONObject();
			subItem.put("Endpoint",Util.toHexString(Endpoint));

			JSONObject item = new JSONObject();
			item.put("Peer",subItem);
			jsonPeerList.put(item);
		}

		params.put("Validators",jsonValidators);
		params.put("PeerList",jsonPeerList);

		this.mTxJson = params;
		return this;
	}


	public Chainsql modifySchema(SchemaOpType type,JSONObject schemaInfo)   throws Exception{

		boolean bValid = schemaInfo.has("SchemaID")  && schemaInfo.has("Validators") && schemaInfo.has("PeerList");

		if(!bValid){
			throw new Exception("Invalid schemaInfo parameter");
		}

		this.schemaModifyTx = true;

		JSONObject params = new JSONObject();

		if(type == SchemaOpType.schema_del){
			params.put("OpType","2");
		}else{
			params.put("OpType","1");
		}

		JSONArray validatorsArr = schemaInfo.getJSONArray("Validators");
		JSONArray peerListArr   = schemaInfo.getJSONArray("PeerList");

		JSONArray jsonValidators = new JSONArray();
		for(int i=0; i<validatorsArr.length(); i++){
			String validator = (String)validatorsArr.get(i);

			//System.out.println(validator);
			JSONObject subItem = new JSONObject();
			subItem.put("PublicKey",validatorsArr.get(i));

			JSONObject item = new JSONObject();
			item.put("Validator",subItem);
			jsonValidators.put(item);
		}

		JSONArray jsonPeerList = new JSONArray();

		for(int i=0; i<peerListArr.length(); i++){
			String Endpoint = (String)peerListArr.get(i);
			JSONObject subItem = new JSONObject();
			subItem.put("Endpoint",Util.toHexString(Endpoint));

			JSONObject item = new JSONObject();
			item.put("Peer",subItem);
			jsonPeerList.put(item);
		}

		params.put("Account", this.connection.address);
		params.put("SchemaID",schemaInfo.getString("SchemaID"));

		params.put("Validators",jsonValidators);
		params.put("PeerList",jsonPeerList);

		this.mTxJson = params;
		return this;
	}


	
	private Chainsql createTable(String name, List<String> rawList, JSONObject operationRule,boolean confidential) {
		List<JSONObject> listRaw = Util.ListToJsonList(rawList);
		try {
			Validate.checkCreate(listRaw,name);
		} catch (Exception e) {
			this.mTxJson = new JSONObject();
			System.out.println("Exception:" + e.getLocalizedMessage());
			return this;
		}
		
		JSONObject json = new JSONObject();
		json.put("OpType", Constant.opType.get("t_create"));
		json.put("Tables", getTableArray(name));
		json.put("StrictMode", this.strictMode);
		
		String strRaw = listRaw.toString();
		String token = "";
		if(confidential){

			boolean bSM = ( Utils.getAlgType(this.connection.secret).equals("softGMAlg") );
			int randomSize = bSM? PASSWORD_LENGTH /2 :PASSWORD_LENGTH ;

			byte[] password = Util.getRandomBytes(randomSize);
			token = generateUserToken(this.connection.secret,password);
			if(token.length() == 0){
				System.out.println("generateUserToken failed");
				return null;
			}
			json.put("Token", token);

			byte[] rawBytes = EncryptCommon.symEncrypt(strRaw.getBytes(),password,bSM );

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
				this.mapToken.put(new GenericPair<>(this.connection.address, name),token);
				this.needVerify = 0;
			}else {
				this.mapToken.put(new GenericPair<>(this.connection.address, name),"");
			}
			this.cache.add(json);
			return null;
		}
		this.mTxJson = json;
		return this;
	}
	
	private String generateUserToken(String seed,byte[] password){
		IKeyPair keyPair = Seed.getKeyPair(seed);
		byte[] tokenBytes;
		if(Config.isUseGM())
			tokenBytes = EncryptCommon.asymEncrypt(password, null);
		else {
			assert keyPair != null;
			tokenBytes = EncryptCommon.asymEncrypt(password, keyPair.canonicalPubBytes());
		}
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
		if(newName == null || newName.isEmpty()) {
			System.out.println("new table name can not be empty");
			mTxJson = new JSONObject();
			return this;
		}
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
	 * Add fields for a table
	 * @param name Table name
	 * @param rawList Raw specifing the new fields. 
	 * @return
	 * @throws Exception
	 */
	public Chainsql addTableFields(String name,List<String> rawList) throws Exception{		
		return modifyTable(Constant.opType.get("t_add_fields"),name,rawList);
	}
	
	/**
	 * Add fields for a table
	 * @param name Table name
	 * @param rawList Raw specifing the new fields. 
	 * @return
	 * @throws Exception
	 */
	public Chainsql deleteTableFields(String name,List<String> rawList) throws Exception{		
		return modifyTable(Constant.opType.get("t_delete_fields"),name,rawList);
	}
	
	/**
	 * Add fields for a table
	 * @param name Table name
	 * @param rawList Raw specifing the fields to modify. 
	 * @return
	 * @throws Exception
	 */
	public Chainsql modifyTableFields(String name,List<String> rawList) throws Exception{
		return modifyTable(Constant.opType.get("t_modify_fields"),name,rawList);
	}
	
	/**
	 * Add fields for a table
	 * @param name Table name
	 * @param rawList Raw specifing the index to create. 
	 * @return
	 * @throws Exception
	 */
	public Chainsql createIndex(String name,List<String> rawList) throws Exception{		
		return modifyTable(Constant.opType.get("t_create_index"),name,rawList);
	}
	
	/**
	 * Add fields for a table
	 * @param name Table name
	 * @param rawList Indexes name to delete. 
	 * @return
	 * @throws Exception
	 */
	public Chainsql deleteIndex(String name,List<String> rawList) throws Exception{		
		return modifyTable(Constant.opType.get("t_delete_index"),name,rawList);
	}
	
	private Chainsql modifyTable(int opType,String name,List<String> rawList)throws Exception{
		List<JSONObject> listRaw = Util.ListToJsonList(rawList);
		String strRaw = listRaw.toString();
		
		String token = Util.getUserToken(this.connection, this.connection.address, name);
		strRaw = Util.encryptRaw(connection,token,strRaw);

		JSONObject json = new JSONObject();
		json.put("OpType", opType);
		json.put("Tables", getTableArray(name));
		json.put("Raw", strRaw);
		
		this.mTxJson = json;
		
		return this;
	}
	/**
	 * check if publickey matches user
	 * @param user user
	 * @param userPublicKey userPublicKey
	 * @return true
	 */
	private boolean checkUserMatchPublic(String user,String userPublicKey) {
		if(user.isEmpty() || userPublicKey.isEmpty())
			return false;
		byte[] pubBytes = getB58IdentiferCodecs().decode(userPublicKey, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
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
		String address = getB58IdentiferCodecs().encodeAddress(o);
		return user.equals(address);
	}

	/**
	 * Grant a user with authorities to operate a table.
	 * @param name Table name
	 * @param user User address,start with a lower case 'z'.
	 * @param userPublicKey User's public key,start with a lower case 'c'.
	 * 						Will be used if the table is confidential.
	 * @param flag Options to notify the authorities.eg:"{insert:true,delete:false}" means 
	 * 			   the user can insert to this table,but cannot delete from this table.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql grant(String name, String user,String userPublicKey,String flag){
		String token = "";
		if(!checkUserMatchPublic(user,userPublicKey)) {
			logger.log(Level.SEVERE, "PublicKey does not match User");
			return null;
		}
		GenericPair<String,String> pair = new GenericPair<>(this.connection.address, name);
		if(mapToken.containsKey(pair)){
			token = mapToken.get(pair);
		}else {
			JSONObject res = this.connection.client.getUserToken(this.connection.address,connection.address,name);
			if(res.has("error")){
				System.err.println(res.getString("error_message"));
				return this;
			}
			if(res.has("token"))
				token = res.getString("token");
		}

		String newToken = "";
		if(token.length() != 0){
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
	 * @param user User address,start with a lower case 'z'.
	 * @param flag Options to notify the authorities.eg:"{insert:true,delete:false}" means 
	 * 			   the user can insert to this table,but cannot delete from this table.
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public Chainsql grant(String name, String user,String flag) {
		return grant_inner(name,user,flag,"");
	}

	private Chainsql grant_inner(String name, String user,String flag,String token) {
		List<JSONObject> flags = new ArrayList<>();
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
	 * Start a Ripple transaction
	 * @param tx_json  transaction tx_json
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple createRippleTransaction(JSONObject tx_json)
	{
		Ripple ripple = new Ripple(this);
		ripple.setTxJson(tx_json);
		return ripple; 
	}
	
	/**
	 * Start a payment transaction, can be used to activate account 
	 * @param accountId The Address of an account.
	 * @param value		Count of coins to transfer,Unit:ZXC,max value:1e11.
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple pay(String accountId, String value)
	{
		Ripple ripple = new Ripple(this);
		return ripple.pay(accountId, value);
	}
	
	/**
	 * 
	 * @param accountId The Address of an account.
	 * @param value Count of coins to transfer,max value:1e11.
	 * @param sCurrency  Arbitrary code for currency.
	 * @param sIssuer currency Issuer address
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple pay(String accountId, String value, String sCurrency, String sIssuer)
	{
		Ripple ripple = new Ripple(this);
		return ripple.pay(accountId, value, sCurrency, sIssuer);
	}

	/**
	 * 
	 * @param contract_address The Address of a contract account.
	 * @param value Count of coins to transfer.
	 * @param gasLimit The maximum amount of gas available
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple payToContract(String contract_address, String value, int gasLimit)
	{
		Ripple ripple = new Ripple(this);
		return ripple.payToContract(contract_address, value, gasLimit);
	}

	/**
	 * 
	 * @param sDestAddr Address to receive escrowed amount
	 * @param value Amounts to escrow,Unit:ZXC.
	 * @param dateFormatTMFinish The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when the escrowed ZXC can be released to the recipient,use "" if not set.
	 * @param dateFormatTMCancel The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when this escrow expires,use "" if not set.
	 * @return You can use this to call other Chainsql functions continually.
	 * @throws Exception Exceptions.
	 */
	public Ripple escrowCreate(String sDestAddr, String value, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		Ripple ripple = new Ripple(this);
		return ripple.escrowCreate(sDestAddr, value, dateFormatTMFinish, dateFormatTMCancel);
	}

	/**
	 * 
	 * @param sDestAddr Address to receive escrowed amount
	 * @param value Amounts to escrow
	 * @param sCurrency  Arbitrary code for currency.
	 * @param sIssuer currency Issuer
	 * @param dateFormatTMFinish The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when the escrowed coin can be released to the recipient,use "" if not set.
	 * @param dateFormatTMCancel The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when this escrow expires,use "" if not set.
	 * @return You can use this to call other Chainsql functions continually.
	 * @throws Exception Exceptions.
	 */
	public Ripple escrowCreate(String sDestAddr, String value, String sCurrency, String sIssuer, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		Ripple ripple = new Ripple(this);
		return ripple.escrowCreate(sDestAddr, value, sCurrency, sIssuer, dateFormatTMFinish, dateFormatTMCancel);
	}

	/**
	 * 
	 * @param sOwner Address of the source account that funded the held payment.
	 * @param nCreateEscrowSeq Transaction sequence of EscrowCreate transaction that created the held payment to finish.
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple escrowExecute(String sOwner, int nCreateEscrowSeq)
	{
		Ripple ripple = new Ripple(this);
		return ripple.escrowExecute(sOwner, nCreateEscrowSeq);
	}

	/**
	 * 
	 * @param sOwner Address of the source account that funded the held payment.
	 * @param nCreateEscrowSeq Transaction sequence of EscrowCreate transaction that created the held payment to finish.
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple escrowCancel(String sOwner, int nCreateEscrowSeq)
	{
		Ripple ripple = new Ripple(this);
		return ripple.escrowCancel(sOwner, nCreateEscrowSeq);
	}
	
	/**
	 * accountSet
	 * @param nFlag accountSet flag which can be enable or disabled for an account
	 * @param bSet true:SetFlag; false:ClearFlag
	 * @return  You can use this to call other Ripple functions continually.
	 */
	public Ripple accountSet(int nFlag, boolean bSet)
	{
		Ripple ripple = new Ripple(this);
		return ripple.accountSet(nFlag, bSet);
	}

	/**
	 * 
	 * @param transferRate    1.0 - 2.0 string
	 * @param transferFeeMin  decimal number string
	 * @param transferFeeMax  decimal number string
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple accountSet(String transferRate, String transferFeeMin, String transferFeeMax) throws Exception
	{
		Ripple ripple = new Ripple(this);
		return ripple.accountSet(transferRate, transferFeeMin, transferFeeMax);
	}

	/**
	 * 
	 * @param value Amounts to escrow
	 * @param sCurrency  Arbitrary code for currency.
	 * @param sIssuer currency Issuer
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple trustSet(String value, String sCurrency, String sIssuer)
	{
		Ripple ripple = new Ripple(this);
		return ripple.trustSet(value, sCurrency, sIssuer);
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
	private void endTran(){
		this.transaction = false;
		this.mapToken.clear();
		this.cache.clear();
	}
	/**
	 * Commit a sql-transaction type operation.
	 * @return Commit result.
	 */
	public JSONObject commit(){
		JSONObject obj = doCommit("");
		endTran();
		return obj;
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
		
		return this.connection.client.getLedger(option);		
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
		return this.connection.client.getLedgerVersion();		
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
	public JSONObject getAccountTransactions(String address,int limit){
		return this.connection.client.getTransactions(address,limit);
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
		this.connection.client.getCrossChainTxs(hash, limit,include, data -> {
			if(data == null){
				mRetJson = new JSONObject();
			}else{
				mRetJson = (JSONObject) data;
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
	public JSONObject getAccountTransactions(String address){
		return getAccountTransactions(address,DEFAULT_TX_LIMIT);
	}
	/**
	 * Get trasactions submitted by notified account,asynchronous.
	 * @param address Account address.
	 * @param cb Callback.
	 */
	public void getAccountTransactions(String address,Callback<JSONObject> cb){
		this.connection.client.getTransactions(address,DEFAULT_TX_LIMIT,cb);	
	}
	/**
	 * Get trasactions submitted by notified account,asynchronous.
	 * @param address Account address.
	 * @param limit Max transaction count to get.
	 * @param cb Callback.
	 */
	public void getAccountTransactions(String address,int limit,Callback<JSONObject> cb){
		this.connection.client.getTransactions(address,limit,cb);
	}
	/**
	 * Get transaction identified by hash.
	 * @param hash Transaction hash.
	 * @return Transaction information.
	 */
	public JSONObject getTransaction(String hash){
		return this.connection.client.getTransaction(hash);
	}




	/**
	 * Get transaction identified by hash.
	 * @param txInfo txInfo  {"hash": "B168F7FC87EC5D435F85885B21DEA3C55B98C9390CA9FDB75F14571E451BD1B3", "meta":false,"meta_chain":true}
	 * @return Transaction information.
	 */
	public JSONObject getTransaction(JSONObject txInfo) throws Exception{


		if(txInfo == null || !txInfo.has("hash")){
			throw new Exception("txInfo has no field of hash");
		}

		return this.connection.client.getTransaction(txInfo);
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
	 * 		   address:Account address.
	 * 		   publicKey:Account publickey. 
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

	/**
	 *  generate address
	 * @param options {algorithm:"softGMAlg",secret:"pw5MLePoMLs1DA8y7CgRZWw6NfHik7ZARg8Wp2pr44vVKrpSeUV"}
	 * @return
	 */
	public JSONObject generateAddress(JSONObject options){
		Security.addProvider(new BouncyCastleProvider());

		byte[] version = Seed.VER_K256;
		if(options.has("algorithm") ){

			String sVersion = options.getString("algorithm");

			switch (sVersion) {
				case "ed25519":
					version = Seed.VER_ED25519;
					break;
				case "secp256k1":
					version = Seed.VER_K256;
					break;
				case "softGMAlg":
					version = Seed.VER_SOFT_SM;
					break;
				default:
					version = Seed.VER_ED25519;
			}
		}

		Seed seed;
		if(options.has("secret")){

			String sSecret = options.getString("secret");
			seed = Seed.fromBase58(sSecret);

		}else{
			seed = Seed.randomSeed(version);
		}

		return generateAddress(seed);
	}
	
	private JSONObject generateAddress(Seed seed){
		if(Config.isUseGM()){
			seed.setGM();
		}
		IKeyPair keyPair = seed.keyPair();
		if(keyPair.type().equals("softGMAlg")){

			JSONObject  softGMAddress = new JSONObject();

			String privHex = keyPair.privHex();

			String secretKey   = getB58IdentiferCodecs().encodeAccountPrivate(ByteUtils.fromHexString(privHex));
			String publicKey   = getB58IdentiferCodecs().encodeAccountPublic(keyPair.canonicalPubBytes());

			String address = Utils.deriveAddressFromBytes(keyPair.canonicalPubBytes());
			softGMAddress.put("secret", secretKey);
			softGMAddress.put("publicKey", publicKey);
			softGMAddress.put("address", address);

			return softGMAddress;
		}

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
		String address   = getB58IdentiferCodecs().encodeAddress(o);
		
		JSONObject obj = new JSONObject();
		if(!Config.isUseGM()){
			obj.put("secret", secretKey);
		}
		obj.put("address", address);
		obj.put("publicKey", publicKey);
		return obj;
	}
	/**
	 * Create validation key
	 * @param count Count to create.
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

		IKeyPair keyPair = seed.keyPair(-1);
		byte[] pubBytes = keyPair.canonicalPubBytes();
		
		String secretKey = getB58IdentiferCodecs().encodeFamilySeed(seed.bytes());
		String validation_publickey = getB58IdentiferCodecs().encodeNodePublic(pubBytes);
		
		ret.put("seed", secretKey);
		ret.put("publickey", validation_publickey);
		
		return ret;
	}


	/**
	 *
	 * @param options JSONObject with field "seed" and "algorithm".
	 * @return JSONObject with field "seed" and "publickey".
	 */
	public JSONObject validationCreate(JSONObject options){
		Security.addProvider(new BouncyCastleProvider());
		boolean bSoftGMAlg = ( options.has("algorithm") && options.get("algorithm") == "softGMAlg" );

		boolean hasSecret  = options.has("secret") ;

		if(!bSoftGMAlg){
			return validationCreate();
		}

		byte[] version = Seed.VER_SOFT_SM;
		Seed seed;

		if(hasSecret){
			String sSecret = options.getString("secret");
			byte[] secretBytes =   getB58IdentiferCodecs().decodeNodePrivate(sSecret);
			seed = new Seed(version,secretBytes);
		}else{
			seed = Seed.randomSeed(version);

		}

		IKeyPair keyPair = seed.keyPair();

		String sPrivHex  = keyPair.privHex();
		String secretKey = getB58IdentiferCodecs().encodeNodePrivate(ByteUtils.fromHexString(sPrivHex));

		assert secretKey.charAt(0) == 'p';

		String validationPub = getB58IdentiferCodecs().encodeNodePublic(keyPair.canonicalPubBytes());

		JSONObject ret = new JSONObject();
		ret.put("seed", secretKey);
		ret.put("publickey", validationPub);
		ret.put("validation_public_key_hex" , Util.bytesToHex(keyPair.canonicalPubBytes()));
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
	
	public JSONObject getAccountInfo(String address) {
		AccountID account = AccountID.fromAddress(address);
		return connection.client.accountInfo(account);
	}
	
	public String getAccountBalance(String address){
		try{
			AccountID account = AccountID.fromAddress(address);
			JSONObject result = connection.client.accountInfo(account);
			if(!result.has("error")){
				String balance = result.optJSONObject("account_data").getString("Balance");
				BigInteger bal = new BigInteger(balance);
				BigInteger zxc = bal.divide(BigInteger.valueOf(1000000));
				BigInteger mod = bal.mod(BigInteger.valueOf(1000000));

				// sMod's length must be 6  ( 1ZXC =  10^6 drops )
				String sMod = mod.toString();
				int addNum =  6 - sMod.length() ;
				while( addNum > 0 ){

					sMod = "0" + sMod;
					addNum--;
				}

				String finalZxc = zxc.toString();
				finalZxc += ".";
				finalZxc += sMod;
				return finalZxc;
			}else {
				System.err.println(result.get("error_message"));
				return null;
			}
		}catch(Exception e){
			e.printStackTrace();
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
		for (JSONObject jsonObject : cache) {
			statements.put(jsonObject);
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
		JSONObject obj = doCommit(cond);
		endTran();
		return obj;
	}
	/**
	 * Commit sql-transactoin asynchronously.
	 * @param cb Callback object.
	 * @return Commit result.
	 */
	public JSONObject commit(Callback<?> cb){
		JSONObject obj = doCommit(cb);
		endTran();
		return obj;
	}
	
	/**
	 * 加密接口
	 * @param plainText 明文
	 * @param listPublicKey 公钥数组
	 * @return 密文
	 */
	public String encrypt(String plainText,List<String> listPublicKey) {
		if(listPublicKey.size() == 0) {
			logger.log(Level.SEVERE, "PublicKey list is empty");
			return "";
		}
		byte[] cipher = Ecies.encryptText(plainText,listPublicKey);
		if(cipher == null)
			return "";
		return Util.bytesToHex(cipher);
	}
	
	/**
	 * 解密接口
	 * @param cipher 密文
	 * @param secret 私钥
	 * @return 明文，解密失败返回""
	 */
	public String decrypt(String cipher,String secret) {
		byte[] cipherBytes = Util.hexToBytes(cipher);
		return Ecies.decryptText(cipherBytes, secret);
	}
	
	/**
	 * 获取账户建的表
	 * @param address 账户地址
	 * @param bGetDetail 是否获取详细信息（建表的raw字段）
	 * @return 用户建的表（数组）
	 */
	public JSONObject getAccountTables(String address,boolean bGetDetail) {
		return connection.client.getAccountTables(address,bGetDetail);
	}
	
	/**
	 * 获取账户建的表
	 * @param address 账户地址
	 * @param bGetDetail 是否获取详细信息（建表的raw字段）
	 * @param cb 回调函数
	 */
	public void getAccountTables(String address,boolean bGetDetail,Callback<JSONObject> cb) {
		connection.client.getAccountTables(address, bGetDetail, cb);
	}
	
	/**
	 * 获取表授权列表
	 * @param owner 表的拥有者地址
	 * @param tableName 表名
	 * @return 授权列表
	 */
	public JSONObject getTableAuth(String owner,String tableName) {
		return connection.client.getTableAuth(owner, tableName,null);
	}
	
	/**
	 * 获取表授权列表
	 * @param owner 表的拥有者地址
	 * @param tableName 表名
	 * @param accounts 指定账户地址列表，只查这个列表中相关地址的授权
	 * @return 授权列表
	 */
	public JSONObject getTableAuth(String owner,String tableName,List<String> accounts) {
		return connection.client.getTableAuth(owner, tableName,accounts);
	}
	
	/**
	 * 获取表授权列表
	 * @param owner 表的拥有者地址
	 * @param tableName 表名
	 * @param cb 回调函数
	 */
	public void getTableAuth(String owner,String tableName,Callback<JSONObject> cb) {
		connection.client.getTableAuth(owner, tableName, null,cb);
	}
	
	/**
	 * 获取表授权列表
	 * @param owner 表的拥有者地址
	 * @param tableName 表名
	 * @param accounts 指定账户地址列表，只查这个列表中相关地址的授权
	 * @param cb 回调函数
	 */
	public void getTableAuth(String owner,String tableName,List<String> accounts,Callback<JSONObject> cb) {
		connection.client.getTableAuth(owner, tableName, accounts,cb);
	}
	
	/**
	 * 签名接口
	 * @param message 要签名的内容
	 * @param secret 签名私钥
	 * @return 签名
	 */
	public byte[] sign(byte[] message,String secret) {
		return Util.sign(message, secret);
	}
	/**
	 * 验证签名接口
	 * @param message 被签名的内容
	 * @param signature 签名
	 * @param publicKey 签名公钥
	 * @return 是否验签成功
	 */
	public boolean verify(byte[] message,byte[] signature,String publicKey) {
		return Util.verify(message, signature, publicKey);
	}
	
	/**
	 * 根据sql语句查询，admin接口，同步调用
	 * @param sql 要查询的sql语句
	 * @return 返回格式：
	 * {
	 * 	 "lines":[...]
	 * 	 "status":"..."
	 * }
	 */
	public JSONObject getBySqlAdmin(String sql) {
		return connection.client.getBySqlAdmin(sql);
	}
	
	/**
	 * 根据sql语句查询，admin接口，异步调用
	 * @param sql 要查询的sql语句
	 * @param cb 查询结果回调
	 */
	public void getBySqlAdmin(String sql,Callback<JSONObject> cb) {
		connection.client.getBySqlAdmin(sql, cb);
	}
	
	/**
	 * 根据sql语句查询，普通用户接口，需签名验证
	 * @param sql 要查询的sql语句
	 * @return 返回格式：
	 * {
	 * 	 "lines":[...]
	 * 	 "status":"..."
	 * }
	 */
	public JSONObject getBySqlUser(String sql) {
		return connection.client.getBySqlUser(connection.secret, connection.address, sql);
	}
	
	/**
	 * 根据sql语句查询，普通用户接口，异步调用
	 * @param sql 要查询的sql语句
	 * @param cb 查询结果回调
	 */
	public void getBySqlUser(String sql,Callback<JSONObject> cb) {
		connection.client.getBySqlUser(connection.secret, connection.address, sql,cb);
	}
	
	/**
	 * 获取表的NameInDB字符串
	 * @param owner 表的拥有者地址
	 * @param tableName 表名
	 * @return 
	 * success:
	 * {
	 *   "status":"success"
	 *   "nameInDB":"xxx"
	 * }
	 * failed:
	 * {
	 *   "status":"error"
	 *   "error_message":"xxx"
	 * }
	 */
	public JSONObject getTableNameInDB(String owner,String tableName) {
		return connection.client.getNameInDB(owner, tableName);
	}
	
	public void getTableNameInDB(String owner,String tableName,Callback<JSONObject> cb) {
		connection.client.getNameInDB(owner, tableName, cb);
	}
	/**
	 * 获取区块中的交易，在bIncludeSuccess与bIncludefailure都为false的情况下，只返回成功与失败的交易数量
	 * @param ledgerSeq 要查询的区块号
	 * @param bIncludeSuccess 查询结果中是否包含成功交易
	 * @param bIncludefailure 查询结果中是否包含失败交易
	 */
	public JSONObject getLedgerTxs(Integer ledgerSeq,boolean bIncludeSuccess,boolean bIncludefailure)
	{
		return connection.client.getLedgerTxs(ledgerSeq,bIncludeSuccess,bIncludefailure);
	}
	/**
	 * 获取区块中的交易，在bIncludeSuccess与bIncludefailure都为false的情况下，只返回成功与失败的交易数量
	 * @param ledgerSeq 要查询的区块号
	 * @param bIncludeSuccess 查询结果中是否包含成功交易
	 * @param bIncludefailure 查询结果中是否包含失败交易
	 * @param cb 查询结果回调
	 */
	public void getLedgerTxs(Integer ledgerSeq,boolean bIncludeSuccess,boolean bIncludefailure,Callback<JSONObject> cb)
	{
		connection.client.getLedgerTxs(ledgerSeq,bIncludeSuccess,bIncludefailure,cb);
	}


	/**
	 * Get schema list.
	 * @return schema information.
	 */
	public JSONObject getSchemaList(JSONObject params){
		return connection.client.getSchemaList(params);
	}

	/**
	 *  查询子链的信息
	 * @param schemaID schemaID
	 */
	public JSONObject getSchemaInfo(String schemaID){
		return connection.client.getSchemaInfo(schemaID);
	}
}
