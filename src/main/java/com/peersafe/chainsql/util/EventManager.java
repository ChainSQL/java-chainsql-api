package com.peersafe.chainsql.util;

import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.pubsub.Publisher.Callback;
import com.ripple.client.transport.TransportEventHandler;

public class EventManager {
	public Connection connection;
	public JSONObject cache;
	public boolean onMessage;
	private static Callback cb;

	public EventManager(Connection connection) {
		super();
		this.connection = connection;
		this.cache = new JSONObject();
		this.onMessage = false;
	}

	public void subTable(String name, String owner ,Callback cb) {
		this.cb = cb;
 		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscriptions.addMessage(messageTx);
		if (!this.onMessage) {
			// _onMessage( this , callback);
			this.connection.client.OnTBMessage(EventManager::OnMessage);
			this.onMessage = true;
		}
		//cb.called(EventManager::OnMessage);
	}

	public void subTx(String id,Callback cb) {

		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("transaction", id);
		this.connection.client.subscriptions.addMessage(messageTx);
		if (!this.onMessage) {
			this.connection.client.OnTXMessage(EventManager::OnMessage);
			this.onMessage = true;
		}
		this.cache.put(id, true);

	}

	public void unsubTable(String name, String owner) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("owner", owner);
		messageTx.put("tablename", name);
		this.connection.client.subscriptions.addMessage(messageTx);
		
		if (!this.onMessage) {
			this.connection.client.OnTBMessage(EventManager::OnMessage);
			this.onMessage = true;
		}
		this.cache.remove(name);

	}

	public void unsubTx(String id) {
		JSONObject messageTx = new JSONObject();
		messageTx.put("command", "unsubscribe");
		messageTx.put("transaction", id);
		
		this.connection.client.subscriptions.addMessage(messageTx);
		if (!this.onMessage) {
			this.connection.client.OnTXMessage(EventManager::OnMessage);
			this.onMessage = true;
		}
		this.cache.remove(id);

	}

	  private static void OnMessage(JSONObject json) {
	    	System.out.println(json.toString());
	    	cb.called(json);
	            
	   }

}
