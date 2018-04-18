package com.peersafe.chainsql.crypto;

import com.peersafe.base.config.Config;

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
			return Aes128.encrypt(password, plainBytes);
		}
	}
	//通用对称解密
	public static byte[] symDecrypt(byte[] cipherText, byte[] password){
		if(Config.isUseGM()){
//			return SM4.decrypt(password, cipherText);
			return null;
		}else{
			return Aes128.decrypt(cipherText, password);
		}
	}
}
