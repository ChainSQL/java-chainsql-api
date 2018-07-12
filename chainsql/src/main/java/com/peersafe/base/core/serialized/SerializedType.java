package com.peersafe.base.core.serialized;

import com.peersafe.base.core.fields.Type;

public interface SerializedType {
    Object toJSON();
    byte[] toBytes();
    String toHex();
    void toBytesSink(BytesSink to);
    Type type();
}
