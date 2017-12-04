package com.peersafe.base.core.types.shamap;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;

public interface TransactionResultVisitor {
    public void onTransaction(TransactionResult tx);
}
