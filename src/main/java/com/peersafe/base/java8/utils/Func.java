package com.peersafe.base.java8.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Func {
	/**
	 * 
	 * @param BiConsumer<T, Y> consumer
	 * @param Y bind
	 * @return
	 */
    public static <T, Y> Consumer<T> bind(BiConsumer<T, Y> consumer, Y bind) {
        return t -> consumer.accept(t, bind);
    }
    /**
     * 
     * @param Function<T, Y> func
     * @param T value
     * @return
     */
    public static <T, Y> boolean itThrows(Function<T, Y> func, T value) {
        try {
            func.apply(value);
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
