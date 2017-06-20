package com.peersafe.chainsql.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;


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
     * Check fields
     * @param strraw Raw data list.
     * @throws Exception Throws when exception occur.
     */
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
    				int length = (int) json.getInt("length");
    			} catch (Exception e) {
    				throw new Exception(" The type varchar must have length");
    			}
            }else if("blob".equals(type)){

            }else if("text".equals(type)){

            }else if("datetime".equals(type)){

            }else{
            	throw new Exception("invalid type "+type);
            }
            try {
            	int PK =(int) json.getInt("PK");
            	if(PK == 1){
            		if (isHavePk) {
            			throw new Exception("the table only have a PK");
            		}
            		isHavePk = true;
            	}
			} catch (Exception e) {
				//throw new Exception("Raw must have  field and type");
				//e.printStackTrace();
			}
		}
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
		if(tx.has("Raw")){
			tx.put("Raw", fromHexString(tx.getString("Raw")));
		}
		if(tx.has("Tables")){
			JSONObject table = (JSONObject)tx.getJSONArray("Tables").get(0);
			table = table.getJSONObject("Table");
			table.put("TableName", fromHexString(table.getString("TableName")));
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
}
