package com.peersafe.base.crypto.sm;

import junit.framework.TestCase;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.Assert;

public class SM4UtilTest extends TestCase {

    public void testEncrypt_ECB_Padding() {

        try{

            byte[] password    =  ByteUtils.fromHexString("C35242CC90CB75935A536F32149F5C35");
            byte[] plainBytes  =  ByteUtils.fromHexString("ECB57EE6D15BD06632CFE9FD09B822AF");
            byte[] cipherBytes =  SM4Util.encrypt_ECB_Padding(password, plainBytes);

            String sm4ECBCipher = "03D8C466A7C245971925E35540E9209F95505AEE5DCBFC4210DC2921722198F0";
            Assert.assertEquals("公钥的Y坐标为32字节",sm4ECBCipher,ByteUtils.toHexString(cipherBytes).toUpperCase());

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testDecrypt_ECB_Padding() {


        try{

            String sm4ECBPlain  = "ECB57EE6D15BD06632CFE9FD09B822AF";
            String sm4ECBCipher = "03D8C466A7C245971925E35540E9209F95505AEE5DCBFC4210DC2921722198F0";

            byte[] cipherBytes  =  ByteUtils.fromHexString(sm4ECBCipher);
            byte[] password     =  ByteUtils.fromHexString("C35242CC90CB75935A536F32149F5C35");
            byte[] plainBytes =  SM4Util.decrypt_ECB_Padding(password, cipherBytes);

            Assert.assertEquals("公钥的Y坐标为32字节",sm4ECBPlain,ByteUtils.toHexString(plainBytes).toUpperCase());

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }
}