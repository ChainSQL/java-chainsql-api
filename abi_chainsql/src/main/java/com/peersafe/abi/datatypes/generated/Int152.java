package com.peersafe.abi.datatypes.generated;

import java.math.BigInteger;
import com.peersafe.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.web3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class Int152 extends Int {
    public static final Int152 DEFAULT = new Int152(BigInteger.ZERO);

    public Int152(BigInteger value) {
        super(152, value);
    }

    public Int152(long value) {
        this(BigInteger.valueOf(value));
    }
}
