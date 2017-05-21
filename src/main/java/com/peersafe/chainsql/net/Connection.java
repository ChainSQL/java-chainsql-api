package com.peersafe.chainsql.net;

import java.io.Closeable;
import java.io.IOException;

import com.peersafe.base.client.Client;
import com.peersafe.base.client.transport.impl.JavaWebSocketTransportImpl;

public class Connection implements Closeable {

	public String address;
	public String secret;
	public String scope;
	public Client client;

	/**
	 * Connect to a websocket address.
	 * @param url Websocket url.
	 * @return Connection object.
	 */
	public Connection connect(String url){
		this.client = new Client(new JavaWebSocketTransportImpl()).connect(url);
		return this;  
	} 
	
	/**
	 * Disconnect from websocket connection.
	 */
	public void  disconnect(){
		this.client.disconnect();
	
	} 
	
	/**
	 * Get Client object.
	 * @return
	 */
	public Client getClient() {
		return client;
	}
	
	/**
	 * Set Client object.
	 * @param client
	 */
	public void setClient(Client client) {
		this.client = client;
	}
	/**
	 * Get account address.
	 * @return
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * Set account address.
	 * @param address
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * 
	 * @return
	 */
	public String getSecret() {
		return secret;
	}
	/**
	 * Set secret.
	 * @param secret
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}
	/**
	 * Get scope
	 * @return
	 */
	public String getScope() {
		return scope;
	}
	/**
	 * Set scope.
	 * @param scope
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	/**
	 * Close a connection.
	 */
	@Override
	public void close() throws IOException {
		disconnect();
	}

}
