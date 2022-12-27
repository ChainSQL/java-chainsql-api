package com.peersafe.base.client.transport;

import org.json.JSONObject;

import java.net.URI;

public interface WebSocketTransport {
    public abstract void setHandler(TransportEventHandler events);
    public abstract void sendMessage(JSONObject msg);
    public abstract void connect(URI url);
    public abstract void connectSSL(URI url,String serverCertPath,String storePass) throws Exception;
    public abstract void connectSSL(URI url,String[] trustCAsPath, String sslKeyPath, String sslCertPath) throws Exception;
    /**
     * It's the responsibility of implementations to trigger
     * {@link com.peersafe.base.client.transport.TransportEventHandler#onDisconnected}
     */
    public abstract void disconnect();
}
