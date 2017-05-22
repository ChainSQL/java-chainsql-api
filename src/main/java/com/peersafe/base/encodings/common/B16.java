package com.peersafe.base.encodings.common;


import static org.bouncycastle.util.encoders.Hex.toHexString;

public class B16 {
	/**
	 * 
	 * @param bytes
	 * @return HexString value
	 */
    public static String toStringTrimmed(byte[] bytes) {
        int offset = 0;
        if (bytes[0] == 0) {
            offset = 1;
        }
        return toHexString(bytes, offset, bytes.length - offset).toUpperCase();
    }
    @Deprecated
    public static String toString(byte[] bytes) {
        return encode(bytes);
    }
    /**
     * 
     * @param bytes
     * @return String value
     */
    public static String encode(byte[] bytes) {
        return toHexString(bytes).toUpperCase();
    }
    /**
     * 
     * @param hex
     * @return byte[] value
     */
    public static byte[] decode(String hex) {
        return org.bouncycastle.util.encoders.Hex.decode(hex);
    }
}
