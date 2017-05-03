package com.peersafe.chainsql.net;

import java.io.Closeable;
import java.io.IOException;

import com.ripple.client.Client;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

public class Connection implements Closeable {

	public String address;
	public String secret;
	public String scope;
	public Client client;

/*	public void as (String address ,String secret) {
		  this.address = address;
		  this.secret = secret;
		  this.scope = this.address;
	}
	public void use(String address){
		  this.scope =address;
	} 
	*/
	public Connection connect(String url){
		this.client = new Client(new JavaWebSocketTransportImpl()).connect(url);
		return this;  
	} 
	
	public void  disconnect(){
		this.client.disconnect();
	
	} 
	
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
