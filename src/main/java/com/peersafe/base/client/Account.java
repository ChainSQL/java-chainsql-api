
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
    /**
     * Create transactionManager.
     * @return return value.
     */
    public TransactionManager transactionManager() {
        return tm;
    }
    /**
     * Create publisher.
     * @return return value.
     */
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
     * @return AccountID.
     */
    public AccountID id() {
        return id;
    }

    /**
     * Get trackedAccountRoot
     * @return TrackedAccountRoot.
     */
    public TrackedAccountRoot getAccountRoot() {
        return accountRoot;
    }

    /**
     * Set AccountRoot
     * @param accountRoot AccountRoot.
     */
    public void setAccountRoot(TrackedAccountRoot accountRoot) {
        Account.this.accountRoot = accountRoot;
    }

    private AccountID id;

    /**
     * Set Account parameters
     * @param id Account address.
     * @param keyPair KeyPair.
     * @param root TrackedAccountRoot
     * @param tm TransactionManager.
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
     * @return return value.
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
