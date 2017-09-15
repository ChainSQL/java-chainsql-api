package com.peersafe.chainsql.crypto;

import com.peersafe.base.config.Config;
import com.peersafe.base.crypto.sm.SM2;
import com.peersafe.base.crypto.sm.SM4;

public class EncryptCommon {
	//通用非对称加密
	public static byte[] asymEncrypt(byte[] plainBytes,byte[] publicKey){
		if(Config.isUseGM()){
			return SM2.encrypt(plainBytes,publicKey);
		}else{
			return Ecies.eciesEncrypt(plainBytes, publicKey);
		}
	}
	//通用非对称解密
	public static byte[]  asymDecrypt (byte[] cipher,byte[] privateKey) {
		if(Config.isUseGM()){
			return SM2.decrypt(cipher,privateKey);
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
			return SM4.encrypt(password, plainBytes);
		}else{
			return Aes.encrypt(password, plainBytes);
		}
	}
	//通用对称解密
	public static byte[] symDecrypt(byte[] cipherText, byte[] password){
		if(Config.isUseGM()){
			return SM4.decrypt(password, cipherText);
		}else{
			return Aes.decrypt(cipherText, password);
		}
	}
}
