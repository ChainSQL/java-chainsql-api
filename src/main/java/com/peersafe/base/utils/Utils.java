package com.peersafe.base.utils;

import java.lang.reflect.Method;
import java.math.BigInteger;

import com.peersafe.base.encodings.common.B16;

public class Utils {
    public static String bigHex(BigInteger bn) {
        return B16.toStringTrimmed(bn.toByteArray());
    }
    public static BigInteger uBigInt(byte[] bytes) {
        return new BigInteger(1, bytes);
    }
    
	/*** 
     * encode by Base64 
     */  
    public static String encodeBase64(byte[]input) throws Exception{  
		Class clazz = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method mainMethod = clazz.getMethod("encode", byte[].class);
		mainMethod.setAccessible(true);
		Object retObj = mainMethod.invoke(null, new Object[] { input });
		String result = (String) retObj;
		
		result = result.replace("+", "-");
		result = result.replace("/", "_");
		result = result.replace("=", "");
		return result;
    }  
    /*** 
     * decode by Base64 
     */  
    public static byte[] decodeBase64(String input) throws Exception{ 
    	input = input.replace( "-", "+");
    	input = input.replace("_", "/");
    	
		Class clazz = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method mainMethod = clazz.getMethod("decode", String.class);
		mainMethod.setAccessible(true);
		Object retObj = mainMethod.invoke(null, input);
		return (byte[]) retObj;
    } 
}
