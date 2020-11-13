package com.peersafe.base.crypto.sm;

import com.peersafe.chainsql.util.Util;
import junit.framework.TestCase;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.Assert;

public class SMKeyPairTest extends TestCase {

    public void testVerifySignature() {
    }

    public void testSignMessage() {
    }

    public void testGenerateKeyPair() {

        try{

            for(int i=0;i<100000;i++){

                SMKeyPair smKeyPair = SMKeyPair.generateKeyPair();

                int privLen      = ByteUtils.fromHexString( smKeyPair.privHex()).length;
                int pubLen       = smKeyPair.canonicalPubBytes().length;
                int pubFirstChar = smKeyPair.canonicalPubBytes()[0];

                Assert.assertEquals("公钥长度不匹配!", 65, pubLen);
                Assert.assertEquals("公钥的首字节不匹配!", 0x47,pubFirstChar);
                Assert.assertEquals("私钥长度不匹配!", 32, privLen);
            }

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail("生成私钥格式不对");
        }





    }

    public void testFrom256Seed() {


        for(int i=0;i<100000;i++) {

            SMKeyPair smKeyPair         = SMKeyPair.generateKeyPair();
            byte[] privBytes            = ByteUtils.fromHexString(smKeyPair.privHex());
            SMKeyPair smKeyPairRecovery = SMKeyPair.from256Seed(privBytes);

            int privLen      = ByteUtils.fromHexString( smKeyPairRecovery.privHex()).length;
            int pubLen       = smKeyPairRecovery.canonicalPubBytes().length;
            int pubFirstChar = smKeyPairRecovery.canonicalPubBytes()[0];

            Assert.assertEquals("公钥长度不匹配!", 65, pubLen);
            Assert.assertEquals("公钥的首字节不匹配!", 0x47,pubFirstChar);
            Assert.assertEquals("私钥长度不匹配!", 32, privLen);

        }

    }
}