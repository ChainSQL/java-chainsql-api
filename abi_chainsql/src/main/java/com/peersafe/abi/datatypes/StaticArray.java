package com.peersafe.abi.datatypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.peersafe.abi.datatypes.generated.AbiTypes;


/**
 * Static array type.
 */
public abstract class StaticArray<T extends Type> extends Array<T>  {

    /**
     * Warning: increasing this constant will cause more generated StaticArrayN types, see:
     * org.web3j.codegen.AbiTypesGenerator#generateStaticArrayTypes
     */
	   public static final int MAX_SIZE_OF_STATIC_ARRAY = 32;

	    @Deprecated
	    @SafeVarargs
	    public StaticArray(T... values) {
	        this(values.length, values);
	    }

	    @Deprecated
	    @SafeVarargs
	    public StaticArray(int expectedSize, T... values) {
	        this(expectedSize, Arrays.asList(values));
	    }

	    @Deprecated
	    public StaticArray(List<T> values) {
	        this(values.size(), values);
	    }

	    @Deprecated
	    @SuppressWarnings("unchecked")
	    public StaticArray(int expectedSize, List<T> values) {
	        super(
	                StructType.class.isAssignableFrom(values.get(0).getClass())
	                        ? (Class<T>) values.get(0).getClass()
	                        : (Class<T>) AbiTypes.getType(values.get(0).getTypeAsString()),
	                values);
	        checkValid(expectedSize);
	    }

	    @SafeVarargs
	    public StaticArray(Class<T> type, T... values) {
	        this(type, Arrays.asList(values));
	    }

	    @SafeVarargs
	    public StaticArray(Class<T> type, int expectedSize, T... values) {
	        this(type, expectedSize, Arrays.asList(values));
	    }

	    public StaticArray(Class<T> type, List<T> values) {
	        this(type, values == null ? 0 : values.size(), values);
	    }

	    public StaticArray(Class<T> type, int expectedSize, List<T> values) {
	        super(type, values);
	        checkValid(expectedSize);
	    }

	    @Override
	    public List<T> getValue() {
	        // Static arrays cannot be modified
	        return Collections.unmodifiableList(value);
	    }

	    @Override
	    public String getTypeAsString() {
	        String type;
	        if (StructType.class.isAssignableFrom(value.get(0).getClass())) {
	            type = value.get(0).getTypeAsString();
	        } else {
	            type = AbiTypes.getTypeAString(getComponentType());
	        }
	        return type + "[" + value.size() + "]";
	    }

	    private void checkValid(int expectedSize) {
	        if (value.size() > MAX_SIZE_OF_STATIC_ARRAY) {
	            throw new UnsupportedOperationException(
	                    "Static arrays with a length greater than "
	                            + MAX_SIZE_OF_STATIC_ARRAY
	                            + " are not supported.");
	        } else if (value.size() != expectedSize) {
	            throw new UnsupportedOperationException(
	                    "Expected array of type ["
	                            + getClass().getSimpleName()
	                            + "] to have ["
	                            + expectedSize
	                            + "] elements.");
	        }
	    }
}
