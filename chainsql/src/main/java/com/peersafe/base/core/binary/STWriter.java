package com.peersafe.base.core.binary;

import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.serialized.BinarySerializer;
import com.peersafe.base.core.serialized.BytesSink;
import com.peersafe.base.core.serialized.SerializedType;
import com.peersafe.base.core.types.known.sle.LedgerEntry;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;

public class STWriter implements BytesSink {
    BytesSink sink;
    BinarySerializer serializer;
    /**
     * Constructor.
     * @param bytesSink bytesSink
     */
    public STWriter(BytesSink bytesSink) {
        serializer = new BinarySerializer(bytesSink);
        sink = bytesSink;
    }
    /**
     * Write method.
     * @param obj Serialized Object.
     */
    public void write(SerializedType obj) {
        obj.toBytesSink(sink);
    }
    /**
     * Write v1
     * @param obj Serialized Object.
     */
    public void writeVl(SerializedType obj) {
        serializer.addLengthEncoded(obj);
    }

    @Override
    public void add(byte aByte) {
        sink.add(aByte);
    }

    @Override
    public void add(byte[] bytes) {
        sink.add(bytes);
    }

    /**
     * Write TransactionResult
     * @param result TransactionResult
     */
    public void write(TransactionResult result) {
        write(result.hash);
        writeVl(result.txn);
        writeVl(result.meta);
    }

    /**
     * Write ledger_entry.
     * @param hash256 ledger hash.
     * @param le ledger_entry.
     */
    public void write(Hash256 hash256, LedgerEntry le) {
        write(hash256);
        writeVl(le);
    }
}
