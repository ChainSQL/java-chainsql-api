package com.peersafe.abi.datatypes.generated;

import java.math.BigInteger;
import com.peersafe.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.web3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class Int144 extends Int {
    public static final Int144 DEFAULT = new Int144(BigInteger.ZERO);

    public Int144(BigInteger value) {
        super(144, value);
    }

    public Int144(long value) {
        this(BigInteger.valueOf(value));
    }
}
