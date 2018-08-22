package com.peersafe.chainsql.contract.exception;

/**
 * Transaction timeout exception indicates that we have breached some threshold waiting for a
 * transaction to execute.
 */
public class TransactionException extends Exception {
	public int error_code;
    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message,int error_code) {
    	super(message);
    	this.error_code = error_code;
    }
    public TransactionException(Throwable cause) {
        super(cause);
    }
}
