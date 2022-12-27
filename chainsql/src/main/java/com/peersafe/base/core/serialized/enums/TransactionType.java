package com.peersafe.base.core.serialized.enums;

import com.peersafe.base.core.fields.Type;
import com.peersafe.base.core.serialized.BinaryParser;
import com.peersafe.base.core.serialized.BytesSink;
import com.peersafe.base.core.serialized.SerializedType;
import com.peersafe.base.core.serialized.TypeTranslator;
import com.peersafe.base.encodings.common.B16;

import java.util.TreeMap;

public enum TransactionType implements SerializedType {
    Invalid (-1),
    Payment (0),
    EscrowCreate (1), // open
    EscrowFinish (2),
    AccountSet (3),
    EscrowCancel (4), // open
    SetRegularKey(5),
    NickNameSet (6), // open
    OfferCreate (7),
    OfferCancel (8),
    unused(9),
    TicketCreate(10),
    TicketCancel(11),
    SignerListSet(12),
    TrustSet (20),
    EnableAmendment(100),
    SetFee(101),
	TableListSet(21),
	SQLStatement(22),
	SQLTransaction(23),
	Contract(24),
    SchemaCreate(25),
    SchemaModify(26),
	FreezeAccount(27),
	SchemaDelete(28),
	Authorize(29);
	
    public int asInteger() {
        return ord;
    }

    final int ord;
    TransactionType(int i) {
       ord = i;
    }

    @Override
    public Type type() {
        return Type.UInt16;
    }

    static private TreeMap<Integer, TransactionType> byCode = new TreeMap<Integer, TransactionType>();
    static {
        for (Object a : TransactionType.values()) {
            TransactionType f = (TransactionType) a;
            byCode.put(f.ord, f);
        }
    }

    static public TransactionType fromNumber(Number i) {
        return byCode.get(i.intValue());
    }

    // SeralizedType interface
    @Override
    public byte[] toBytes() {
        // TODO: bytes
        return new byte[]{(byte) (ord >> 8), (byte) (ord & 0xFF)};
    }
    @Override
    public Object toJSON() {
        return toString();
    }
    @Override
    public String toHex() {
        return B16.toString(toBytes());
    }
    @Override
    public void toBytesSink(BytesSink to) {
        to.add(toBytes());
    }
    public static class Translator extends TypeTranslator<TransactionType> {
        @Override
        public TransactionType fromParser(BinaryParser parser, Integer hint) {
            byte[] read = parser.read(2);
            return fromNumber((read[0] << 8) | read[1]);
        }

        @Override
        public TransactionType fromInteger(int integer) {
            return fromNumber(integer);
        }

        @Override
        public TransactionType fromString(String value) {
            return TransactionType.valueOf(value);
        }
    }
    public static Translator translate = new Translator();

}
