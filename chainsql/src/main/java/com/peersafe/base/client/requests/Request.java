package com.peersafe.base.client.requests;

import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.peersafe.base.client.Client;
import com.peersafe.base.client.enums.Command;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.client.responses.Response;

// We can just shift to using delegation
public class Request extends Publisher<Request.events> {
    // com.peersafe.base.client.requests.Request // ??
    public static final Logger logger = Logger.getLogger(Request.class.getName());
    public static final long TIME_OUT = 120000;
    static public final int VALIDATED_LEDGER = -3;
    static public final int CLOSED_LEDGER = -2;
    static public final int OPEN_LEDGER = -1;

    public void json(JSONObject jsonObject) {
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            String next = (String) keys.next();
            json(next, jsonObject.opt(next));
        }
    }

    public static interface Builder<T> {
        void beforeRequest(Request request);
        T buildTypedResponse(Response response);
    }

    // Base events class and aliases
    public static interface events<T>  extends Publisher.Callback<T> {}
    public static interface OnSuccess  extends events<Response> {}
    public static interface OnError    extends events<Response> {}
    public static interface OnResponse extends events<Response> {}
    public static interface OnTimeout   extends events<Response> {}

    public static abstract class Manager<T> {
        abstract public void cb(Response response, T t) throws JSONException;
        public boolean retryOnUnsuccessful(Response r) {
            return false;
        }
        public void beforeRequest(Request r) {}
    }

    Client client;
    public Command           cmd;
    public Response     response;
    private JSONObject      json;
    public int                id;
    public long         sendTime;

    /**
     * Constructor.
     * @param command command
     * @param assignedId assignedId
     * @param client client
     */
    public Request(Command command, int assignedId, Client client) {
        this.client = client;
        cmd         = command;
        id          = assignedId;
        json        = new JSONObject();

        if(!this.client.schemaID.equals("")) {
        	json("schema_id", this.client.schemaID);
        }
        json("command", cmd.toString());
        json("id",      assignedId);

    }

    /**
     * Get json.
     * @return json value.
     */
    public JSONObject json() {
        return json;
    }

    /**
     * Create json value.
     * @param key Key
     * @param value Value.
     */
    public void json(String key, Object value) {
        json.put(key, value);
    }

    /**
     * Request.
     */
    public void request() {
        client.nowOrWhenConnected(new Client.OnConnected() {
            @Override
            public void called(final Client client_) {
                client.sendRequest(Request.this);
            }
        });
    }

    /**
     * bumpSendTime
     */
    public  void bumpSendTime() {
        sendTime = System.currentTimeMillis();
    }

    /**
     * toJSON
     * @return JSONObject.
     */
    public JSONObject toJSON() {
        return json();
    }

    /**
     * jsonRepr
     * @return return value.
     */
    public JSONObject jsonRepr() {
        JSONObject repr = new JSONObject();
        if (response != null) {
            repr.put("response", response.message);
        }
        // Copy this
        repr.put("request", new JSONObject(json.toString()));
        return repr;
    }

    /**
     * handleResponse
     * @param msg msg
     */
    public void handleResponse(JSONObject msg) {
        response = new Response(this, msg);

        if (response.succeeded) {
            emit(OnSuccess.class, response);
        } else {
            emit(OnError.class, response);
        }

        emit(OnResponse.class, response);
    }

}
