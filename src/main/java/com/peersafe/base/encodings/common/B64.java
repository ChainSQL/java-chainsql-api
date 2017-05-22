package com.peersafe.base.encodings.common;

import org.bouncycastle.util.encoders.Base64;

public class B64 {
	/**
	 * 
	 * @param bytes
	 * @return String value 
	 */
    public static String toString(byte[] bytes) {
        return Base64.toBase64String(bytes);
    }
    /**
     * 
     * @param string
     * @return byte[] value
     */
    public static byte[] decode(String string) {
        return Base64.decode(string);
    }
}
