/**
 * 
 */
package com.peersafe.chainsql.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.Currency;
import com.peersafe.base.core.coretypes.RippleDate;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.chainsql.util.Util;

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
	 * @return You can use this to call other Ripple functions continually.
	 */
	private Ripple pay(String accountId,Amount amount){
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("Destination", accountId);
		mTxJson.put("Amount", amount.toJSON());
		mTxJson.put("TransactionType", "Payment");
		return this;
	}
	public Ripple pay(String accountId,String value){
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount);
		return pay(accountId, amount);
	}
	
	public Ripple pay(String accountId, String value, String sCurrency, String sIssuer)
	{
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount, Currency.fromString(sCurrency), AccountID.fromAddress(sIssuer));
		return pay(accountId, amount);
	}
	
	public Ripple payToContract(String contract_address, String value, int gasLimit) {
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("ContractAddress", contract_address);
		mTxJson.put("ContractOpType", 2);
		mTxJson.put("Gas", gasLimit);
		BigInteger bigCount = new BigInteger(value);
		BigInteger amount = bigCount.multiply(BigInteger.valueOf(1000000));
		mTxJson.put("ContractValue", amount.toString());
		mTxJson.put("ContractData", "");
		mTxJson.put("TransactionType", "Contract");
		return this;
	}
	
	private Ripple escrowCreate(String sDestAddr, Amount amount, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		long diffFinish = RippleDate.secondsSinceRippleEpoch(dateFormatTMFinish);
		long diffCancel = RippleDate.secondsSinceRippleEpoch(dateFormatTMCancel);
		if(diffFinish >= diffCancel)
		{
			throw new Exception("\"CancelAfter\" must be after \"FinishAfter\" for EscrowCreate!");
		}
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("Destination", sDestAddr);
		mTxJson.put("Amount", amount.toJSON());
		mTxJson.put("FinishAfter", diffFinish);
		mTxJson.put("CancelAfter", diffCancel);
		mTxJson.put("TransactionType", "EscrowCreate");
		return this;
	}

	public Ripple escrowCreate(String sDestAddr, String value, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		BigDecimal bigValue = new BigDecimal(value);
		Amount amount = new Amount(bigValue);
		return escrowCreate(sDestAddr, amount, dateFormatTMFinish, dateFormatTMCancel);
	}

	public Ripple escrowCreate(String sDestAddr, String value, String sCurrency, String sIssuer, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount, Currency.fromString(sCurrency), AccountID.fromAddress(sIssuer));
		return escrowCreate(sDestAddr, amount, dateFormatTMFinish, dateFormatTMCancel);
	}
	
	public Ripple escrowExecute(String sOwner, int nCreateEscrowSeq)
	{
		mTxJson = new JSONObject();
		mTxJson.put("Owner", sOwner);
		mTxJson.put("OfferSequence", nCreateEscrowSeq);
		mTxJson.put("TransactionType", "EscrowFinish");
		return this;
	}

	public Ripple escrowCancel(String sOwner, int nCreateEscrowSeq)
	{
		mTxJson = new JSONObject();
		mTxJson.put("Owner", sOwner);
		mTxJson.put("OfferSequence", nCreateEscrowSeq);
		mTxJson.put("TransactionType", "EscrowCancel");
		return this;
	}
	
	public Ripple accountSet()
	{
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("SetFlag", 8);//rippling
		mTxJson.put("TransactionType", "AccountSet");
		//
		return this;
	}

	public Ripple trustSet(String value, String sCurrency, String sIssuer)
	{
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount, Currency.fromString(sCurrency), AccountID.fromAddress(sIssuer));
		mTxJson = new JSONObject();
		mTxJson.put("LimitAmount", amount.toJSON());
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("TransactionType", "TrustSet");
		//
		return this;
	}
}
