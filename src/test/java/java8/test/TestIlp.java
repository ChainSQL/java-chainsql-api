package java8.test;

import java.math.BigInteger;

import com.peersafe.base.ilp.IlpInterface;

public class TestIlp {
	
	public static void main(String[] args) {		
		IlpInterface ilp = new IlpInterface();
		String pluginPrefix = "g.crypto.ripple.escrow1.";
		String srcAccount = "g.crypto.ripple.escrow1.rQGkq415dqDij5EkfvV5nTZ8EYbGc3Xr7G";
		System.out.println("begin to generate SharedSecret" );
		String sharedSecret = ilp.generateSharedSecret();
		System.out.println("SharedSecret:" + sharedSecret + "\n");

		System.out.println("begin to generate Packet" );
		String destinationAccount = "g.crypto.ripple.escrow2.rQGCddPFfcPmSsca4rvKwerRnQmAZcSN9o";
		BigInteger destAmount = BigInteger.valueOf(12300);
		String packetStr = ilp.generatePacket(sharedSecret, destinationAccount, destAmount);
		System.out.println("Packet:" + packetStr + "\n");
	

		System.out.println("begin to generate Condition" );
		String conditionStr = ilp.generateCondition(packetStr);
		System.out.println("Condition:" + conditionStr + "\n");

		System.out.println("begin to generate Fulfillment" );
		String fulfillmentStr = ilp.generateFulfillment(packetStr);
		System.out.println("Fulfillment:" + fulfillmentStr + "\n");

		System.out.println("begin to generate FulfillmentAndCondition" );
		StringBuffer fulfillment = new StringBuffer();
		StringBuffer condition = new StringBuffer();
		ilp.generateFulfillmentAndCondition(fulfillment, condition);
		System.out.println("Fulfillment:" + fulfillment.toString() );
		System.out.println("Condition:" + condition.toString() + "\n");

		//System.out.println("begin to generate Condition by fulfillment" );
		//String ConByFulStr = ilp.fulfillment2Condition(String("di56TJ_tdOhjhBz8dB3Jig"));
		//String ConByFulStr = ilp.fulfillment2Condition(fulfillmentStr);
		//System.out.println("Condition by fulfillment:" << ConByFulStr << std::endl );

		System.out.println("begin to generate QuoteMessage" );
		String dstAccount = "g.crypto.ripple.escrow2.rQGCddPFfcPmSsca4rvKwerRnQmAZcSN9o";
		String quoteMessageStr = ilp.generateQuoteMessage(pluginPrefix, srcAccount, dstAccount, destAmount);
		System.out.println("QuoteMessage:" + quoteMessageStr + "\n");

		System.out.println("begin to parse QuoteResponse : Bw0AAAAAAAAwDQAAKvgA" );
		String quoteResponseStr = "Bw0AAAAAAAAwDQAAKvgA";
		BigInteger quoteResponseAmount = ilp.parseQuoteResult(quoteResponseStr);
		System.out.println("quoteResponseAmount:" + quoteResponseAmount + "\n");

	}
}
