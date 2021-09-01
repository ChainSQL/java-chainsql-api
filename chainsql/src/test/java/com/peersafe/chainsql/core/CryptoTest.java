package com.peersafe.chainsql.core;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.encodings.B58IdentiferCodecs;
import com.peersafe.chainsql.crypto.EncryptCommon;
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
            byte[] rawBytes = EncryptCommon.symEncrypt(plainText.getBytes(),password, true);
            byte[] newBytes = EncryptCommon.symDecrypt(rawBytes, password, true);
            System.out.println("解密后的明文为 : " +  new String(newBytes));
            
        	byte [] pubBytes = getB58IdentiferCodecs().decode("pYvXDbsUUr5dpumrojYApjG8nLfFMXhu3aDvxq5oxEa4ZSeyjrMzisdPsYjfxyg9eN3ZJsNjtNENbzXPL89st39oiSp5yucU", B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
            rawBytes = EncryptCommon.asymEncrypt(plainText.getBytes(),pubBytes);
            byte[] seedBytes   = getB58IdentiferCodecs().decodeAccountPrivate("pwRdHmA4cSUKKtFyo4m2vhiiz5g6ym58Noo9dTsUU97mARNjevj");
            newBytes = EncryptCommon.asymDecrypt(rawBytes, seedBytes, true);
            System.out.println("解密后的明文为 : " +  new String(newBytes));
            
        	IKeyPair keyPair = Seed.getKeyPair("xpvPjSRCtmQ3G99Pfu1VMDMd9ET3W");
            rawBytes = EncryptCommon.asymEncrypt(plainText.getBytes(),keyPair.canonicalPubBytes());
            seedBytes   = getB58IdentiferCodecs().decodeFamilySeed("xpvPjSRCtmQ3G99Pfu1VMDMd9ET3W");
            newBytes = EncryptCommon.asymDecrypt(rawBytes, seedBytes, false);
            System.out.println("解密后的明文为 : " +  new String(newBytes));
            
        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

}