package com.peersafe.base.crypto.sm;

public class SM4 {
//	public static byte[] encrypt (byte[] key,byte[] plainText){
//		try {
//			SWJAPI sdkey = SMDevice.sdkey;
//			if(sdkey == null)
//				return null;
//			byte[] iv = new byte[16];
//			pkcs5Padding(plainText);
//			return sdkey.SymEncrypt(6, SWJAPI.ALG_MOD_ECB, key, iv, plainText);
//		} catch (SDKeyException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//	
//	public static byte[] decrypt (byte[] key,byte[] cipherText){
//		try {
//			SWJAPI sdkey = SMDevice.sdkey;
//			if(sdkey == null)
//				return null;
//			byte[] iv = new byte[16];
//			byte[] plainText = sdkey.SymDecrypt(6, SWJAPI.ALG_MOD_ECB, key, iv, cipherText);
//			//return dePkcs5Padding(plainText);
//			return plainText;
//		} catch (SDKeyException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//	
//	public static byte[] pkcs5Padding(byte[] srcUC){
//			byte paddingData = (byte)(16 - srcUC.length % 16);
//		    byte[] byteRet = new byte[srcUC.length + paddingData];
//		    System.arraycopy(srcUC, 0, byteRet, 0, srcUC.length);
//		    for (int i = 0; i < paddingData; ++i)
//		    {
//		    	byteRet[srcUC.length + i] = paddingData;
//		    }
//		    return byteRet;
//	}
//	
//	public static byte[] dePkcs5Padding(byte[] srcUC){		
//		int dePaddingData = srcUC[srcUC.length - 1];
//		byte[] byteRet = new byte[srcUC.length - dePaddingData];
//		System.arraycopy(srcUC, 0, byteRet, 0, byteRet.length);
//		return byteRet;
//	}
}
