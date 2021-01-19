package com.peersafe.base.utils;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.peersafe.base.encodings.common.B16;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

public class Utils {
    public static String bigHex(BigInteger bn) {
        return B16.toStringTrimmed(bn.toByteArray());
    }
    public static BigInteger uBigInt(byte[] bytes) {
        return new BigInteger(1, bytes);
    }

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

    public static byte[] decodeBase64(String input) throws Exception{ 
    	input = input.replace( "-", "+");
    	input = input.replace("_", "/");
    	
		Class clazz = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method mainMethod = clazz.getMethod("decode", String.class);
		mainMethod.setAccessible(true);
		Object retObj = mainMethod.invoke(null, input);
		return (byte[]) retObj;
    }

    public  static  String deriveAddressFromBytes(byte[] pubBytes){

    	// 区分是sm3 还是 sha256

		byte[] o;
		{
			SHA256Digest sha = new SHA256Digest();
			sha.update(pubBytes, 0, pubBytes.length);
			byte[] result = new byte[sha.getDigestSize()];
			sha.doFinal(result, 0);

			RIPEMD160Digest d = new RIPEMD160Digest();
			d.update (result, 0, result.length);
			o = new byte[d.getDigestSize()];
			d.doFinal (o, 0);
		}

		String address   = getB58IdentiferCodecs().encodeAddress(o);

    	return address;
	}


	public static String getAlgType(String secret){

		String regEx = "^[a-zA-Z1-9]{51,51}";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(secret);

    	if(matcher.matches()){

    		return "softGMAlg";
		}else{
    		return "secp256k1";
		}
	}
}
