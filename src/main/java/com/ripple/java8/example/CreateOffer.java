package com.ripple.java8.example;

import static com.ripple.java8.utils.Print.print;
import static com.ripple.java8.utils.Print.printErr;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.requests.Request;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.TableListSet;

/**
 * This example creates an offer to sell an account's
 * own issue.
 */
public class CreateOffer {
    public static void main(String[] args) {
        // We need a valid seed

       new Client(new JavaWebSocketTransportImpl())
                    .connect("wss://s-east.ripple.com" ,(c)->new CreateOffer(c,"snoPBrXtMeMyMHUVTgbuqAfg1SUTb")); 
 
    }

    public CreateOffer (Client client, String seed) {
        Account account = client.accountFromSeed(seed);
        TransactionManager tm = account.transactionManager();
        AccountID account1 = AccountID.fromAddress("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
        Request req = client.accountInfo(account1);
        String secret = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";

        TableListSet payment = new TableListSet();
        payment.as(AccountID.Account,     	"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
        payment.as(Blob.TableName, 			"7461626C6531");
        payment.as(Blob.Raw,         		"5B7B224E4E223A312C226669656C64223A226964222C226C656E677468223A31312C224149223A312C22504B223A312C2274797065223A22696E74222C225551223A317D2C207B2264656661756C74223A6E756C6C2C226669656C64223A226E616D65222C226C656E677468223A35302C2274797065223A22737472696E67227D5D");
        payment.as(UInt16.OpType,       	1);
        payment.as(UInt32.Sequence,        1);
        payment.as(Amount.Fee,            	"1");
 
        SignedTransaction signed = payment.sign(secret);
        print("The original transaction:");
        print("{0}", payment.prettyJSON());
        print("The signed transaction, with SigningPubKey and TxnSignature:");
        print("{0}", signed.txn.prettyJSON());
        print("The transaction id: {0}", signed.hash);
        print("The blob to submit to rippled:");
        print(signed.tx_blob);
        tm.queue(tm.manage(signed.txn)
	            .onValidated(this::onValidated)
	                .onError(this::onError));

    }


	private void onValidated(ManagedTxn managed) {
        TransactionResult tr = managed.result;
        print("Result:\n{0}", tr.toJSON().toString(2));
        print("Transaction result was: {0}", tr.engineResult);
        System.exit(0);
    }

    private void onError(ManagedTxn managed) {
        printErr("Transaction failed!");
        managed.submissions.forEach(sub ->
                printErr("{0}", sub.hash) );
        System.exit(1);
    }
}
