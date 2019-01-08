package com.peersafe.account;

public class Utils {
	private static String hexString = "0123456789ABCDEF";
	
	/**
	 * Transfer byte array to Hex String
	 * @param bytes Byte array to be hexed.
	 * @return Hexed String.
	 */
	public static String bytesToHex(byte[] bytes) {
		return encode(bytes);
	}
	
	private static String encode(byte[] bytes){
		StringBuilder sb=new StringBuilder(bytes.length*2);
		//将字节数组中每个字节拆解成2位16进制整数
	    for(int i=0;i<bytes.length;i++)
	    {
		    sb.append(hexString.charAt((bytes[i]&0xf0)>>4));
		    sb.append(hexString.charAt((bytes[i]&0x0f)>>0));
	    }
	    return sb.toString();
	}
}
