package com.peersafe.base.core.coretypes.hash;

import com.peersafe.base.core.serialized.BinaryParser;
import com.peersafe.base.core.serialized.BytesSink;
import com.peersafe.base.core.serialized.SerializedType;
import com.peersafe.base.core.serialized.TypeTranslator;
import com.peersafe.base.encodings.common.B16;

import java.math.BigInteger;
import java.util.Arrays;

abstract public class Hash<Subclass extends Hash> implements SerializedType, Comparable<Subclass> {
    protected final byte[] hash;
    protected int hashCode = -1;

    /**
     * Constructor.
     * @param bytes bytes.
     * @param size size.
     */
    public Hash(byte[] bytes, int size) {
        hash = normalizeAndCheckHash(bytes, size);
    }

    @Override
    public String toString() {
        return B16.toString(hash);
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = Arrays.hashCode(hash);
        }
        return hashCode;
    }

    private byte[] normalizeAndCheckHash(byte[] bytes, int size) {
        int length = bytes.length;
        if (length > size) {
            String simpleName = "";

            throw new RuntimeException("Hash length of " + length + "  is too wide for " + simpleName);
        }
        if (length == size) {
            return bytes;
        } else {
            byte[] hash = new byte[size];
            System.arraycopy(bytes, 0, hash, size - length, length);
            return hash;
        }
    }

    /**
     * Create a BigInteger.
     * @return BigInteger Object.
     */
    BigInteger bigInteger() {
        return new BigInteger(1, hash);
    }

    /**
     * Get hash bytes.
     * @return hash bytes.
     */
    public byte[] bytes() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Hash) {
            return Arrays.equals(hash, ((Hash) obj).hash);
        }

        return super.equals(obj);
    }

    @Override
    public int compareTo(Subclass another) {
        byte[] thisBytes = bytes();
        byte[] bytes = another.bytes();

        return compareBytes(thisBytes, bytes, 0, thisBytes.length);
    }

    /**
     * compareStartingAt
     * @param another another.
     * @param start start.
     * @return return value.
     */
    public int compareStartingAt(Subclass another, int start) {
        byte[] thisBytes = bytes();
        byte[] bytes = another.bytes();

        return compareBytes(thisBytes, bytes, start, thisBytes.length);
    }

    /**
     * compareBytes
     * @param thisBytes thisBytes.
     * @param bytes bytes.
     * @param start start.
     * @param numBytes number of bytes.
     * @return return value.
     */
    public int compareBytes(byte[] thisBytes, byte[] bytes, int start, int numBytes) {
        int thisLength = thisBytes.length;
        if (!(bytes.length == thisLength)) {
            throw new RuntimeException();
        }

        for (int i = start; i < numBytes; i++) {
            int cmp = (thisBytes[i] & 0xFF) - (bytes[i] & 0xFF);
            if (cmp != 0) {
                return cmp < 0 ? -1 : 1;
            }
        }
        return 0;
    }

    /**
     * Slice.
     * @param start Start.
     * @return return value.
     */
    public byte[] slice(int start) {
        return slice(start, 0);
    }

    /**
     * Get i.
     * @param i index.
     * @return hash bytes.
     */
    public byte get(int i) {
        if (i < 0) i += hash.length;
        return hash[i];
    }

    /**
     * Slice an array.
     * @param start start index
     * @param end End index.
     * @return return value.
     */
    public byte[] slice(int start, int end) {
        if (start < 0)  start += hash.length;
        if (end  <= 0)  end   += hash.length;

        int length = end - start;
        byte[] slice = new byte[length];

        System.arraycopy(hash, start, slice, 0, length);
        return slice;
    }

    static public abstract class HashTranslator<T extends Hash> extends TypeTranslator<T> {

        public abstract T newInstance(byte[] b);
        public abstract int byteWidth();

        @Override
        public T fromParser(BinaryParser parser, Integer hint) {
            return newInstance(parser.read(byteWidth()));
        }

        @Override
        public Object toJSON(T obj) {
            return B16.toString(obj.hash);
        }

        @Override
        public T fromString(String value) {
            return newInstance(B16.decode(value));
        }

        @Override
        public void toBytesSink(T obj, BytesSink to) {
            to.add(obj.hash);
        }
    }
}
