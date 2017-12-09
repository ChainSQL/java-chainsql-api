package com.peersafe.base.crypto.sm;

import cn.com.sansec.key.SWJAPI;
import cn.com.sansec.key.exception.SDKeyException;

public class SM2 {
	public static byte[] encrypt (byte[] plainText,byte[] publickey){
		try {
			SWJAPI sdkey = SMDevice.sdkey;
			if(sdkey == null)
				return null;
			byte[] cipher = null;
			if(publickey == null)
				cipher = sdkey.EccEncrypt(SMDevice.getContainerName(), 1, plainText);
			else{
				byte[] pub2 = new byte[64];
				System.arraycopy(publickey, 1, pub2, 0, 64);
				cipher =  sdkey.ExtEccEncrypt(pub2, plainText);
			}
			return cipher == null ? null : bytes2CipherBytes(cipher);
		} catch (SDKeyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] decrypt(byte[] cipher,byte[] privatekey){
		try {
			SWJAPI sdkey = SMDevice.sdkey;
			if(sdkey == null)
				return null;
			if(privatekey == null)
				return sdkey.EccDecrypt(SMDevice.getContainerName(), 1, cipherBytes2Bytes(cipher));
			else
				return sdkey.ExtEccDecrypt(privatekey, cipherBytes2Bytes(cipher));
		} catch (SDKeyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**  
	    * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用 
	    * @param value  
	    *            要转换的int值 
	    * @return byte数组 
	    */
	public static byte[] intToBytes( int value )   
	{   
	    byte[] src = new byte[4];  
	    src[3] =  (byte) ((value>>24) & 0xFF);  
	    src[2] =  (byte) ((value>>16) & 0xFF);  
	    src[1] =  (byte) ((value>>8) & 0xFF);    
	    src[0] =  (byte) (value & 0xFF);                  
	    return src;   
	}  
	/**  
	    * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用 
	    *   
	    * @param src  
	    *            byte数组  
	    * @return int数值  
	    */    
	public static int bytesToInt(byte[] src) {  
	    int value;    
	    value = (int) ((src[0] & 0xFF)   
	            | ((src[1] & 0xFF)<<8)   
	            | ((src[2] & 0xFF)<<16)   
	            | ((src[3] & 0xFF)<<24));  
	    return value;  
	}  
	public static byte[] bytes2CipherBytes(byte[] cipherBytes1){
		int cipherLength = cipherBytes1.length - 96;
		byte[] finalBytes = new byte[236];
		System.arraycopy(intToBytes(cipherLength), 0, finalBytes, 0, 4);
		System.arraycopy(cipherBytes1,0 , finalBytes, 4, 32);
		System.arraycopy(cipherBytes1, 32, finalBytes, 4+32, 32);
		System.arraycopy(cipherBytes1, 96, finalBytes, 4+64, cipherBytes1.length - 96);
		System.arraycopy(cipherBytes1, 64, finalBytes, 204, 32);
		return finalBytes;
	}
	
	public static byte[] cipherBytes2Bytes(byte[] cipherBytes){
		byte[] cbytes = new byte[4];
		System.arraycopy(cipherBytes, 0, cbytes, 0, 4);
		int clength = bytesToInt(cbytes);
		byte[] finalBytes = new byte[96 + clength];
		System.arraycopy(cipherBytes, 4, finalBytes, 0, 32);
		System.arraycopy(cipherBytes, 4+32, finalBytes, 32, 32);
		System.arraycopy(cipherBytes, 204, finalBytes, 64, 32);
		System.arraycopy(cipherBytes, 4+64, finalBytes, 96, clength);
		return finalBytes;
	}
}
