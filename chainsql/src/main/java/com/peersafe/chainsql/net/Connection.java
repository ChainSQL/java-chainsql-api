package com.peersafe.chainsql.net;

import java.io.Closeable;
import java.io.IOException;
import java.security.Security;

import com.peersafe.base.client.Client;
import com.peersafe.base.client.transport.impl.JavaWebSocketTransportImpl;

public class Connection implements Closeable {

	public String address;
	public String secret;
	public String scope;
	public Client client;
	public String userCert;
	public String schemaID="";



	/**
	 * Connect to a websocket address.
	 * @param url Websocket url.
	 * @return Connection object.
	 */
	public Connection connect(String url){
		this.client = new Client(new JavaWebSocketTransportImpl()).connect(url);
		return this;  
	}
	
	public Connection connect(String url,String serverCertPath,String storePass){
		this.client = new Client(new JavaWebSocketTransportImpl()).connect(url,serverCertPath,storePass);
		return this;  
	}

	public Connection connect(String url, String[] trustCAsPath, String sslKeyPath, String sslCertPath){
		String disableCurves = Security.getProperty("jdk.disabled.namedCurves");
		if(disableCurves != null) {
			disableCurves = disableCurves.replace("secp256k1, ", "");
		}
		else disableCurves = "";
		Security.setProperty("jdk.disabled.namedCurves", disableCurves);
		System.setProperty("jdk.tls.namedGroups", "secp256k1, sm2p256v1");
		this.client = new Client(new JavaWebSocketTransportImpl()).connect(url, trustCAsPath, sslKeyPath, sslCertPath);
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
	 * @return return value.
	 */
	public Client getClient() {
		return client;
	}
	
	/**
	 * Set Client object.
	 * @param client client.
	 */
	public void setClient(Client client) {
		this.client = client;
	}
	/**
	 * Get account address.
	 * @return Current account address.
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * Set account address.
	 * @param address Account address.
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * 
	 * @return Secret value.
	 */
	public String getSecret() {
		return secret;
	}
	/**
	 * Set secret.
	 * @param secret Account secret.
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}
	/**
	 * Get scope
	 * @return Scope value.
	 */
	public String getScope() {
		return scope;
	}
	/**
	 * Set scope.
	 * @param scope Scope value.
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
