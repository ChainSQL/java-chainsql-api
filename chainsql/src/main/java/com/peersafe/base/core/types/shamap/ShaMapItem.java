package com.peersafe.base.core.types.shamap;

import com.peersafe.base.core.coretypes.hash.prefixes.Prefix;
import com.peersafe.base.core.serialized.BytesSink;

abstract public class ShaMapItem<T> {
    abstract void toBytesSink(BytesSink sink);
    public abstract ShaMapItem<T> copy();
    public abstract T value();
    public abstract Prefix hashPrefix();
}
