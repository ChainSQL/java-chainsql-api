package com.peersafe.example.chainsql;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;

public class TestSign {
	public static final Chainsql c = new Chainsql();
	public static void main(String[] args) {
		testSignPayment();
		testSignTrustSet();
		testSignAccountSet();
		testSignForPayment();
		testSignForTrustSet();
		testSignSignerListSet();
		testSignPathset();
		
		//签名任意数据
		testSignSimple();
	}
	
	private static void testSignSimple() {
		String hello = "helloworld";
		byte[] signature = c.sign(hello.getBytes(), "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		if(c.verify(hello.getBytes(), signature, "cBQG8RQArjx1eTKFEAQXz2gS4utaDiEC9wmi7pfUPTi27VCchwgw"))
		{
			System.out.println("verify success");
		}else {
			System.out.println("verify failed");
		}
	}
	
	private static void testSignPayment(){
		JSONObject obj = new JSONObject();
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		tx_json.put("Amount", "10000000000");
		tx_json.put("Destination", "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q");
		tx_json.put("TransactionType", "Payment");
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.sign(obj, "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign payment result:" + res);
	}
	
	private static void testSignTrustSet(){
		JSONObject obj = new JSONObject();
		
		JSONObject limitAmount = new JSONObject();
		limitAmount.put("currency", "GRD");
		limitAmount.put("issuer", "zhWACtoujvNmmER29zVXrCxxFzpitPajdm");
		limitAmount.put("value", "1000");
		
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh");
		tx_json.put("LimitAmount", limitAmount);
		tx_json.put("Flags", 0);
		tx_json.put("TransactionType", "TrustSet");
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.sign(obj, "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign TrustSet result:" + res);
	}

	private static void testSignAccountSet(){
		JSONObject obj = new JSONObject();
		obj.put("secret", "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh");
		tx_json.put("SetFlag", 8);
		tx_json.put("TransactionType", "AccountSet");
		JSONObject memo = new JSONObject();
		memo.put("MemoData", "687474703a2f2f6578616d706c652e636f6d2f6d656d6f2f67656e65726963");
		JSONObject memo2 = new JSONObject();
		memo2.put("Memo", memo);
		JSONArray arr = new JSONArray();
		arr.put(memo2);
		tx_json.put("Memos", arr);
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.sign(obj, "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign AccountSet result:" + res);
	}

	private static void testSignSignerListSet(){
		JSONObject obj = new JSONObject();
		obj.put("secret", "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh");
		tx_json.put("TransactionType", "SignerListSet");
		tx_json.put("SignerQuorum", 3);
		
		JSONObject entry1 = new JSONObject();
		entry1.put("Account", "zMcXHEkD78T1pwAgG2pf6QWALyBKF1YvD1");
		entry1.put("SignerWeight", 2);
		JSONObject entry11 = new JSONObject();
		entry11.put("SignerEntry", entry1);
		
		JSONObject entry2 = new JSONObject();
		entry2.put("Account", "zhWACtoujvNmmER29zVXrCxxFzpitPajdm");
		entry2.put("SignerWeight", 1);
		JSONObject entry22 = new JSONObject();
		entry22.put("SignerEntry", entry2);
		
		
		JSONObject entry3 = new JSONObject();
		entry3.put("Account", "zKvHeBUtEoNRW1wtvA42tfJx1bh7pqxZmT");
		entry3.put("SignerWeight", 1);
		JSONObject entry33 = new JSONObject();
		entry33.put("SignerEntry", entry3);
		
		JSONArray arr = new JSONArray();
		arr.put(entry11);
		arr.put(entry22);
		arr.put(entry33);
		
		tx_json.put("SignerEntries", arr);
		tx_json.put("TransactionType", "SignerListSet");
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.sign(obj, "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign signerListSet result:" + res);
	}
	
	private static void testSignForPayment(){
		JSONObject obj = new JSONObject();
		obj.put("secret", "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		obj.put("account","zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh");
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "zKvHeBUtEoNRW1wtvA42tfJx1bh7pqxZmT");
		tx_json.put("Amount", "1000000000");
		tx_json.put("Destination", "zMcXHEkD78T1pwAgG2pf6QWALyBKF1YvD1");
		tx_json.put("TransactionType", "Payment");
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.signFor(obj, "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign_for payment signer:" + res);
	}
	
	private static void testSignForTrustSet(){
		JSONObject obj = new JSONObject();
		obj.put("secret", "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		obj.put("account","zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh");
		
		JSONObject limitAmount = new JSONObject();
		limitAmount.put("currency", "GRD");
		limitAmount.put("issuer", "zMcXHEkD78T1pwAgG2pf6QWALyBKF1YvD1");
		limitAmount.put("value", "1000");
		
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "zBB1AiU6kGCBiUL2LPbimNp15cBkJLV7bp");
		tx_json.put("LimitAmount", limitAmount);
		tx_json.put("Flags", 0);
		tx_json.put("TransactionType", "TrustSet");
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.signFor(obj, "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign_for trustset signer:" + res);
	}
	
	private static void testSignPathset(){
		JSONObject ptPayment = new JSONObject();
	    ptPayment.put("Amount", "10000000000");
	    ptPayment.put("Fee", "10000");
	    ptPayment.put("Destination", "zMcXHEkD78T1pwAgG2pf6QWALyBKF1YvD1");
	    ptPayment.put("Account", "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh");
	    ptPayment.put("Sequence", 1);
	    ptPayment.put("TransactionType", "Payment");

	    JSONObject sendMax = new JSONObject();
	    sendMax.put("currency", "GRD");
	    sendMax.put("issuer", "zKvHeBUtEoNRW1wtvA42tfJx1bh7pqxZmT");
	    sendMax.put("value", "10000");
	    ptPayment.put("SendMax", sendMax);

	    JSONObject  pathObj = new JSONObject();
	    pathObj.put("type_hex", "0000000000000030");
	    pathObj.put("currency", "GRD");
	    pathObj.put("type", 48);
	    pathObj.put("issuer", "zBB1AiU6kGCBiUL2LPbimNp15cBkJLV7bp");
	    //ptPayment.push_back(std::make_pair("SendMax", sendMax));

	    JSONArray   arrayPathInner  = new JSONArray();
	    JSONArray arrayPathOuter  = new JSONArray();
	    arrayPathInner.put( pathObj);

	    arrayPathOuter.put(arrayPathInner);

	    ptPayment.put("Paths", arrayPathOuter);
	    JSONObject obj = new JSONObject();
	    obj.put("tx_json", ptPayment);
//	    System.out.println(ptPayment);
	    JSONObject res = c.sign(obj, "xn6ph3eheqhv12SrcbEPt8jsAG5M6");
		System.out.println("sign for Pathset signer:" + res);
	}
}
