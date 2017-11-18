package com.peersafe.chainsql.util;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.util.HashMap;

import org.json.JSONObject;

import com.peersafe.base.client.Client;
import com.peersafe.base.client.Client.OnTBMessage;
import com.peersafe.base.client.Client.OnTXMessage;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.chainsql.crypto.EncryptCommon;
import com.peersafe.chainsql.net.Connection;

public class EventManager {
	public Connection connection;
	public boolean onMessage;
	public boolean onSubRet;
	private HashMap<String,Callback> mapCache;
	private HashMap<String,byte[]> mapPass;
	public JSONObject result;

	/**
	 * Constructor
	 * @param connection connection object.
	 */
	public EventManager(Connection connection) {
		super();
		this.connection = connection;
		this.mapCache = new HashMap<String,Callback>();
		mapPass = new HashMap<String,byte[]>();
		this.onMessage = false;
		this.onSubRet = false;
	}
	
	/**
	 * Resubscribe automatically after reconnected.
	 */
	public void reSubscribe(){
		int ownerLen = this.connection.address.length();
		for(String key : mapCache.keySet()){
			String name = key.substring(0,key.length() - ownerLen);
			String owner = key.substring(key.length() - ownerLen);
			
	 		JSONObject messageTx = new JSONObject();
			messageTx.put("command", "subscribe");
			messageTx.put("owner", owner);
			messageTx.put("tablename", name);
			
			this.connection.client.subscriptions.addMessage(messageTx);
		}
	}
	private void onChainsqlSubRet() {
		this.connection.client.OnSubChainsqlRet(new Client.OnChainsqlSubRet() {
			@Override
			public void called(JSONObject args) {
				if(args.has("owner") && args.has("tablename")) {
					String key = args.getString("tablename") + args.getString("owner");
					makeCallback(key,args.getJSONObject("result"));
				}
				if(args.has("transaction")) {
					String key = args.getString("transaction");
					makeCallback(key,args.getJSONObject("result"));
				}
			}				
		});
	}
	/**
	 * Subscribe for a table.
	 * @param name Table name.
	 * @param owner Table owner address.
	 * @param cb Callback.
	 */
	public void subTable(String name, String owner ,Callback<?> cb) {
 		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscriptions.addMessage(messageTx);
		
		if (!this.onMessage) {
			//this.connection.client.OnTBMessage(this::onTBMessage);
			this.connection.client.OnTBMessage(new OnTBMessage(){
				@Override
				public void called(JSONObject args) {
					onTBMessage(args);
				}
			});
			this.onMessage = true;
		}
		if(!this.onSubRet) {
			onChainsqlSubRet();
			this.onSubRet = true;
		}
		this.mapCache.put(name + owner,cb);
	}

	/**
	 * Subscribe a transaction.
	 * @param id Transaction hash.
	 * @param cb Callback.
	 */
	public void subTx(String id,Callback<?> cb) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("transaction", id);
		this.connection.client.subscriptions.addMessage(messageTx);
		if (!this.onMessage) {
//			this.connection.client.OnTXMessage(this::onTXMessage);
			this.connection.client.OnTXMessage(new OnTXMessage(){
				@Override
				public void called(JSONObject args) {
					onTXMessage(args);
				}
			});
			this.onMessage = true;
		}
		if(!this.onSubRet) {
			onChainsqlSubRet();
			this.onSubRet = true;
		}
		this.mapCache.put(id, cb);
	}

	/**
	 * Un-subscribe a table.
	 * @param name Table name.
	 * @param owner Table owner address.
	 */
	public void unsubTable(String name, String owner,Callback<JSONObject> cb) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscriptions.addMessage(messageTx);
	
		String key = name + owner;
		this.mapCache.remove(key);
		this.mapPass.remove(key);
		
		JSONObject obj = new JSONObject();
		obj.put("status", "success");
		obj.put("result", "unsubscribe table success");
		obj.put("type", "response");
		
		cb.called(obj);
	}

	/**
	 * Un-subscribe a transaction.
	 * @param id Transaction hash.
	 */
	public void unsubTx(String id,Callback<JSONObject> cb) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("transaction", id);
		
		this.connection.client.subscriptions.addMessage(messageTx);
		
		this.mapCache.remove(id);
		
		JSONObject obj = new JSONObject();
		obj.put("status", "success");
		obj.put("result", "unsubscribe transaction success");
		obj.put("type", "response");
		
		cb.called(obj);
	}

	private void onChainsqlMessage(final JSONObject data,final String key,final String owner,final String name) {
		final JSONObject tx = data.getJSONObject("transaction");
		if(mapPass.containsKey(key)) {
   	 		Util.decryptData(mapPass.get(key), tx);
   	 		makeCallback(key,data);
   	 	}else {
   	 		connection.client.getUserToken(owner,connection.address,name,new Callback<JSONObject>(){
   				@Override
   				public void called(JSONObject res) {
   					if(res.get("status").equals("error")){
   						System.out.println(res.getString("error_message"));
						mapPass.put(key, null);
						Util.decryptData(mapPass.get(key), tx);
						makeCallback(key,data);
   					}else {
   						String token = res.getString("token");
   						if(token.length() != 0){
   							try {
   								byte[] seedBytes = null;
   								if(!connection.secret.isEmpty()){
   									seedBytes = getB58IdentiferCodecs().decodeFamilySeed(connection.secret);
   								}
   								byte[] password = EncryptCommon.asymDecrypt(Util.hexToBytes(token), seedBytes);
   								mapPass.put(key, password);
   								Util.decryptData(mapPass.get(key), tx);
   								makeCallback(key,data);
   							} catch (Exception e) {
   								e.printStackTrace();
   							}
   						}else {
   							mapPass.put(key, null);
							Util.decryptData(mapPass.get(key), tx);
							makeCallback(key,data);
   						}
   					}
   				}
   			});
   	 	}
	}
	/**
	 * Table transaction trigger.
	 * @param data Table message data.
	 */
	private void onTBMessage(JSONObject data){
		String owner = data.getString("owner");
		String name = data.getString("tablename");
   	 	String key = name + owner;
   	 	onChainsqlMessage(data,key,owner,name);
	}
	
	private void onTXMessage(JSONObject data){
		String key = ((JSONObject) data.get("transaction")).getString("hash");
		//解密
//		if(isChainsqlType(data)) {
//			JSONObject tx = data.getJSONObject("transaction");
//			String name = "";
//			String owner = "";
//			if(tx.has("Tables")){
//				JSONObject table = (JSONObject)tx.getJSONArray("Tables").get(0);
//				table = table.getJSONObject("Table");
//				name = Util.fromHexString(table.getString("TableName"));
//			}
//			if(tx.has("Owner")) {
//				owner = tx.getString("Owner");
//			}else {
//				owner = tx.getString("Account");
//			}
//			if(!name.isEmpty() && !owner.isEmpty()) {
//				onChainsqlMessage(data,key,owner,name);
//			}else {
//				makeCallback(key,data);	
//			}
//		}else {
//			makeCallback(key,data);	
//		}
		makeCallback(key,data);	
		if(isChainsqlType(data)) {
			if(!("validate_success".equals(data.getString("status")))){
				mapCache.remove(key);
			}
		}else {
			mapCache.remove(key);
		}
	}
	
	private boolean isChainsqlType(JSONObject data) {
		JSONObject tx = data.getJSONObject("transaction");
		String type = tx.getString("TransactionType");
		if(type.equals(TransactionType.TableListSet.toString()) || 
		   type.equals(TransactionType.SQLStatement.toString()) || 
		   type.equals(TransactionType.SQLTransaction.toString())) {
			return true;
		}
		return false;
	}
	
	private void makeCallback(String key,JSONObject data){
		if (mapCache.containsKey(key)) {
	    	 mapCache.get(key).called(data);
	     }
	}
}
