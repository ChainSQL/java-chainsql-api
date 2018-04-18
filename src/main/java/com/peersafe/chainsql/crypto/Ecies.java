package com.peersafe.chainsql.crypto;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.crypto.KeyAgreement;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peersafe.base.config.Config;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.encodings.B58IdentiferCodecs;
import com.peersafe.base.utils.HashUtils;
import com.peersafe.base.utils.Sha512;
import com.peersafe.chainsql.crypto.EncryptMsg.MultiEncrypt.HashToken;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.ZLibUtils;

public class Ecies {
	public static final int AESKeyLength = 32;
	public static final int AESBlockLength = 16;
	public static final int HMACKeyLength = 32;
	public static final int IVLength = 16; // bytes
	public static final int MACBYTELENGTH = 32;
    
	final static String ALGORITHM = "secp256k1";

	/**
	 * encrypt text with a publickey list,the cipher can be decrypted 
	 * by every secret whose publickey is in this list.
	 * @param plainText  plainText
	 * @param listPublicKey listPublicKey
	 * @return byte array
	 */
	public static byte[] encryptText(String plainText,List<String> listPublicKey) {
		Security.addProvider(new BouncyCastleProvider());
		//check size
		if(listPublicKey.size() == 0)
			return null;
		
		//AES encrypt
		byte[] password = Util.getRandomBytes(16);
    	byte[] aesEnc = Aes128.encrypt(password, plainText.getBytes());
    	
    	try {
    	   	//encrypt password
        	List<byte[]> listPubHash = new ArrayList<byte[]>();
        	List<byte[]> listPassCipher = new ArrayList<byte[]>();
        	byte[] dataPubA = {0};
        	//如果使用国密
        	if(Config.isUseGM()) {
            	for(String sPub : listPublicKey) {
            		byte[] pubBytes = getB58IdentiferCodecs().decode(sPub, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
            		byte[] pubHash = HashUtils.quarterSha512(pubBytes);
            		byte[] passCipher = EncryptCommon.asymEncrypt(password, pubBytes);;
            		listPubHash.add(pubHash);
            		listPassCipher.add(passCipher);
            	}
        	}else {
        		//random key-pair
            	IKeyPair pair = Seed.randomKeyPair();
        		byte [] dataPrvA = pair.priv().toByteArray();
        		dataPubA = pair.pub().toByteArray();

            	for(String sPub : listPublicKey) {
            		byte[] pubBytes = getB58IdentiferCodecs().decode(sPub, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
            		byte[] pubHash = HashUtils.quarterSha512(pubBytes);
            		byte[] passCipher = simpleEncrypt(password,pubBytes,dataPrvA);
            		listPubHash.add(pubHash);
            		listPassCipher.add(passCipher);
            	}
        	}

//        	int singleLen = listPubHash.get(0).length + listPassCipher.get(0).length;
//        	int finalLen = 1 + singleLen * listPublicKey.size() + dataPubA.length + aesEnc.length;
//        	byte[] finalBytes = new byte[finalLen];
//        	byte[] lenByte = new byte[1];
//        	lenByte[0] = (byte) (listPublicKey.size() & 0xff);
//        	System.arraycopy(lenByte, 0, finalBytes, 0, 1);
//        	System.arraycopy(dataPubA, 0, finalBytes, 1, dataPubA.length);
//        	int pos = 1 + dataPubA.length;
//        	for(int i=0; i<listPubHash.size(); i++) {
//        		byte[] pubHash = listPubHash.get(i);
//        		byte[] passCipher = listPassCipher.get(i);
//        		
//        		System.arraycopy(pubHash, 0, finalBytes, pos, pubHash.length);
//        		pos += pubHash.length;
//        		System.arraycopy(passCipher, 0, finalBytes, pos, passCipher.length);
//        		pos += passCipher.length;
//        	}
//        	System.arraycopy(aesEnc, 0, finalBytes, pos, aesEnc.length);

//    		return finalBytes;
        	EncryptMsg.MultiEncrypt.Builder builder = EncryptMsg.MultiEncrypt.newBuilder();
        	builder.setPublicOther(ByteString.copyFrom(dataPubA));
        	for(int i=0; i<listPubHash.size(); i++) {
        		EncryptMsg.MultiEncrypt.HashToken.Builder bd = EncryptMsg.MultiEncrypt.HashToken.newBuilder();
        		bd.setPublicHash(ByteString.copyFrom(listPubHash.get(i)));
        		bd.setToken(ByteString.copyFrom(listPassCipher.get(i)));
        		builder.addHashTokenPair(bd);
        	}
        	builder.setCipher(ByteString.copyFrom(aesEnc));
        	
        	byte[] finalByte =  builder.build().toByteArray();
        	
        	return ZLibUtils.compress(finalByte);	
    	}catch(Exception e) {
    		e.printStackTrace();
    		return null;
    	}
	}
	
	public static String decryptText(byte[] cipher,String secret) {
		
		byte[] cipherBytes = ZLibUtils.decompress(cipher);
		try {
			EncryptMsg.MultiEncrypt msg = EncryptMsg.MultiEncrypt.parseFrom(cipherBytes);
			byte[] password = null;

			//如果使用国密
	    	if(Config.isUseGM()) {
	    		Seed seed = Seed.randomSeed();
	    		seed.setGM();
	    		IKeyPair keyPair = seed.keyPair();
	    		byte[] pubBytes = keyPair.canonicalPubBytes();
	    		byte[] pubHash = HashUtils.quarterSha512(pubBytes);
	    		String sPubHash = Arrays.toString(pubHash);
	    		
	    		List<HashToken> listHashToken = msg.getHashTokenPairList();
				for(HashToken hashPair : listHashToken) {
					//byte[] tmp = hashPair.getPublicHash().toByteArray();
					if(sPubHash.equals(Arrays.toString(hashPair.getPublicHash().toByteArray()))){
						password = EncryptCommon.asymDecrypt(hashPair.getToken().toByteArray(), null);	;
						break;
					}
				}
				if(password != null) {
					byte[] plain = Aes128.decrypt(msg.getCipher().toByteArray(), password);
					return new String(plain);
				}
	    	}else {
	    		IKeyPair pair = Seed.fromBase58(secret).keyPair();
	    		byte[] dataPrvA = pair.priv().toByteArray();
	    		byte[] dataPubA = pair.pub().toByteArray();
				byte[] pubHashSelf = HashUtils.quarterSha512(dataPubA);
				String sPubHash = Arrays.toString(pubHashSelf);
				
				byte[] pubOther = msg.getPublicOther().toByteArray();
				List<HashToken> listHashToken = msg.getHashTokenPairList();
				for(HashToken hashPair : listHashToken) {
					//byte[] tmp = hashPair.getPublicHash().toByteArray();
					if(sPubHash.equals(Arrays.toString(hashPair.getPublicHash().toByteArray()))){
						password = simpleDecrypt(hashPair.getToken().toByteArray(),dataPrvA,pubOther);
						break;
					}
				}
				if(password != null) {
					byte[] plain = Aes128.decrypt(msg.getCipher().toByteArray(), password);
					return new String(plain);
				}
	    	}
		}catch(InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		return "";
//		//get public-list size
//		int clength = (int)cipher[0];
//		
//		//get random public
//		byte[] pubOther = new byte[dataPubA.length];
//		System.arraycopy(cipher, 1, pubOther, 0, pubOther.length);
//		
//		//get token
//		byte[] password = null;
//		byte[] pubHashSelf = HashUtils.quarterSha512(dataPubA);
//		String sPubHash = Arrays.toString(pubHashSelf);
//		int pos = 1 + dataPubA.length;
//		for(int i=0; i<clength; i++) {
//			byte[] pubHash = new byte[16];
//			byte[] passCipher = new byte[48];
//			System.arraycopy(cipher, pos, pubHash, 0, pubHash.length);
//			pos += pubHash.length;
//			System.arraycopy(cipher, pos, passCipher, 0, passCipher.length);
//			pos += passCipher.length;
//			if(sPubHash.equals(Arrays.toString(pubHash))) {
//				//decrypt token
//				password = simpleDecrypt(passCipher,dataPrvA,pubOther);
//				break;
//			}
//		}
//		if(password == null)
//			return "";
//		//get cipher
//		int cipherLen = cipher.length - 1 - pubOther.length - clength * (16 + 48);
//		byte[] cipherBytes = new byte[cipherLen];
//		System.arraycopy(cipher, cipher.length - cipherLen, cipherBytes, 0, cipherLen);
//		byte[] plain = Aes.decrypt(cipherBytes, password);
//		return new String(plain);
	}
	
	private static byte[] simpleEncrypt(byte[] plainBytes,byte[] publicKey,byte[] dataPrvA) {
		try{
			byte[] secret = doECDH(dataPrvA, publicKey);
			Sha512 hash = new Sha512(secret);
			byte[] kdOutput = hash.finish();
			//System.out.println("kdOutput:" + Util.bytesToHex(kdOutput));
			
	        byte[] aesKey = new byte[AESKeyLength];
	        System.arraycopy(kdOutput, 0, aesKey, 0, AESKeyLength);
	        
	        //generate random iv
	        byte[] iv = new byte[IVLength];
	        Random r = new Random();
	        r.nextBytes(iv);
	        //System.out.println(Util.bytesToHex(iv));
	        //aes-256-cbc
	        ParametersWithIV keyWithIv = new ParametersWithIV(new KeyParameter(aesKey), iv);
	        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
	        cipher.init(true, keyWithIv);        
	        byte[] encryptedBytes = new byte[cipher.getOutputSize(plainBytes.length)];
	        int length1 = cipher.processBytes(plainBytes, 0, plainBytes.length, encryptedBytes, 0);	        
	        cipher.doFinal(encryptedBytes, length1);
	        //System.out.println("cipher:" + Util.bytesToHex(encryptedBytes));
	       
	        byte[] finalBytes = new byte[iv.length + encryptedBytes.length];
	        System.arraycopy(iv, 0, finalBytes, 0, iv.length);
	        System.arraycopy(encryptedBytes, 0, finalBytes, iv.length, encryptedBytes.length);
	        return finalBytes;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] simpleDecrypt(byte[] cipherText,byte[] priv,byte[] dataPubA) {
		try{
			byte[] secret = doECDH(priv, dataPubA);
			Sha512 hash = new Sha512(secret);
			byte[] kdOutput = hash.finish();
			
	        byte[] aesKey = new byte[AESKeyLength];
	        System.arraycopy(kdOutput, 0, aesKey, 0, AESKeyLength);
	        
	        byte[] iv = new byte[IVLength];
	        byte[] cipherBytes = new byte[cipherText.length - IVLength];
	        System.arraycopy(cipherText, 0, iv, 0, IVLength);
	        System.arraycopy(cipherText, IVLength, cipherBytes, 0, cipherBytes.length);
	        
	        //aes-256-cbc
	        ParametersWithIV keyWithIv = new ParametersWithIV(new KeyParameter(aesKey), iv);
	        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
	        cipher.init(false, keyWithIv);        
	        byte[] decryptedBytes  = new byte[cipher.getOutputSize(cipherBytes.length)];
	        int length1 = cipher.processBytes(cipherBytes, 0, cipherBytes.length, decryptedBytes , 0);	        
	        int length2 = cipher.doFinal(decryptedBytes , length1);
	        byte[] finalBytes = new byte[length1 + length2];
	        System.arraycopy(decryptedBytes, 0, finalBytes, 0, finalBytes.length);
	        
	        return finalBytes;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param plainBytes bytes to be encrypted.
	 * @param publicKey publickey bytes.
	 * @return return value.
	 */
	public static byte[] eciesEncrypt(byte[] plainBytes,byte[] publicKey){
		Security.addProvider(new BouncyCastleProvider());

		//random key-pair
    	IKeyPair pair = Seed.randomKeyPair();
		byte [] dataPrvA = pair.priv().toByteArray();
		byte [] dataPubA = pair.pub().toByteArray();
		//decode publickey
		
		String finalHex = "";
		try{
			byte[] secret = doECDH(dataPrvA, publicKey);
			Sha512 hash = new Sha512(secret);
			byte[] kdOutput = hash.finish();
			//System.out.println("kdOutput:" + Util.bytesToHex(kdOutput));
			
	        byte[] aesKey = new byte[AESKeyLength];
	        System.arraycopy(kdOutput, 0, aesKey, 0, AESKeyLength);
	        byte[] hmacKey = new byte[HMACKeyLength];
	        System.arraycopy(kdOutput, AESKeyLength, hmacKey, 0, HMACKeyLength);
	        //System.out.println("aesKey:" + Util.bytesToHex(aesKey));

	        byte[] macBytes = hMac(hmacKey,plainBytes);
	        //System.out.println("hmac:" + Util.bytesToHex(macBytes));
	        
	        byte[] plainBuf = new byte[macBytes.length + plainBytes.length];
	        System.arraycopy(macBytes, 0, plainBuf, 0, macBytes.length);
	        System.arraycopy(plainBytes, 0, plainBuf, macBytes.length, plainBytes.length);


	        //generate random iv
	        byte[] iv = new byte[IVLength];
	        Random r = new Random();
	        r.nextBytes(iv);
	        //System.out.println(Util.bytesToHex(iv));
	        //aes-256-cbc
	        ParametersWithIV keyWithIv = new ParametersWithIV(new KeyParameter(aesKey), iv);
	        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
	        cipher.init(true, keyWithIv);        
	        byte[] encryptedBytes = new byte[cipher.getOutputSize(plainBuf.length)];
	        int length1 = cipher.processBytes(plainBuf, 0, plainBuf.length, encryptedBytes, 0);	        
	        cipher.doFinal(encryptedBytes, length1);
	        //System.out.println("cipher:" + Util.bytesToHex(encryptedBytes));
	        
	        finalHex = Util.bytesToHex(dataPubA) + Util.bytesToHex(iv) + Util.bytesToHex(encryptedBytes);
		}catch(Exception e){
			e.printStackTrace();
		}
		return Util.hexToBytes( finalHex);
	}
	/**
	 * 
	 * @param cipherHex cipherHex.
	 * @param privateKey privateKey
	 * @return return value.
	 * @throws Exception Exception throws.
	 */
	public static byte[] eciesDecrypt (String cipherHex,String privateKey) throws Exception
	{
	    return eciesDecrypt(Util.hexToBytes(cipherHex),getB58IdentiferCodecs().decodeFamilySeed(privateKey));
	}
	
	public static byte[] eciesDecrypt (byte[] cipherText,byte[] privateKey) throws Exception{
		Security.addProvider(new BouncyCastleProvider());
		byte[] ciphertext = cipherText;
	    int level = 256;
	    int Rb_len = 33;
	    int D_len = level >> 3;
	    int ct_len = ciphertext.length;
	    if (ct_len < Rb_len + IVLength + D_len + AESBlockLength)
	        throw new Exception("Illegal cipherText length: " + ct_len + " must be >= " + (Rb_len + IVLength + D_len + AESBlockLength));
	    
	    byte[] publicOther = new byte[Rb_len];
	    byte[] iv = new byte[IVLength];
	    byte[] cipherBytes = new byte[ciphertext.length - Rb_len - IVLength];
	    System.arraycopy(ciphertext, 0, publicOther, 0, Rb_len);
	    System.arraycopy(ciphertext, Rb_len, iv, 0, IVLength);
	    System.arraycopy(ciphertext, Rb_len + IVLength, cipherBytes, 0, ciphertext.length - Rb_len - IVLength);

	    byte [] seedSelf = privateKey;
	    IKeyPair pair = Seed.getKeyPair(seedSelf);
	    try{
			byte[] secret = doECDH(pair.priv().toByteArray(), publicOther);
			Sha512 hash = new Sha512(secret);
			byte[] kdOutput = hash.finish();
			//System.out.println("kdOutput:" + Util.bytesToHex(kdOutput));
			
	        byte[] aesKey = new byte[AESKeyLength];
	        System.arraycopy(kdOutput, 0, aesKey, 0, AESKeyLength);
	        byte[] hmacKey = new byte[HMACKeyLength];
	        System.arraycopy(kdOutput, AESKeyLength, hmacKey, 0, HMACKeyLength);
	        //System.out.println("aesKey:" + Util.bytesToHex(aesKey));
	        //System.out.println(Util.bytesToHex(iv));
	        
	        //aes-256-cbc
	        ParametersWithIV keyWithIv = new ParametersWithIV(new KeyParameter(aesKey), iv);
	        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
	        cipher.init(false, keyWithIv);        
	        byte[] decryptedBytes  = new byte[cipher.getOutputSize(cipherBytes.length)];
	        int length1 = cipher.processBytes(cipherBytes, 0, cipherBytes.length, decryptedBytes , 0);	        
	        int length2 = cipher.doFinal(decryptedBytes , length1);
	        byte[] finalBytes = new byte[length1 + length2];
	        System.arraycopy(decryptedBytes, 0, finalBytes, 0, finalBytes.length);
	        
	        byte[] macBytes = new byte[MACBYTELENGTH];
	        byte[] plainText = new byte[finalBytes.length - MACBYTELENGTH];
	        System.arraycopy(finalBytes, 0, macBytes, 0, MACBYTELENGTH);
	        System.arraycopy(finalBytes, macBytes.length, plainText, 0, plainText.length);
	        byte[] recoveredD = hMac(hmacKey, plainText);
	        if(!Arrays.toString(recoveredD).equals(Arrays.toString(macBytes))){
	        	return null;
	        }
	        return plainText;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static byte[] hMac(byte[] hmacKey,byte[] plainText){
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(hmacKey));
        hmac.update(plainText, 0, plainText.length);
        byte[] macBytes = new byte[MACBYTELENGTH];
        hmac.doFinal(macBytes, 0);
        return macBytes;
	}
	private static PublicKey loadPublicKey (byte [] data) throws Exception
	{
		ECParameterSpec params = ECNamedCurveTable.getParameterSpec(ALGORITHM);
		ECPublicKeySpec pubKey = new ECPublicKeySpec(
				params.getCurve().decodePoint(data), params);
		KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
		return kf.generatePublic(pubKey);
	}

	private static PrivateKey loadPrivateKey (byte [] data) throws Exception
	{
		ECParameterSpec params = ECNamedCurveTable.getParameterSpec(ALGORITHM);
		ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(data), params);
		KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
		return kf.generatePrivate(prvkey);
	}

	private static byte[] doECDH (byte[] dataPrv, byte[] dataPub) throws Exception
	{
		KeyAgreement ka = KeyAgreement.getInstance("ECDH", "BC");
		ka.init(loadPrivateKey(dataPrv));
		ka.doPhase(loadPublicKey(dataPub), true);
		byte [] secret = ka.generateSecret();
		
//		System.out.println("doECDH-priv:" + Util.bytesToHex(dataPrv));
//		System.out.println("doECDH-pub:" + Util.bytesToHex(dataPub));
//		System.out.println("doECDH-secret:" + Util.bytesToHex(secret));
		return secret;
	}
}
