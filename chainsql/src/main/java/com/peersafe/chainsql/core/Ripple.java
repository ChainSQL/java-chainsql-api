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
	
	public void setTxJson(JSONObject obj) {
		mTxJson = obj;
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
	 * @return You can use this to call other Chainsql functions continuely.
	 */
	public JSONObject pay(String accountId,String count){
		JSONObject obj = new JSONObject();
		obj.put("Account", this.connection.address);
		obj.put("Destination", accountId);
		BigInteger bigCount = new BigInteger(count);
		BigInteger amount = bigCount.multiply(BigInteger.valueOf(1000000));
		obj.put("Amount", amount.toString());
		obj.put("TransactionType", "Payment");
		return SubmitTransaction(obj, TransactionType.Payment);
	}
	
	public JSONObject payToContract(String contract_address, String count, int gasLimit) {
		JSONObject obj = new JSONObject();
		obj.put("Account", this.connection.address);
		obj.put("ContractAddress", contract_address);
		obj.put("ContractOpType", 2);
		obj.put("Gas", gasLimit);
		BigInteger bigCount = new BigInteger(count);
		BigInteger amount = bigCount.multiply(BigInteger.valueOf(1000000));
		obj.put("ContractValue", amount.toString());
		obj.put("ContractData", "");
		obj.put("TransactionType", "Contract");
		
		//
		return SubmitTransaction(obj, TransactionType.Contract);
	}

	private JSONObject SubmitTransaction(JSONObject obj, TransactionType type) {
		mTxJson = obj;
		Transaction payment;
		try {
			payment = toTransaction(obj, type);
			signed = payment.sign(this.connection.secret);
			return doSubmitNoPrepare();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
