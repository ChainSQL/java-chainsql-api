package com.ripple.client;

import static com.ripple.client.requests.Request.VALIDATED_LEDGER;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ripple.client.enums.Command;
import com.ripple.client.enums.Message;
import com.ripple.client.enums.RPCErr;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.requests.Request;
import com.ripple.client.requests.Request.Manager;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.ServerInfo;
import com.ripple.client.subscriptions.SubscriptionManager;
import com.ripple.client.subscriptions.TrackedAccountRoot;
import com.ripple.client.subscriptions.TransactionSubscriptionManager;
import com.ripple.client.transactions.AccountTxPager;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.TransportEventHandler;
import com.ripple.client.transport.WebSocketTransport;
import com.ripple.client.types.AccountLine;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Issue;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.entries.AccountRoot;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;

public class Client extends Publisher<Client.events> implements TransportEventHandler {
    // Logger
    public static final Logger logger = Logger.getLogger(Client.class.getName());

    // Events
    public static interface events<T> extends Publisher.Callback<T> {}
    public static interface OnLedgerClosed extends events<ServerInfo> {}
    public static interface OnConnected extends events<Client> {}
    public static interface OnDisconnected extends events<Client> {}
    public static interface OnSubscribed extends events<ServerInfo> {}
    public static interface OnMessage extends events<JSONObject> {}
    public static interface OnTBMessage extends events<JSONObject> {}
    public static interface OnTXMessage extends events<JSONObject> {}
    public static interface OnSendMessage extends events<JSONObject> {}
    public static interface OnStateChange extends events<Client> {}
    public static interface OnPathFind extends events<JSONObject> {}
    public static interface OnValidatedTransaction extends events<TransactionResult> {}

    // Fluent binders
    public Client onValidatedTransaction(OnValidatedTransaction cb) {
        on(OnValidatedTransaction.class, cb);
        return this;
    }

	public Client onLedgerClosed(OnLedgerClosed cb) {
        on(OnLedgerClosed.class, cb);
        return this;
    }
	public Client OnTBMessage(OnTBMessage cb) {
        on(OnTBMessage.class, cb);
        return this;
    }
	public Client OnTXMessage(OnTXMessage cb) {
        on(OnTXMessage.class, cb);
        return this;
    }

	public Client OnMessage(OnMessage cb) {
        on(OnMessage.class, cb);
        return this;
    } 
	
    public Client onConnected(OnConnected onConnected) {
        this.on(OnConnected.class, onConnected);
        return this;
    }

    // Event handler sugar
    public Client onDisconnected(OnDisconnected cb) {
        on(OnDisconnected.class, cb);
        return this;
    }

    // ### Members
    // The implementation of the WebSocket
    WebSocketTransport ws;

    /**
     * When this is non 0, we randomly disconnect when trying to send messages
     * See {@link Client#sendMessage}
     */
    public double randomBugsFrequency = 0;
    Random randomBugs = new Random();
    // When this is set, all transactions will be routed first to this, which
    // will then notify the client
    TransactionSubscriptionManager transactionSubscriptionManager;

    // This is in charge of executing code in the `clientThread`
    protected ScheduledExecutorService service;
    // All code that use the Client api, must be run on this thread

    /**
     See {@link Client#run}
     */
    protected Thread clientThread;
    protected TreeMap<Integer, Request> requests = new TreeMap<Integer, Request>();

    // Keeps track of the `id` doled out to Request objects
    private int cmdIDs;
    // The last uri we were connected to
    String previousUri;

    // Every x ms, we clean up timed out requests
    public long maintenanceSchedule = 10000; //ms

    public int SEQUENCE;
    
    public String NAMEINDB="";
    // Are we currently connected?
    public boolean connected = false;
    // If we haven't received any message from the server after x many
    // milliseconds, disconnect and reconnect again.
    private long reconnectDormantAfter = 20000; // ms
    // ms since unix time of the last indication of an alive connection
    private long lastConnection = -1; // -1 means null
    // Did we disconnect manually? If not, try and reconnect
    private boolean manuallyDisconnected = false;

    // Tracks the serverInfo we are currently connected to
    public ServerInfo serverInfo = new ServerInfo();
    private HashMap<AccountID, Account> accounts = new HashMap<AccountID, Account>();
    // Handles [un]subscription requests, also on reconnect
    public SubscriptionManager subscriptions = new SubscriptionManager();
    
    private static final int MAX_REQUEST_COUNT = 10; 
    
    // Constructor
    public Client(WebSocketTransport ws) {
        this.ws = ws;
        ws.setHandler(this);

        prepareExecutor();
        // requires executor, so call after prepareExecutor
        scheduleMaintenance();

        subscriptions.on(SubscriptionManager.OnSubscribed.class, new SubscriptionManager.OnSubscribed() {
            @Override
            public void called(JSONObject subscription) {
                if (!connected)
                    return;
                subscribe(subscription);
            }
        });
    }

    // ### Getters

    private int reconnectDelay() {
        return 1000;
    }
    public boolean isManuallyDisconnected() {
        return manuallyDisconnected;
    }

    // ### Setters

    public void reconnectDormantAfter(long reconnectDormantAfter) {
        this.reconnectDormantAfter = reconnectDormantAfter;
    }

    public Client transactionSubscriptionManager(TransactionSubscriptionManager transactionSubscriptionManager) {
        this.transactionSubscriptionManager = transactionSubscriptionManager;
        return this;
    }

    // ### Helpers

    public static void log(Level level, String fmt, Object... args) {
        if (logger.isLoggable(level)) {
            logger.log(level, fmt, args);
        }
    }

    public static String prettyJSON(JSONObject object) {
        return object.toString(4);
    }
    public static JSONObject parseJSON(String s) {
        return new JSONObject(s);
    }


    /* --------------------------- CONNECT / RECONNECT -------------------------- */

    /**
     * After calling this method, all subsequent interaction with the api should
     * be called via posting Runnable() run blocks to the Executor.
     *
     * Essentially, all ripple-lib-java api interaction
     * should happen on the one thread.
     *
     * @see #onMessage(org.json.JSONObject)
     */
    public Client connect(final String uri) {
        manuallyDisconnected = false;

        schedule(50, new Runnable() {
            @Override
            public void run() {
                doConnect(uri);
            }
        });
        return this;
    }

    public void doConnect(String uri) {
        log(Level.INFO, "Connecting to " + uri);
        previousUri = uri;
        ws.connect(URI.create(uri));
    }

    public void disconnect() {
        manuallyDisconnected = true;
        ws.disconnect();
        service.shutdownNow();
        // our disconnect handler should do the rest
    }

    private void emitOnDisconnected() {
        // This ensures that the callback method onDisconnect is
        // called before a new connection is established this keeps
        // the symmetry of connect-> disconnect -> reconnect
        emit(OnDisconnected.class, this);
    }

    /**
     * This will detect stalled connections When connected we are subscribed to
     * a ledger, and ledgers should be at most 20 seconds apart.
     *
     * This also
     */
    private void scheduleMaintenance() {
        schedule(maintenanceSchedule, new Runnable() {
            @Override
            public void run() {
                try {
                    manageTimedOutRequests();
                    int defaultValue = -1;

                    if (!manuallyDisconnected) {
                        if (connected && lastConnection != defaultValue) {
                            long time = new Date().getTime();
                            long msSince = time - lastConnection;
                            if (msSince > reconnectDormantAfter) {
                                lastConnection = defaultValue;
                                reconnect();
                            }
                        }
                    }
                } finally {
                    scheduleMaintenance();
                }
            }
        });
    }

    public void reconnect() {
        disconnect();
        connect(previousUri);
    }

    void manageTimedOutRequests() {
        long now = System.currentTimeMillis();
        ArrayList<Request> timedOut = new ArrayList<Request>();

        for (Request request : requests.values()) {
            if (request.sendTime != 0) {
                long since = now - request.sendTime;
                if (since >= Request.TIME_OUT) {
                    timedOut.add(request);
                }
            }
        }
        for (Request request : timedOut) {
            request.emit(Request.OnTimeout.class, request.response);
            requests.remove(request.id);
        }
    }

    // ### Handler binders binder

    public void connect(final String s, final OnConnected onConnected) {
        run(new Runnable() {
            public void run() {
                connect(s);
                once(OnConnected.class, onConnected);
            }
        });
    }

    public void disconnect(final OnDisconnected onDisconnected) {
        run(new Runnable() {
            public void run() {
                Client.this.once(OnDisconnected.class, onDisconnected);
                disconnect();
            }
        });
    }

    public void whenConnected(boolean nextTick, final OnConnected onConnected) {
        if (connected) {
            if (nextTick) {
                schedule(0, new Runnable() {
                    @Override
                    public void run() {
                        onConnected.called(Client.this);
                    }
                });
            } else {
                onConnected.called(this);
            }
        }  else {
            once(OnConnected.class, onConnected);
        }
    }

    public void nowOrWhenConnected(OnConnected onConnected) {
        whenConnected(false, onConnected);
    }

    public void nextTickOrWhenConnected(OnConnected onConnected) {
        whenConnected(true, onConnected);
    }

    public void dispose() {
        ws = null;
    }

    /* -------------------------------- EXECUTOR -------------------------------- */

    public void run(Runnable runnable) {
        // What if we are already in the client thread?? What happens then ?
        if (runningOnClientThread()) {
            runnable.run();
        } else {
            service.submit(errorHandling(runnable));
        }
    }

    public void schedule(long ms, Runnable runnable) {
        service.schedule(errorHandling(runnable), ms, TimeUnit.MILLISECONDS);
    }

    public boolean runningOnClientThread() {
        return clientThread != null && Thread.currentThread().getId() == clientThread.getId();
    }

    protected void prepareExecutor() {
        service = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                clientThread = new Thread(r);
                return clientThread;
            }
        });
    }

    public static abstract class ThrowingRunnable implements Runnable {
        public abstract void throwingRun() throws Exception;

        @Override
        public void run() {
            try {
                throwingRun();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Runnable errorHandling(final Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    onException(e);
                }
            }
        };
    }

    protected void onException(Exception e) {
        e.printStackTrace(System.out);
        if (logger.isLoggable(Level.WARNING)) {
            log(Level.WARNING, "Exception {0}", e);
        }
    }

    private void resetReconnectStatus() {
        lastConnection = new Date().getTime();
    }


    private void updateServerInfo(JSONObject msg) {
		serverInfo.update(msg);
    }

    /* ------------------------- TRANSPORT EVENT HANDLER ------------------------ */

    /**
     * This is to ensure we run everything on {@link Client#clientThread}
     */
    @Override
    public void onMessage(final JSONObject msg) {
        resetReconnectStatus();
        run(new Runnable() {
            @Override
            public void run() {
                onMessageInClientThread(msg);
            }
        });
    }

    @Override
    public void onConnecting(int attempt) {
    }

    @Override
    public void onError(Exception error) {
        onException(error);
    }

    @Override
    public void onDisconnected(boolean willReconnect) {
        run(new Runnable() {
            @Override
            public void run() {
                doOnDisconnected();
            }
        });
    }

    @Override
    public void onConnected() {
        run(new Runnable() {
            public void run() {
                doOnConnected();
            }
        });
    }

    /* ----------------------- CLIENT THREAD EVENT HANDLER ---------------------- */

    public void onMessageInClientThread(JSONObject msg) {
    	String str = msg.optString("type",null);
        Message type = Message.valueOf(str);
        try {
            emit(OnMessage.class, msg);
            if (logger.isLoggable(Level.FINER)) {
                log(Level.FINER, "Receive `{0}`: {1}", type, prettyJSON(msg));
            }

            switch (type) {
                case serverStatus:
                    updateServerInfo(msg);
                    break;
                case ledgerClosed:
                    updateServerInfo(msg);
                    // TODO
                    emit(OnLedgerClosed.class, serverInfo);
                    break;
                case response:
                    onResponse(msg);
                    break;
                case transaction:
                    onTransaction(msg);
                    break;
                case path_find:
                    emit(OnPathFind.class, msg);
                    break;
                case singleTransaction:
                	emit(OnTXMessage.class,msg);
                	break;
                case table:
                	emit(OnTBMessage.class,msg);
                	break;
                default:
                    unhandledMessage(msg);
                    break;
            }
        } catch (Exception e) {
            //logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            // This seems to be swallowed higher up, (at least by the
            // Java-WebSocket transport implementation)
            System.out.println("error_message: "+e.getLocalizedMessage());
           // throw new RuntimeException(e);
        } finally {
            emit(OnStateChange.class, this);
        }
    }
    private void doOnDisconnected() {
        logger.entering(getClass().getName(), "doOnDisconnected");
        connected = false;
        emitOnDisconnected();

        if (!manuallyDisconnected) {
            schedule(reconnectDelay(), new Runnable() {
                @Override
                public void run() {
                    connect(previousUri);
                }
            });
        } else {
            logger.fine("Currently disconnecting, so will not reconnect");
        }

        logger.entering(getClass().getName(), "doOnDisconnected");
    }

    private void doOnConnected() {
        resetReconnectStatus();

        logger.entering(getClass().getName(), "doOnConnected");
        connected = true;
        emit(OnConnected.class, this);

        subscribe(prepareSubscription());
        logger.exiting(getClass().getName(), "doOnConnected");
    }

    void unhandledMessage(JSONObject msg) {
        log(Level.WARNING, "Unhandled message: " + msg);
    }

    void onResponse(JSONObject msg) {
        Request request = requests.remove(msg.optInt("id", -1));

        if (request == null) {
            log(Level.WARNING, "Response without a request: {0}", msg);
            return;
        }
        request.handleResponse(msg);
    }

    void onTransaction(JSONObject msg) {
        TransactionResult tr = new TransactionResult(msg, TransactionResult
                .Source
                .transaction_subscription_notification);
        if (tr.validated) {
            if (transactionSubscriptionManager != null) {
                transactionSubscriptionManager.notifyTransactionResult(tr);
            } else {
                onTransactionResult(tr);
            }
        }
    }

    public void onTransactionResult(TransactionResult tr) {
        log(Level.INFO, "Transaction {0} is validated", tr.hash);
        Map<AccountID, STObject> affected = tr.modifiedRoots();

        if (affected != null) {
            Hash256 transactionHash = tr.hash;
            UInt32 transactionLedgerIndex = tr.ledgerIndex;

            for (Map.Entry<AccountID, STObject> entry : affected.entrySet()) {
                Account account = accounts.get(entry.getKey());
                if (account != null) {
                    STObject rootUpdates = entry.getValue();
                    account.getAccountRoot()
                            .updateFromTransaction(
                                    transactionHash, transactionLedgerIndex, rootUpdates);
                }
            }
        }

        Account initator = accounts.get(tr.initiatingAccount());
        if (initator != null) {
            log(Level.INFO, "Found initiator {0}, notifying transactionManager", initator);
            initator.transactionManager().notifyTransactionResult(tr);
        } else {
            log(Level.INFO, "Can't find initiating account!");
        }
        emit(OnValidatedTransaction.class, tr);
    }

    public void sendMessage(JSONObject object) {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Send: {0}", prettyJSON(object));
        }
        emit(OnSendMessage.class, object);
        ws.sendMessage(object);

        if (randomBugsFrequency != 0) {
            if (randomBugs.nextDouble() > (1D - randomBugsFrequency)) {
                disconnect();
                connect(previousUri);
                String msg = "I disconnected you, now I'm gonna throw, " +
                        "deal with it suckah! ;)";
                logger.warning(msg);
                throw new RuntimeException(msg);
            }

        }
    }

    /* -------------------------------- ACCOUNTS -------------------------------- */

    public Account accountFromSeed(String masterSeed) {
        IKeyPair kp = Seed.fromBase58(masterSeed).keyPair();
        return account(AccountID.fromKeyPair(kp), kp);
    }

    public Account account(final AccountID id, IKeyPair keyPair) {
        if (accounts.containsKey(id)) {
            return accounts.get(id);
        } else {
            TrackedAccountRoot accountRoot = accountRoot(id);
            Account account = new Account(
                    id,
                    keyPair,
                    accountRoot,
                    new TransactionManager(this, accountRoot, id, keyPair)
            );
            accounts.put(id, account);
            subscriptions.addAccount(id);

            return account;
        }
    }

    private TrackedAccountRoot accountRoot(AccountID id) {
        TrackedAccountRoot accountRoot = new TrackedAccountRoot();
        requestAccountRoot(id, accountRoot);
        return accountRoot;
    }

    private void requestAccountRoot(final AccountID id,
                                    final TrackedAccountRoot accountRoot) {

        makeManagedRequest(Command.ledger_entry, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
                return r == null || r.rpcerr == null || r.rpcerr != RPCErr.entryNotFound;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	//System.out.println("response:" + jsonObject);
                if (response.succeeded) {
                    accountRoot.setFromJSON(jsonObject);
                } else {
                    log(Level.INFO, "Unfunded account: {0}", response.message);
                    accountRoot.setUnfundedAccount(id);
                }
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("account_root", id);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return response.result.getJSONObject("node");
            }
        });
    }

    /* ------------------------------ SUBSCRIPTIONS ----------------------------- */

    private void subscribe(JSONObject subscription) {
        Request request = newRequest(Command.subscribe);

        request.json(subscription);
        request.on(Request.OnSuccess.class, new Request.OnSuccess() {
            @Override
            public void called(Response response) {
                // TODO ... make sure this isn't just an account subscription
                serverInfo.update(response.result);
                emit(OnSubscribed.class, serverInfo);
            }
        });
        request.request();
    }

    private JSONObject prepareSubscription() {
        subscriptions.pauseEventEmissions();
        subscriptions.addStream(SubscriptionManager.Stream.ledger);
        subscriptions.addStream(SubscriptionManager.Stream.server);
        subscriptions.unpauseEventEmissions();
        return subscriptions.allSubscribed();
    }

    /* ------------------------------ REQUESTS ------------------------------ */

    public Request newRequest(Command cmd) {
        return new Request(cmd, cmdIDs++, this);
    }

    public void sendRequest(final Request request) {
    	//System.out.println("request:"+request.json());
        Logger reqLog = Request.logger;

        try {
            requests.put(request.id, request);
            request.bumpSendTime();
            sendMessage(request.toJSON());
            // Better safe than sorry
        } catch (Exception e) {
            if (reqLog.isLoggable(Level.WARNING)) {
                reqLog.log(Level.WARNING, "Exception when trying to request: {0}", e);
            }
            nextTickOrWhenConnected(new OnConnected() {
                @Override
                public void called(Client args) {
                    sendRequest(request);
                }
            });
        }
    }

    // ### Managed Requests API
    public <T> Request makeManagedRequest(final Command cmd, final Manager<T> manager, final Request.Builder<T> builder){
    	return makeManagedRequest(cmd,manager,builder,0);
    }
    
    public <T> Request makeManagedRequest(final Command cmd, final Manager<T> manager, final Request.Builder<T> builder,int depth) {
    	if(depth > MAX_REQUEST_COUNT){
    		return null;
    	}
        final Request request = newRequest(cmd);
        final boolean[] responded = new boolean[]{false};
        request.once(Request.OnTimeout.class, new Request.OnTimeout() {
            @Override
            public void called(Response args) {
                if (!responded[0] && manager.retryOnUnsuccessful(null)) {
                    logRetry(request, "Request timed out");
                    request.clearAllListeners();
                    queueRetry(50, cmd, manager, builder,depth);
                }
            }
        });
        final OnDisconnected cb = new OnDisconnected() {
            @Override
            public void called(Client c) {
                if (!responded[0] && manager.retryOnUnsuccessful(null)) {
                    logRetry(request, "Client disconnected");
                    request.clearAllListeners();
                    queueRetry(50, cmd, manager, builder,depth);
                }
            }
        };
        once(OnDisconnected.class, cb);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(final Response response) {
                responded[0] = true;
                Client.this.removeListener(OnDisconnected.class, cb);

                if (response.succeeded) {
                    final T t = builder.buildTypedResponse(response);
                    manager.cb(response, t);
                } else {
                    if (manager.retryOnUnsuccessful(response)) {
                        queueRetry(50, cmd, manager, builder,depth);
                    } else {
                        manager.cb(response, null);
                    }
                }
            }
        });
        builder.beforeRequest(request);
        manager.beforeRequest(request);

    	//System.out.println("request:" + request.toJSON());
    	
        request.request();
        return request;
    }

    private <T> void queueRetry(int ms,
                                final Command cmd,
                                final Manager<T> manager,
                                final Request.Builder<T> builder,
                                final int depth) {
        schedule(ms, new Runnable() {
            @Override
            public void run() {
                makeManagedRequest(cmd, manager, builder,depth + 1);
            }
        });
    }

    private void logRetry(Request request, String reason) {
        if (logger.isLoggable(Level.WARNING)) {
            log(Level.WARNING, previousUri + ": " + reason + ", muting listeners " +
                    "for " + request.json() + "and trying again");
        }
    }

    // ### Managed Requests

    public AccountTxPager accountTxPager(AccountID accountID) {
        return new AccountTxPager(this, accountID, null);
    }

    public void requestLedgerEntry(final Hash256 index, final Number ledger_index, final Manager<LedgerEntry> cb) {
        makeManagedRequest(Command.ledger_entry, cb, new Request.Builder<LedgerEntry>() {
            @Override
            public void beforeRequest(Request request) {
                if (ledger_index != null) {
                    request.json("ledger_index", ledgerIndex(ledger_index));
                }
                request.json("index", index.toJSON());
            }
            @Override
            public LedgerEntry buildTypedResponse(Response response) {
                String node_binary = response.result.optString("node_binary");
                STObject node = STObject.translate.fromHex(node_binary);
                node.put(Hash256.index, index);
                return (LedgerEntry) node;
            }
        });
    }

    private Object ledgerIndex(Number ledger_index) {
        long l = ledger_index.longValue();
        if (l == VALIDATED_LEDGER) {
            return "validated";
        }
        return l;
    }

    public void requestAccountInfo(final AccountID addy, final Manager<AccountRoot> manager) {
        makeManagedRequest(Command.account_info, manager, new Request.Builder<AccountRoot>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("account", addy);
            }

            @Override
            public AccountRoot buildTypedResponse(Response response) {
                JSONObject root = response.result.optJSONObject("account_data");
                return (AccountRoot) STObject.fromJSONObject(root);
            }
        });
    }

    public void requestLedgerData(final long ledger_index, final Manager<ArrayList<LedgerEntry>> manager) {
        makeManagedRequest(Command.ledger_data, manager, new Request.Builder<ArrayList<LedgerEntry>>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("ledger_index", ledger_index);
                request.json("binary", true);
            }

            @Override
            public ArrayList<LedgerEntry> buildTypedResponse(Response response) {
                JSONArray state = response.result.getJSONArray("state");
                ArrayList<LedgerEntry> result = new ArrayList<LedgerEntry>();
                for (int i = 0; i < state.length(); i++) {
                    JSONObject stateObject = state.getJSONObject(i);
                    LedgerEntry le = (LedgerEntry) STObject.fromHex(stateObject.getString("data"));
                    le.index(Hash256.fromHex(stateObject.getString("index")));
                    result.add(le);
                }
                return result;
            }
        });
    }

    public void requestAccountLines(final AccountID addy, final Manager<ArrayList<AccountLine>> manager) {
        makeManagedRequest(Command.account_lines, manager, new Request.Builder<ArrayList<AccountLine>>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("account", addy);
            }

            @Override
            public ArrayList<AccountLine> buildTypedResponse(Response response) {
                ArrayList<AccountLine> lines = new ArrayList<AccountLine>();
                JSONArray array = response.result.optJSONArray("lines");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject line = array.optJSONObject(i);
                    lines.add(AccountLine.fromJSON(addy, line));
                }
                return lines;
            }
        });
    }

    public void requestHostID(final Callback<String> callback) {
        makeManagedRequest(Command.server_info, new Manager<String>() {
            @Override
            public void cb(Response response, String hostid) throws JSONException {
                callback.called(hostid);
            }

            @Override
            public boolean retryOnUnsuccessful(Response r) {
                return true;
            }
        }, new Request.Builder<String>() {
            @Override
            public void beforeRequest(Request request) {

            }

            @Override
            public String buildTypedResponse(Response response) {
                JSONObject info = response.result.getJSONObject("info");
                return info.getString("hostid");
            }
        });
    }

    public void requestBookOffers(final Number ledger_index,
                                  final Issue get,
                                  final Issue pay,
                                  final Manager<ArrayList<Offer>> cb) {
        makeManagedRequest(Command.book_offers, cb, new Request.Builder<ArrayList<Offer>>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("taker_gets", get.toJSON());
                request.json("taker_pays", pay.toJSON());

                if (ledger_index != null) {
                    request.json("ledger_index", ledger_index);
                }
            }
            @Override
            public ArrayList<Offer> buildTypedResponse(Response response) {
                ArrayList<Offer> offers = new ArrayList<Offer>();
                JSONArray offersJson = response.result.getJSONArray("offers");
                for (int i = 0; i < offersJson.length(); i++) {
                    JSONObject jsonObject = offersJson.getJSONObject(i);
                    STObject object = STObject.fromJSONObject(jsonObject);
                    offers.add((Offer) object);
                }
                return offers;
            }
        });
    }

    public void requestLedger(final Number ledger_index, final Manager<JSONObject> cb) {
        makeManagedRequest(Command.ledger, cb, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("ledger_index", ledgerIndex(ledger_index));
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return response.result.optJSONObject("ledger");
            }
        });
    }

    public void requestTransaction(final Hash256 hash, final Manager<TransactionResult> cb) {
        makeManagedRequest(Command.tx, cb, new Request.Builder<TransactionResult>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("binary", true);
                request.json("transaction", hash);
            }

            @Override
            public TransactionResult buildTypedResponse(Response response) {
                return new TransactionResult(response.result, TransactionResult.Source.request_tx_binary);
            }
        });
    }

    // TODO: some nice way of using lambdas for the common case
    // sending null, is one way of indicating `some` kind of error
    // but that's kind of lame.
    @Deprecated // before it even got started
    public void requestTransaction(final Hash256 hash, final Callback<TransactionResult> cb) {
        requestTransaction(hash, new Manager<TransactionResult>() {
            @Override
            public void cb(Response response, TransactionResult transactionResult) throws JSONException {
                if (response.succeeded) {
                    cb.called(transactionResult);
                } else {
                    throw new RuntimeException("Failed" + response.message);
                }
            }
        });
    }

    // ### Request builders
    // These all return Request

    public Request submit(String tx_blob, boolean fail_hard) {
        Request req = newRequest(Command.submit);
        req.json("tx_blob", tx_blob);
        req.json("fail_hard", fail_hard);
        return req;
    }

    public Request accountLines(AccountID account) {
        Request req = newRequest(Command.account_lines);
        req.json("account", account.address);
        return req;

    }

    public void getRipplemsg(String name ,AccountID account){
    	accountInfo(account);
    	getNameInDB( name,account);
    }
    
    
    public Request accountInfo(AccountID account) {
        Request request = newRequest(Command.account_info);
        request.json("account", account.address);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            public void called(Response response) {
                if (response.succeeded) {
                  //SEQUENCE = (int) response.result.optJSONObject("account_data").get("Sequence");
                }
            }
        });
        request.request();
        while(request.response==null){
        	 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}  
   	 	}
   		return request;
    }
    
    public Request messageTx(JSONObject messageTx){
    	 Request request = newRequest(Command.subscribe);
    	 request.json("tx_json", messageTx);
         request.once(Request.OnResponse.class, new Request.OnResponse() {
             public void called(Response response) {
                 if (response.succeeded) {
                   //SEQUENCE = (int) response.result.optJSONObject("account_data").get("Sequence");
                 }
             }
         });
         request.request();
         while(request.response==null){
         	 try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}  
	 	}
		return request;
    }
    public  Request getNameInDB(String name ,AccountID account){   	
    	 Request request = newRequest(Command.g_dbname);
    	 JSONObject txjson = new JSONObject();
    	 txjson.put("Account", account.address);
    	 txjson.put("TableName", name); 
       	 request.json("tx_json", txjson);
         request.once(Request.OnResponse.class, new Request.OnResponse() {
	            public  void called(Response response) {
	                if (response.succeeded) {
	   
	                }
	            }
	        });
         request.request();
         while(request.response==null){
        	 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}  
   	 	}
    	 return request;	
    }
    public  Request select(AccountID account,JSONObject[] tabarr,String raw,Callback cb){
	   	 Request request = newRequest(Command.r_get);
	   	 JSONObject txjson = new JSONObject();
	   	 txjson.put("Owner", account);
	   	 txjson.put("Tables", tabarr);
	   	 txjson.put("Raw", raw);
	   	 txjson.put("OpType", 7);
	   	 request.json("tx_json", txjson);
	        request.once(Request.OnResponse.class, new Request.OnResponse() {
		            public  void called(Response response) {
		                if (response.succeeded) {
		                	//System.out.println("response:" + response.message.toString());
		                	cb.called(response);
		                   //Integer Sequence = (Integer) response.result.optJSONObject("account_data").get("Sequence");
		                }
		            }
		        });
	        request.request();
	        while(request.response==null){
	       	 try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}  
	  	 	}
	   	 return request;	
	   }
    public  void getLedger(JSONObject option,Callback cb){  
    	makeManagedRequest(Command.ledger, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
                return r == null || r.rpcerr == null || r.rpcerr != RPCErr.entryNotFound;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	cb.called(jsonObject);
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
        		request.json("ledger_index", option.get("ledger_index"));
       		 	request.json("expand", false);
       		 	request.json("transactions",true);
       		 	request.json("accounts",false );
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return response.result;
            }
        });
      }
    public  void getLedgerVersion(Callback cb){   	
    	makeManagedRequest(Command.ledger_current, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
                return r == null || r.rpcerr == null || r.rpcerr != RPCErr.entryNotFound;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	cb.called(jsonObject);
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return response.result;
            }
        });
     }
    
    public  void getTransactions(String address,Callback cb){   
    	
    	makeManagedRequest(Command.account_tx, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
                return r == null || r.rpcerr == null || r.rpcerr != RPCErr.entryNotFound;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	cb.called(jsonObject);
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            	 request.json("account", address);
            	 request.json("ledger_index_min", -1);
            	 request.json("ledger_index_max", -1);
            	 request.json("binary", false);
            	 request.json("count", false);
            	 request.json("limit", 10);
            	 request.json("forward", false);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return response.result;
            }
        });
    }
    public Request getUserToken(String scope,String address,String neme){
    	 Request request = newRequest(Command.g_userToken);
	   	 JSONObject txjson = new JSONObject();
	   	 txjson.put("Owner", scope);
	   	 txjson.put("User", address);
	   	 txjson.put("TableName", neme);
	   	 request.json("tx_json", txjson);
	        request.once(Request.OnResponse.class, new Request.OnResponse() {
		            public  void called(Response response) {
		                if (response.succeeded) {
		                	//cb.called(response);
		                }
		            }
		        });
	        request.request();
	        while(request.response==null){
	       	 try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}  
	  	 	}
	   	 return request;	
    }
    public Request ping() {
        return newRequest(Command.ping);
    }

    public Request subscribeAccount(AccountID... accounts) {
        Request request = newRequest(Command.subscribe);
        JSONArray accounts_arr = new JSONArray();
        for (AccountID acc : accounts) {
            accounts_arr.put(acc);
        }
        request.json("accounts", accounts_arr);
        return request;
    }

    public Request subscribeBookOffers(Issue get, Issue pay) {
        Request request = newRequest(Command.subscribe);
        JSONObject book = new JSONObject();
        JSONArray books = new JSONArray(new Object[] { book });
        book.put("snapshot", true);
        book.put("taker_gets", get.toJSON());
        book.put("taker_pays", pay.toJSON());
        request.json("books", books);
        return request;
    }

    public Request requestBookOffers(Issue get, Issue pay) {
        Request request = newRequest(Command.book_offers);
        request.json("taker_gets", get.toJSON());
        request.json("taker_pays", pay.toJSON());
        return request;
    }
    
    public void Response(Response response){
    	
    }

}
