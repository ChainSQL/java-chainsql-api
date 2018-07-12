package com.peersafe.base.core.binary;

import com.peersafe.base.core.coretypes.*;
import com.peersafe.base.core.coretypes.hash.Hash128;
import com.peersafe.base.core.coretypes.hash.Hash160;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.hash.prefixes.HashPrefix;
import com.peersafe.base.core.coretypes.uint.UInt16;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.coretypes.uint.UInt64;
import com.peersafe.base.core.coretypes.uint.UInt8;
import com.peersafe.base.core.serialized.BinaryParser;
import com.peersafe.base.core.serialized.StreamBinaryParser;
import com.peersafe.base.core.types.known.sle.LedgerEntry;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.core.types.known.tx.result.TransactionMeta;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;

import java.util.Arrays;
import java.util.Date;

public class STReader {
    protected BinaryParser parser;
    /**
     * Constructor.
     * @param parser Parser.
     */
    public STReader(BinaryParser parser) {
        this.parser = parser;
    }
    /**
     * Constructor.
     * @param hex hex.
     */
    public STReader(String hex) {
        this.parser = new BinaryParser(hex);
    }

    /**
     * From file.
     * @param arg file args.
     * @return STReader object.
     */
    public static STReader fromFile(String arg) {
        return new STReader(StreamBinaryParser.fromFile(arg));
    }

    /**
     * Transfer to uInt8.
     * @return return value.
     */
    public UInt8 uInt8() {
        return UInt8.translate.fromParser(parser);
    }
    /**
     * Transfer to uInt16.
     * @return return value.
     */
    public UInt16 uInt16() {
        return UInt16.translate.fromParser(parser);
    }
    /**
     * Transfer to uInt32.
     * @return return value.
     */
    public UInt32 uInt32() {
        return UInt32.translate.fromParser(parser);
    }
    /**
     * Transfer to uInt64.
     * @return return value.
     */
    public UInt64 uInt64() {
        return UInt64.translate.fromParser(parser);
    }
    
    /**
     * Transfer to hash128.
     * @return return value.
     */
    public Hash128 hash128() {
        return Hash128.translate.fromParser(parser);
    }
    /**
     * Parse hash160.
     * @return return value.
     */
    public Hash160 hash160() {
        return Hash160.translate.fromParser(parser);
    }
    /**
     * Parse currency.
     * @return return value.
     */
    public Currency currency() {
        return Currency.translate.fromParser(parser);
    }
    /**
     * Parse hash256.
     * @return return value.
     */
    public Hash256 hash256() {
        return Hash256.translate.fromParser(parser);
    }
    /**
     * Parse vector256
     * @return return value.
     */
    public Vector256 vector256() {
        return Vector256.translate.fromParser(parser);
    }
    /**
     * Parse AccountId
     * @return return value.
     */
    public AccountID accountID() {
        return AccountID.translate.fromParser(parser);
    }
    
    /**
     * Get variable length.
     * @return Blob value.
     */
    public Blob variableLength() {
        int hint = parser.readVLLength();
        return Blob.translate.fromParser(parser, hint);
    }
    /***
     * Parse amount value.
     * @return return value.
     */
    public Amount amount() {
        return Amount.translate.fromParser(parser);
    }
    /**
     * Parse path set.
     * @return return value.
     */
    public PathSet pathSet() {
        return PathSet.translate.fromParser(parser);
    }

    /**
     * Parse stObject.
     * @return return value.
     */
    public STObject stObject() {
        return STObject.translate.fromParser(parser);
    }
    /**
     * Parse vlStObject.
     * @return return value.
     */
    public STObject vlStObject() {
        return STObject.translate.fromParser(parser, parser.readVLLength());
    }

    /**
     * hashPrefix
     * @return return value.
     */
    public HashPrefix hashPrefix() {
        byte[] read = parser.read(4);
        for (HashPrefix hashPrefix : HashPrefix.values()) {
            if (Arrays.equals(read, hashPrefix.bytes)) {
                return hashPrefix;
            }
        }
        return null;
    }

    /**
     * Parse stArray.
     * @return return value.
     */
    public STArray stArray() {
        return STArray.translate.fromParser(parser);
    }
    /**
     * Parse Date value.
     * @return Date value.
     */
    public Date rippleDate() {
        return RippleDate.fromParser(parser);
    }

    /**
     * Get parse.
     * @return Parser.
     */
    public BinaryParser parser() {
        return parser;
    }

    /**
     * readTransactionResult
     * @param ledgerIndex ledger_index.
     * @return return value.
     */
    public TransactionResult readTransactionResult(UInt32 ledgerIndex) {
        Hash256 hash = hash256();
        Transaction txn = (Transaction) vlStObject();
        TransactionMeta meta = (TransactionMeta) vlStObject();
        return new TransactionResult(ledgerIndex.longValue(), hash, txn, meta);
    }

    /**
     * Read Ledger entry.
     * @return LedgerEntry value.
     */
    public LedgerEntry readLE() {
        Hash256 index = hash256();
        STObject object = vlStObject();
        LedgerEntry le = (LedgerEntry) object;
        le.index(index);
        return le;
    }

    /**
     * Read One Int
     * @return int value.
     */
    public int readOneInt() {
        return parser.readOneInt();
    }

    /**
     * Set parse end.
     * @return return value.
     */
    public boolean end() {
        return parser.end();
    }
}
