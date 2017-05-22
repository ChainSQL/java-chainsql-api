package com.peersafe.base.core.coretypes.uint;

import com.peersafe.base.core.fields.Field;
import com.peersafe.base.core.fields.Type;
import com.peersafe.base.core.fields.UInt64Field;
import com.peersafe.base.core.serialized.BytesSink;
import com.peersafe.base.core.serialized.TypeTranslator;

import java.math.BigInteger;

public class UInt64 extends UInt<UInt64> {
    public final static UInt64 ZERO = new UInt64(0);

    public static TypeTranslator<UInt64> translate = new UINTTranslator<UInt64>() {
        @Override
        public UInt64 newInstance(BigInteger i) {
            return new UInt64(i);
        }

        @Override
        public int byteWidth() {
            return 8;
        }
    };
    /**
     * Constructor.
     * @param bytes byte array.
     */
    public UInt64(byte[] bytes) {
        super(bytes);
    }
    /**
     * Constructor.
     * @param value BigInteger value.
     */
    public UInt64(BigInteger value) {
        super(value);
    }
    /**
     * Constructor from Number.
     * @param s Number value.
     */
    public UInt64(Number s) {
        super(s);
    }
    /**
     * From string value.
     * @param s String value.
     */
    public UInt64(String s) {
        super(s);
    }
    /**
     * From String and radix.
     * @param s s
     * @param radix radix.
     */
    public UInt64(String s, int radix) {
        super(s, radix);
    }

    @Override
    public int getByteWidth() {
        return 8;
    }

    @Override
    public UInt64 instanceFrom(BigInteger n) {
        return new UInt64(n);
    }

    @Override
    public BigInteger value() {
        return bigInteger();
    }

    private UInt64(){}
    /**
     * Generate int64Field.
     * @param f Field.
     * @return return value.
     */
    private static UInt64Field int64Field(final Field f) {
        return new UInt64Field(){ @Override public Field getField() {return f;}};
    }

    static public UInt64Field IndexNext = int64Field(Field.IndexNext);
    static public UInt64Field IndexPrevious = int64Field(Field.IndexPrevious);
    static public UInt64Field BookNode = int64Field(Field.BookNode);
    static public UInt64Field OwnerNode = int64Field(Field.OwnerNode);
    static public UInt64Field BaseFee = int64Field(Field.BaseFee);
    static public UInt64Field ExchangeRate = int64Field(Field.ExchangeRate);
    static public UInt64Field LowNode = int64Field(Field.LowNode);
    static public UInt64Field HighNode = int64Field(Field.HighNode);

    @Override
    public Object toJSON() {
        return translate.toJSON(this);
    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        translate.toBytesSink(this, to);
    }

    @Override
    public Type type() {
        return Type.UInt64;
    }
}
