package com.peersafe.base.client.async;

abstract public class ComposedOperation<T1, T2, T3, T4> {
    public T1 first;
    public T2 second;
    public T3 third;
    public T4 fourth;

    protected abstract void finished();
    protected abstract int numOps();

    int done = 0;

    private void doneOne() {
        if (++done == numOps()) {
            finished();
        }
    }
    /**
     * First.
     * @param pfirst Put first.
     */
    public void first(T1 pfirst) {
        first = pfirst;
        doneOne();
    }

    /**
     * Put second parameter.
     * @param psecond Second parameter.
     */
    public void second(T2 psecond) {
        second = psecond;
        doneOne();
    }

    /**
     * Put third parameter.
     * @param pthird Third parameter.
     */
    public void third(T3 pthird) {
        third = pthird;
        doneOne();
    }

    /**
     * Put fourth.
     * @param pfourth Fourth parameter.
     */
    public void fourth(T4 pfourth) {
        fourth = pfourth;
        doneOne();
    }
}
