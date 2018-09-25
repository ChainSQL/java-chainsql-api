/**
 * 
 */
package com.peersafe.chainsql.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.logging.Level;

import org.json.JSONObject;

import com.peersafe.base.client.Client;
import com.peersafe.base.client.requests.Request;
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
	 * @param amount Currency Amounts to transfer
	 * @return You can use this to call other Ripple functions continually.
	 */
	private Ripple pay(String accountId,Amount amount){
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		if(amount.currency() != Currency.ZXC)
		{
			Request request = this.connection.client.accountInfo(amount.issuer());
			if(request.response.result!=null){
				String transferFeeMax = request.response.result.optJSONObject("account_data").getString("TransferFeeMax");
				BigDecimal bigCount = new BigDecimal(transferFeeMax);
				Amount maxAmount = new Amount(bigCount.add(amount.value()), amount.currency(), amount.issuer());
				mTxJson.put("SendMax", maxAmount.toJSON());
			}
		}
		mTxJson.put("Destination", accountId);
		mTxJson.put("Amount", amount.toJSON());
		mTxJson.put("TransactionType", "Payment");
		return this;
	}
	
	/**
	 * Start a payment transaction, can be used to activate account 
	 * @param accountId The Address of an account.
	 * @param value		Count of coins to transfer,max value:1e11.
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple pay(String accountId,String value){
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount);
		return pay(accountId, amount);
	}

	/**
	 * 
	 * @param accountId The Address of an account.
	 * @param value Count of coins to transfer,max value:1e11.
	 * @param sCurrency  Arbitrary code for currency.
	 * @param sIssuer currency Issuer address
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple pay(String accountId, String value, String sCurrency, String sIssuer)
	{
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount, Currency.fromString(sCurrency), AccountID.fromAddress(sIssuer));
		return pay(accountId, amount);
	}
	
	/**
	 * 
	 * @param contract_address The Address of a contract account.
	 * @param value Count of coins to transfer.
	 * @param gasLimit The maximum amount of gas available
	 * @return You can use this to call other Ripple functions continually.
	 */
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
	
	/**
	 * 
	 * @param sDestAddr Address to receive escrowed amount
	 * @param amount Amounts to escrow
	 * @param dateFormatTMFinish The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when the escrowed XRP can be released to the recipient.
	 * @param dateFormatTMCancel The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when this escrow expires.
	 * @return You can use this to call other Ripple functions continually.
	 * @throws Exception
	 */
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

	/**
	 * 
	 * @param sDestAddr Address to receive escrowed amount
	 * @param value Amounts to escrow
	 * @param dateFormatTMFinish The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when the escrowed XRP can be released to the recipient.
	 * @param dateFormatTMCancel The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when this escrow expires.
	 * @return You can use this to call other Ripple functions continually.
	 * @throws Exception
	 */
	public Ripple escrowCreate(String sDestAddr, String value, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		BigDecimal bigValue = new BigDecimal(value);
		Amount amount = new Amount(bigValue);
		return escrowCreate(sDestAddr, amount, dateFormatTMFinish, dateFormatTMCancel);
	}

	/**
	 * 
	 * @param sDestAddr Address to receive escrowed amount
	 * @param value Amounts to escrow
	 * @param sCurrency  Arbitrary code for currency.
	 * @param sIssuer currency Issuer
	 * @param dateFormatTMFinish The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when the escrowed XRP can be released to the recipient.
	 * @param dateFormatTMCancel The time(format:yyyy-MM-dd HH:mm:ss), in seconds since the Ripple Epoch, when this escrow expires.
	 * @return You can use this to call other Ripple functions continually.
	 * @throws Exception
	 */
	public Ripple escrowCreate(String sDestAddr, String value, String sCurrency, String sIssuer, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount, Currency.fromString(sCurrency), AccountID.fromAddress(sIssuer));
		return escrowCreate(sDestAddr, amount, dateFormatTMFinish, dateFormatTMCancel);
	}
	
	/**
	 * 
	 * @param sOwner Address of the source account that funded the held payment.
	 * @param nCreateEscrowSeq Transaction sequence of EscrowCreate transaction that created the held payment to finish.
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple escrowExecute(String sOwner, int nCreateEscrowSeq)
	{
		mTxJson = new JSONObject();
		mTxJson.put("Owner", sOwner);
		mTxJson.put("OfferSequence", nCreateEscrowSeq);
		mTxJson.put("TransactionType", "EscrowFinish");
		return this;
	}

	/**
	 * 
	 * @param sOwner Address of the source account that funded the held payment.
	 * @param nCreateEscrowSeq Transaction sequence of EscrowCreate transaction that created the held payment to finish.
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple escrowCancel(String sOwner, int nCreateEscrowSeq)
	{
		mTxJson = new JSONObject();
		mTxJson.put("Owner", sOwner);
		mTxJson.put("OfferSequence", nCreateEscrowSeq);
		mTxJson.put("TransactionType", "EscrowCancel");
		return this;
	}

	/**
	 * accountSet
	 * @param flag accountSet flag which can be enable or disabled for an account
	 * @param bSet true:SetFlag; false:ClearFlag
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple accountSet(int nFlag, boolean bSet)
	{
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		if(bSet)
			mTxJson.put("SetFlag", nFlag);
		else
			mTxJson.put("ClearFlag", nFlag);
		mTxJson.put("TransactionType", "AccountSet");
		//
		return this;
	}
	
	/**
	 * 
	 * @param transferRate    1.0 - 2.0 string
	 * @param transferFeeMin  decimal number string
	 * @param transferFeeMax  decimal number string
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple accountSet(String transferRate, String transferFeeMin, String transferFeeMax)
	{
		//
		double rate = 1.0;double feeMin;double feeMax;
		try {
			rate = Double.parseDouble(transferRate);
			if(rate<1.0 || rate>2.0)
			{
				Client.logger.log(Level.WARNING, "TransferRate must be a number >= 1.0 && <= 2.0");
				return null;
			}
			feeMin = Double.parseDouble(transferFeeMin);
			feeMax = Double.parseDouble(transferFeeMax);
			if(feeMin < 0 || feeMax <0)
			{
				Client.logger.log(Level.WARNING, "opt.min or opt.max cannot be less than 0");
				return null;
			}
			if(feeMin > feeMax)
			{
				Client.logger.log(Level.WARNING, "min cannot be greater than max");
				return null;
			}
			//
			if(feeMin == feeMax && feeMin>0)
			{
				if(rate != 1.0)
				{
					Client.logger.log(Level.WARNING, "Cannot set transferRate if set fixed fee");
					return null;
				}
			}
		}
		catch(Exception e)
		{
			Client.logger.log(Level.WARNING, e + "\nTransferRate must be a number >= 1.0 && <= 2.0; TransferFeeMin and TransferFeeMax must be decimal number string.");
			return null;
		}
		transferRate = transferRate.replace(".", "");
		int nLen = 10 - transferRate.length();
		while (nLen>0)
		{
			transferRate = transferRate.concat("0");
			nLen--;
		}
		transferFeeMin = Util.toHexString(transferFeeMin);
		transferFeeMax = Util.toHexString(transferFeeMax);
		
		//
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("TransferRate", transferRate);
		mTxJson.put("TransferFeeMin", transferFeeMin);
		mTxJson.put("TransferFeeMax", transferFeeMax);
		mTxJson.put("TransactionType", "AccountSet");
		//
		return this;
	}

	/**
	 * 
	 * @param value Amounts to escrow
	 * @param sCurrency  Arbitrary code for currency.
	 * @param sIssuer currency Issuer
	 * @return You can use this to call other Ripple functions continually.
	 */
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
