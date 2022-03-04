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
import java.util.Arrays;
import java.io.IOException;
import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.peersafe.base.client.transport.TransportEventHandler;
import com.peersafe.base.client.transport.WebSocketTransport;
import com.peersafe.base.crypto.X509CryptoSuite;

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
    WebSocketClientHandler wscHandler = null;
    boolean isGM=false;

    public JavaWebSocketTransportImpl(){
        try {
            X509CryptoSuite.enableX509CertificateWithGM();
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void setHandler(TransportEventHandler events) {
        handler = new WeakReference<TransportEventHandler>(events);
        if (client != null) {
            client.setEventHandler(events);
        }
    }

    @Override
    public void sendMessage(JSONObject msg) {
        // System.out.println(msg.toString());
        if(isGM)
        {
            wscHandler.sendMessage(msg.toString());
        }
        else
        {
            client.send(msg.toString());
        }
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

        String certSigAlg = ((X509Certificate)readCert(trustCAsPath[0])).getSigAlgName();
        if(certSigAlg.equals("SM3withSM2"))
        {
            isGM = true;
        }

        KeyStore tks;
        tks = getKeyStore(trustCAsPath, null);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(tks);
    
        if(isGM)
        {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                final String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
                final int port = uri.getPort();
                SslContextBuilder sslCtxBuilder = SslContextBuilder.forClient().sslProvider(SslProvider.OPENSSL)
                    .trustManager(tmf)
                    .protocols(new String[]{"TLSv1.2"})
                    .ciphers(Arrays.asList("ECDHE-SM2-WITH-SMS4-GCM-SM3"));
                SslContext sslCtx = sslKeyPath == null ? sslCtxBuilder.build() : 
                            sslCtxBuilder.keyManager(new File(sslCertPath), new File(sslKeyPath)).build();

                wscHandler = new WebSocketClientHandler(
                                WebSocketClientHandshakerFactory.newHandshaker(
                                        uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));
    
                Bootstrap b = new Bootstrap();
                b.group(group)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) {
                         ChannelPipeline p = ch.pipeline();
                         if (sslCtx != null) {
                             p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                         }
                         p.addLast(
                                 new HttpClientCodec(),
                                 new HttpObjectAggregator(8192),
                                 WebSocketClientCompressionHandler.INSTANCE,
                                 wscHandler);
                    }
                });

                wscHandler.setEventHandler(curHandler);
                wscHandler.doConnect(b, uri);
            } catch (Exception e){
                e.printStackTrace();
                group.shutdownGracefully();
            }
        }
        else
        {
            disconnect();
            client = new WS(uri);

            client.setEventHandler(curHandler);
            curHandler.onConnecting(1);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyStore ks;
            if (sslKeyPath != null) {
                ks = getKeyStore(sslCertPath, sslKeyPath, null);
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, null);
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            } else {
                sslContext.init(null, tmf.getTrustManagers(), null);
            }
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            client.setSocketFactory(socketFactory);
		    client.connectBlocking();
        }
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
    private KeyStore getKeyStore(String[] certPaths, String pwd) throws IOException {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            if(certPaths.length == 0)
            {
                throw new IOException("certPath can not be null");
            }

            if( pwd == null){
                keystore.load(null, null);
            } else {
                keystore.load(null, pwd.toCharArray());
            }

            // for(String certPath : certPaths)
            for(int index = 0; index < certPaths.length; index ++)
            {
                // Reading the cert
                Certificate cert = readCert(certPaths[index]);
                // Adding the cert to the keystore
                keystore.setCertificateEntry("cert-alias" + index, cert);
            }

            return keystore;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
