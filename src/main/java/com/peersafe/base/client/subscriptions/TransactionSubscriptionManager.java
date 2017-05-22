package com.peersafe.base.client.subscriptions;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;

public interface TransactionSubscriptionManager {
    public void notifyTransactionResult(TransactionResult tr);
}
