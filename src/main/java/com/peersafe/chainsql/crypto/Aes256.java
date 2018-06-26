package com.peersafe.chainsql.crypto;

import java.security.Security;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.peersafe.base.utils.Sha512;
import com.peersafe.chainsql.util.Util;  
  
public class Aes256 {  
  
    public static boolean initialized = false;  
      
	public static final int AESKeyLength = 32;
	public static final int IVLength = 16; // bytes
	
      
   public static byte[] crypt(byte[] bytes, byte[] key,boolean bEncrypt){  
		if(bytes.length == 0 || key.length == 0) {
			return null;
		}
       initialize();  
		try{
	        key = Util.paddingPass(key, AESKeyLength);
	        byte[] iv = new byte[IVLength];
	        System.arraycopy(key, 0, iv, 0, IVLength);
	        
	        //aes-256-cbc
	        ParametersWithIV keyWithIv = new ParametersWithIV(new KeyParameter(key), iv);
	        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
	        cipher.init(bEncrypt, keyWithIv);        
	        byte[] cryptedBytes  = new byte[cipher.getOutputSize(bytes.length)];
	        int length1 = cipher.processBytes(bytes, 0, bytes.length, cryptedBytes , 0);	        
	        int length2 = cipher.doFinal(cryptedBytes , length1);
	        byte[] finalBytes = cryptedBytes;
	        int finalLength = length1+length2;
	        if(!bEncrypt && finalLength != cryptedBytes.length) {
	        	finalBytes = new byte[finalLength];
	        	System.arraycopy(cryptedBytes, 0, finalBytes, 0, finalLength);
	        }
	        return finalBytes;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
   }  
 
   /**
    * 
    * @param plainBytes 要被加密的字符串 
    * @param key 加/解密要用的长度为32的字节数组（256位）密钥 
    * @return 加密后的字节数组 
    */
    public static byte[] encrypt(byte[] plainBytes, byte[] key){  
        return crypt(plainBytes,key,true);
    }  
      
    /**
     * 
     * @param cipherBytes 要被解密的字节数组 
     * @param key 加/解密要用的长度为32的字节数组（256位）密钥 
     * @return 解密后的字符串 
     */
    public static byte[] decrypt(byte[] cipherBytes, byte[] key){  
    	return crypt(cipherBytes,key,false);
    }  
      
    public static void initialize(){  
        if (initialized) return;  
        Security.addProvider(new BouncyCastleProvider());  
        initialized = true;  
    }
    
    public static void main(String[] args) {
    	String plainText = "hello,world";
    	String key = "abcdefg";
    	byte[] cipher = Aes256.encrypt(plainText.getBytes(), key.getBytes());
    	System.out.println(Util.bytesToHex(cipher));
    	byte[] decrypted = Aes256.decrypt(cipher, key.getBytes());
    	System.out.println(new String(decrypted));
    	
    }
}  
