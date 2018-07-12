package com.peersafe.chainsql.core;

import org.json.JSONObject;

import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.chainsql.util.Util;

public class ContractOp extends Submit{
	
	private JSONObject mTxJson;
	
	public ContractOp(JSONObject obj,Chainsql chainsql) {
		mTxJson = obj;
		this.connection = chainsql.connection;
	}
	
	public void setTxJson(JSONObject obj) {
		mTxJson = obj;
	}
	
	@Override
	JSONObject prepareSigned() {
		try {
			if(mTxJson.toString().equals("{}")) {
				return Util.errorObject("Exception occured:Json not prepared");
			}
			mTxJson.put("Account",this.connection.address);
	    	
	    	Transaction tx = toTransaction(mTxJson,TransactionType.Contract);
			
			signed = tx.sign(this.connection.secret);
			
			return Util.successObject();
		} catch (Exception e) {
			return Util.errorObject(e.getMessage());
		}
	}
	
}
