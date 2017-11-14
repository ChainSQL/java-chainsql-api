package com.peersafe.base.core.serialized;

import java.security.MessageDigest;
import java.util.ArrayList;

public class BytesList implements BytesSink {
    private ArrayList<byte[]> buffer = new ArrayList<byte[]>();

    private int len = 0;

    public void add(BytesList bl) {
        for (byte[] bytes : bl.rawList()) {
            add(bytes);
        }
    }

    @Override
    public void add(byte aByte) {
        add(new byte[]{aByte});
    }

    @Override
    public void add(byte[] bytes) {
        len += bytes.length;
        buffer.add(bytes);
    }

    public int get8(int offset) {
    	if(offset + 1 > len) {
        	return -1;
    	}
    	return bytes()[offset] & 0xff;
    }
    
    public int getInteger(int offset) {
    	if(offset + Integer.BYTES > len) {
    		return -1;
    	}
    	int number = 0;
    	byte[] bts = bytes();
    	for(int i=0; i<Integer.BYTES; i++) {
    		if(i != 0) {
    			number <<= 8;
    		}
    		number |= bts[offset + i]; 
    	}
    	return number;
    }
    public byte[] bytes() {
        int n = bytesLength();
        byte[] bytes = new byte[n];
        addBytes(bytes, 0);
        return bytes;
    }

    static public String[] hexLookup = new String[256];
    static {
        for (int i = 0; i < 256; i++) {
            String s = Integer.toHexString(i).toUpperCase();
            if (s.length() == 1) {
                s = "0" + s;
            }
            hexLookup[i] = s;
        }
    }

    public String bytesHex() {
        StringBuilder builder = new StringBuilder(len * 2);
        for (byte[] buf : buffer) {
            for (byte aBytes : buf) {
                builder.append(hexLookup[aBytes & 0xFF]);
            }
        }
        return builder.toString();
    }

    public int bytesLength() {
        return len;
    }

    private int addBytes(byte[] bytes, int destPos) {
        for (byte[] buf : buffer) {
            System.arraycopy(buf, 0, bytes, destPos, buf.length);
            destPos += buf.length;
        }
        return destPos;
    }

    public void updateDigest(MessageDigest digest) {
        for (byte[] buf : buffer) {
            digest.update(buf);
        }
    }

    public ArrayList<byte[]> rawList() {
        return buffer;
    }
}
