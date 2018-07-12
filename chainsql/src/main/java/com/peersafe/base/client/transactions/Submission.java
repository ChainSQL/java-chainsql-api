package com.peersafe.base.client.transactions;

import com.peersafe.base.client.requests.Request;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.uint.UInt32;

public class Submission {
    public Request request;
    public Hash256 hash;
    // These aren't actually used yet, but I guess at some stage may be useful to
    // look at statistics. TODO: consider gutting until such time.
    public UInt32 sequence;
    public UInt32 lastLedgerSequence;
    public Amount fee;
    // The latest KNOWN validated ledger at the time of submission, not necessarily at the
    // time of submit response
    public long ledgerSequence;

    /**
     * Constructor.
     * @param request request
     * @param sequence sequence
     * @param hash hash
     * @param ledgerSequence ledgerSequence
     * @param fee fee
     * @param lastLedgerIndex lastLedgerIndex
     */
    public Submission(Request request, UInt32 sequence, Hash256 hash, long ledgerSequence, Amount fee, UInt32 lastLedgerIndex) {
        this.request = request;
        this.sequence = sequence;
        this.hash = hash;
        this.ledgerSequence = ledgerSequence;
        this.fee = fee;
        this.lastLedgerSequence = lastLedgerIndex;
    }
}
