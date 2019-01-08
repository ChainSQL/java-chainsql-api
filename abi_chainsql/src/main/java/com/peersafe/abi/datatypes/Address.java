package com.peersafe.abi.datatypes;

import static com.peersafe.account.Config.getB58IdentiferCodecs;

import java.math.BigInteger;

import org.web3j.utils.Numeric;

import com.peersafe.abi.datatypes.generated.Uint160;
import com.peersafe.account.Utils;

/**
 * Address type, which is equivalent to uint160.
 */
public class Address implements Type<String> {

    public static final String TYPE_NAME = "address";
    public static final int LENGTH = 160;
    public static final int LENGTH_IN_HEX = LENGTH >> 2;
    public static final Address DEFAULT = new Address(BigInteger.ZERO);

    private final Uint160 value;

    public Address(Uint160 value) {
        this.value = value;
    }

    public Address(BigInteger value) {
        this(new Uint160(value));
    }

    public Address(String addr) {
    	String hexValue = addr;
    	if(addr.length() == 42)
    		addr = addr.substring(2);
    	if(addr.length() != 40) {
    		byte[] bytes = getB58IdentiferCodecs().decodeAddress(addr);
    		hexValue = Utils.bytesToHex(bytes);
    	}
    			
    	this.value = new Uint160(Numeric.toBigInt(hexValue));
    }

    public Uint160 toUint160() {
        return value;
    }

    @Override
    public String getTypeAsString() {
        return TYPE_NAME;
    }

    @Override
    public String toString() {
        return Numeric.toHexStringWithPrefixZeroPadded(
                value.getValue(), LENGTH_IN_HEX);
    }

    @Override
    public String getValue() {
        return toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Address address = (Address) o;

        return value != null ? value.equals(address.value) : address.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
