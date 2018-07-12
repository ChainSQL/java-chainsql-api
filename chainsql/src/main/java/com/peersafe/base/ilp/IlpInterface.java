package com.peersafe.base.ilp;

import java.math.BigInteger;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.json.JSONObject;

import com.peersafe.base.core.coretypes.Blob;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.coretypes.uint.UInt64;
import com.peersafe.base.core.coretypes.uint.UInt8;
import com.peersafe.base.core.serialized.BytesList;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.base.utils.Utils;

public class IlpInterface {
	
	final static int SHARED_SECRET_LENGTH = 16;
	final static int PSK_TOKEN_LENGTH = 16;
	final static int DEFAULT_MESSAGE_TIMEOUT = 5000;
	final static int DEFAULT_EXPIRY_DURATION = 10;
	
	enum PacketType {
		TYPE_ILP_PAYMENT(1),
		TYPE_ILQP_LIQUIDITY_REQUEST(2),
		TYPE_ILQP_LIQUIDITY_RESPONSE (3),
		TYPE_ILQP_BY_SOURCE_REQUEST (4),
		TYPE_ILQP_BY_SOURCE_RESPONSE (5),
		TYPE_ILQP_BY_DESTINATION_REQUEST (6),
		TYPE_ILQP_BY_DESTINATION_RESPONSE (7),
		TYPE_ILP_ERROR (8);
		
		private PacketType(int value) {
			this.mValue = value;
		}
		public int value() {
			return mValue;
		}
		private int mValue;
	}
    
	public String generateSharedSecret() {
		Seed seed = Seed.randomSeed();
		
		String sharedSecretStr = "";
		try {
			sharedSecretStr = Utils.encodeBase64(seed.bytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sharedSecretStr;
	}
	
	public String generatePacket(String sharedSecret, String destAccount, BigInteger destAmount) {
		if(destAccount.isEmpty()) {
			return "";
		}else {
			BytesList bl = new BytesList();
			new UInt32(destAmount.shiftRight(32)).toBytesSink(bl);
			new UInt32(destAmount).toBytesSink(bl);
			new UInt8(destAccount.length()).toBytesSink(bl);
			new Blob(destAccount.getBytes()).toBytesSink(bl);;
			new UInt8(0).toBytesSink(bl);
			byte[] packetBinaryStr = serializeEnvelope(PacketType.TYPE_ILP_PAYMENT.value(),bl.bytes());
			try {
				return Utils.encodeBase64(packetBinaryStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return "";
	}
	public String generateCondition(String packet) {
		if(packet.isEmpty()) {
			return "";
		}else {
			return preimage2Condition(packet2Preimage(null, packet));
		}
	}
	public String generateFulfillment(String packet) {
		if (packet.isEmpty())
		{
			return "";
		}
		else
		{
			return preimage2Fulfillment(packet2Preimage(null, packet));
		}
	}
	
	public boolean generateFulfillmentAndCondition(StringBuffer fulfillmentStr, StringBuffer conditionStr) {
		byte[] preimageStr = packet2Preimage(null, "");
		fulfillmentStr.append(preimage2Fulfillment(preimageStr));
		conditionStr.append(preimage2Condition(preimageStr));
		return true;
	}
	public String generateQuoteMessage(String pluginPrefix, String srcAccountStr,
		String dstAccountStr, BigInteger destAmount) {
		
		JSONObject obj = new JSONObject();
		obj.put("ledger",pluginPrefix);
		obj.put("from", srcAccountStr);
		obj.put("to", dstAccountStr);
		obj.put("ilp", serializeQuoteRequest(dstAccountStr, destAmount));
		obj.put("timeout", String.valueOf(DEFAULT_MESSAGE_TIMEOUT));
		
		return obj.toString();
    }
	public BigInteger parseQuoteResult(String quoteResultPacket) {
		try {
			byte[] quoteResponseStr = Utils.decodeBase64(quoteResultPacket);
			if (quoteResponseStr[0] == PacketType.TYPE_ILQP_BY_DESTINATION_RESPONSE.value())
			{
				BytesList bl = new BytesList();
				bl.add(quoteResponseStr);
				
				int type = bl.get8(0);
				if (type != PacketType.TYPE_ILQP_BY_DESTINATION_RESPONSE.value()) 
				{
					return BigInteger.valueOf(-1);
				}
				else
				{
//					int infoLength = 0;
					int sourceAmountHigh;
					int sourceAmountLow;
					BigInteger sourceAmount;
//					infoLength = bl.get8(1);
					sourceAmountHigh = bl.getInteger(2);
					sourceAmountLow = bl.getInteger(6);
					sourceAmount = BigInteger.valueOf(sourceAmountHigh).shiftLeft(32).add(BigInteger.valueOf(sourceAmountLow));
					return sourceAmount;
				}
			}
			else
				return BigInteger.valueOf(-1);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return BigInteger.valueOf(-1);
    }
    
	private byte[] sha256(byte[] data) {
		SHA256Digest sha = new SHA256Digest();
		sha.update(data, 0, data.length);
	    byte[] result = new byte[sha.getDigestSize()];
	    sha.doFinal(result, 0);
	    return result;
	}
    private byte[] packet2Preimage(byte[] secret, String packet) {
    	Seed seed = Seed.randomSeed();
    	return sha256(seed.bytes());
    }
    private String preimage2Condition(byte[] preimageStr) {
    	byte[] sha = sha256(preimageStr);
    	try {
			return Utils.encodeBase64(sha);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return "";
	}
    private String preimage2Fulfillment(byte[] preimage) {
    	try {
			return Utils.encodeBase64(preimage);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return "";
	}
    private String fulfillment2Condition(String fulfillmentStr) {
    	byte[] fulfillmentBinaryStr;
		try {
			fulfillmentBinaryStr = Utils.decodeBase64(fulfillmentStr);
	    	return preimage2Condition(fulfillmentBinaryStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
    private String serializeQuoteRequest(String dstAccountStr, BigInteger destAmount) {
    	BytesList bl = new BytesList();
    	new UInt8(dstAccountStr.length()).toBytesSink(bl);
    	new Blob(dstAccountStr.getBytes()).toBytesSink(bl);
    	new UInt64(destAmount).toBytesSink(bl);
    	new UInt32(DEFAULT_EXPIRY_DURATION * 1000).toBytesSink(bl);
    	new UInt8(0).toBytesSink(bl);
		byte[] quoteRequestBinaryStr = serializeEnvelope(PacketType.TYPE_ILQP_BY_DESTINATION_REQUEST.value(),bl.bytes());
		try {
			return Utils.encodeBase64(quoteRequestBinaryStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
    private byte[] serializeEnvelope(int type, byte[] contents) {
    	BytesList bl = new BytesList();
    	new UInt8(type).toBytesSink(bl);;
    	new UInt8(contents.length).toBytesSink(bl);;
    	new Blob(contents).toBytesSink(bl);;
    	return bl.bytes();
	}
}
