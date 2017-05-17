package com.peersafe.base.java8.example;

import static com.peersafe.base.core.coretypes.AccountID.fromAddress;
import static com.peersafe.base.java8.utils.Print.print;
import static com.peersafe.base.java8.utils.Print.printErr;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import com.peersafe.base.client.Account;
import com.peersafe.base.client.Client;
import com.peersafe.base.client.payments.PaymentFlow;
import com.peersafe.base.client.transactions.ManagedTxn;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.client.transport.impl.JavaWebSocketTransportImpl;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Currency;
import com.peersafe.base.core.coretypes.uint.UInt32;

/**
 *
 * This example shows how to donate a tiny fraction of a dollar to
 * the BitStamp account `1337`, by finding a payment path from the
 * account derived from a passed in secret to BitStamp, then using
 * the transaction manager to submit a transaction.
 *
 */
public class PaymentPaths {
    public static void main(String[] args) {
 
     
            new Client(new JavaWebSocketTransportImpl())
               .connect("ws://192.168.0.151:6006",(c)->example(c,"ssFw56mwiuVD43CJ81p4dsUXMAnJF")
                );
        
    }

    public static void example(Client client, String secret) {
        Account account = client.accountFromSeed(secret);
        PaymentFlow flow = new PaymentFlow(client);
        TransactionManager tm = account.transactionManager();
        // We could get these from user input
        AccountID destination = fromAddress("r3p9EUN8hxnbNvCaqrrXY3QnN7ji8bPf5Y");
        BigDecimal slippageFactor = new BigDecimal("1.001");
        BigDecimal amount = new BigDecimal("0.0000001");
        Currency USD = Currency.fromString("USD");
        // We use this not for its atomic properties, but because
        // it's `effectively final` and can be mutated from inside
        // a lambda below.
        AtomicInteger attempts = new AtomicInteger(1);

        flow.source(account)
            .destination(destination)
            .destinationAmountValue(amount)
            .destinationAmountCurrency(USD)
            .onAlternatives((alts) -> {
                if (alts.size() > 0) {
                    // Create a payment, bind the handlers
                    // No more onAlternatives events will be emitted
                    // after createPayment has been invoked.
                    ManagedTxn payment =
                            flow.createPayment(alts.get(0), slippageFactor)
                                    //.onError(PaymentPaths::onError)
                                    .onValidated(PaymentPaths::onValidated);

                    // Set the destination tag
                    payment.txn.as(UInt32.DestinationTag, 1337);
                    // Tell the manager to submit it
                    tm.queue(payment);
                } else {
                    printErr("Message {0} had no payment paths", attempts);

                    if (attempts.incrementAndGet() > 3) {
                        printErr("Aborting!");
                        System.exit(1);
                    }
                }
            });
    }

    private static void onValidated(ManagedTxn managed) {
        print("Transaction was successful!");
        print("Result: ");
        print("{0}", managed.result.toJSON().toString(2));
       // printSubmissions(managed);
        System.exit(0);
    }

  /*  private static void onError(ManagedTxn managed) {
        print("Transaction failed!");
        printSubmissions(managed);
        System.exit(1);
    }

    private static void printSubmissions(ManagedTxn managed) {
        ArrayList<Submission> submissions = managed.submissions;
        print("{0} Submission[s]:", submissions.size());
        for (Submission submission : submissions) {
            print("Hash: {0} Fee: {1} Ledgers: {2}-{3}",
                    submission.hash,
                    submission.fee,
                    submission.ledgerSequence,
                    submission.lastLedgerSequence);
        }
    }*/
}
