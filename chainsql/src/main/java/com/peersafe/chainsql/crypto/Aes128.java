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

public class Aes128 {
	private static final String KEY_ALGORITHM = "AES";  
	
    //转化成JAVA的密钥格式  
	private static Key convertToKey(byte[] keyBytes) throws Exception{  
        SecretKey secretKey = new SecretKeySpec(keyBytes,KEY_ALGORITHM);  
        return secretKey;  
    }  
    
    //生成iv  
	private static AlgorithmParameters generateIV(byte[] pass) throws Exception{  
        //iv 为一个 16 字节的数组，这里采用和 iOS 端一样的构造方法，数据全为0  
        AlgorithmParameters params = AlgorithmParameters.getInstance(KEY_ALGORITHM);  
        byte[] iv = new byte[16];
        for(int i=0; i<16; i++)
        {
        	iv[i] = pass[i];
        }
        params.init(new IvParameterSpec(iv));  
        return params;  
    } 
    
	/** 
	 * 加密 
	 *  
	 * @param byteContent 需要加密的内容 
	 * @param password  加密密码 
	 * @return 加密结果
	 */  
	public static byte[] encrypt(byte[] byteContent, byte[] password) {  
		if(password.length == 0 || byteContent.length == 0) {
			return null;
		}
        try {             
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器  
            password = Util.paddingPass(password,16);
            Key key = convertToKey(password);
            AlgorithmParameters algo = generateIV(password);
            cipher.init(Cipher.ENCRYPT_MODE, key, algo);// 初始化  
            byte[] result = cipher.doFinal(byteContent);  
            return result; // 加密  
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
        return null;  
	}

    
	/**解密 
	 * @param content  待解密内容 
	 * @param password 解密密钥 
	 * @return 解密结果
	 */  
	public static byte[] decrypt(byte[] content, byte[] password) {
		if(password.length == 0 || content.length == 0) {
			return null;
		}
		
        try {
            password = Util.paddingPass(password,16);              
            Key key = convertToKey(password);
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
