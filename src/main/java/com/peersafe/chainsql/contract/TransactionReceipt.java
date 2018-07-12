
package com.peersafe.chainsql.contract;

import java.math.BigInteger;

import org.json.JSONObject;
import org.web3j.utils.Numeric;

/**
 * TransactionReceipt object used by {@link EthGetTransactionReceipt}.
 */
public class TransactionReceipt {
    private String transactionHash;
    private String contractAddress;
    private JSONObject transactionSubRet;
    private String status;

    public TransactionReceipt() {
    	
    }

    public TransactionReceipt(String contractAddress,JSONObject ret) {
    	if(ret.has("tx_hash")) {
    		this.transactionHash = ret.getString("tx_hash");	
    	}        
        this.contractAddress = contractAddress;
        this.transactionSubRet = ret;
        if(ret.has("status")) {
        	this.status = ret.getString("status");	
        }        
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

   
    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
    
    public void setTransactionSubRet(JSONObject ret) {
    	this.transactionSubRet = ret;
    }
    
    public JSONObject getTransactionSubRet() {
    	return this.transactionSubRet;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionReceipt)) {
            return false;
        }

        TransactionReceipt that = (TransactionReceipt) o;

        if (getTransactionHash() != null
                ? !getTransactionHash().equals(that.getTransactionHash())
                : that.getTransactionHash() != null) {
            return false;
        }
       
        if (getContractAddress() != null
                ? !getContractAddress().equals(that.getContractAddress())
                : that.getContractAddress() != null) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return "TransactionReceipt{"
                + "transactionHash='" + transactionHash + '\''
                + ", contractAddress='" + contractAddress +'\''
                + ",status='" + status + '\''
                + '}';
    }
}
