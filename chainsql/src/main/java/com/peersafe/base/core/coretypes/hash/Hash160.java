package com.peersafe.base.core.coretypes.hash;

import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.fields.Field;
import com.peersafe.base.core.fields.Hash160Field;
import com.peersafe.base.core.fields.Type;
import com.peersafe.base.core.serialized.BytesSink;

public class Hash160 extends Hash<Hash160> {
	/**
	 * Create a hash160.
	 * @param bytes hash160 bytes.
	 */
    public Hash160(byte[] bytes) {
        super(bytes, 20);
    }

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
        return Type.Hash160;
    }

    public static class Translator extends HashTranslator<Hash160> {
        @Override
        public Hash160 newInstance(byte[] b) {
            return new Hash160(b);
        }

        @Override
        public int byteWidth() {
            return 20;
        }

        @Override
        public Hash160 fromString(String value) {
            if (value.startsWith("r")) {
                return newInstance(AccountID.fromAddress(value).bytes());
            }
            return super.fromString(value);
        }
    }
    public static Translator translate = new Translator();

    /**
     * hash160 field.
     * @param f Field.
     * @return Hash160 field.
     */
    public static Hash160Field hash160Field(final Field f) {
        return new Hash160Field(){ @Override public Field getField() {return f;}};
    }

    static public Hash160Field TakerPaysIssuer = hash160Field(Field.TakerPaysIssuer);
    static public Hash160Field TakerGetsCurrency = hash160Field(Field.TakerGetsCurrency);
    static public Hash160Field TakerPaysCurrency = hash160Field(Field.TakerPaysCurrency);
    static public Hash160Field TakerGetsIssuer = hash160Field(Field.TakerGetsIssuer);
    static public Hash160Field NameInDB = hash160Field(Field.NameInDB);
}
