package com.peersafe.base.core.coretypes.uint;

import com.peersafe.base.core.serialized.BinaryParser;
import com.peersafe.base.core.serialized.BytesSink;
import com.peersafe.base.core.serialized.SerializedType;
import com.peersafe.base.core.serialized.TypeTranslator;
import com.peersafe.base.encodings.common.B16;

import java.math.BigInteger;

abstract public class UInt<Subclass extends UInt> extends Number implements SerializedType, Comparable<UInt> {

    private BigInteger value;

    public static BigInteger Max8  = new BigInteger("256"),
                             Max16 = new BigInteger("65536"),
                             Max32 = new BigInteger("4294967296"),
                             Max64 = new BigInteger("18446744073709551616");

    /**
     * getMinimumValue
     * @return BigInteger
     */
    public BigInteger getMinimumValue() {
        return BigInteger.ZERO;
    }
    /**
     * 
     * @param bytes bytes.
     */
    public UInt(byte[] bytes) {
        setValue(new BigInteger(1, bytes));
    }
    /**
     * Uint.
     * @param bi bi.
     */
    public UInt(BigInteger bi) {
        setValue(bi);
    }
    /**
     * Set uint from number.
     * @param s s.
     */
    public UInt(Number s) {
        setValue(BigInteger.valueOf(s.longValue()));
    }
    /**
     * Set Uint from String.
     * @param s s.
     */
    public UInt(String s) {
        setValue(new BigInteger(s));
    }
    /**
     * Set Uint from String and int.
     * @param s String value.
     * @param radix radix.
     */
    public UInt(String s, int radix) {
        setValue(new BigInteger(s, radix));
    }


    @Override
    public String toString() {
        return value.toString();
    }

    public UInt() {}

    public abstract int getByteWidth();
    public abstract Subclass instanceFrom(BigInteger n);

    /**
     * isValid.
     * @param n BigInteger.
     * @return return value.
     */
    public boolean isValid(BigInteger n) {
        return !((bitLength() / 8) > getByteWidth());
    }

    /**
     * Add value.
     * @param val Uint.
     * @return return value.
     */
    public Subclass add(UInt val) {
        return instanceFrom(value.add(val.value));
    }

    /**
     * Substract.
     * @param val Uint value.
     * @return Subclass value.
     */
    public Subclass subtract(UInt val) {
        return instanceFrom(value.subtract(val.value));
    }

    /**
     * Multiply.
     * @param val value.
     * @return return value.
     */
    public Subclass multiply(UInt val) {
        return instanceFrom(value.multiply(val.value));
    }

    /**
     * Divide.
     * @param val value.
     * @return return value.
     */
    public Subclass divide(UInt val) {
        return instanceFrom(value.divide(val.value));
    }

    /**
     * Or operation.
     * @param val value.
     * @return return value.
     */
    public Subclass or(UInt val) {
        return instanceFrom(value.or(val.value));
    }

    /**
     * shiftLeft operation.
     * @param n value.
     * @return return value.
     */
    public Subclass shiftLeft(int n) {
        return instanceFrom(value.shiftLeft(n));
    }

    /**
     * Shift Right operation.
     * @param n n.
     * @return return value.
     */
    public Subclass shiftRight(int n) {
        return instanceFrom(value.shiftRight(n));
    }

    /**
     * bitLength.
     * @return return value.
     */
    public int bitLength() {
        return value.bitLength();
    }

    /**
     * Compare to.
     * @param val value.
     */
    public int compareTo(UInt val) {
        return value.compareTo(val.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UInt) {
            return equals((UInt) obj);
        }
        else return super.equals(obj);
    }

    /**
     * Equeals.
     * @param x x.
     * @return return value.
     */
    public boolean equals(UInt x) {
        return value.equals(x.value);
    }

    /**
     * Min.
     * @param val value.
     * @return return value.
     */
    public BigInteger min(BigInteger val) {
        return value.min(val);
    }

    /**
     * Max.
     * @param val BigInteger value.
     * @return return value.
     */
    public BigInteger max(BigInteger val) {
        return value.max(val);
    }

    /**
     * toString.
     * @param radix radix.
     * @return return value.
     */
    public String toString(int radix) {
        return value.toString(radix);
    }
    /**
     * To byte array.
     * @return return value.
     */
    public byte[] toByteArray() {
        int length = getByteWidth();

        {
            byte[] bytes = value.toByteArray();

            if (bytes[0] == 0) {
                if (bytes.length - 1 > length) {
                    throw new IllegalArgumentException("standard length exceeded for value");
                }

                byte[] tmp = new byte[length];

                System.arraycopy(bytes, 1, tmp, tmp.length - (bytes.length - 1), bytes.length - 1);

                return tmp;
            } else {
                if (bytes.length == length) {
                    return bytes;
                }

                if (bytes.length > length) {
                    throw new IllegalArgumentException("standard length exceeded for value");
                }

                byte[] tmp = new byte[length];

                System.arraycopy(bytes, 0, tmp, tmp.length - bytes.length, bytes.length);

                return tmp;
            }
        }
    }

    abstract public Object value();

    /**
     * return BigInter value.
     * @return BigInter value.
     */
    public BigInteger bigInteger(){
        return value;
    }


    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public byte byteValue() {
        return value.byteValue();
    }

    @Override
    public short shortValue() {
        return value.shortValue();
    }

    /**
     * Set BigInteger value.
     * @param value value.
     */
    public void setValue(BigInteger value) {
        this.value = value;
    }

    /**
     * lte
     * @param sequence Sequence.
     * @param <T> int type.
     * @return return value.
     */
    public <T extends UInt> boolean  lte(T sequence) {
        return compareTo(sequence) < 1;
    }

    /**
     * Test Bit.
     * @param f int value.
     * @return return value.
     */
    public boolean testBit(int f) {
        // TODO, optimized ;) // move to Uint32
        return value.testBit(f);
    }

    /**
     * isZero.
     * @return return value.
     */
    public boolean isZero() {
        return value.signum() == 0;
    }

    static public abstract class UINTTranslator<T extends UInt> extends TypeTranslator<T> {
        public abstract T newInstance(BigInteger i);
        public abstract int byteWidth();

        @Override
        public T fromParser(BinaryParser parser, Integer hint) {
            return newInstance(new BigInteger(1, parser.read(byteWidth())));
        }

        @Override
        public Object toJSON(T obj) {
            if (obj.getByteWidth() <= 4) {
                return obj.longValue();
            } else {
                return toString(obj);
            }
        }

        @Override
        public T fromLong(long aLong) {
            return newInstance(BigInteger.valueOf(aLong));
        }

        @Override
        public T fromString(String value) {
            int radix = byteWidth() <= 4 ? 10 : 16;
            return newInstance(new BigInteger(value, radix));
        }

        @Override
        public T fromInteger(int integer) {
            return fromLong(integer);
        }

        @Override
        public String toString(T obj) {
            return B16.toString(obj.toByteArray());
        }

        @Override
        public void toBytesSink(T obj, BytesSink to) {
            to.add(obj.toByteArray());
        }
    }
}
