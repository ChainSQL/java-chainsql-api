package com.peersafe.base.core.coretypes.hash;

import com.peersafe.base.core.coretypes.hash.prefixes.Prefix;
import com.peersafe.base.core.serialized.BytesSink;
import com.peersafe.base.core.serialized.SerializedType;

import java.security.MessageDigest;

public class HalfSha512 implements BytesSink {
    MessageDigest messageDigest;

    /**
     * HalfSha512
     */
    public HalfSha512() {
        try {
            messageDigest = MessageDigest.getInstance("SHA-512", "BC");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * prefixed256
     * @param bytes Prefix
     * @return return value.
     */
    public static HalfSha512 prefixed256(Prefix bytes) {
        HalfSha512 halfSha512 = new HalfSha512();
        halfSha512.update(bytes);
        return halfSha512;
    }

    /**
     * update
     * @param bytes bytes.
     */
    public void update(byte[] bytes) {
        messageDigest.update(bytes);
    }

    /**
     * Update
     * @param hash hash.
     */
    public void update(Hash256 hash) {
        messageDigest.update(hash.bytes());
    }

    /**
     * Digest
     * @return MessageDigest.
     */
    public MessageDigest digest() {
        return messageDigest;
    }

    /**
     * Finish.
     * @return Final hash.
     */
    public Hash256 finish() {
        byte[] half = digestBytes();
        return new Hash256(half);
    }

    /**
     * DigestAllBytes
     * @return DigestAllBytes .
     */
    public byte[] digestAllBytes(){
    	return messageDigest.digest();
    }
    private byte[] digestBytes() {
        byte[] digest = messageDigest.digest();
        byte[] half = new byte[32];
        System.arraycopy(digest, 0, half, 0, 32);
        return half;
    }

    private Hash256 makeHash(byte[] half) {
        return new Hash256(half);
    }

    @Override
    public void add(byte aByte) {
        messageDigest.update(aByte);
    }

    @Override
    public void add(byte[] bytes) {
        messageDigest.update(bytes);
    }

    /**
     * Update a prefix.
     * @param prefix prefix.
     */
    public void update(Prefix prefix) {
        messageDigest.update(prefix.bytes());
    }

    /**
     * Add serialized object.
     * @param st serialized object.
     * @return return value.
     */
    public HalfSha512 add(SerializedType st) {
        st.toBytesSink(this);
        return this;
    }
}
