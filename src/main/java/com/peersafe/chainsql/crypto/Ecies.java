package com.peersafe.chainsql.crypto;

import static com.ripple.config.Config.getB58IdentiferCodecs;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Arrays;
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

import com.peersafe.chainsql.util.Util;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.encodings.B58IdentiferCodecs;
import com.ripple.utils.Sha512;

public class Ecies {
	public static final int AESKeyLength = 32;
	public static final int AESBlockLength = 16;
	public static final int HMACKeyLength = 32;
	public static final int IVLength = 16; // bytes
	public static final int MACBYTELENGTH = 32;
    
	final static String ALGORITHM = "secp256k1";


	/**
	 * 非对称加密
	 * @param plainText 要加密的内容
	 * @param publicKey base58格式的publicKey
	 * @return
	 */
	public static String eciesEncrypt (String plainText,String publicKey)
	{
		byte [] dataPubB = getB58IdentiferCodecs().decode(publicKey, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
		return eciesEncrypt(plainText.getBytes(),dataPubB);
	}
	public static String eciesEncrypt(byte[] plainBytes,byte[] publicKey){
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
		return finalHex;
	}
	public static byte[] eciesDecrypt (String cipherHex,String privateKey) throws Exception
	{
		Security.addProvider(new BouncyCastleProvider());
		byte[] ciphertext = Util.hexToBytes(cipherHex);
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

	    byte [] seedSelf = getB58IdentiferCodecs().decodeFamilySeed(privateKey);
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
