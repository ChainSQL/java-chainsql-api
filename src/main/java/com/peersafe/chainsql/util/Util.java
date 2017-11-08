package com.peersafe.chainsql.util;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.encodings.B58IdentiferCodecs;
import com.peersafe.chainsql.crypto.EncryptCommon;
import com.peersafe.chainsql.net.Connection;


public class Util {
	private static String hexString = "0123456789ABCDEF";

	/**
	 * @param args String.
	 * @return JSON String.
	 */
	public static String StrToJsonStr(String args) {
		JSONObject a = new JSONObject(args);
		return a.toString();
	}
  
	/**
	 * Transfer a json-str to JSONObject.
	 * @param args String of JSON.
	 * @return JSONObject.
	 */
    public static JSONObject StrToJson(String args) {
		JSONObject a = new JSONObject(args);
        return a;
    }
    
    /**
     * Transfer a JSON-str List to a JSONObject List.
     * @param list JSON String list.
     * @return JSONObject list.
     */
    public static List<JSONObject> ListToJsonList(List<String> list){
    	List<JSONObject> listJson = new ArrayList<JSONObject>();
		for (String s : list) {
			JSONObject json = Util.StrToJson(s);
			listJson.add(json);
		}
		return listJson;
    }
    
    /**
     * JSON-Str to JSONArray
     * @param str JSON String
     * @return JSON array.
     */
    public static JSONArray strToJSONArray(String str){
		JSONArray array = new JSONArray();
		array.put(new JSONObject(str));
		return array;
    }
    
    /**
     * Get random byte array.
     * @param length Random array length.
     * @return Byte array.
     */
    public static byte[] getRandomBytes(int length){
    	byte[] bytes = new byte[length];
    	Random r = new Random();
    	r.nextBytes(bytes);
    	return bytes;
    }
    
	/**
	 * Transfer byte array to Hex String
	 * @param bytes Byte array to be hexed.
	 * @return Hexed String.
	 */
	public static String bytesToHex(byte[] bytes) {
		return encode(bytes);
	}
	
	/**
	 * Hex String to byte array.
	 * @param bytes hexString
	 * @return Hexed byte array.
	 */
	public static byte[] hexToBytes(String bytes){
	    ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
	    //将每2位16进制整数组装成一个字节
	    for(int i=0;i<bytes.length();i+=2)
	    	baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1))));
	    return baos.toByteArray();
	}
	
	/**
	 * String to HexString
	 * @param s String to be hexed.
	 * @return String hexed.
	 */
	public static String toHexString(String s){
		String str = encode(s.getBytes());
		return str;
	}
	
	/**
	 * Transfer from HexString to String
	 * @param s Hex String.
	 * @return Unhexed String.
	 */
	public static String fromHexString(String s){
		return decode(s);
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
	/*
	* 将16进制数字解码成字符串,适用于所有字符（包括中文）
	*/
	private static String decode(String bytes){
	    ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
	    //将每2位16进制整数组装成一个字节
	    for(int i=0;i<bytes.length();i+=2)
	    	baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1))));
	    return new String(baos.toByteArray());
	} 
	
//	public static List array(Object val0, Object... vals){
//	 	List res = new ArrayList();
//	 	if(val0.getClass().isArray()){
//	 		String[] a = (String[]) val0; 
//	 		for(String s:a){
//	 			res.add(s);
//	 		}
//	 	}else{
//	 		  res.add(val0);
//		      res.addAll(Arrays.asList(vals));
//	 	}
//        return res;
//	}
	/**
	 * String Parameters to List of String
	 * @param val0 String 
	 * @param vals String
	 * @return List of String
	 */
	public static List<String> array(String val0, String... vals){
	 	List<String> res = new ArrayList<String>();
	 	res.add(val0);
	 	res.addAll(Arrays.asList(vals));

        return res;
	}
	
	/**
	 * unhex some fields
	 * @param tx JSONObject to be unhexed.
	 */
	public static void unHexData(JSONObject tx){
		String sTableName = "";
		if(tx.has("Tables")){
			JSONObject table = (JSONObject)tx.getJSONArray("Tables").get(0);
			table = table.getJSONObject("Table");
			sTableName = fromHexString(table.getString("TableName"));
			table.put("TableName", sTableName);
			if(table.has("TableNewName")){
				table.put("TableNewName", fromHexString(table.getString("TableNewName")));
			}
		}
		if(tx.has("Raw")){
			System.out.println("before decrypt:" + tx.getString("Raw"));
			String sRaw = fromHexString(tx.getString("Raw"));		
			tx.put("Raw", sRaw);
		}

		if(tx.has("Statements")){
			tx.put("Statements", fromHexString(tx.getString("Statements")));
		}
		if(tx.has("OperationRule")){
			tx.put("OperationRule", fromHexString(tx.getString("OperationRule")));
		}
	}
	/**
	 * unhex some fields
	 * @param tx JSONObject to be unhexed.
	 */
	public static void decryptData(byte[] pass,JSONObject tx){
		String sTableName = "";
		if(tx.has("Tables")){
			JSONObject table = (JSONObject)tx.getJSONArray("Tables").get(0);
			table = table.getJSONObject("Table");
			sTableName = fromHexString(table.getString("TableName"));
			table.put("TableName", sTableName);
			if(table.has("TableNewName")){
				table.put("TableNewName", fromHexString(table.getString("TableNewName")));
			}
		}
		if(tx.has("Raw")){
			String sRaw = tx.getString("Raw");
			if(pass != null) {
				byte[] rawBytes = hexToBytes(sRaw);
				rawBytes = EncryptCommon.symDecrypt(rawBytes, pass);
				sRaw = new String(rawBytes);
			}else {
				sRaw = fromHexString(sRaw);
			}
			tx.put("Raw", sRaw);
		}
		
		if(tx.has("Statements")){
			String sStatement = fromHexString(tx.getString("Statements"));
			JSONArray statement = new JSONArray(sStatement);
			for(int i=0; i<statement.length(); i++) {
				JSONObject obj = statement.getJSONObject(i);
				decryptData(pass,obj);
			}
			tx.put("Statements", statement);
		}
		if(tx.has("OperationRule")){
			tx.put("OperationRule", fromHexString(tx.getString("OperationRule")));
		}
	}
	/**
	 * Wait for 50 milliseconds.
	 */
	public static void waiting(){
      	try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static JSONObject successObject(){
		JSONObject obj = new JSONObject();
		obj.put("status", "success");
		return obj;
	}
	
	public static JSONObject errorObject(String errMsg){
		JSONObject obj = new JSONObject();
		obj.put("status", "error");
		obj.put("error_message", errMsg);
		return obj;
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
	    * @param offset  
	    *            从数组的第offset位开始  
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
}
