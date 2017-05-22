package com.peersafe.base.encodings;


import com.peersafe.base.encodings.base58.B58;

public class B58IdentiferCodecs {
    public static final int VER_ACCOUNT_ID        = 0;
    public static final int VER_FAMILY_SEED       = 33;

    public static final int VER_NONE              = 1;
    public static final int VER_NODE_PUBLIC       = 28;
    public static final int VER_NODE_PRIVATE      = 32;
    public static final int VER_ACCOUNT_PUBLIC    = 35;
    public static final int VER_ACCOUNT_PRIVATE   = 34;
    public static final int VER_FAMILY_GENERATOR  = 41;

    public B58 b58;
    /**
     * 
     * @param base58encoder
     */
    public B58IdentiferCodecs(B58 base58encoder) {
        this.b58 = base58encoder;
    }
    /**
     * 
     * @param String d
     * @param int version
     * @return
     */
    public byte[] decode(String d, int version) {
        return b58.decodeChecked(d, version);
    }
    /**
     * 
     * @param byte[] d
     * @param int version
     * @return
     */
    public String encode(byte[] d, int version) {
        return b58.encodeToStringChecked(d, version);
    }
    /**
     * 
     * @param String master_seed
     * @return
     */
    public byte[] decodeFamilySeed(String master_seed) {
        return b58.decodeChecked(master_seed, VER_FAMILY_SEED);
    }
    /**
     * 
     * @param byte[] bytes
     * @return
     */
    public String encodeFamilySeed(byte[] bytes) {
        return encode(bytes, VER_FAMILY_SEED);
    }
    /**
     * 
     * @param byte[] bytes
     * @return String value
     */
    public String encodeAddress(byte[] bytes) {
        return encode(bytes, VER_ACCOUNT_ID);
    }
    /**
     * 
     * @param byte[] bytes
     * @return String value
     */
    public String encodeNodePublic(byte[] bytes) {
        return encode(bytes, VER_NODE_PUBLIC);
    }
    /**
     * 
     * @param String address
     * @return byte[] value
     */
    public byte[] decodeAddress(String address) {
        return decode(address, VER_ACCOUNT_ID);
    }
}
