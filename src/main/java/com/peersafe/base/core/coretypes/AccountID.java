package com.peersafe.base.core.coretypes;

import com.peersafe.base.core.coretypes.hash.Hash160;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.hash.Index;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.fields.AccountIDField;
import com.peersafe.base.core.fields.Field;
import com.peersafe.base.core.fields.Type;
import com.peersafe.base.core.serialized.BinaryParser;
import com.peersafe.base.core.serialized.BytesSink;
import com.peersafe.base.core.serialized.TypeTranslator;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.encodings.common.B16;

import java.util.HashMap;
import java.util.Map;

import static com.peersafe.base.config.Config.getB58IdentiferCodecs;

/**
 * Originally it was intended that AccountIDs would be variable length so that's
 * why they are variable length encoded as top level field objects.
 *
 * Note however, that in practice, all account ids are just 160 bit hashes.
 * Consider the fields TakerPaysIssuer and fixed length encoding of issuers in
 * amount serializations.
 *
 * Thus, we extend Hash160 which affords us some functionality.
 */
public class AccountID extends Hash160 {
    // We can set aliases, so fromString(x) will return a given AccountID
    // this is currently only used for tests, and not recommended to be used
    // elsewhere.
    public static Map<String, AccountID> aliases = new HashMap<String, AccountID>();
    //
    public static AccountID NEUTRAL = fromInteger(1), XRP_ISSUER = fromInteger(0);
    final public String address;

    /**
     * Constructor.
     * @param bytes byte array.
     */
    public AccountID(byte[] bytes) {
        this(bytes, encodeAddress(bytes));
    }

    /**
     * Constructor.
     * @param bytes byte array.
     * @param address Address.
     */
    public AccountID(byte[] bytes, String address) {
        super(bytes);
        this.address = address;
    }

    /**
     *  Static from* constructors
     * @param value Address.
     * @return AccountID.
     */
    public static AccountID fromString(String value) {
        if (value.length() == 160 / 4) {
            return fromAddressBytes(B16.decode(value));
        } else {
            if (value.startsWith("r") && value.length() >= 26) {
                return fromAddress(value);
            }
            AccountID accountID = accountForAlias(value);
            if (accountID == null) {
                throw new UnknownAlias("Alias unset: " + value);
            }
            return accountID;
        }
    }

    /**
     * fromAddress
     * @param address Account address.
     * @return AccountID.
     */
    static public AccountID fromAddress(String address) {
        byte[] bytes = getB58IdentiferCodecs().decodeAddress(address);
        return new AccountID(bytes, address);
    }

    /**
     * From keypair.
     * @param kp keypair.
     * @return AccountID.
     */
    public static AccountID fromKeyPair(IKeyPair kp) {
        byte[] bytes = kp.pub160Hash();
        return new AccountID(bytes, encodeAddress(bytes));
    }

    /**
     * From passphrase.
     * @param phrase Passphrase.
     * @return AccountID.
     */
    public static AccountID fromPassPhrase(String phrase) {
        return fromKeyPair(Seed.fromPassPhrase(phrase).keyPair());
    }

    /**
     * fromSeedString
     * @param seed Seed.
     * @return return value.
     */
    static public AccountID fromSeedString(String seed) {
        return fromKeyPair(Seed.getKeyPair(seed));
    }

    /**
     * From Seed byte array.
     * @param seed Seed byte array.
     * @return AccountID.
     */
    static public AccountID fromSeedBytes(byte[] seed) {
        return fromKeyPair(Seed.getKeyPair(seed));
    }

    /**
     * Get AccountID from integer.
     * @param n Integer value.
     * @return AccountID.
     */
    static public AccountID fromInteger(Integer n) {
        // The hash160 constructor will extend the 4bytes address
        return fromBytes(new Hash160(new UInt32(n).toByteArray()).bytes());
    }

    /**
     * From bytes.
     * @param bytes byte array.
     * @return AccountID.
     */
    public static AccountID fromBytes(byte[] bytes) {
        return new AccountID(bytes, encodeAddress(bytes));
    }

    /**
     * Create from Address bytes.
     * @param bytes address bytes.
     * @return AccountID.
     */
    static public AccountID fromAddressBytes(byte[] bytes) {
        return fromBytes(bytes);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public String toString() {
        return address;
    }

    /**
     * Isue.
     * @param code code.
     * @return return value.
     */
    public Issue issue(String code) {
        return new Issue(Currency.fromString(code), this);
    }

    /**
     * Issue.
     * @param c Currency.
     * @return Issue.
     */
    public Issue issue(Currency c) {
        return new Issue(c, this);
    }

    /**
     * isNativeIssuer.
     * @return return value.
     */
    public boolean isNativeIssuer() {
        return equals(XRP_ISSUER);
    }

    // SerializedType interface implementation
    @Override
    public Object toJSON() {
        return toString();
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
        to.add(bytes());
    }

    @Override
    public Type type() {
        return Type.AccountID;
    }

    /**
     * line-index.
     * @param issue Issue.
     * @return return value.
     */
    public Hash256 lineIndex(Issue issue) {
        if (issue.isNative()) throw new AssertionError();
        return Index.rippleState(this, issue.issuer(), issue.currency());
    }

    public static class Translator extends TypeTranslator<AccountID> {
        @Override
        public AccountID fromParser(BinaryParser parser, Integer hint) {
            if (hint == null) {
                hint = 20;
            }
            return AccountID.fromAddressBytes(parser.read(hint));
        }

        @Override
        public String toString(AccountID obj) {
            return obj.toString();
        }

        @Override
        public AccountID fromString(String value) {
            return AccountID.fromString(value);
        }
    }

    //

    static public Translator translate = new Translator();

    // helpers

    private static String encodeAddress(byte[] a) {
        return getB58IdentiferCodecs().encodeAddress(a);
    }

    /**
     * addAliasFromPassPhrase
     * @param n  n
     * @param n2 Phrase.
     * @return AccountID.
     */
    public static AccountID addAliasFromPassPhrase(String n, String n2) {
        return aliases.put(n, fromPassPhrase(n2));
    }

    /**
     * accountForAlias
     * @param value value.
     * @return return value.
     */
    public static AccountID accountForAlias(String value) {
        return aliases.get(value);
    }

    /**
     * accountField
     * @param f f.
     * @return return value.
     */
    public static AccountIDField accountField(final Field f) {
        return new AccountIDField() {
            @Override
            public Field getField() {
                return f;
            }
        };
    }

    static public AccountIDField Account = accountField(Field.Account);
    static public AccountIDField Owner = accountField(Field.Owner);
    static public AccountIDField Destination = accountField(Field.Destination);
    static public AccountIDField Issuer = accountField(Field.Issuer);
    static public AccountIDField Target = accountField(Field.Target);
    static public AccountIDField RegularKey = accountField(Field.RegularKey);
    static public AccountIDField User = accountField(Field.User);
    static public AccountIDField OriginalAddress = accountField(Field.OriginalAddress);

    // Exceptions
    public static class UnknownAlias extends RuntimeException {
        /**
         *
         */
        private static final long serialVersionUID = -8042838677708510072L;

        public UnknownAlias(String s) {
            super(s);
        }
    }
}
