package com.peersafe.base.core.types.known.tx.txns;

import com.peersafe.base.core.coretypes.Blob;
import com.peersafe.base.core.coretypes.uint.UInt16;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;

public class SmartContract extends Transaction {
    public SmartContract() {
        super(TransactionType.Contract);
    }

    public UInt16 contractOpType() {return get(UInt16.ContractOpType);}
    public Blob contractData() { return get(Blob.ContractData); }
}

