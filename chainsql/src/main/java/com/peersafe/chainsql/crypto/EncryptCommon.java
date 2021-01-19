package com.peersafe.chainsql.crypto;

import com.peersafe.base.config.Config;
import com.peersafe.base.crypto.sm.BCECUtil;
import com.peersafe.base.crypto.sm.SM2Util;
import com.peersafe.base.crypto.sm.SM4Util;
import com.peersafe.chainsql.util.Util;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;


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
}
