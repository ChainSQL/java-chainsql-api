package com.peersafe.chainsql.crypto;

import com.peersafe.base.config.Config;
import com.peersafe.chainsql.util.Util;

public class EncryptCommon {
	//通用非对称加密
	public static byte[] asymEncrypt(byte[] plainBytes,byte[] publicKey){
		if(Config.isUseGM()){
//			return SM2.encrypt(plainBytes,publicKey);
			return null;
		}else{
			return Ecies.eciesEncrypt(plainBytes, publicKey);
		}
	}
	//通用非对称解密
	public static byte[]  asymDecrypt (byte[] cipher,byte[] privateKey) {
		if(Config.isUseGM()){
			//return SM2.decrypt(cipher,privateKey);
			return null;
		}else{
			try {
				return Ecies.eciesDecrypt(cipher, privateKey);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
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
}
