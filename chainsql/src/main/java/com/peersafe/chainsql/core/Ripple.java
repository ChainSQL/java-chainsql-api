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
	
	private final static Double PRECISION = 0.0000001;
	
	private JSONObject mTxJson;

	public Ripple(Chainsql chainsql) {
		this.connection = chainsql.connection;
		this.eventManager = chainsql.eventManager;
	}

	public void setTxJson(JSONObject obj) {
		mTxJson = obj;
	}
	
	@Override
	protected JSONObject prepareSigned() {
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
	 * Start a payment transaction, can be used to activate account or transfer currency
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
				BigDecimal value = amount.value();
				JSONObject accountData = request.response.result.optJSONObject("account_data");
				if(accountData != null)
				{
					String feeMin = null, feeMax = null;
					long lFeeRate = 0;
					if(accountData.has("TransferFeeMin"))
						feeMin = accountData.getString("TransferFeeMin");
					if(accountData.has("TransferFeeMax"))
						feeMax = accountData.getString("TransferFeeMax");
					if(accountData.has("TransferRate"))
					{
						lFeeRate = accountData.getLong("TransferRate");
					}
					//
					if((null!=feeMin) || (null!=feeMax) || lFeeRate != 0)
					{
						if(feeMin.equals(feeMax) && (!feeMin.isEmpty()))
						{
							value = value.add(new BigDecimal(feeMin));
						}
						else if(lFeeRate>1000000000 && lFeeRate<=2000000000)
						{
							BigDecimal baseComputeNum = new BigDecimal(1000000000);
							BigDecimal rate = new BigDecimal(lFeeRate).subtract(baseComputeNum);
							rate = rate.divide(baseComputeNum);
							//
							BigDecimal fee = value.multiply(rate);
							if ((null!=feeMin) && (!feeMin.isEmpty())) {
								if (new BigDecimal(feeMin).compareTo(fee) > 0) {
									fee = new BigDecimal(feeMin);
								}
							}
							if ((null!=feeMax) && (!feeMax.isEmpty())) {
								if (fee.compareTo(new BigDecimal(feeMax)) > 0) {
									fee = new BigDecimal(feeMax);
								}
							}
							//
							value = value.add(fee);
						}
						else
						{
							try {
								throw new Exception("Exception:transfer fee not valid!");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				Amount maxAmount = new Amount(value, amount.currency(), amount.issuer());
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
	 * @param value		Count of ZXC to transfer,max value:1e11.
	 * @return You can use this to call other Ripple functions continually.
	 */
	public Ripple pay(String accountId,String value){
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount);
		return pay(accountId, amount);
	}

	/**
	 * Start a payment transaction, can be used to transfer currency
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
	 * @param value Count of ZXC to transfer.
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
	 * Sequester currency until the escrow process either finishes or is canceled
	 * @param sDestAddr Address to receive escrowed currency
	 * @param amount Amounts to escrow
	 * @param dateFormatTMFinish The local time(format:yyyy-MM-dd HH:mm:ss)
	 * @param dateFormatTMCancel The local time(format:yyyy-MM-dd HH:mm:ss)
	 * @return You can use this to call other Ripple functions continually.
	 * @throws Exception Exceptions.
	 */
	private Ripple escrowCreate(String sDestAddr, Amount amount, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		
		long diffFinish = 0;
		if(!dateFormatTMFinish.equals("")) {
			diffFinish = RippleDate.secondsSinceRippleEpoch(dateFormatTMFinish);
		}
		long diffCancel = 0;
		if(!dateFormatTMCancel.equals("")) {
			diffCancel = RippleDate.secondsSinceRippleEpoch(dateFormatTMCancel);
		}
		if(diffFinish != 0 && diffCancel != 0 && diffFinish >= diffCancel)
		{
			throw new Exception("\"CancelAfter\" must be after \"FinishAfter\" for EscrowCreate!");
		}else if(diffFinish == 0 && diffCancel == 0) {
			throw new Exception("Either \"CancelAfter\" or \"FinishAfter\" should be valid for EscrowCreate!");
		}
		mTxJson = new JSONObject();
		mTxJson.put("Account", this.connection.address);
		mTxJson.put("Destination", sDestAddr);
		mTxJson.put("Amount", amount.toJSON());
		if(diffFinish != 0) {
			mTxJson.put("FinishAfter", diffFinish);	
		}
		if(diffCancel != 0) {
			mTxJson.put("CancelAfter", diffCancel);	
		}
		mTxJson.put("TransactionType", "EscrowCreate");
		return this;
	}

	/**
	 * Sequester currency until the escrow process either finishes or is canceled
	 * @param sDestAddr Address to receive escrowed currency
	 * @param value Amounts of ZXC to escrow
	 * @param dateFormatTMFinish The local time(format:yyyy-MM-dd HH:mm:ss)
	 * @param dateFormatTMCancel The local time(format:yyyy-MM-dd HH:mm:ss)
	 * @return You can use this to call other Ripple functions continually.
	 * @throws Exception Exceptions.
	 */
	public Ripple escrowCreate(String sDestAddr, String value, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		BigDecimal bigValue = new BigDecimal(value);
		Amount amount = new Amount(bigValue);
		return escrowCreate(sDestAddr, amount, dateFormatTMFinish, dateFormatTMCancel);
	}

	/**
	 * Sequester currency until the escrow process either finishes or is canceled
	 * @param sDestAddr Address to receive escrowed currency
	 * @param value Amounts to escrow
	 * @param sCurrency  Arbitrary code for currency.
	 * @param sIssuer currency Issuer
	 * @param dateFormatTMFinish The local time(format:yyyy-MM-dd HH:mm:ss)
	 * @param dateFormatTMCancel The local time(format:yyyy-MM-dd HH:mm:ss)
	 * @return You can use this to call other Ripple functions continually.
	 * @throws Exception Exceptions.
	 */
	public Ripple escrowCreate(String sDestAddr, String value, String sCurrency, String sIssuer, String dateFormatTMFinish, String dateFormatTMCancel) throws Exception
	{
		BigDecimal bigCount = new BigDecimal(value);
		Amount amount = new Amount(bigCount, Currency.fromString(sCurrency), AccountID.fromAddress(sIssuer));
		return escrowCreate(sDestAddr, amount, dateFormatTMFinish, dateFormatTMCancel);
	}
	
	/**
	 * Deliver currency from a held payment to the recipient
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
	 * Return escrowed currency to the sender
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
	 * An AccountSet transaction modifies the properties of an account in the Ledger, can be used to setFlag or clearFlag
	 * @param nFlag accountSet flag which can be enabled or disabled for an account
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
	 * An AccountSet transaction modifies the properties of an account in the Ledger, can be used to setTransferFee.
	 * @param transferRate    decimal number string
	 * (1.0 - 2.0] : set transferRate
	 * 0/1.0 : decimal number string, cancel tranferRate, no fee or charge fixed fee
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
			if((rate != 0) && rate<1.0 || rate>2.0)
			{
				Client.logger.log(Level.WARNING, "TransferRate must be 0 or a number >= 1.0 && <= 2.0");
				return null;
			}
			feeMin = Double.parseDouble(transferFeeMin);
			feeMax = Double.parseDouble(transferFeeMax);
			if(feeMin < 0 || feeMax <0)
			{
				Client.logger.log(Level.WARNING, "min or max cannot be less than 0");
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
				if(rate>PRECISION && rate-1.0 > PRECISION)
				{
					Client.logger.log(Level.WARNING, "fee mismatch transferRate");
					return null;
				}
			}
			if(feeMin < feeMax) {
				if(rate<PRECISION || rate-1.0 < PRECISION)
				{
					Client.logger.log(Level.WARNING, "fee mismatch transferRate");
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
	 * Create or modify a trust line linking two accounts
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
