package com.ripple.java8.example;

import static com.ripple.java8.utils.Print.print;
import static com.ripple.java8.utils.Print.printErr;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.txns.Payment;

public class CreatePayment {

	    public static void main(String[] args) {
	        // We need a valid seed

	           new Client(new JavaWebSocketTransportImpl())
	                    .connect("ws://192.168.0.162:6006", (c) ->
	                        new CreatePayment(c, "snoPBrXtMeMyMHUVTgbuqAfg1SUTb"));
	 
	    }

	    public CreatePayment (Client client, String secret) {
	        Account account = client.accountFromSeed(secret);
	        TransactionManager tm = account.transactionManager();

	        Payment payment = new Payment();

	        payment.as(AccountID.Account,     "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
	             	.as(AccountID.Destination, "rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr")
	             	.as(Amount.Fee,            "10")
	             	.as(UInt32.Sequence,        1)
	             	.as(Amount.Amount, "1000000000");

	        tm.queue(tm.manage(payment)
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
