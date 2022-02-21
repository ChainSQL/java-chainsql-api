package com.peersafe.base.client.transport.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.security.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.io.IOException;
import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.java_websocket.client.WebSocketClient;
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
	public void connectSSL(URI uri, String serverCertPath, String storePass) throws Exception{
        TransportEventHandler curHandler = handler.get();
        if (curHandler == null) {
            throw new RuntimeException("must call setEventHandler() before connect(...)");
        }
        disconnect();
        client = new WS(uri);

        client.setEventHandler(curHandler);
        curHandler.onConnecting(1);
        
        String STORETYPE = "JKS";
//		String KEYSTORE = "foxclienttrust.keystore";
//		String STOREPASSWORD = "foxclienttrustks";
		String KEYSTORE = serverCertPath;
		String STOREPASSWORD = storePass;

		KeyStore ks = KeyStore.getInstance( STORETYPE );
		File kf = new File( KEYSTORE );
		ks.load( new FileInputStream( kf ), STOREPASSWORD.toCharArray() );

//		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
//		kmf.init( ks, KEYPASSWORD.toCharArray() );
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		tmf.init( ks );

		SSLContext sslContext = null;
		sslContext = SSLContext.getInstance( "TLS" );
		sslContext.init( null, tmf.getTrustManagers(), null );
		SSLSocketFactory factory = sslContext.getSocketFactory();

		client.setSocket( factory.createSocket() );
		client.connectBlocking();
	}
    @Override
	public void connectSSL(URI uri, String[] trustCAsPath, String sslKeyPath, String sslCertPath) throws Exception{
        TransportEventHandler curHandler = handler.get();
        if (curHandler == null) {
            throw new RuntimeException("must call setEventHandler() before connect(...)");
        }
        disconnect();
        client = new WS(uri);

        client.setEventHandler(curHandler);
        curHandler.onConnecting(1);

        KeyStore tks;
        tks = getKeyStore(trustCAsPath[0], null, null);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		tmf.init(tks);
        
        SSLContext sslContext = SSLContext.getInstance( "TLS" );
        KeyStore ks;
        if(sslKeyPath != null)
        {
            ks = getKeyStore(sslCertPath, sslKeyPath, null);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
		    kmf.init(ks, null);
            sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
        }
        else
        {
            sslContext.init( null, tmf.getTrustManagers(), null );
        }
		SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        client.setSocketFactory(socketFactory);
		client.connectBlocking();
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
    private static Certificate readCert(String path) throws IOException, CertificateException {
        try (FileInputStream fin = new FileInputStream(path)) {
            return CertificateFactory.getInstance("X.509").generateCertificate(fin);
        }
    }
    private PrivateKey getPemPrivateKey(String filename) throws Exception {
        if(filename == null)
        {
            throw new Exception("ssl key can not be null");
        }
        PEMParser pem = new PEMParser(new FileReader(filename));
        PrivateKey priKey = new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo)pem.readObject());
        pem.close();
        return priKey;
    }
    
    private KeyStore getKeyStore(String certPath, String keyPath, String pwd) throws IOException {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            if(certPath == null)
            {
                throw new IOException("certPath can not be null");
            }
            // Reading the cert
            Certificate cert = readCert(certPath);

            // KeyStore keystore = KeyStore.getInstance("PKCS12");

            if( pwd == null){
                keystore.load(null, null);
            } else {
                keystore.load(null, pwd.toCharArray());
            }
            // Adding the cert to the keystore
            keystore.setCertificateEntry("cert-alias", cert);

            if(keyPath != null)
            {
                PrivateKey priKey = getPemPrivateKey(keyPath);
                keystore.setKeyEntry("key-alias", priKey, null, new Certificate[] {cert});
            }

            return keystore;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
