package com.peersafe.chainsql.crypto;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peersafe.base.config.Config;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.crypto.sm.SM2Util;
import com.peersafe.base.crypto.sm.SM4Util;
import com.peersafe.base.utils.HashUtils;
import com.peersafe.chainsql.crypto.EncryptMsg.MultiEncrypt.HashToken;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.ZLibUtils;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class EncryptCommon {
	//通用非对称加密
	public static byte[] asymEncrypt(byte[] plainBytes,byte[] publicKey){

		// 通过公钥判断 加密的类别
		// 选择加密算法
		if(Config.isUseGM()){
//			return SM2.encrypt(plainBytes,publicKey);
			return null;
		}else if(publicKey.length == 65 && publicKey[0] == 0x47){
			// softGMALg
			try{
				//
				byte[] ret =   SM2Util.encrypt(publicKey, plainBytes);
				return ret;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		else{
			return Ecies.eciesEncrypt(plainBytes, publicKey);
		}
	}

	public static byte[]  asymDecrypt (byte[] cipher,byte[] privateKey,boolean bSM) {
		try{

			if(bSM){
					return  SM2Util.decrypt(privateKey, cipher);
			} else{
					return Ecies.eciesDecrypt(cipher, privateKey);
			}

		}catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}


	//通用非对称解密
	public static byte[]  asymDecrypt (byte[] cipher,byte[] privateKey) {


		if(Config.isUseGM()){
			//return SM2.decrypt(cipher,privateKey);
			return null;
		}else if(privateKey.length == 50){

			// softGM
			try{
				return  SM2Util.decrypt(privateKey, cipher);

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else{
			try {
				return Ecies.eciesDecrypt(cipher, privateKey);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 *  sm4 encrypt
	 * @param plainBytes
	 * @param password
	 * @return
	 */
	public static byte[] sm4Encrypt(byte[] plainBytes,byte[] password){

		try {
			return SM4Util.encrypt_ECB_Padding(password, plainBytes);

		} catch (Exception ex) {
			ex.printStackTrace();
			return  null;
		}
	}

	/**
	 * sm4 decrypt
	 * @param cipherText
	 * @param password
	 * @return
	 */
	public static byte[] sm4Decrypt(byte[] cipherText, byte[] password){

		try {
			return SM4Util.decrypt_ECB_Padding(password, cipherText);

		} catch (Exception ex) {
			ex.printStackTrace();
			return  null;
		}
	}


	public static byte[] symEncrypt(byte[] plainBytes,byte[] password,boolean bSM){
		if(Config.isUseGM()){
//			return SM4.encrypt(password, plainBytes);
			return null;
		}else if(bSM){
			return sm4Encrypt(plainBytes,password);
		}
		else{
			return Aes256.encrypt(plainBytes, password);
		}
	}

	//通用对称加密
	public static byte[] symEncrypt(byte[] plainBytes,byte[] password){
		if(Config.isUseGM()){
//			return SM4.encrypt(password, plainBytes);
			return null;
		}else{
			return Aes256.encrypt(plainBytes, password);
		}
	}

	//通用对称解密
	public static byte[] symDecrypt(byte[] cipherText, byte[] password){
		if(Config.isUseGM()){
//			return SM4.decrypt(password, cipherText);
			return null;
		}else{
			return Aes256.decrypt(cipherText, password);
		}
	}
	
	public static String symEncrypt(String plainBytes,String password){
		if(Config.isUseGM()){
//			return SM4.encrypt(password, plainBytes);
			return null;
		}else{
			return Util.bytesToHex(Aes256.encrypt(plainBytes.getBytes(), password.getBytes()));
		}
	}
	//通用对称解密
	public static String symDecrypt(String cipherText, String password){
		if(Config.isUseGM()){
//			return SM4.decrypt(password, cipherText);
			return null;
		}else{
			
			return new String(Aes256.decrypt(Util.hexToBytes(cipherText), password.getBytes()));
		}
	}
	
	//通用对称解密
		public static byte[] symDecrypt(byte[] cipherText, byte[] password, boolean bSM){
			if(bSM){
				return sm4Decrypt(cipherText, password);
			}else{
				
				return Aes256.decrypt(cipherText, password);
			}
		}
	
		
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
		//byte[] password = Util.getRandomBytes(AESKeyLength);
		byte[] password = "hello world".getBytes();
    	byte[] aesEnc = Aes256.encrypt(plainText.getBytes(), password);
    	
    	try {
    	   	//encrypt password
        	List<byte[]> listPubHash = new ArrayList<byte[]>();
        	List<byte[]> listPassCipher = new ArrayList<byte[]>();
        	for(String sPub : listPublicKey) {
        		byte[] pubBytes = getB58IdentiferCodecs().decodeAccountPublic(sPub);
        		byte[] pubHash = HashUtils.quarterSha512(pubBytes);
        		byte[] passCipher = EncryptCommon.asymEncrypt(password, pubBytes);
        		
        		System.out.println("加密后的密文为 : " + Util.bytesToHex(passCipher));
        		
        		listPubHash.add(pubHash);
        		listPassCipher.add(passCipher);
        	}

        	EncryptMsg.MultiEncrypt.Builder builder = EncryptMsg.MultiEncrypt.newBuilder();
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
			
			Seed seed = Seed.fromBase58(secret);
    		IKeyPair pair = seed.keyPair();
    		byte[] dataPubA = pair.pub().toByteArray();
    		byte[] pubHash = HashUtils.quarterSha512(dataPubA);
    		String sPubHash = Arrays.toString(pubHash);
    		
    		List<HashToken> listHashToken = msg.getHashTokenPairList();
			for(HashToken hashPair : listHashToken) {
				if(sPubHash.equals(Arrays.toString(hashPair.getPublicHash().toByteArray()))){
					password = EncryptCommon.asymDecrypt(hashPair.getToken().toByteArray(), seed.bytes());
					break;
				}
			}
			if(password != null) {
				byte[] plain = Aes256.decrypt(msg.getCipher().toByteArray(), password);
				return new String(plain);
			}
		}catch(InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		return "";
	}

}
