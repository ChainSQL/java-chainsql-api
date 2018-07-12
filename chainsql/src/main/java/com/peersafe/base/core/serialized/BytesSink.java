package com.peersafe.base.core.serialized;

public interface BytesSink {
    void add(byte aByte);
    void add(byte[] bytes);
}
