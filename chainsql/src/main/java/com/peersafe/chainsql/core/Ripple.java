/**
 * 
 */
package com.peersafe.chainsql.core;

import java.math.BigInteger;

import org.json.JSONObject;

import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.chainsql.util.Util;
import com.peersafe.chainsql.util.Validate;

/**
 * @author mail_
 *
 */
public class Ripple extends Submit {
	
	private JSONObject mTxJson;

	public Ripple(Chainsql chainsql) {
		this.connection = chainsql.connection;
	}
	
	@Override
	JSONObject prepareSigned() {
		try {
			if(mTxJson.toString().equals("{}")) {
				return Util.errorObject("Exception occured");
			}
			mTxJson.put("Account",this.connection.address);

			String sType = mTxJson.get("TransactionType").toString();
			if(sType.isEmpty()) {
				return Util.errorObject("Exception occured, no exist TransactionType");
			}
			//
			TransactionType type = TransactionType.translate.fromString(sType);
	    	Transaction payment = toTransaction(mTxJson, type);
			
			signed = payment.sign(this.connection.secret);
			
			return Util.successObject();
		} catch (Exception e) {
//			e.printStackTrace();
			return Util.errorObject(e.getMessage());
		}
	}

	
	/**
	 * Start a payment transaction, can be used to activate account 
	 * @param accountId The Address of an account.
	 * @param count		Count of coins to transfer,max value:1e11.
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple pay(String accountId,String count){
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("Destination", accountId);
		BigInteger bigCount = new BigInteger(count);
		BigInteger amount = bigCount.multiply(BigInteger.valueOf(1000000));
		mTxJson.put("Amount", amount.toString());
		mTxJson.put("TransactionType", "Payment");
		return this;
	}
	
	public Ripple payToContract(String contract_address, String count, int gasLimit) {
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("ContractAddress", contract_address);
		mTxJson.put("ContractOpType", 2);
		mTxJson.put("Gas", gasLimit);
		BigInteger bigCount = new BigInteger(count);
		BigInteger amount = bigCount.multiply(BigInteger.valueOf(1000000));
		mTxJson.put("ContractValue", amount.toString());
		mTxJson.put("ContractData", "");
		mTxJson.put("TransactionType", "Contract");
		return this;
	}

}
