
package com.peersafe.base.client;

import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.client.subscriptions.TrackedAccountRoot;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.crypto.ecdsa.IKeyPair;

/*
 *
 * We want this guy to be able to track accounts we have the secret for or not
 *
 * */
public class Account {
    private final Publisher<events> publisher = new Publisher<events>();
    public TransactionManager transactionManager() {
        return tm;
    }
    public Publisher<events> publisher() {
        return publisher;
    }
    // events enumeration
    public static interface events<T> extends Publisher.Callback<T> {}
    public static interface OnServerInfo extends events {}

    private TrackedAccountRoot accountRoot;
    private TransactionManager tm;
    public IKeyPair keyPair;

    /**
     * Create AccountID
     * @return
     */
    public AccountID id() {
        return id;
    }

    /**
     * Get trackedAccountRoot
     * @return
     */
    public TrackedAccountRoot getAccountRoot() {
        return accountRoot;
    }

    /**
     * Set AccountRoot
     * @param accountRoot
     */
    public void setAccountRoot(TrackedAccountRoot accountRoot) {
        Account.this.accountRoot = accountRoot;
    }

    private AccountID id;

    /**
     * Set Account parameters
     * @param id
     * @param keyPair
     * @param root
     * @param tm
     */
    public Account(AccountID id,
                   IKeyPair keyPair, TrackedAccountRoot root,
                   TransactionManager tm) {
        this.id = id;
        this.accountRoot = root;
        this.tm = tm;
        this.keyPair = keyPair;
    }

    /**
     * AccountId to String
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
