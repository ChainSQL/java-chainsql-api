package com.peersafe.abi.datatypes.generated;

import java.util.List;
import com.peersafe.abi.datatypes.StaticArray;
import com.peersafe.abi.datatypes.Type;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.web3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class StaticArray20<T extends Type> extends StaticArray<T> {
    public StaticArray20(List<T> values) {
        super(20, values);
    }

    @SafeVarargs
    public StaticArray20(T... values) {
        super(20, values);
    }
}
