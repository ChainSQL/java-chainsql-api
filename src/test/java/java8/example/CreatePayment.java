package java8.example;

import static java8.example.Print.*;

import java.math.BigDecimal;

import com.peersafe.base.client.Account;
import com.peersafe.base.client.Client;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.transactions.ManagedTxn;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.client.transport.impl.JavaWebSocketTransportImpl;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.Currency;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;
import com.peersafe.base.core.types.known.tx.txns.Payment;

public class CreatePayment {

	    public static void main(String[] args) {
	        // We need a valid seed

	           new Client(new JavaWebSocketTransportImpl())
	                    .connect("ws://192.168.0.90:6006", (c) ->
	                        new CreatePayment(c, "xpiRhPbd98y3UiPyyEUNK2N3Kird3"));
	 
	    }

	    public CreatePayment (Client client, String secret) {
	        Account account = client.accountFromSeed(secret);
	        TransactionManager tm = account.transactionManager();

	        Payment payment = new Payment();

	        Amount amount = new Amount(BigDecimal.valueOf(100),Currency.fromString("GRD123456"),
	        		AccountID.fromAddress("znbWk4iuz2HL1e1Ux91TzYfFzJHGeYxBA4"));
	        Amount max = new Amount(BigDecimal.valueOf(102),Currency.fromString("GRD123456"),
	        		AccountID.fromAddress("znbWk4iuz2HL1e1Ux91TzYfFzJHGeYxBA4"));
	        payment.as(AccountID.Account,     "zHYfrrZyyfAMrNgm3akQot6CuSmMM6MLda")
	             	.as(AccountID.Destination, "zMbBhnQAPu7KHgtRXWFxp4YGX4LjtMpgo1")
	             	.as(Amount.Amount, amount)
	             	.as(Amount.SendMax, max);

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

	    private void onError(Response res) {
	    	System.out.println(res.message);
	        printErr("Transaction failed!");
//	        managed.submissions.forEach(sub ->
//	                printErr("{0}", sub.hash) );
	        System.exit(1);
	    }


}
