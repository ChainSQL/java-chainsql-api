package com.peersafe.base.client.subscriptions;

import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.STObject;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.uint.UInt32;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * This should probably be an STObject extending class
 * Publisher should probably be an inner (non static) class
 */
public class TrackedAccountRoot extends Publisher<TrackedAccountRoot.events> {
    static final protected Logger logger = Logger.getLogger(TrackedAccountRoot.class.getName());
    public static interface events<T> extends Publisher.Callback<T> {}
    public static interface OnUpdate extends events<TrackedAccountRoot> {}
    boolean updated = false;

    /**
     * primed.
     * @return return value.
     */
    public boolean primed() {
        return updated;
    }

    /**
     * updateFromTransaction
     * @param transactionHash transactionHash
     * @param transactionLedgerIndex transactionLedgerIndex
     * @param rootUpdates rootUpdates
     */
    public void updateFromTransaction(Hash256 transactionHash, UInt32 transactionLedgerIndex, STObject rootUpdates) {
        // TODO, rethink this
        // If transactionLedgerIndex is higher than current also apply it
        // If we have a direct transaction chain, apply it
        if (!updated  || PreviousTxnID.equals(rootUpdates.get(Hash256.PreviousTxnID))) {
            setFromSTObject(rootUpdates.get(STObject.FinalFields));
            PreviousTxnID = transactionHash;
            PreviousTxnLgrSeq = transactionLedgerIndex;
        } else {
            logger.fine("hrmmm .... "); // We should keep track of these and try and form a chain
        }
    }

    public AccountID     Account;
    /**
     * Get balance.
     * @return return value.
     */
    public Amount getBalance() {
        return Balance;
    }
    /**
     * Set balance.
     * @param balance balance.
     */
    public void setBalance(Amount balance) {
        Balance = balance;
    }

    private Amount        Balance;
    public UInt32        Sequence;
    public UInt32        OwnerCount;
    public UInt32        Flags;
    public Hash256       PreviousTxnID;
    public UInt32        PreviousTxnLgrSeq;

    public TrackedAccountRoot(JSONObject object){setFromJSON(object);    }
    public TrackedAccountRoot(STObject object){setFromSTObject(object);}
    public TrackedAccountRoot()   {}

    public void setFromJSON(JSONObject jsonObject) {
        setFromSTObject(STObject.translate.fromJSONObject(jsonObject));
    }

    /**
     * setUnfundedAccount
     * @param account account
     */
    public void setUnfundedAccount(AccountID account) {
        Account = account;
        Balance = Amount.fromString("0");
        Sequence = new UInt32(1);
        OwnerCount = new UInt32(0);
        Flags = new UInt32(0);
        PreviousTxnID = new Hash256(new byte[32]);
        PreviousTxnLgrSeq = new UInt32(0);
        notifyUpdate();
    }

    /**
     * setFromSTObject
     * @param so STObject.
     */
    public void setFromSTObject(STObject so) {

        if (so.has(AccountID.Account))         Account            = so.get(AccountID.Account);
        if (so.has(Amount.Balance))            Balance            = so.get(Amount.Balance);
        if (so.has(UInt32.Sequence))           Sequence           = so.get(UInt32.Sequence);
        if (so.has(UInt32.OwnerCount))         OwnerCount         = so.get(UInt32.OwnerCount);
        if (so.has(UInt32.Flags))              Flags              = so.get(UInt32.Flags);
        if (so.has(Hash256.PreviousTxnID))     PreviousTxnID      = so.get(Hash256.PreviousTxnID);
        if (so.has(UInt32.PreviousTxnLgrSeq))  PreviousTxnLgrSeq  = so.get(UInt32.PreviousTxnLgrSeq);

        notifyUpdate();
    }

    private void notifyUpdate() {
        updated = true;
        emit(OnUpdate.class, this);
    }

}
