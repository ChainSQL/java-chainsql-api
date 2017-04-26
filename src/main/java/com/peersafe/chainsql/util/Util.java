package com.peersafe.chainsql.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class Util {
	private static String hexString = "0123456789ABCDEF";

	public static String StrToJsonStr(String args) {
		JSONObject a = new JSONObject(args);
		return a.toString();
	}
  
    public static JSONObject StrToJson(String args) {
		JSONObject a = new JSONObject(args);
        return a;
    }
    
	public static void checkinsert(List<JSONObject> strraw) throws Exception{
			boolean isHavePk = false;
			for (int i = 0; i < strraw.size(); i++) {	
			JSONObject json = strraw.get(i);
			String field, type;
	    	try {
				field =json.getString("field");
				type = json.getString("type");
			} catch (Exception e) {
				throw new Exception("Raw must have  field and type");
				// TODO: handle exception
			}

    		if (field==null || type==null) {
    			throw new Exception("field and type cannot be empty");
    		}
            if("int".equals(type)){

            }else if("float".equals(type)){

            }else if("double".equals(type)){

            }else if("decimal".equals(type)){

            }else if("varchar".equals(type)){
            	try {
    				int length = (int) json.get("length");
    			} catch (Exception e) {
    				throw new Exception(" The type varchar must have length");
    			}
            }else if("blob".equals(type)){

            }else if("text".equals(type)){

            }else if("datatime".equals(type)){

            }else{
            	throw new Exception("invalid type "+type);
            }
            try {
            	int PK =(int) json.get("PK");
            	if (isHavePk) {
    				throw new Exception("the table only have a PK");
    			}
            	isHavePk = true;
			} catch (Exception e) {
				//throw new Exception("Raw must have  field and type");
				//e.printStackTrace();
			}
    		
		}
	}
	
	public static String bytesToHex(byte[] bytes) {
		return encode(bytes);
	}
	public static byte[] hexToBytes(String bytes){
	    ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
	    //将每2位16进制整数组装成一个字节
	    for(int i=0;i<bytes.length();i+=2)
	    	baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1))));
	    return baos.toByteArray();
	}
	
	public static String toHexString(String s){
		String str = encode(s.getBytes());
		return str;
	} 
	public static String fromHexString(String s){
		return decode(s);
	}
	 
	public static String encode(byte[] bytes){
		StringBuilder sb=new StringBuilder(bytes.length*2);
		//将字节数组中每个字节拆解成2位16进制整数
	    for(int i=0;i<bytes.length;i++)
	    {
		    sb.append(hexString.charAt((bytes[i]&0xf0)>>4));
		    sb.append(hexString.charAt((bytes[i]&0x0f)>>0));
	    }
	    return sb.toString();
	}
	/*
	* 将16进制数字解码成字符串,适用于所有字符（包括中文）
	*/
	public static String decode(String bytes){
	    ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
	    //将每2位16进制整数组装成一个字节
	    for(int i=0;i<bytes.length();i+=2)
	    	baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1))));
	    return new String(baos.toByteArray());
	} 
}
