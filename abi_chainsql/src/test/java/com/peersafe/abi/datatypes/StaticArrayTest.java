package com.peersafe.abi.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.stream.IntStream;

import org.junit.Test;

import com.peersafe.abi.datatypes.generated.StaticArray3;
import com.peersafe.abi.datatypes.generated.StaticArray32;
import com.peersafe.abi.datatypes.generated.Uint8;

public class StaticArrayTest {

	 @Test
	    public void canBeInstantiatedWithLessThan32Elements() {
	        final StaticArray<Uint> array = new StaticArray32<>(arrayOfUints(32));

	        assertEquals(array.getValue().size(), (32));
	    }

	    @Test
	    public void canBeInstantiatedWithSizeMatchingType() {
	        final StaticArray<Uint> array = new StaticArray3<>(arrayOfUints(3));

	        assertEquals(array.getValue().size(), (3));
	    }

	    @Test
	    public void throwsIfSizeDoesntMatchType() {
	        try {
	            new StaticArray3<>(arrayOfUints(4));
	            fail();
	        } catch (UnsupportedOperationException e) {
	            assertEquals(
	                    e.getMessage(),
	                    ("Expected array of type [StaticArray3] to have [3] elements."));
	        }
	    }

	    @Test
	    public void throwsIfSizeIsAboveMaxOf32() {
	        try {
	            new StaticArray32<>(arrayOfUints(33));
	            fail();
	        } catch (UnsupportedOperationException e) {
	            assertEquals(
	                    e.getMessage(),
	                    ("Static arrays with a length greater than 32 are not supported."));
	        }
	    }

	    private Uint[] arrayOfUints(int length) {
	        return IntStream.rangeClosed(1, length).mapToObj(Uint8::new).toArray(Uint[]::new);
	    }
}