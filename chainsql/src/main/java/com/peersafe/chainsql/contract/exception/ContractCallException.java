package com.peersafe.chainsql.contract.exception;

/**
 * Exception resulting from issues calling methods on Smart Contracts.
 */
public class ContractCallException extends RuntimeException {
	public int error_code;
    public ContractCallException(String message) {
        super(message);
    }
    public ContractCallException(String message,int error_code) {
    	super(message);
    	this.error_code = error_code;
    }
    public ContractCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
