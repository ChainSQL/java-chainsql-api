package java8.test;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;

public class TestSign {
	public static final Chainsql c = Chainsql.c;
	public static void main(String[] args) {
		testSignPayment();
		testSignTrustSet();
		testSignAccountSet();
		testSignForPayment();
		testSignForTrustSet();
		testSignSignerListSet();
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
		limitAmount.put("issuer", "r3kmLJN5D28dHuH8vZNUZpMC43pEHpaocV");
		limitAmount.put("value", "1000");
		
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		tx_json.put("LimitAmount", limitAmount);
		tx_json.put("Flags", 0);
		tx_json.put("TransactionType", "TrustSet");
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.sign(obj, "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign TrustSet result:" + res);
	}

	private static void testSignAccountSet(){
		JSONObject obj = new JSONObject();
		obj.put("secret", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
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
		
		JSONObject res = c.sign(obj, "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign AccountSet result:" + res);
	}

	private static void testSignSignerListSet(){
		JSONObject obj = new JSONObject();
		obj.put("secret", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		tx_json.put("TransactionType", "SignerListSet");
		tx_json.put("SignerQuorum", 3);
		
		JSONObject entry1 = new JSONObject();
		entry1.put("Account", "rDsFXt1KRDNNckSh3exyTqkQeBKQCXawb2");
		entry1.put("SignerWeight", 2);
		JSONObject entry11 = new JSONObject();
		entry11.put("SignerEntry", entry1);
		
		JSONObject entry2 = new JSONObject();
		entry2.put("Account", "rPeRx9WUAivWPpqJnT1ZkDV5r845Rui1Mp");
		entry2.put("SignerWeight", 1);
		JSONObject entry22 = new JSONObject();
		entry22.put("SignerEntry", entry2);
		
		
		JSONObject entry3 = new JSONObject();
		entry3.put("Account", "rL9UctLXeQmvdX6T7JR4yNz2dmgtN8awzq");
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
		
		JSONObject res = c.sign(obj, "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign signerListSet result:" + res);
	}
	
	private static void testSignForPayment(){
		JSONObject obj = new JSONObject();
		obj.put("secret", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		obj.put("account","rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q");
		tx_json.put("Amount", "1000000000");
		tx_json.put("Destination", "rfnmWkQ1jpVvWFvw7rnFNt7AUe58rQvMet");
		tx_json.put("TransactionType", "Payment");
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.sign(obj, "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign_for payment signer:" + res);
	}
	
	private static void testSignForTrustSet(){
		JSONObject obj = new JSONObject();
		obj.put("secret", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		obj.put("account","rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		
		JSONObject limitAmount = new JSONObject();
		limitAmount.put("currency", "GRD");
		limitAmount.put("issuer", "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q");
		limitAmount.put("value", "1000");
		
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", "rfnmWkQ1jpVvWFvw7rnFNt7AUe58rQvMet");
		tx_json.put("LimitAmount", limitAmount);
		tx_json.put("Flags", 0);
		tx_json.put("TransactionType", "TrustSet");
		tx_json.put("Sequence", 2);
		obj.put("tx_json", tx_json);
		
		JSONObject res = c.sign(obj, "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("sign_for trustset signer:" + res);
	}
}
