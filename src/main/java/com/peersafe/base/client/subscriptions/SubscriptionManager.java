package com.peersafe.base.client.subscriptions;

import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.core.coretypes.AccountID;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SubscriptionManager extends Publisher<SubscriptionManager.events> {
	/**
	 * pauseEventEmissions
	 */
    public void pauseEventEmissions() {
        paused = true;
    }

    /**
     * unpauseEventEmissions
     */
    public void unpauseEventEmissions() {
        paused = false;
    }

    public static interface events<T>      extends Publisher.Callback<T> {}

    public static interface OnSubscribed extends events<JSONObject> {}
    public static interface OnUnSubscribed extends events<JSONObject> {}

    public boolean paused = false;

    public enum Stream {
        server,
        ledger,
        transactions,
        transactions_propose
    }

    Set<Stream>                  streams = new TreeSet<Stream>();
    Set<AccountID>              accounts = new TreeSet<AccountID>();

    <T> Set<T> single(T element) {
        Set<T> set = new TreeSet<T>();
        set.add(element);
        return set;
    }

    /**
     * add Stream.
     * @param s Stream.
     */
    public void addStream(Stream s) {
        streams.add(s);
        subscribeStream(s);
    }

    /**
     * Remove Stream.
     * @param s Stream.
     */
    public void removeStream(Stream s) {
        streams.remove(s);
        unsubscribeStream(s);
    }

    private void subscribeStream(Stream s) {
       emit(OnSubscribed.class, basicSubscriptionObject(single(s), null));
    }

    @Override
    public <A, T extends Callback<A>> int emit(Class<T> key, A args) {
        if (paused) {
            return 0;
        }
        return super.emit(key, args);
    }

    private void unsubscribeStream(Stream s) {
        emit(OnUnSubscribed.class, basicSubscriptionObject(single(s), null));
    }

    /**
     * Add Account.
     * @param a Account address.
     */
    public void addAccount(AccountID a) {
        accounts.add(a);
        emit(OnSubscribed.class, basicSubscriptionObject(null, single(a)));
    }
    
    /**
     * Add message.
     * @param json json.
     */
    public void addMessage(JSONObject json) {
        emit(OnSubscribed.class, json);
    }
    
    /**
     * Remove Account.
     * @param a account.
     */
    public void removeAccount(AccountID a) {
        accounts.remove(a);
        emit(OnUnSubscribed.class, basicSubscriptionObject(null, single(a)));
    }

    private JSONObject basicSubscriptionObject(Set<Stream> streams, Set<AccountID> accounts) {
        JSONObject subs = new JSONObject();
        if (streams != null && streams.size() > 0) subs.put("streams", getJsonArray(streams));
        if (accounts != null && accounts.size() > 0) subs.put("accounts", getJsonArray(accounts));
        return subs;
    }

    private JSONArray getJsonArray(Collection<?> streams) {
        // Yes, JSONArray has a Collection constructor, but it doesn't play
        // so nicely on android.
        JSONArray jsonArray = new JSONArray();
        for (Object obj : streams) {
            jsonArray.put(obj);
        }

        return jsonArray;
    }

    /**
     * allSubscribed.
     * @return return value.
     */
    public JSONObject allSubscribed() {
        return basicSubscriptionObject(streams, accounts);
    }
}
