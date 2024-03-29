package com.peersafe.chainsql.util;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.peersafe.base.crypto.sm.SMKeyPair;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.K256KeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.encodings.B58IdentiferCodecs;
import com.peersafe.base.utils.Utils;
import com.peersafe.chainsql.crypto.EncryptCommon;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.base.encodings.common.B16;


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
     * Transform list to JSONArray
     * @param accounts accounts
     * @return JSONArray object
     */
    public static  JSONArray listToJSONArray(List<String> accounts) {
    	JSONArray arr = new JSONArray();
    	for(String obj:accounts) {
    		arr.put(obj);
    	}    	
    	return arr;
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
        return B16.decode(bytes);
	    // ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
	    // //将每2位16进制整数组装成一个字节
	    // for(int i=0;i<bytes.length();i+=2)
	    // 	baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1))));
	    // return baos.toByteArray();
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
	
	public static byte[] paddingPass(byte[] password,int keyLength){
		if(password.length == keyLength)
			return password;
		byte[] retByte = new byte[keyLength];
		if(password.length < keyLength){
			byte byteToPad = (byte) (keyLength - password.length);
			for(int i=0; i<keyLength; i++){
				if(i<password.length)
					retByte[i] = password[i];
				else
					retByte[i] = byteToPad;
			}
		}else{
			System.arraycopy(password, 0, retByte, 0, keyLength);
		}
		return retByte;
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
	 * @param pass Secret used to decrypt
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
		obj.put("error", "internalError");
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
	    src[0] =  (byte) ((value>>24) & 0xFF);  
	    src[1] =  (byte) ((value>>16) & 0xFF);  
	    src[2] =  (byte) ((value>>8) & 0xFF);    
	    src[3] =  (byte) (value & 0xFF);                  
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
	    value = (int) ((src[3] & 0xFF)   
	            | ((src[2] & 0xFF)<<8)   
	            | ((src[1] & 0xFF)<<16)   
	            | ((src[0] & 0xFF)<<24));  
	    return value;  
	} 
	
	public static boolean isChainsqlType(TransactionType type) {
		if(type == TransactionType.TableListSet || 
		   type == TransactionType.SQLStatement || 
		   type == TransactionType.SQLTransaction ||
		   type == TransactionType.Contract) {
			return true;
		}
		return false;
	}


	/**
	 *   获取交易的额外的费用
	 *
	 * @param json
	 *            交易json
	 * @param drops_per_byte
	 *		每字节消耗多少drops
	 * @param type
	 * 	   交易类别
	 * @return  额外的费用
	 */
	public static Amount getExtraFee(JSONObject json,int drops_per_byte,TransactionType type) {
	   	if(isChainsqlType(type)) {
    		int zxcDrops = 1000;

    		if(json.has("Raw")) {
        		String rawHex = json.getString("Raw");
        		int rawSize = rawHex.length()/2;
				zxcDrops += rawSize * drops_per_byte;
    		}else if(json.has("Statements")) {
    			String statementsHex = json.getString("Statements");
    			int stateSize = statementsHex.length()/2;
				zxcDrops +=  stateSize * drops_per_byte;
    		}
    		return Amount.fromString(String.valueOf( zxcDrops));
    	}else {
    		return Amount.fromString("0");
    	}
	}
	
	public static String getNewAccountFromTx(JSONObject tx) {
    	JSONArray nodes = tx.getJSONObject("meta").getJSONArray("AffectedNodes");
    	List<String> listAddr = new ArrayList<String>();
    	for(int i=0; i<nodes.length(); i++) {
    		JSONObject node = nodes.getJSONObject(i);
    		if(node.has("CreatedNode")) {
    			if(node.getJSONObject("CreatedNode").getJSONObject("NewFields").has("ContractCode")) {
    				listAddr.add(node.getJSONObject("CreatedNode").getJSONObject("NewFields").getString("Account")); 
    			}
    		}
    	}
    	if(listAddr.size() == 1) {
    		return listAddr.get(0);
    	}else {
    		AccountID account = AccountID.fromAddress(tx.getString("Account"));
    		byte[] account_bytes = account.toBytes();
 
    		byte[] accountBytes = new byte[account_bytes.length + 4];
    		System.arraycopy(account_bytes, 0, accountBytes, 0, account_bytes.length);
    		byte[] sequence = intToBytes(tx.getInt("Sequence"));
    		System.arraycopy(sequence, 0, accountBytes, account_bytes.length, 4);
    		
			SHA256Digest sha = new SHA256Digest();
			sha.update(accountBytes, 0, accountBytes.length);
		    byte[] result = new byte[sha.getDigestSize()];
		    sha.doFinal(result, 0);
		    
    		byte[] o;
			RIPEMD160Digest d = new RIPEMD160Digest();
		    d.update (result, 0, result.length);
		    o = new byte[d.getDigestSize()];
		    d.doFinal (o, 0);
		    
		    String address = getB58IdentiferCodecs().encodeAddress(o);		    
		    for(String addr : listAddr) {
		    	if(addr.equals(address)) {
		    		return addr;
		    	}
		    }
		    return null;
    	}
	}
	
	/**
	 * 签名接口
	 * @param message 要签名的内容
	 * @param secret 签名私钥
	 * @return 签名
	 */
	public static byte[] sign(byte[] message,String secret) {
		IKeyPair keyPair = Seed.getKeyPair(secret);
		return keyPair.signMessage(message);
	}
	/**
	 * 验证签名接口
	 * @param message 被签名的内容
	 * @param signature 签名
	 * @param publicKey 签名公钥
	 * @return 是否验签成功
	 */
	public static boolean verify(byte[] message,byte[] signature,String publicKey) {
		byte[] pubBytes = null;
		if(publicKey.length() == 66) {
			pubBytes = Util.hexToBytes(publicKey);
		}else {
			pubBytes = getB58IdentiferCodecs().decode(publicKey, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
		}

		if(pubBytes.length == 65 && pubBytes[0] == 0x47){
			// 软国密
			SMKeyPair smKeyPair = new SMKeyPair(null,null,Utils.uBigInt(pubBytes));
			return smKeyPair.verifySignature(message, signature);
		}

        
        K256KeyPair keyPair = new K256KeyPair(null,Utils.uBigInt(pubBytes));
        return keyPair.verifySignature(message, signature);
	}
	
	public static String getPublicHexFromSecret(String secret) {
		Seed seed = Seed.fromBase58(secret);
		IKeyPair keyPair = seed.keyPair();
		byte[] pubBytes = keyPair.canonicalPubBytes();
		return bytesToHex(pubBytes);
	}
	
	public static String getUserToken(Connection connection,String address,String name) throws Exception{
		JSONObject res = connection.client.getUserToken(connection.scope,address,name);
		String token = "";
		if(res.has("error")){
			throw new Exception(res.getString("error_message"));
		}else if(res.has("token")) {
			token = res.getString("token");
		}
		return token;
	}
	
	public static String encryptRaw(Connection connection,String token,String strRaw) throws Exception{
		if(token.equals("")) {
			strRaw = Util.toHexString(strRaw);
			return strRaw;
		}
		try {
            String password = asymDec(token, connection.secret);
			boolean bSoftGM = Utils.getAlgType(connection.secret).equals(Define.algType.gmalg);
			if(password == null){
				throw new Exception("Exception: decrypt token failed");
			}
			byte[] rawBytes = EncryptCommon.symEncrypt( strRaw.getBytes(), password.getBytes(), bSoftGM);
			strRaw = Util.bytesToHex(rawBytes);
			return strRaw;
		} catch (Exception e) {
			e.printStackTrace();
			return strRaw;
		}
	}

    /**
	 * 非对称解密接口
	 * @param cipher 密文
	 * @param privateKey 加密密钥
	 * @return 明文，解密失败返回""
	 */
	public static String asymDec(String cipher, String privateKey) {
		byte[] cipherBytes = Util.hexToBytes(cipher);
		byte[] seedBytes = null;
        Define.algType priAlgType = Utils.getAlgType(privateKey);
        switch(priAlgType) {
            case gmalg:
                seedBytes   = getB58IdentiferCodecs().decodeAccountPrivate(privateKey);
                break;
            case secp256k1:
                seedBytes = getB58IdentiferCodecs().decodeFamilySeed(privateKey);
                break;
            default:
                return new String("");
        }
        byte[] plainBytes = EncryptCommon.asymDecrypt(cipherBytes, seedBytes, 
                                                        priAlgType.equals(Define.algType.gmalg));
		return new String(plainBytes);
	}
}
