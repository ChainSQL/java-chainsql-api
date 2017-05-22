package com.peersafe.base.utils;

import com.peersafe.base.encodings.common.B16;

import java.math.BigInteger;

public class Utils {
	/**
	 * 
	 * @param bn
	 * @return String value
	 */
    public static String bigHex(BigInteger bn) {
        return B16.toStringTrimmed(bn.toByteArray());
    }
    /**
     * 
     * @param bytes
     * @return BigInteger
     */
    public static BigInteger uBigInt(byte[] bytes) {
        return new BigInteger(1, bytes);
    }
}
