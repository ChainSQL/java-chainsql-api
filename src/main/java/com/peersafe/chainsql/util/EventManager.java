package com.peersafe.chainsql.util;

import java.util.HashMap;

import org.json.JSONObject;

import com.peersafe.base.client.Client.OnTBMessage;
import com.peersafe.base.client.Client.OnTXMessage;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.chainsql.net.Connection;

public class EventManager {
	public Connection connection;
	public boolean onMessage;
	private HashMap<String,Callback> mapCache;
	public JSONObject result;

	/**
	 * Constructor
	 * @param connection connection object.
	 */
	public EventManager(Connection connection) {
		super();
		this.connection = connection;
		this.mapCache = new HashMap<String,Callback>();
		this.onMessage = false;
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
		this.mapCache.put(id, cb);
	}

	/**
	 * Un-subscribe a table.
	 * @param name Table name.
	 * @param owner Table owner address.
	 */
	public void unsubTable(String name, String owner) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscriptions.addMessage(messageTx);
	
		this.mapCache.remove(name + owner);
	}

	/**
	 * Un-subscribe a transaction.
	 * @param id Transaction hash.
	 */
	public void unsubTx(String id) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("transaction", id);
		
		this.connection.client.subscriptions.addMessage(messageTx);
		
		this.mapCache.remove(id);

	}

	/**
	 * Table transaction trigger.
	 * @param data Table message data.
	 */
	private void onTBMessage(JSONObject data){
		String key = data.getString("tablename") + data.getString("owner");
		makeCallback(key,data);
	}
	
	private void onTXMessage(JSONObject data){
		String key = ((JSONObject) data.get("transaction")).getString("hash");
		makeCallback(key,data);
        if ( !"validate_success".equals(data.getString("status"))) {
        	mapCache.remove(key);
        }
	}
	
	private void makeCallback(String key,JSONObject data){
		if (mapCache.containsKey(key)) {
	    	 Util.unHexData(data.getJSONObject("transaction"));
	    	 mapCache.get(key).called(data);
	     }
	}
}
