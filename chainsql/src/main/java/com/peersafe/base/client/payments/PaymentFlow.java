package com.peersafe.base.client.payments;

import com.peersafe.base.client.Account;
import com.peersafe.base.client.Client;
import com.peersafe.base.client.enums.Command;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.transactions.ManagedTxn;
import com.peersafe.base.core.coretypes.*;
import com.peersafe.base.core.types.known.tx.txns.Payment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class PaymentFlow extends Publisher<PaymentFlow.events> {
    public static interface events<T> extends Publisher.Callback<T> {}
    public static interface OnDestInfo extends events<STObject>{}
    public static interface OnAlternatives extends events<Alternatives> {}
    public static interface OnAlternativesStale extends events<Alternatives> {}
    public static interface OnPathFind extends events<Request> {}

    Client client;

    private final Client.OnPathFind onPathFind = new Client.OnPathFind() {
        @Override
        public void called(JSONObject jsonObject) {
            int id = jsonObject.getInt("id");
            if (pathFind != null && id == pathFind.id) {
                emit(OnAlternatives.class, constructAlternatives(jsonObject.getJSONArray("alternatives"),
                                                                 alternatives));
            }
        }
    };
    private final Client.OnConnected onConnected = new Client.OnConnected() {
        @Override
        public void called(Client client) {
            if (pathFind != null) {
                makePathFindRequestIfCan();
            }
        }
    };

    /**
     * Payment flow.
     * @param client Client object.
     */
    public PaymentFlow(final Client client) {
        this.client = client;

        client.on(Client.OnConnected.class, onConnected);
        client.on(Client.OnPathFind.class,  onPathFind);

        on(OnAlternatives.class, new OnAlternatives() {
            @Override
            public void called(Alternatives alts) {
                alternatives = alts;
            }
        });
    }

    /**
     * unsubscribeFromClientEvents
     */
    public void unsubscribeFromClientEvents() {
        client.removeListener(Client.OnConnected.class, onConnected);
        client.removeListener(Client.OnPathFind.class, onPathFind);
    }

    public Amount destinationAmount;

    Account srcAccount;
    AccountID dest, src;
    STObject srcInfo, destInfo; // AccountRoot objects
    Alternatives alternatives;

    Currency destAmountCurrency;
    BigDecimal destAmountValue;

    // We store these guys here so we can know if they have become stale
    Request pathFind;
    public JSONObject json = null;
    /**
     * do from cache ;)
     * @param id Account address.
     * @return Request data.
     */
    public Request requestAccountInfo(final AccountID id) {
        // TODO try from cache

        Request request;
        request = client.newRequest(Command.account_info);
        request.json("account", id);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.succeeded) {
                	
                    JSONObject accountJSON = response.result.optJSONObject("account_data");
                    STObject accountData = STObject.translate.fromJSONObject(accountJSON);
                    json = accountJSON;
                    if (PaymentFlow.this.src == id) {
                        srcInfo = accountData;
                    } else if (PaymentFlow.this.dest == id) {
                        destInfo = accountData;
                        emit(OnDestInfo.class, destInfo);
                    }
                   
                }
            }
        });
        request.request();
        return request;
    }

    /**
     * source
     * @param account account
     * @return return.
     */
    public PaymentFlow source(final Account account) {
        AccountID id = account.id();
        if (src == null|| !src.equals(id)) {
            requestAccountInfo(id);
            srcAccount = account;
            src = srcAccount.id();
            makePathFindRequestIfCan();
        }
        return this;
    }

    /**
     * Payment destination.
     * @param id id.
     * @return return.
     */
    public PaymentFlow destination(final AccountID id) {
        if (dest == null || !dest.equals(id)) {
            requestAccountInfo(id);
            dest = id;
            makePathFindRequestIfCan();
        }
        return this;
    }

    /**
     * destinationAmountValue
     * @param amt amt.
     * @return return value.
     */
    public PaymentFlow destinationAmountValue(final BigDecimal amt) {
        if (destAmountValue == null || amt == null || amt.compareTo(destAmountValue) != 0) {
            destAmountValue = amt;
            makePathFindRequestIfCan();
        }
        return this;
    }
    /**
     * makePathFindRequestIfNoneAlready
     */
    public void makePathFindRequestIfNoneAlready() {
        if (pathFind == null) {
            makePathFindRequestIfCan();
        }
    }

    /**
     * makePathFindRequestIfCan
     */
    public void makePathFindRequestIfCan() {
        // TODO: ...
        ignoreCurrentRequestAndPublishStaleState();

        if (tooLittleInfoForPathFindRequest()) {
            return;
        }
        if (destAmountCurrency.equals(Currency.ZXC)) {
            // TODO: some way of ...
            destinationAmount = Issue.ZXC.amount(destAmountValue);
        } else {
            destinationAmount = new Amount(destAmountValue, destAmountCurrency, dest);
        }

        if (destinationAmount.isNative()) {
            // TODO, check if destination has no ZXC flag set ;)
            Alternatives alts = constructInitialAlternatives();
            if (!alts.isEmpty()) {
                emit(OnAlternatives.class, alts);
            }
        }


        pathFind = client.newRequest(Command.path_find);
        pathFind.json("subcommand", "create");
        pathFind.json("source_account", src);
        pathFind.json("destination_account", dest);

        // toJSON will give us what we want ;) drops string if native, else an {} if IOU
        pathFind.json("destination_amount", destinationAmount.toJSON());


        pathFind.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.succeeded && response.request == pathFind) {
                    try {
                        JSONArray alternatives = response.result.getJSONArray("alternatives");
                        emit(OnAlternatives.class, constructAlternatives(alternatives, null));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        pathFind.request();

        emit(OnPathFind.class, pathFind);
    }

    private Alternatives constructInitialAlternatives() {
        return constructAlternatives(null, null);
    }

    private Alternatives constructAlternatives(JSONArray alternatives, Alternatives prior) {
        Alternatives alts;

        if (alternatives == null) {
            alts = new Alternatives();
        } else {
            alts = new Alternatives(alternatives, prior);
        }

        if (destinationAmount.isNative() && !src.equals(dest) && srcCanSendNative(destinationAmount)) {
            injectNativeAlternative(alts, prior);
        }

        return alts;
    }

    private boolean srcCanSendNative(Amount destinationAmount) {
        return true; // TODO
    }

    private void injectNativeAlternative(Alternatives alts, Alternatives prior) {
        Alternative directXRP = new Alternative(new PathSet(), destinationAmount);
        alts.addRecyclingPrior(0, directXRP, prior);
    }

    private boolean tooLittleInfoForPathFindRequest() {
        return destAmountValue == null  ||
               destAmountValue.compareTo(BigDecimal.ZERO) == 0  ||
               destAmountCurrency == null ||
               src == null ||
               dest == null;
    }

    private void ignoreCurrentRequestAndPublishStaleState() {
        pathFind = null;
        if (alternatives != null) {
            emit(OnAlternativesStale.class, alternatives);
        }
        // TODO invalidate existing alternatives
    }

    /**
     * destinationAmountCurrency
     * @param currency currency
     * @return return.
     */
    public PaymentFlow destinationAmountCurrency(final Currency currency) {
        if (destAmountCurrency == null || !currency.equals(destAmountCurrency)) {
            destAmountCurrency = currency;
            makePathFindRequestIfCan();
        }
        return this;
    }

    /**
     * Abort.
     */
    public void abort() {
        requestPathFindClose();
        ignoreCurrentRequestAndPublishStaleState();
    }

    /**
     * createPayment
     * @param alternative alternative
     * @param sendMaxMultiplier sendMaxMultiplier
     * @return return value.
     */
    public ManagedTxn createPayment(Alternative alternative, BigDecimal sendMaxMultiplier) {
        Amount sourceAmount = alternative.sourceAmount;
        boolean hasPaths = alternative.hasPaths();

        ignoreCurrentRequestAndPublishStaleState();

        // Cancel the path finding request.
        requestPathFindClose();

        Payment payment = new Payment();
        ManagedTxn managed = srcAccount.transactionManager().manage(payment);
        payment.destination(dest);

        if (hasPaths) {
            // A payment with an empty, but specified paths is invalid
            payment.paths(alternative.paths);
        }
        // If we are sending ZXC directly it's pointless to specify SendMax
        if (!alternative.directXRP()) {
            payment.sendMax(sourceAmount.multiply(sendMaxMultiplier));
        }

        payment.amount(destinationAmount);
        return managed;
    }

    private void requestPathFindClose() {
        Request request = client.newRequest(Command.path_find);
        request.json("subcommand", "close");
        request.request();
    }

    /**
     * onAlternatives
     * @param handler handler
     * @return return value.
     */
    public PaymentFlow onAlternatives(OnAlternatives handler) {
        on(OnAlternatives.class, handler);
        return this;
    }

}
