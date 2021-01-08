package com.peersafe.chainsql.manager;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.peersafe.chainsql.resources.Constant;
import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.abi.EventEncoder;
import com.peersafe.abi.datatypes.Event;
import com.peersafe.base.client.Client;
import com.peersafe.base.client.Client.OnContractEvent;
import com.peersafe.base.client.Client.OnTBMessage;
import com.peersafe.base.client.Client.OnTXMessage;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.chainsql.crypto.EncryptCommon;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.Util;

public class EventManager {
	public Connection connection;
	public boolean onTbMessage;
	public boolean onTxMessage;
	public boolean onContractMessage;
	//订阅响应
	public boolean onSubRet;
	private HashMap<String,Callback> mapCache;
	private HashMap<String,byte[]> mapPass;
	private HashMap<String,Callback> mapTableCache;
	private HashMap<String,Map<Event,Callback>> mapContractEvents;
	public JSONObject result;
	
//	private static EventManager single = new EventManager();
//	
//	private EventManager() {
//	}
//	
//	public static EventManager instance() {
//		return single;
//	}
	/**
	 * Constructor
	 * @param connection connection object.
	 */
	public void init(Connection connection) {
		this.connection = connection;
		this.mapCache = new HashMap<String,Callback>();
		this.mapTableCache = new HashMap<String,Callback>();
		mapPass = new HashMap<String,byte[]>();
		mapContractEvents = new HashMap<String,Map<Event,Callback>>();
		this.onTbMessage = false;
		this.onTxMessage = false;
		this.onSubRet = false;
		this.onContractMessage = false;
	}
	
	/**
	 * Resubscribe automatically after reconnected.
	 */
	public void reSubscribe(){
		for(String key : mapTableCache.keySet()){
			String[] keys = key.split(";");
			if(keys.length != 2){
				continue;
			}
			String name = keys[0];
			String owner = keys[1];
			
	 		JSONObject messageTx = new JSONObject();
			messageTx.put("command", "subscribe");
			messageTx.put("owner", owner);
			messageTx.put("tablename", name);
			
			this.connection.client.subscribe(messageTx);
		}

		for(String key : mapCache.keySet()){

			JSONObject messageTx = new JSONObject();
			messageTx.put("command", "subscribe");
			messageTx.put("transaction", key);
			this.connection.client.subscribe(messageTx);
		}
		
		
		for(String address : mapContractEvents.keySet()){

			JSONObject contractEv = new JSONObject();
			contractEv.put("command", "subscribe");
			JSONArray arrAdd = new JSONArray();
			arrAdd.put(address);
			contractEv.put("accounts_contract", arrAdd);

			this.connection.client.subscribe(contractEv);
		}
		
	}
	private void onChainsqlSubRet() {
		this.connection.client.OnSubChainsqlRet(new Client.OnChainsqlSubRet() {
			@Override
			public void called(JSONObject args) {
				if(args.has("owner") && args.has("tablename")) {
					String key = args.getString("tablename") + ";" + args.getString("owner");
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
	public void subscribeTable(String name, String owner ,Callback<?> cb) {
 		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscribe(messageTx);
		
		if (!this.onTbMessage) {
			//this.connection.client.OnTBMessage(this::onTBMessage);
			this.connection.client.OnTBMessage(new OnTBMessage(){
				@Override
				public void called(JSONObject args) {
					onTBMessage(args);
				}
			});
			this.onTbMessage = true;
		}
		if(!this.onSubRet) {
			onChainsqlSubRet();
			this.onSubRet = true;
		}
		this.mapTableCache.put(name +";" + owner,cb);
	}

	/**
	 * Subscribe a transaction.
	 * @param id Transaction hash.
	 * @param cb Callback.
	 */
	public void subscribeTx(String id,Callback<?> cb) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("transaction", id);
		this.connection.client.subscribe(messageTx);
		if (!this.onTxMessage) {
			this.connection.client.OnTXMessage(new OnTXMessage(){
				@Override
				public void called(JSONObject args) {
					onTXMessage(args);
				}
			});
			this.onTxMessage = true;
		}
		if(!this.onSubRet) {
			onChainsqlSubRet();
			this.onSubRet = true;
		}
		this.mapCache.put(id, cb);
	}

	public void subscribeContract(final String address,final Event event,Callback<?> cb) {
		if(mapContractEvents.containsKey(address)) {
			mapContractEvents.get(address).put(event, cb);
		}else {
			Map<Event,Callback> map = new HashMap<Event,Callback>();
			map.put(event, cb);
			mapContractEvents.put(address, map);
			
			JSONObject messageEv = new JSONObject();
			messageEv.put("command", "subscribe");
			JSONArray arr = new JSONArray();
			arr.put(address);
			messageEv.put("accounts_contract", arr);
			
			this.connection.client.subscribe(messageEv);
			if (!this.onContractMessage) {
				this.connection.client.onContractEvent(new OnContractEvent() {

					@SuppressWarnings("unchecked")
					@Override
					public void called(JSONObject args) {
//						System.out.println(args);
						if(!args.has("ContractEventTopics")) {
							System.err.println("no ContractEventTopics found,not a valid event callback!");
							return;
						}
						Map<Event,Callback> mapCb = mapContractEvents.get(address);
						for (Entry<Event,Callback> entry : mapCb.entrySet()) {
							String encodedEventSignature = EventEncoder.encode(entry.getKey());
							encodedEventSignature = encodedEventSignature.substring(2, encodedEventSignature.length());
							if(encodedEventSignature.toUpperCase().equals(args.getJSONArray("ContractEventTopics").get(0))) {
								entry.getValue().called(args);
								break;
							}
						}					
					}
					
				});
				this.onContractMessage = true;
			}
		}
	}
	
	public void unsubscribeContract(String address,Event event) {
		
	}
	/**
	 * Un-subscribe a table.
	 * @param name Table name.
	 * @param owner Table owner address.
	 * @param cb Callback.
	 */
	public void unsubscribeTable(String name, String owner,Callback<JSONObject> cb) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.unsubscribe(messageTx);
	
		String key = name +";" + owner;

		JSONObject obj = new JSONObject();
		if(this.mapTableCache.containsKey(key)) {
			obj.put("status", "success");
			obj.put("result", "unsubscribe table success");
			obj.put("type", "response");
			this.mapTableCache.remove(key);
			this.mapPass.remove(key);
		}else {
			obj.put("status", "error");
			obj.put("result", "have not subscribe the table:" + name);
			obj.put("type", "response");
		}
		
		cb.called(obj);
	}

	/**
	 * Un-subscribe a transaction.
	 * @param id Transaction hash.
	 * @param cb Callback.
	 */
	public void unsubscribeTx(String id,Callback<JSONObject> cb) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("transaction", id);
		
		this.connection.client.unsubscribe(messageTx);
		
		JSONObject obj = new JSONObject();
		if(this.mapCache.containsKey(id)) {
			obj.put("status", "success");
			obj.put("result", "unsubscribe transaction success");
			obj.put("type", "response");
			this.mapCache.remove(id);
		}else {
			obj.put("status", "error");
			obj.put("result", "have not subscribe the tx:" + id);
			obj.put("type", "response");
		}
		if(cb != null){
			cb.called(obj);
		}
	}

	private void onChainsqlMessage(final JSONObject data,final String key,final String owner,final String name) {
		final JSONObject tx = data.getJSONObject("transaction");
		if(mapPass.containsKey(key)) {
   	 		Util.decryptData(mapPass.get(key), tx);
   	 		makeCallback(key,data);
   	 	}else {

        int opType = -1;
        if(data.has("transaction") && data.getJSONObject("transaction").has("OpType")) {

          opType = data.getJSONObject("transaction").getInt("OpType");
        }


        if( opType == Constant.opType.get("t_drop") || opType == Constant.opType.get("t_rename") || opType == Constant.opType.get("t_grant")) {

          mapPass.put(key, null);
          Util.decryptData(mapPass.get(key), tx);
          makeCallback(key,data);

        }else{

          connection.client.getUserToken(owner,connection.address,name,new Callback<JSONObject>(){
            @Override
            public void called(JSONObject res) {
              if(res.has("error")){
                System.err.println(res);
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
	}
	/**
	 * Table transaction trigger.
	 * @param data Table message data.
	 */
	private void onTBMessage(JSONObject data){
		String owner = data.getString("owner");
		String name = data.getString("tablename");
   	 	String key = name + ";" + owner;
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
		if(data.has("transaction") && data.getJSONObject("transaction").has("TransactionType"))
		{
			JSONObject tx = data.getJSONObject("transaction");
			TransactionType type = TransactionType.valueOf(tx.getString("TransactionType"));
			if(Util.isChainsqlType(type)) {
				if(!("validate_success".equals(data.getString("status")))){
					mapCache.remove(key);
				}
			}else {
				mapCache.remove(key);
			}
		}
	}
	

	
	private void makeCallback(String key,JSONObject data){
		if (mapCache.containsKey(key)) {
			if(mapCache.get(key) != null)
				mapCache.get(key).called(data);
			else
				mapCache.remove(key);
	    }

		if (mapTableCache.containsKey(key)) {
			if(mapTableCache.get(key) != null)
				mapTableCache.get(key).called(data);
			else
				mapTableCache.remove(key);
		}
	}
}
