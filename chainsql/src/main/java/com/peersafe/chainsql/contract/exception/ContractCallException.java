package com.peersafe.chainsql.contract.exception;

/**
 * Exception resulting from issues calling methods on Smart Contracts.
 */
public class ContractCallException extends RuntimeException {
	public String error;
    public ContractCallException(String error) {
        super(error);
    }
    public ContractCallException(String error,String error_message) {
    	super(error_message);
    	this.error = error;
    }
    public ContractCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
