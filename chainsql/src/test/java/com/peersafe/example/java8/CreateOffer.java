package com.peersafe.example.java8;

import static com.peersafe.example.java8.Print.*;

import org.json.JSONObject;

import com.peersafe.base.client.Account;
import com.peersafe.base.client.Client;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.transactions.ManagedTxn;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.client.transport.impl.JavaWebSocketTransportImpl;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.Blob;
import com.peersafe.base.core.coretypes.uint.UInt16;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;
import com.peersafe.base.core.types.known.tx.signed.SignedTransaction;
import com.peersafe.base.core.types.known.tx.txns.TableListSet;
import com.peersafe.chainsql.core.Chainsql;

/**
 * This example creates an offer to sell an account's
 * own issue.
 */
public class CreateOffer {
	public static final Chainsql c = Chainsql.c;
    public static void main(String[] args) {
        // We need a valid seed

       new Client(new JavaWebSocketTransportImpl())
                    .connect("wss://192.168.0.114:6007" ,(c)->new CreateOffer(c,"xxWFBu6veVgMnAqNf6YFRV2UENRd3")); 
       
 
    }

    public CreateOffer (Client client, String seed) {
    	buyOffer(client,seed);
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	System.out.println("begin sell");
    	sellOffer(client,seed);
    }

    private void buyOffer(Client client, String seed) {
        Account account = client.accountFromSeed(seed);
        TransactionManager tm = account.transactionManager();

        String str ="{" +
        	    "'TransactionType': 'OfferCreate',"+
        	    "'TakerPays': {"+
        	     "   'currency': 'CNY'," +
        	     "   'value': '1'," +
        	      "  'issuer': 'zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh'"+
        	    "}," +
        	    "'TakerGets': '100000'," +
        	    "'Account': 'z9VF7yQPLcKgUoHwMbzmQBjvPsyMy19ubs'" +
        	"}";
        JSONObject tx_json = new JSONObject(str);
		TransactionType type = TransactionType.valueOf(tx_json.getString("TransactionType"));
		Transaction transaction = new Transaction(type);
		try {
			transaction.parseFromJson(tx_json);
			//Fee
		} catch (Exception e) {
			e.printStackTrace();
		}

//        System.out.println(transaction.prettyJSON());
        tm.queue(tm.manage(transaction)
	            .onValidated(this::onValidated1)
	                .onError(this::onError1));
    }

    private void sellOffer(Client client, String seed) {
        Account account = client.accountFromSeed(seed);
        TransactionManager tm = account.transactionManager();

        String str ="{" +
        	    "'TransactionType': 'OfferCreate',"+
        	    "'TakerPays': '100000'," +
        	    "'TakerGets': {"+
	       	     "   'currency': 'CNY'," +
	       	     "   'value': '1'," +
	       	      "  'issuer': 'zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh'"+
	       	    "}," +
	        	"'Account': 'z9VF7yQPLcKgUoHwMbzmQBjvPsyMy19ubs'" +
	        	"}";
        JSONObject tx_json = new JSONObject(str);
		TransactionType type = TransactionType.valueOf(tx_json.getString("TransactionType"));
		Transaction transaction = new Transaction(type);
		try {
			transaction.parseFromJson(tx_json);
			//Fee
		} catch (Exception e) {
			e.printStackTrace();
		}

//        System.out.println(transaction.prettyJSON());
        tm.queue(tm.manage(transaction)
	            .onValidated(this::onValidated2)
	                .onError(this::onError2));
    }

	private void onValidated2(ManagedTxn managed) {
        TransactionResult tr = managed.result;
        print("Result:\n{0}", tr.toJSON().toString(2));
        print("Transaction result was: {0}", tr.engineResult);
//        System.exit(0);
    }

    private void onError2(Response res) {
        printErr("Transaction failed!");
//        managed.submissions.forEach(sub ->
//                printErr("{0}", sub.hash) );
//        System.exit(1);
    }
    
	private void onValidated1(ManagedTxn managed) {
        TransactionResult tr = managed.result;
        print("Result:\n{0}", tr.toJSON().toString(2));
        print("Transaction result was: {0}", tr.engineResult);
//        System.exit(0);
    }

    private void onError1(Response res) {
        printErr("Transaction failed!");
//        managed.submissions.forEach(sub ->
//                printErr("{0}", sub.hash) );
//        System.exit(1);
    }
}
