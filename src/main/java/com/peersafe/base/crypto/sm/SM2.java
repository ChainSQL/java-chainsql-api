package com.peersafe.base.crypto.sm;

import cn.com.sansec.key.SWJAPI;
import cn.com.sansec.key.exception.SDKeyException;

import com.peersafe.base.config.Config;

public class SM2 {
	public static byte[] encrypt (byte[] plainText){
		try {
			SWJAPI sdkey = SMDevice.sdkey;
			if(sdkey == null)
				return null;
			return sdkey.EccEncrypt(SMDevice.getContainerName(), 1, plainText);
		} catch (SDKeyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] decrypt(byte[] cipher){
		try {
			SWJAPI sdkey = SMDevice.sdkey;
			if(sdkey == null)
				return null;
			return sdkey.EccDecrypt(SMDevice.getContainerName(), 1, cipher);
		} catch (SDKeyException e) {
			e.printStackTrace();
			return null;
		}
	}
}
