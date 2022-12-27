package com.peersafe.chainsql.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import com.peersafe.chainsql.util.Util;

import junit.framework.TestCase;

public class CryptoTest extends TestCase {

    public static final Chainsql  c    = new Chainsql();
    public static String sTableName    = "T1";
    public static String smRootSecret  = "p97evg5Rht7ZB7DbEpVqmV3yiSBMxR3pRBKJyLcRWt7SL5gEeBb";
    public static String smRootAddress = "zN7TwUjJ899xcvNXZkNJ8eFFv2VLKdESsj";
    public static String smUserSecret  = "pw5MLePoMLs1DA8y7CgRZWw6NfHik7ZARg8Wp2pr44vVKrpSeUV";
    public static String smUserAddress = "zKzpkRTZPtsaQ733G8aRRG5x5Z2bTqhGbt";

    public static String smUserPublicKey =  "pYvKjFb71Qrx26jpfMPAkpN1zfr5WTQoHCpsEtE98ZrBCv2EoxEs4rmWR7DcqTwSwEY81opTgL7pzZ2rZ3948vHi4H23vnY3";


    public static String userSecret = "xnnUqirFepEKzVdsoBKkMf577upwT";
    public static String userAddress = "zpMZ2H58HFPB5QTycMGWSXUeF47eA8jyd4";
    public static String userPublicKey = "cB4pxq1LUfwPxNP9Xyj223mqM8zfeW6t2DqP1Ek3UQWaUVb9ciCZ";
    public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
    public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";

    public void testCrypto(){

        try{

            String plainText = "hello world";
            List<String> pubList = new ArrayList<>();
            pubList.add("cBP7JPfSVPgqGfGXVJVw168sJU5HhQfPbvDRZyriyKNeYjYLVL8M");
            pubList.add("cBPaLRSCwtsJbz4Rq4K2NvoiDZWJyL2RnfdGv5CQ2UFWqyJ7ekHM");

            String cipherText = c.encrypt(plainText,pubList);
            System.out.println("加密后的密文为 : " + cipherText);


            String plain = c.decrypt(cipherText,"xpvPjSRCtmQ3G99Pfu1VMDMd9ET3W");
            System.out.println("解密后的明文为 : " + plain);
            plain = c.decrypt(cipherText,"xnHAcvtn1eVLDskhxPKNrhTsYKqde");
            System.out.println("解密后的明文为 : " + plain);
        	
        	byte[] password = Util.getRandomBytes(16);
            cipherText = c.symEncrypt(plainText,password, true); 
            System.out.println("加密后的密文为 : " + cipherText);
            plain = c.symDecrypt(cipherText, password, true);
            System.out.println("解密后的明文为 : " +  plain);
            
            //国密算法非对称加解密
        	String pubKey = "pYvXDbsUUr5dpumrojYApjG8nLfFMXhu3aDvxq5oxEa4ZSeyjrMzisdPsYjfxyg9eN3ZJsNjtNENbzXPL89st39oiSp5yucU";
        	cipherText = c.asymEncrypt(plainText,pubKey, true);
            String seed   = "pwRdHmA4cSUKKtFyo4m2vhiiz5g6ym58Noo9dTsUU97mARNjevj";
            plain = c.asymDecrypt(cipherText, seed, true);
            System.out.println("解密后的明文为 : " +  plain);
            
            //非国密算法非对称加解密
            pubKey = "cB4vvJpFQHUiWiiJz46fG7ogC9qdsQ1hskZ6KdGuYqGTVLZWXSzK";
            cipherText = c.asymEncrypt(plainText,pubKey, false);
            seed   = "xhJz3kketmLvY6SR6vVnuuxj15D13";
            plain = c.asymDecrypt(cipherText, seed, false);
            System.out.println("解密后的明文为 : " +  plain);
            
        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

}