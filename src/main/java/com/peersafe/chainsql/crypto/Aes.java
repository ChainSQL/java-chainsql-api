package com.peersafe.chainsql.crypto;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.peersafe.chainsql.util.Util;

public class Aes {
	public static final String KEY_ALGORITHM = "AES";  
	private static final String hexString = "0123456789ABCDEF";
	
	public static String aesEncrypt(String password,String content){
		return encrypt(password,content);
	}
	
	public static byte[] aesDecrypt(String password,String encryptedHex){
		return decrypt(Util.hexToBytes(encryptedHex),password);
	}
	
    //转化成JAVA的密钥格式  
	private static Key convertToKey(byte[] keyBytes) throws Exception{  
        SecretKey secretKey = new SecretKeySpec(keyBytes,KEY_ALGORITHM);  
        return secretKey;  
    }  
    
    //生成iv  
	private static AlgorithmParameters generateIV(String pass) throws Exception{  
        //iv 为一个 16 字节的数组，这里采用和 iOS 端一样的构造方法，数据全为0  
        AlgorithmParameters params = AlgorithmParameters.getInstance(KEY_ALGORITHM);  
        params.init(new IvParameterSpec(pass.getBytes()));  
        return params;  
    } 
    
	/** 
	 * 加密 
	 *  
	 * @param content 需要加密的内容 
	 * @param password  加密密码 
	 * @return 
	 */  
	private static String encrypt(String password,String content) {  
        try {             
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器  
            byte[] byteContent = content.getBytes("utf-8"); 
            Key key = convertToKey(password.getBytes());
            AlgorithmParameters algo = generateIV(password);
            cipher.init(Cipher.ENCRYPT_MODE, key, algo);// 初始化  
            byte[] result = cipher.doFinal(byteContent);  
            return Util.bytesToHex(result); // 加密  
        } catch (NoSuchAlgorithmException e) {  
                e.printStackTrace();  
        } catch (NoSuchPaddingException e) {  
                e.printStackTrace();  
        } catch (InvalidKeyException e) {  
                e.printStackTrace();  
        } catch (UnsupportedEncodingException e) {  
                e.printStackTrace();  
        } catch (IllegalBlockSizeException e) {  
                e.printStackTrace();  
        } catch (BadPaddingException e) {  
                e.printStackTrace();  
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        return "";  
	}

    
	/**解密 
	 * @param content  待解密内容 
	 * @param password 解密密钥 
	 * @return 
	 */  
	private static byte[] decrypt(byte[] content, String password) {  
        try {              
            Key key = convertToKey(password.getBytes());
            AlgorithmParameters algo = generateIV(password);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器  
            cipher.init(Cipher.DECRYPT_MODE, key,algo);// 初始化  
            byte[] result = cipher.doFinal(content);  
            return result; // 解密  
        } catch (NoSuchAlgorithmException e) {  
                e.printStackTrace();  
        } catch (NoSuchPaddingException e) {  
                e.printStackTrace();  
        } catch (InvalidKeyException e) {  
                e.printStackTrace();  
        } catch (IllegalBlockSizeException e) {  
                e.printStackTrace();  
        } catch (BadPaddingException e) {  
                e.printStackTrace();  
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        return null;  
	}  
}
