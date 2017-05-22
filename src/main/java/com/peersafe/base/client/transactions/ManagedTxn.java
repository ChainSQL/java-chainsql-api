package com.peersafe.base.client.transactions;

import com.peersafe.base.client.Client.OnLedgerClosed;
import com.peersafe.base.client.pubsub.CallbackContext;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;
import com.peersafe.base.core.types.known.tx.signed.SignedTransaction;

import java.util.ArrayList;
import java.util.TreeSet;

public class ManagedTxn extends SignedTransaction {
    public static interface events<T> extends Callback<T> {}
    public static interface OnSubmitSuccess extends events<Response> {}
    public static interface OnSubmitFailure extends events<Response> {}
    public static interface OnSubmitError extends events<Response> {}
    public static interface OnTransactionValidated extends events<TransactionResult> {}

    public TransactionResult result;

    /**
     * On validated.
     * @param handler Callback handler.
     * @return This.
     */
    public ManagedTxn onValidated(final Callback<ManagedTxn> handler) {
        on(OnTransactionValidated.class, new OnTransactionValidated() {
            @Override
            public void called(TransactionResult args) {
                result = args;
                handler.called(ManagedTxn.this);
            }
        });
        return this;
    }
    
    /**
     * onSubmitSuccess
     * @param cb Callback.
     * @return return value.
     */
    public ManagedTxn onSubmitSuccess(OnSubmitSuccess cb){
        on(OnSubmitSuccess.class, cb);
        return this;
    }

    /**
     * On error.
     * @param cb callback.
     * @return ManagedTxn.
     */
    public ManagedTxn onError(final Callback<Response> cb) {
        on(OnSubmitFailure.class, new OnSubmitFailure() {
            @Override
            public void called(Response args) {
                cb.called(args);
            }
        });
        on(OnSubmitError.class, new OnSubmitError() {
            @Override
            public void called(Response args) {
                cb.called(args);
            }
        });
        return this;
    }

    /**
     * Remove Listener.
     * @param key Key.
     * @param <T> generic.
     * @param cb Callback.
     * @return Return value.
     */
    public <T extends events> boolean removeListener(Class<T> key, Callback cb) {
        return publisher.removeListener(key, cb);
    }

    /**
     * Emit.
     * @param key key.
     * @param args args.
     * @param <T> generic.
     * @return return value.
     */
    public <T extends events> int emit(Class<T> key, Object args) {
        return publisher.emit(key, args);
    }

    /**
     * Once
     * @param key key.
     * @param executor executor.
     * @param cb callback.
     * @param <T> generic.
     */
    public <T extends events> void once(Class<T> key, CallbackContext executor, T cb) {
        publisher.once(key, executor, cb);
    }

    /**
     * Once.
     * @param key key.
     * @param cb callback.
     * @param <T> generic.
     */
    public <T extends events> void once(Class<T> key, T cb) {
        publisher.once(key, cb);
    }

    /**
     * On
     * @param key key.
     * @param executor executor.
     * @param cb callback.
     * @param <T> generic.
     */
    public <T extends events> void on(Class<T> key, CallbackContext executor, T cb) {
        publisher.on(key, executor, cb);
    }

    /**
     * On 
     * @param key key 
     * @param cb callback.
     * @param <T> generic.
     */
    public <T extends events> void on(Class<T> key, T cb) {
        publisher.on(key, cb);
    }

    /**
     * Publisher constructor.
     * @return return value.
     */
    public Publisher<events> publisher() {
        return publisher;
    }

    private boolean isSequencePlug;
    /**
     * isSequencePlug
     * @return return value.
     */
    public boolean isSequencePlug() {
        return isSequencePlug;
    }
    /**
     * Set sequence plug.
     * @param isNoop isNoop.
     */
    public void setSequencePlug(boolean isNoop) {
        this.isSequencePlug = isNoop;
        setDescription("SequencePlug");
    }

    private String description;
    public String description() {
        if (description == null) {
            return txn.transactionType().toString();
        }
        return description;
    }
    /**
     * Set description
     * @param description description.
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * ManagedTxn Constructor.
     * @param txn Transaction object.
     */
    public ManagedTxn(Transaction txn) {
        this.txn = txn;
    }
    private final Publisher<events> publisher = new Publisher<events>();
//    private final MyTransaction publisher = new MyTransaction();
    private boolean finalized = false;

    /**
     * responseWasToLastSubmission
     * @param res response.
     * @return return value.
     */
    public boolean responseWasToLastSubmission(Response res) {
        Request req = lastSubmission().request;
        return res.request == req;
    }


    /**
     * finalizedOrResponseIsToPriorSubmission
     * @param res response.
     * @return return value.
     */
    public boolean finalizedOrResponseIsToPriorSubmission(Response res) {
        return isFinalized() || !responseWasToLastSubmission(res);
    }

    public ArrayList<Submission> submissions = new ArrayList<Submission>();

    /**
     * lastSubmission
     * @return return value.
     */
    public Submission lastSubmission() {
        if (submissions.isEmpty()) {
            return null;
        } else {
            return submissions.get(submissions.size() - 1);
        }
    }
    private TreeSet<Hash256> submittedIDs = new TreeSet<Hash256>();

    /**
     * isFinalized
     * @return return value.
     */
    public boolean isFinalized() {
        return finalized;
    }

    /**
     * setFinalized
     */
    public void setFinalized() {
        finalized = true;
    }

    /**
     * trackSubmitRequest
     * @param submitRequest request
     * @param ledger_index ledger index.
     */
    public void trackSubmitRequest(Request submitRequest, long ledger_index) {
        Submission submission = new Submission(submitRequest,
                                               sequence(),
                                               hash,
                                               ledger_index,
                                               txn.get(Amount.Fee),
                                               txn.get(UInt32.LastLedgerSequence));
        submissions.add(submission);
        trackSubmittedID();
    }

    /**
     * trackSubmittedID
     */
    public void trackSubmittedID() {
        submittedIDs.add(hash);
    }

    boolean wasSubmittedWith(Hash256 hash) {
        return submittedIDs.contains(hash);
    }

    /**
     * Sequence.
     * @return return value.
     */
    public UInt32 sequence() {
        return txn.sequence();
    }
}
