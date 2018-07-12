package com.peersafe.base.core.types.known.tx.txns;

import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;

public class TicketCancel extends Transaction {
    public TicketCancel() {
        super(TransactionType.TicketCancel);
    }
    public Hash256 ticketID() {
        return get(Hash256.TicketID);
    }
    public void ticketID(Hash256 id) {
        put(Hash256.TicketID, id);
    }
}
