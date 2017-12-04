package com.peersafe.base.core.types.shamap;

public interface TreeWalker {
    public void onLeaf(ShaMapLeaf leaf);
    public void onInner(ShaMapInner inner);
}
