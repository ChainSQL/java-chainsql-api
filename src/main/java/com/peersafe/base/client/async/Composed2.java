package com.peersafe.base.client.async;

abstract public class Composed2<T1, T2> extends ComposedOperation<T1, T2, Object, Object> {
	/**
	 * Default comments.
	 */
    @Override
    protected int numOps() {
        return 2;
    }
	/**
	 * Default comments.
	 */
    @Override
    protected void finished() {
        on2(first, second);
    }

    public abstract void on2(T1 first, T2 second);
}
