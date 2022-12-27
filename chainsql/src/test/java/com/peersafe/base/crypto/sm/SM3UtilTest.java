package com.peersafe.base.crypto.sm;

import junit.framework.TestCase;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.Assert;

public class SM3UtilTest extends TestCase {

    public static String sm3Plain1 = "11AD63D24656D0922A7647E25AC7EF5E";

    public void testPrefixedHash() {

        try {

            byte[] prefixedBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
            byte[] hashValue = SM3Util.prefixedHash(prefixedBytes,ByteUtils.fromHexString(sm3Plain1));

            Assert.assertEquals("加前缀的sm3不匹配!","0E4BE15BC066205D34487C7717047BC71BBE6C25E4DC6916753ABEC6BFBF9AC5",
                    ByteUtils.toHexString(hashValue).toUpperCase());

        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    public void testHash() {

        try {
            byte[] hashValue = SM3Util.hash(ByteUtils.fromHexString(sm3Plain1));

            Assert.assertEquals("sm3不匹配!","9690C000D57A987671B3D5709D3CF26154AC198BA9DB012D457B2BB3E5BD9CB4",
                    ByteUtils.toHexString(hashValue).toUpperCase());

        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }
}