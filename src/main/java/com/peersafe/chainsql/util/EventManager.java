package com.peersafe.chainsql.util;

import java.util.HashMap;

import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.ripple.client.pubsub.Publisher.Callback;

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
	
	public void subTable(String name, String owner ,Callback cb) {
 		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscriptions.addMessage(messageTx);
		if (!this.onMessage) {
			 _onMessage(this);
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
			 _onMessage(this);
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
		
		if (!this.onMessage) {
			 _onMessage(this);
			this.onMessage = true;
		}
		this.mapCache.remove(name + owner);

	}

	public void unsubTx(String id) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("transaction", id);
		
		this.connection.client.subscriptions.addMessage(messageTx);
		if (!this.onMessage) {
			 _onMessage(this);
			this.onMessage = true;
		}
		this.mapCache.remove(id);

	}
	private void _onMessage(EventManager em){
		em.connection.client.OnMessage((data)->{
			   if ( "table".equals(data.getString("type")) || "singleTransaction".equals(data.getString("type"))) {
			      String key = null;
			      if ("table".equals(data.getString("type"))) {
			    	  key = data.getString("tablename") + data.getString("owner");
			      };
			      if ("singleTransaction".equals(data.getString("type"))) {
			    	  key = ((JSONObject) data.get("transaction")).getString("hash");
			      }
			      if (em.mapCache.containsKey(key)) {
		    	     em.mapCache.get(key).called(data);
			        if ( !"validate_success".equals(data.getString("status"))) {
			        	em.mapCache.remove(key);
			        }
			      }
		    }
		});
	}
}
