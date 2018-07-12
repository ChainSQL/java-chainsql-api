package com.peersafe.base.core.types.shamap;

import com.peersafe.base.core.types.known.sle.LedgerEntry;

public interface LedgerEntryVisitor {
    public void onEntry(LedgerEntry entry);
}
