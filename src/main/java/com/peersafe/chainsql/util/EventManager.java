package com.peersafe.chainsql.util;

import java.util.HashMap;

import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.peersafe.base.client.pubsub.Publisher.Callback;

public class EventManager {
	public Connection connection;
	public boolean onMessage;
	private HashMap<String,Callback> mapCache;
	public JSONObject result;

	public EventManager(Connection connection) {
		super();
		this.connection = connection;
		this.mapCache = new HashMap<String,Callback>();
		this.onMessage = false;
	}
	
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
	
	public void subTable(String name, String owner ,Callback cb) {
 		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscriptions.addMessage(messageTx);
		
		if (!this.onMessage) {
			this.connection.client.OnTBMessage(this::onTBMessage);
			this.onMessage = true;
		}
		this.mapCache.put(name + owner,cb);
	}

	public void subTx(String id,Callback cb) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("transaction", id);
		this.connection.client.subscriptions.addMessage(messageTx);
		if (!this.onMessage) {
			this.connection.client.OnTXMessage(this::onTXMessage);
			this.onMessage = true;
		}
		this.mapCache.put(id, cb);

	}

	public void unsubTable(String name, String owner) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscriptions.addMessage(messageTx);
	
		this.mapCache.remove(name + owner);

	}

	public void unsubTx(String id) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("transaction", id);
		
		this.connection.client.subscriptions.addMessage(messageTx);
		
		this.mapCache.remove(id);

	}

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
