package com.peersafe.base.client.transport.impl;

import java.lang.ref.WeakReference;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import com.peersafe.base.client.transport.TransportEventHandler;
import com.peersafe.base.client.transport.WebSocketTransport;

class WS extends WebSocketClient {

    WeakReference<TransportEventHandler> h;
    String frameData = "";
    /**
     * WS constructor.
     * @param serverURI
     */
    public WS(URI serverURI) {
        super(serverURI);
    }

    /**
     * muteEventHandler
     */
    public void muteEventHandler() {
        h.clear();
    }

    /**
     * setEventHandler
     * @param eventHandler eventHandler
     */
    public void setEventHandler(TransportEventHandler eventHandler) {
        h = new WeakReference<TransportEventHandler>(eventHandler);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        TransportEventHandler handler = h.get();
        if (handler != null) {
            handler.onConnected();
        }
    }
    //数据量大时按段返回
    public void onFragment( Framedata frame ) {
        frameData += new String( frame.getPayloadData().array() );
        if(frame.isFin()){
          onMessage(frameData);
          frameData = "";
        }
      }
    
    @Override
    public void onMessage(String message) {
    	//System.out.println(message);
        TransportEventHandler handler = h.get();
        if (handler != null) {
            handler.onMessage(new JSONObject(message));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        TransportEventHandler handler = h.get();
        if (handler != null) {
            handler.onDisconnected(false);
        }
    }

    @Override
    public void onError(Exception ex) {
        TransportEventHandler handler = h.get();
        if (handler != null) {
            handler.onError(ex);
        }
    }
}

public class JavaWebSocketTransportImpl implements WebSocketTransport {

    WeakReference<TransportEventHandler> handler;
    WS client = null;

    @Override
    public void setHandler(TransportEventHandler events) {
        handler = new WeakReference<TransportEventHandler>(events);
        if (client != null) {
            client.setEventHandler(events);
        }
    }

    @Override
    public void sendMessage(JSONObject msg) {
        client.send(msg.toString());
    }

    @Override
    public void connect(URI uri) {
        TransportEventHandler curHandler = handler.get();
        if (curHandler == null) {
            throw new RuntimeException("must call setEventHandler() before connect(...)");
        }
        disconnect();
        client = new WS(uri);

        client.setEventHandler(curHandler);
        curHandler.onConnecting(1);
        client.connect();
    }

    @Override
    public void disconnect() {
        if (client != null) {
            TransportEventHandler handler = this.handler.get();
            // Before we mute the handler, call disconnect
            if (handler != null) {
                handler.onDisconnected(false);
            }
            client.muteEventHandler();
            client.close();
            client = null;
        }
    }
}
