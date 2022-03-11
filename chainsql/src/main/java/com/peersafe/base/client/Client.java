package com.peersafe.base.client;

import static com.peersafe.base.client.requests.Request.VALIDATED_LEDGER;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.peersafe.chainsql.manager.CallbackManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.peersafe.base.client.enums.Command;
import com.peersafe.base.client.enums.Message;
import com.peersafe.base.client.enums.RPCErr;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.client.requests.Request.Manager;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.subscriptions.ServerInfo;
import com.peersafe.base.client.subscriptions.SubscriptionManager;
import com.peersafe.base.client.subscriptions.TrackedAccountRoot;
import com.peersafe.base.client.subscriptions.TransactionSubscriptionManager;
import com.peersafe.base.client.transactions.AccountTxPager;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.client.transport.TransportEventHandler;
import com.peersafe.base.client.transport.WebSocketTransport;
import com.peersafe.base.client.types.AccountLine;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Issue;
import com.peersafe.base.core.coretypes.STObject;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.types.known.sle.LedgerEntry;
import com.peersafe.base.core.types.known.sle.entries.Offer;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.chainsql.util.Util;

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
    public static interface OnChainsqlSubRet extends events<JSONObject> {}
    public static interface OnTXMessage extends events<JSONObject> {}
    public static interface OnSendMessage extends events<JSONObject> {}
    public static interface OnStateChange extends events<Client> {}
    public static interface OnPathFind extends events<JSONObject> {}
    public static interface OnValidatedTransaction extends events<TransactionResult> {}
    public static interface OnReconnecting extends events<JSONObject> {}
    public static interface OnReconnected extends events<JSONObject> {}
    public static interface OnContractEvent extends events<JSONObject> {}

    /**
     * Trigger when a transaction validated.
     * @param cb Callback
     * @return This.
     */
    public Client onValidatedTransaction(OnValidatedTransaction cb) {
        on(OnValidatedTransaction.class, cb);
        return this;
    }

    /**
     * Trigger when ledger closed
     * @param cb  Callback
     * @return  This.
     */
	public Client onLedgerClosed(OnLedgerClosed cb) {
        on(OnLedgerClosed.class, cb);
        return this;
    }
	
	/**
	 * Trigger when transaction related to a subscribed table validate_success or db_success.
	 * @param cb   Callback
	 * @return    This.
	 */
	public Client OnTBMessage(OnTBMessage cb) {
        on(OnTBMessage.class, cb);
        return this;
    }
	
	public Client OnSubChainsqlRet(OnChainsqlSubRet cb) {
		on(OnChainsqlSubRet.class,cb);
		return this;
	}
	/**
	 * Trigger when a subscribed  transaction validate_success or db_success.
	 * @param cb  Callback
	 * @return  This.
	 */
	public Client OnTXMessage(OnTXMessage cb) {
        on(OnTXMessage.class, cb);
        return this;
    }

	/**
	 * Trigger when websocket message received.
	 * @param cb  Callback
	 * @return  This.
	 */
	public Client OnMessage(OnMessage cb) {
        on(OnMessage.class, cb);
        return this;
    } 
	
	/**
	 * Trigger when reconnecting to a server begins.
	 * @param cb  Callback
	 * @return  This.
	 */
	public Client onReconnecting(OnReconnecting cb){
        on(OnReconnecting.class, cb);
        return this;
	}
	
	/**
	 * Trigger when reconnect to a server succeed.
	 * @param cb  Callback
	 * @return  This.
	 */
	public Client onReconnected(OnReconnected cb){
        on(OnReconnected.class, cb);
        return this;
	}
	
	/**
	 * Trigger when websocket connection succeed.
	 * @param onConnected  Callback
	 * @return  This.
	 */
    public Client onConnected(OnConnected onConnected) {
        this.on(OnConnected.class, onConnected);
        return this;
    }

    /**
     * Trigger when websocket connection disconnected.
     * @param cb  Callback.
     * @return  This.
     */
    public Client onDisconnected(OnDisconnected cb) {
        on(OnDisconnected.class, cb);
        return this;
    }
    
    public Client onContractEvent(OnContractEvent cb) {
        on(OnContractEvent.class, cb);
        return this;
    }

    public static void shutdown(){
        CallbackManager.instance().shutdown();
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
    public String previousUri;

    // Every x ms, we clean up timed out requests
    public long maintenanceSchedule = 10000; //ms

    public int SEQUENCE;
    
    public String NAMEINDB="";
    // Are we currently connected?
    public boolean connected = false;
    // If we haven't received any message from the server after x many
    // milliseconds, disconnect and reconnect again.
    private long reconnectDormantAfter = 300000; // ms
    // ms since unix time of the last indication of an alive connection
    private long lastConnection = -1; // -1 means null
    // Did we disconnect manually? If not, try and reconnect
    private boolean manuallyDisconnected = false;

    // Tracks the serverInfo we are currently connected to
    public ServerInfo serverInfo = new ServerInfo();
    private HashMap<AccountID, Account> accounts = new HashMap<AccountID, Account>();
    // Handles [un]subscription requests, also on reconnect
    public SubscriptionManager subscriptions = new SubscriptionManager();

    public String schemaID = "";
    
    private static final int MAX_REQUEST_COUNT = 10; 
    
    private ScheduledFuture reconnect_future = null;
    
    private boolean reconnecting = false;
    /**
     *  Constructor
     * @param ws Websocket implementation.
     */
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
        return 2000;
    }

    /**
     * 
     * @param transactionSubscriptionManager Subscribe manager.
     * @return Self.
     */
    public Client transactionSubscriptionManager(TransactionSubscriptionManager transactionSubscriptionManager) {
        this.transactionSubscriptionManager = transactionSubscriptionManager;
        return this;
    }
    
    /**
     * Log tools.
     * @param level Log level.
     * @param fmt Format.
     * @param args Args.
     */
    public static void log(Level level, String fmt, Object... args) {
        if (logger.isLoggable(level)) {
            logger.log(level, fmt, args);
        }
    }

    /**
     * JSON object to String.
     * @param object JSONObject.
     * @return JSON String.
     */
    public static String prettyJSON(JSONObject object) {
        return object.toString(4);
    }
    /**
     * JSON String to JSONObject.
     * @param s JSON String.
     * @return JSONObject.
     */
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
     * 
     * @param uri Websocket uri.
     * @return Self.
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
    
    public Client connect(final String uri,final String serverCertPath,final String storePass){
        manuallyDisconnected = false;

        schedule(50, new Runnable() {
            @Override
            public void run() {
                try {
					doConnect(uri,serverCertPath,storePass);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        });
        return this;
    }

    /**
     * Connect.
     * @param uri Connect uri.
     */
    public void doConnect(String uri) {
        log(Level.INFO, "Connecting to " + uri);
        previousUri = uri;
        ws.connect(URI.create(uri));
    }

    public void doConnect(String uri,String serverCertPath,String storePass) throws Exception {
        log(Level.INFO, "Connecting to " + uri);
        previousUri = uri;
        ws.connectSSL(URI.create(uri),serverCertPath,storePass);
    }
    /**
     * Disconnect from websocket-url
     */
    public void disconnect() {
        manuallyDisconnected = true;
    	disconnectInner();
        service.shutdownNow();
        //CallbackManager.instance().shutdown();
        // our disconnect handler should do the rest
    }
    
    private void disconnectInner(){
        ws.disconnect();
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

    /**
     * Reconnect when disconnected.
     */
    public void reconnect() {
    	//make sure only one schedule task at the same time
    	if(reconnecting) {
    		return;
    	}
    	
    	//sleep for 2 seconds first,because scheduleAtFixedRate execute immediately
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	emit(OnReconnecting.class,null);
    	
    	log(Level.INFO, "reconnecting");
//    	disconnectInner();
    	reconnecting = true;
    	reconnect_future = service.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				disconnectInner();
				doConnect(previousUri);
			}
        	
        }, 0,2000, TimeUnit.MILLISECONDS);
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

    /**
     *  Handler binders binder
     * @param s Url.
     * @param onConnected Callback.
     */
    public void connect(final String s, final OnConnected onConnected) {
        run(new Runnable() {
            public void run() {
                connect(s);
                once(OnConnected.class, onConnected);
            }
        });
    }

    /**
     * Disconnect from current connection.
     * @param onDisconnected Callback.
     */
    public void disconnect(final OnDisconnected onDisconnected) {
        run(new Runnable() {
            public void run() {
                Client.this.once(OnDisconnected.class, onDisconnected);
                disconnect();
            }
        });
    }

    /**
     * Trigger when connected.
     * @param nextTick Next tick trigger callback.
     * @param onConnected Callback.
     */
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

    /**
     * Now or when connected trigger.
     * @param onConnected Callback.
     */
    public void nowOrWhenConnected(OnConnected onConnected) {
        whenConnected(false, onConnected);
    }

    /**
     * Trigger next tick or when connected.
     * @param onConnected Callback.
     */
    public void nextTickOrWhenConnected(OnConnected onConnected) {
        whenConnected(true, onConnected);
    }

    /**
     * Release websocket connection.
     */
    public void dispose() {
        ws = null;
    }

    /* -------------------------------- EXECUTOR -------------------------------- */

    /**
     * Run a task.
     * @param runnable Thread object.
     */
    public void run(Runnable runnable) {
        // What if we are already in the client thread?? What happens then ?
        if (runningOnClientThread()) {
            runnable.run();
        } else {
            service.submit(errorHandling(runnable));
        }
    }

    /**
     * Start a scheduled task.
     * @param ms Milliseconds.
     * @param runnable Runnable object.
     */
    public void schedule(long ms, Runnable runnable) {
        service.schedule(errorHandling(runnable), ms, TimeUnit.MILLISECONDS);
    }

    private boolean runningOnClientThread() {
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
    /**
     * Override method,default.
     */
    @Override
    public void onConnecting(int attempt) {
    }

    /**
     * Override method,default.
     */
    @Override
    public void onError(Exception error) {
        onException(error);
    }

    /**
     * Run when disconnected.
     */
    @Override
    public void onDisconnected(boolean willReconnect) {
        run(new Runnable() {
            @Override
            public void run() {
                doOnDisconnected();
            }
        });
    }

    /**
     * Run when connected.
     */
    @Override
    public void onConnected() {
        run(new Runnable() {
            public void run() {
                doOnConnected();
            }
        });
    }
    
    public void unsubscribeStreams() {
    	unsubscribe(prepareSubscription());
    }
    public void resubscribeStreams() {
    	serverInfo.unprime();
    	subscribe(prepareSubscription());    	
    }

    /* ----------------------- CLIENT THREAD EVENT HANDLER ---------------------- */

    /**
     * Client message thread.
     * @param msg JSONObject msg.
     */
    public void onMessageInClientThread(JSONObject msg) {
//    	System.out.println("onMessageInClientThread:" + msg);
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
                case contract_event:
                	emit(OnContractEvent.class,msg);
                	break;
                case viewChange:
                	break;
                default:
                    unhandledMessage(msg);
                    break;
            }
        } catch (Exception e) {
            //logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            // This seems to be swallowed higher up, (at least by the
            // Java-WebSocket transport implementation)
            //System.out.println("error_message: "+msg);
           // throw new RuntimeException(e);
        } finally {
            emit(OnStateChange.class, this);
        }
    }
    private void doOnDisconnected() {
    	log(Level.INFO, getClass().getName() + ": doOnDisconnected");
        System.out.println("disconnected " + previousUri);
    	if(connected)
    		connected = false;
    	else
    		return;
        emitOnDisconnected();

        if (!manuallyDisconnected) {
        	reconnect();
        } else {
        	log(Level.INFO, "Currently disconnecting, so will not reconnect");
        }
    }

    private void doOnConnected() {
        resetReconnectStatus();

        logger.entering(getClass().getName(), "doOnConnected");
        connected = true;
        emit(OnConnected.class, this);
        
        //deal with reconnect
        if(reconnecting) {
        	log(Level.INFO,"reconnected");
			emit(OnReconnected.class,null);
			reconnect_future.cancel(true);
			reconnect_future = null;
			if(!reconnecting && serverInfo.primed()) {
				getLedgerVersion(new Callback<JSONObject>() {
					@Override
					public void called(JSONObject args) {
						serverInfo.ledger_index = args.getInt("ledger_current_index");
					}
		    	});	
			}
        	reconnecting = false;
        }
        
        subscribe(prepareSubscription());
        logger.exiting(getClass().getName(), "doOnConnected");
    }

    void unhandledMessage(JSONObject msg) {
        log(Level.WARNING, "Unhandled message: " + msg);
    }

    synchronized void onResponse(JSONObject msg) {
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

    /**
     * Transaction returned.
     * @param tr Result data.
     */
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

    private void sendMessage(JSONObject object) {
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

    /**
     * Request account information.
     * @param masterSeed Master seed for account.
     * @return Account information.
     */
    public Account accountFromSeed(String masterSeed) {
        IKeyPair kp = Seed.fromBase58(masterSeed).keyPair();
        return account(AccountID.fromKeyPair(kp), kp);
    }

    private Account account(final AccountID id, IKeyPair keyPair) {
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
//            subscriptions.addAccount(id);

            return account;
        }
    }

    private TrackedAccountRoot accountRoot(AccountID id) {
        TrackedAccountRoot accountRoot = new TrackedAccountRoot();
        // requestAccountRoot(id, accountRoot);
        return accountRoot;
    }

    private void requestAccountRoot(final AccountID id,
                                    final TrackedAccountRoot accountRoot) {

        makeManagedRequest(Command.account_info, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
                return r == null || r.rpcerr == null || r.rpcerr != RPCErr.entryNotFound;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	//System.out.println("requestAccountRoot response:" + jsonObject);
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
                request.json("account", id);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return response.result.getJSONObject("node");
            }
        });
    }

    /* ------------------------------ SUBSCRIPTIONS ----------------------------- */

    public void subscribe(JSONObject subscription) {
        Request request = newRequest(Command.subscribe);
        
        request.json(subscription);
        
        request.on(Request.OnSuccess.class, new Request.OnSuccess() {
            @Override
            public void called(Response response) {
            	JSONObject req = response.request.json();
            	if(req.has("streams")) {
                    serverInfo.update(response.result);
                    emit(OnSubscribed.class, serverInfo);	
            	}
            }
        });
        request.on(Request.OnResponse.class,new Request.OnResponse() {
			@Override
			public void called(Response response) {
				JSONObject req = response.request.json();
            	if(req.has("transaction") || (req.has("owner") && req.has("tablename"))) {
            		JSONObject obj = new JSONObject();
            		if(req.has("transaction"))
            			obj.put("transaction", req.getString("transaction"));
            		if(req.has("owner")) {
            			obj.put("owner", req.getString("owner"));
            		}
            		if(req.has("tablename")) {
            			obj.put("tablename", req.getString("tablename"));
            		}
            		obj.put("result", response.message);
            		emit(OnChainsqlSubRet.class,obj);
            	}
			}
		});
        request.request();
    }
    
    public void unsubscribe(JSONObject subscription) {
        Request request = newRequest(Command.unsubscribe);
        
        request.json(subscription);
        request.request();
    }

    private JSONObject prepareSubscription() {
        subscriptions.pauseEventEmissions();
        subscriptions.addStream(SubscriptionManager.Stream.ledger);
        subscriptions.addStream(SubscriptionManager.Stream.server);
        subscriptions.addStream(SubscriptionManager.Stream.view_change);
        subscriptions.unpauseEventEmissions();
        return subscriptions.allSubscribed();
    }

    /* ------------------------------ REQUESTS ------------------------------ */

    /**
     * Create a new request.
     * @param cmd Command name.
     * @return Request data.
     */
    public synchronized Request newRequest(Command cmd) {
        return new Request(cmd, cmdIDs++, this);
    }

    /**
     * Send a request message.
     * @param request Request data.
     */
    public void sendRequest(final Request request) {
    	//System.out.println("request:"+request.json());
        Logger reqLog = Request.logger;

        try {
        	synchronized(requests) {
                requests.put(request.id, request);
        	}
            request.bumpSendTime();
            sendMessage(request.toJSON());
            // Better safe than sorry
        } catch (Exception e) {
//            if (reqLog.isLoggable(Level.WARNING)) {
//                reqLog.log(Level.WARNING, "Exception when trying to request: {0}", e);
//            }
            nextTickOrWhenConnected(new OnConnected() {
                @Override
                public void called(Client args) {
                    sendRequest(request);
                }
            });
        }
    }

    /**
     *  Managed Requests API
     * @param cmd Command.
     * @param manager Manager.
     * @param builder Builder data.
     * @param <T> Builder parameter type.
     * @return Request data.
     */
    public <T> Request makeManagedRequest(final Command cmd, final Manager<T> manager, final Request.Builder<T> builder){
    	return makeManagedRequest(cmd,manager,builder,0);
    }
    
    private <T> Request makeManagedRequest(final Command cmd, final Manager<T> manager, final Request.Builder<T> builder,final int depth) {
    	if(depth > MAX_REQUEST_COUNT){
    		return null;
    	}
        final Request request = newRequest(cmd);
        final boolean[] responded = new boolean[]{false};
        request.once(Request.OnTimeout.class, new Request.OnTimeout() {
            @Override
            public void called(Response args) {
            	System.out.println("timeout");
                if (!responded[0] && manager.retryOnUnsuccessful(null)) {
                    logRetry(request, "Request timed out");
                    request.clearAllListeners();
                    queueRetry(50, cmd, manager, builder,depth);
                }else {
                	JSONObject msg = new JSONObject();
                	msg.put("status", "error");
                	msg.put("error", "timeOutError");
                	msg.put("error_message","Request for command:" + cmd.toString() + " time out!");
                	Response response = new Response(request,msg);
                	manager.cb(response, null);
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
        if (!manuallyDisconnected)
        {
            try {
                // Maybe service shutdown
                schedule(ms, new Runnable() {
                    @Override
                    public void run() {
                        makeManagedRequest(cmd, manager, builder,depth + 1);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void logRetry(Request request, String reason) {
        if (logger.isLoggable(Level.WARNING)) {
            log(Level.WARNING, previousUri + ": " + reason + ", muting listeners " +
                    "for " + request.json() + "and trying again");
        }
    }

    // ### Managed Requests

    /**
     * Request account transaction page.
     * @param accountID Account address.
     * @return AccountTxPager data.
     */
    public AccountTxPager accountTxPager(AccountID accountID) {
        return new AccountTxPager(this, accountID, null);
    }

    /**
     * Request for a ledger_entry.
     * @param index Hash.
     * @param ledger_index Ledger index.
     * @param cb Callback.
     */
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

    /**
     * Request for book_offers.
     * @param ledger_index Ledger's index.
     * @param get Get.
     * @param pay Pay.
     * @param cb Callback.
     */
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

 

    /**
     * Submit a transaction
     * @param tx_blob Tx_blob signed.
     * @param fail_hard Fail_hard.
     * @return Request data.
     */
    public Request submit(String tx_blob, boolean fail_hard) {
        Request req = newRequest(Command.submit);
        req.json("tx_blob", tx_blob);
        req.json("fail_hard", fail_hard);
        return req;
    }
    
    /**
     * Request for account information.
     * @param account Account address.
     * @return Request data.
     */
    public JSONObject accountInfo(AccountID account) {
        Request request = newRequest(Command.account_info);
        request.json("account", account.address);

        //request.json("schema_id", schemaID);

        request.request();
        waiting(request);
   		return getResult(request);
    }
    
    /**
     * Select data from chain.
     * @param secret Account seed.
     * @param account Account address.
     * @param owner Table owner address.
     * @param name  Table name.
     * @param raw Raw data.
     * @param cb Callback function.
     * @return Request data.
     */
	public JSONObject select(final String secret,final AccountID account, final AccountID owner, final String name, final String raw,final Callback<JSONObject> cb) {
		String tablestr = "{\"Table\":{\"TableName\":\"" + name+ "\"}}";
		JSONArray tableArray =  Util.strToJSONArray(tablestr);
		JSONObject txjson = new JSONObject();
		txjson.put("Account", account);
		txjson.put("Owner", owner);
		txjson.put("Tables", tableArray);
		txjson.put("Raw", raw);

		if(cb != null) {
        	getLedgerVersion(new Callback<JSONObject>() {
				@Override
				public void called(JSONObject args) {
					if(args.has("ledger_current_index")) {
						txjson.put("LedgerIndex", args.getInt("ledger_current_index") - 1);
					}
					selectASync(Command.r_get,secret,txjson,cb);
				}
        	});
			JSONObject obj = new JSONObject();
			obj.put("final_result", true);
			return obj;
		}else {
			return selectSync(secret,txjson);
		}
	}
	private void prepareRequestForSelect(Request request,final String secret,final JSONObject txjson) {
		String signData = txjson.toString();
		byte[] signature = Util.sign(signData.getBytes(), secret);
		request.json("tx_json", txjson);
		request.json("publicKey",Util.getPublicHexFromSecret(secret));
		request.json("signature",Util.bytesToHex(signature));
		request.json("signingData",signData);
	}
	private JSONObject selectSync(final String secret,final JSONObject txjson) {
		JSONObject ledger = getLedgerVersion();
		if(ledger.has("ledger_current_index")) {
			txjson.put("LedgerIndex", ledger.getInt("ledger_current_index") - 1);
		}else {
			return ledger;
		}

		Request request = newRequest(Command.r_get);
		prepareRequestForSelect(request,secret,txjson);
		request.request();
		waiting(request);
		JSONObject res = getResult(request);
		return getSelectRes(res);
	}
	

	private void selectASync(Command command,final String secret,final JSONObject txjson,final Callback<JSONObject> cb) {
		makeManagedRequest(command, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            	prepareRequestForSelect(request,secret,txjson);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
        		return getSelectRes(response.result);
            }
        });
	}
	
	private JSONObject getSelectRes(JSONObject result){
		JSONObject obj = new JSONObject();
		if(!result.has("error")) {
			if(result.has("diff")) {
				obj.put("diff", result.getInt("diff"));
			}
			if(obj.has("lines")) {
				obj.put("lines", result.get("lines"));
			}else {
				obj = result;
			}
		}else {
			obj = result;
		}
		obj.put("final_result", true);
		return obj;
	}

	public JSONObject getBySqlUser(String secret,String accountID,String sql) {
		JSONObject tx_json = new JSONObject();
		tx_json.put("Account", accountID);
		tx_json.put("Sql", sql);
		JSONObject ledger = getLedgerVersion();
		if(ledger.has("ledger_current_index")) {
			tx_json.put("LedgerIndex", ledger.getInt("ledger_current_index") - 1);
		}else {
			return ledger;
		}

		Request request = newRequest(Command.r_get_sql_user);
		prepareRequestForSelect(request,secret,tx_json);
		request.request();
		waiting(request);
		JSONObject res = getResult(request);
		return getSelectRes(res);
	}
	
	public void getBySqlUser(String secret,String accountID,String sql,Callback<JSONObject> cb) {
		getLedgerVersion(new Callback<JSONObject>() {
			@Override
			public void called(JSONObject args) {
				JSONObject tx_json = new JSONObject();
				tx_json.put("Account", accountID);
				tx_json.put("Sql", sql);
				if(args.has("ledger_current_index")) {
					tx_json.put("LedgerIndex", args.getInt("ledger_current_index") - 1);
				}
				selectASync(Command.r_get_sql_user,secret,tx_json,cb);
			}
    	});
		
	}	
	
	public JSONObject getBySqlAdmin(String sql) {
		Request request = newRequest(Command.r_get_sql_admin);
		request.json("sql", sql);
		request.request();
		waiting(request);
		JSONObject res = getResult(request);
		return getSelectRes(res);
	}
	
	public void getBySqlAdmin(String sql,Callback<JSONObject> cb) {
		makeManagedRequest(Command.r_get_sql_admin, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            	request.json("sql", sql);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
            	JSONObject res = getResult(response.request);
        		return getSelectRes(res);
            }
        });
	}
	
	public JSONObject getNameInDB(String owner,String tableName) {
		Request request = newRequest(Command.g_dbname);
		request.json("account", owner);
		request.json("tablename", tableName);
		
		request.request();
		
		waiting(request);
		return getResult(request);
	}
	
	public void getNameInDB(String owner,String tableName,Callback<JSONObject> cb) {
		makeManagedRequest(Command.g_dbname, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            	request.json("account", owner);
       		 	request.json("tablename", tableName);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return getResult(response.request);
            }
        });
	}
	
	public JSONObject getLedgerTxs(Integer ledgerSeq,boolean bIncludeSuccess,boolean bIncludefailure)
	{
		Request request = newRequest(Command.ledger_txs);

		request.json("ledger_index", ledgerSeq);
	 	request.json("include_success", bIncludeSuccess);
	 	request.json("include_failure",bIncludefailure);

	 	request.request();
	 	
		waiting(request);
		return getResult(request);	
	}
	

	 public void getLedgerTxs(final Integer ledgerSeq,final boolean bIncludeSuccess,final boolean bIncludefailure,final Callback<JSONObject> cb){
	 	makeManagedRequest(Command.ledger_txs, new Manager<JSONObject>() {
	         @Override
	         public boolean retryOnUnsuccessful(Response r) {
	         	return false;
	         }
	
	         @Override
	         public void cb(Response response, JSONObject jsonObject) throws JSONException {
	         	if(response.succeeded) {
	         		cb.called(jsonObject);
	         	}else {
	         		JSONObject res = getResult(response.request);
	         		cb.called(res);
	         	}
	         }
	     }, new Request.Builder<JSONObject>() {
	         @Override
	         public void beforeRequest(Request request) {
	     		request.json("ledger_index", ledgerSeq);
	    	 	request.json("include_success", bIncludeSuccess);
	    	 	request.json("include_failure",bIncludefailure);
	         }
	
	         @Override
	         public JSONObject buildTypedResponse(Response response) {
	             return getResult(response.request);
	         }
	     });
	}
	 
	public JSONObject getLedger(JSONObject option) {
		Request request = newRequest(Command.ledger);

		request.json("ledger_index", option.get("ledger_index"));
	 	request.json("expand", false);
	 	request.json("transactions",true);
	 	request.json("accounts",false );

	 	request.request();
	 	
		waiting(request);
		return getResult(request);	 	
	}
    /**
     * Request for ledger data.
     * @param option Ledger options.
     * @param cb Callback.
     */
    public void getLedger(final JSONObject option,final Callback<JSONObject> cb){
    	makeManagedRequest(Command.ledger, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
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
                return getResult(response.request);
            }
        });
    }
    
    private void UnhexResult(Response response) {
    	if(response != null && response.result != null && response.result.has("transactions")) {
    		JSONArray txs = (JSONArray)response.result.get("transactions");
        	for(int i=0; i<txs.length(); i++){
        		JSONObject tx = (JSONObject)txs.get(i);
        		Util.unHexData(tx.getJSONObject("tx"));
//        		if(tx.has("meta")){
//        			tx.remove("meta");
//        		}
        	}
    	}
    }
    public JSONObject getTransactions(String address,int limit) {
    	Request request = newRequest(Command.account_tx);
    	request.json("account", address);
      	request.json("ledger_index_min", -1);
      	request.json("ledger_index_max", -1);
      	request.json("limit", limit);
      	 
      	request.request();
      	
 		waiting(request);
 		UnhexResult(request.response); 		
 		return getResult(request);	
    }
    
    /**
     * Request for transaction information.
     * @param address Account address.
     * @param limit Transaction count limit.
     * @param cb Callback.
     */
    public  void getTransactions(final String address,final int limit,final Callback<JSONObject> cb){
    	getTransactions(address,limit,null,cb);
    }
    /**
     * Request for transaction information.
     * @param address Account address.
     * @param limit Transaction count limit.
     * @param marker Marker from previous call response.
     * @param cb Callback.
     */
    public  void getTransactions(final String address,final int limit,final JSONObject marker,final Callback<JSONObject> cb){
    	makeManagedRequest(Command.account_tx, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
	           	 request.json("account", address);
	           	 if(marker != null) {
	           		 request.json("marker",marker);
	           	 }
	           	 request.json("ledger_index_min", -1);
	           	 request.json("ledger_index_max", -1);
	           	 request.json("limit", limit);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
            	UnhexResult(response);            	
                return getResult(response.request);
            }
        });
    }
    
    /**
     * Request for transaction information.
     * @param hash Tx hash ,if "" it will find first tx on this chain.
     * @param limit Transaction count limit.
     * @param include If include the transaction that hash point out.
     * @param cb Callback.
     */
    public  void getCrossChainTxs(final String hash,final int limit,final boolean include,final Callback<JSONObject> cb){
    	makeManagedRequest(Command.tx_crossget, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
	           	 request.json("transaction_hash", hash);
	           	 request.json("limit", limit);
	           	 request.json("inclusive",include);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
            	UnhexResult(response);            	
                return response.result;
            }
        });
    }
    private void waiting(Request request){
        int count = 100;
        while(request.response==null){
        	Util.waiting(); 
        	if(--count == 0){
        		break;
        	}
   	 	}
    }
    
    private JSONObject getResult(Request request) {
    	Response response = request.response;
    	if(response != null) {
    		if(response.result != null) {
    			return response.result;	
    		}else if(response.message != null) {
    			return response.message;
    		}else {
    			return new JSONObject();
    		}
    		
    	}else {
    		JSONObject ret = new JSONObject();
    		ret.put("error", "timeOutError");
    		ret.put("error_message", "request for command:" + request.cmd.toString() + " timeout");
    		return ret;
    	}
    }
    
    public JSONObject getLedgerVersion() {
    	Request request = newRequest(Command.ledger_current);
	    request.request();
	    waiting(request);
	    return getResult(request);
    }
    
    public void getLedgerVersion(final Callback<JSONObject> cb) {
    	makeManagedRequest(Command.ledger_current, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
	       	   	 
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return getResult(response.request);
            }
        });
    }
    /**
     * Get transaction count on chain.
     * @return Transaction account data.
     */
    public JSONObject getTransactionCount(){
    	Request request = newRequest(Command.tx_count);
	    request.request();
	    waiting(request);
	    return getResult(request);
    }
    
    /**
     * Get server_info
     * @return Server_info data.
     */
    public JSONObject getServerInfo(){
    	Request request = newRequest(Command.server_info);
        request.request();
        waiting(request);
        return getResult(request);
    }

    /**
     * Get unl_list
     * @return unl_list data
     */
    public JSONObject getUnlList(){
    	Request request = newRequest(Command.unl_list);
        request.request();
        waiting(request);
        return getResult(request);
    }
    /**
     * Get user_token for table,if token got not null, it is a confidential table.
     * @param owner Table's owner/creator.
     * @param user	Operating account.
     * @param name	Table name.
     * @return Request object contains response data.
     */
    public JSONObject getUserToken(String owner,String user,String name){
    	 Request request = newRequest(Command.g_userToken);
	   	 JSONObject txjson = new JSONObject();
	   	 txjson.put("Owner", owner);
	   	 txjson.put("User", user);
	   	 txjson.put("TableName", name);
	   	 request.json("tx_json", txjson);

         request.request();
         waiting(request);

         return getResult(request);	
    }
    
    public void getUserToken(final String owner,final String user,final String name,final Callback<JSONObject> cb) {
       	makeManagedRequest(Command.g_userToken, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
	       	   	 JSONObject txjson = new JSONObject();
	    	   	 txjson.put("Owner", owner);
	    	   	 txjson.put("User", user);
	    	   	 txjson.put("TableName", name);
	    	   	 request.json("tx_json", txjson);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return getResult(response.request);
            }
        });
    }
    
    /**
     * Prepare for a transaction for : filling in NameInDB field, filling in CheckHash field for StrictMode
     * @param txjson tx_json with fields and value a transaction needed.
     * @return Prepared tx_json.
     */
    public JSONObject tablePrepare(JSONObject txjson){
    	Request request = newRequest(Command.t_prepare);
	   	request.json("tx_json", txjson);
	    request.request();
	    waiting(request);
	    return getResult(request);	
   }
    /**
     * contractCall synchronously
     * @param obj call parameters
     * @return call return
     */
    public JSONObject contractCall(JSONObject obj) {
    	Request request = newRequest(Command.contract_call);
    	Iterator<String> it = obj.keys();
    	while(it.hasNext()) {
    		String key = (String) it.next();  
            String value = obj.getString(key);  
            request.json(key,value);
    	}
	    request.request();
	    waiting(request);
	    return getResult(request);
    }

    /**
     * GetAccountLines synchronously
     * @param address Address to get trust lines.
     * @return account trustlines
     */
	public JSONObject GetAccountLines(String address){
		Request request = newRequest(Command.account_lines);
		request.json("account", address);
		request.request();
		waiting(request);
		return getResult(request);
	}

    /**
     * Get peers
     * @return peers data.
     */
    public JSONObject getPeers(){
        Request request = newRequest(Command.peers);
        request.request();
        waiting(request);
        return getResult(request);
    }

    /**
     * Request for account_lines.
     * @param addy account id.
     * @param cb Callback.
     */
    public void GetAccountLines(final AccountID addy,final Callback<JSONObject> cb) {
        makeManagedRequest(Command.account_lines, new Manager<JSONObject>() {
        	 @Override
             public boolean retryOnUnsuccessful(Response r) {
             	return false;
             }

             @Override
             public void cb(Response response, JSONObject jsonObject) throws JSONException {
             	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
             }
         	}, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
                request.json("account", addy);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
               return response.result;
            }
        });
    }

	public JSONObject getTableAuth(String owner,String tableName,List<String> accounts) {
		Request request = newRequest(Command.table_auth);
		request.json("owner", owner);
		request.json("tablename", tableName);
		if(accounts != null && accounts.size() != 0) {
			request.json("accounts",Util.listToJSONArray(accounts));
		}
		request.request();
		waiting(request);
		return getResult(request);
	}
	
	public void getTableAuth(final String owner,final String tableName,final List<String> accounts,final Callback<JSONObject> cb) {
       	makeManagedRequest(Command.table_auth, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            	request.json("owner", owner);
        		request.json("tablename", tableName);
        		if(accounts != null && accounts.size() != 0) {
        			request.json("accounts",Util.listToJSONArray(accounts));
        		}
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return getResult(response.request);            
            }
        });
    }
	
	public JSONObject getAccountTables(String address,boolean bGetDetail) {
		Request request = newRequest(Command.g_accountTables);
		request.json("account", address);
		if(bGetDetail) {
    		request.json("detail",true);
    	}
		request.request();
		waiting(request);
		return getResult(request);
	}
	
    public void getAccountTables(final String address,final boolean bGetDetail,final Callback<JSONObject> cb) {
       	makeManagedRequest(Command.g_accountTables, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            	request.json("account", address);
            	
            	if(bGetDetail) {
            		request.json("detail",true);
            	}
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
                return getResult(response.request);
            }
        });
    }
	
    /**
     * contractCall asynchronously
     * @param obj call parameters
     * @param cb Callback.
     */
    public void contractCall(final JSONObject obj,final Callback<JSONObject> cb) {
	    makeManagedRequest(Command.contract_call, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            	Iterator<String> it = obj.keys();
            	while(it.hasNext()) {
            		String key = (String) it.next();  
                    String value = obj.getString(key);  
                    request.json(key,value);
            	}
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
            	return getResult(response.request);
            }
        });
    }
    
    /**
     * getTransaction synchronously
     * @param hash Transaction hash.
     * @return Transaction details.
     */
    public JSONObject getTransaction(String hash) {
    	Request request = newRequest(Command.tx);
    	request.json("transaction", hash);
	    request.request();
	    waiting(request);
	    return getResult(request);
    }
    
    /**
     * Request for a transaction's information.
     * @param hash Transaction hash.
     * @param cb Callback.
     */
    public  void getTransaction(final String hash,final Callback<JSONObject> cb){   
    	
    	makeManagedRequest(Command.tx, new Manager<JSONObject>() {
            @Override
            public boolean retryOnUnsuccessful(Response r) {
            	return false;
            }

            @Override
            public void cb(Response response, JSONObject jsonObject) throws JSONException {
            	if(response.succeeded) {
            		cb.called(jsonObject);
            	}else {
            		JSONObject res = getResult(response.request);
            		cb.called(res);
            	}
            }
        }, new Request.Builder<JSONObject>() {
            @Override
            public void beforeRequest(Request request) {
            	 request.json("transaction", hash);
            }

            @Override
            public JSONObject buildTypedResponse(Response response) {
//    			if(response.result.has("meta")){
//    				response.result.remove("meta");
//    			}
    			Util.unHexData(response.result);
                return getResult(response.request);
            }
        });
    }



    /**
     * getTransaction synchronously
     * @param txInfo tx information.
     * @return Transaction details.
     */
    public JSONObject getTransaction(JSONObject txInfo) {

        boolean meta,meta_chain;
        meta = meta_chain = true;

        if(txInfo.has("meta") && !txInfo.getBoolean("meta")){
            meta = false;
        }

        if(txInfo.has("meta_chain") && !txInfo.getBoolean("meta_chain")){
            meta_chain = false;
        }

        Request request = newRequest(Command.tx);
        request.json("transaction", txInfo.getString("hash"));
        request.json("meta", meta);
        request.json("meta_chain", meta_chain);
        request.request();
        waiting(request);
        return getResult(request);
    }


    
    /**
     * Request ping.
     * @return ping Result.
     */
    public Request ping() {
        return newRequest(Command.ping);
    }

    /**
     * Subscribe for account.
     * @param accounts Account addresses.
     * @return Request data.
     */
    public Request subscribeAccount(AccountID... accounts) {
        Request request = newRequest(Command.subscribe);
        JSONArray accounts_arr = new JSONArray();
        for (AccountID acc : accounts) {
            accounts_arr.put(acc);
        }
        request.json("accounts", accounts_arr);
        return request;
    }

    public Request unsubscribeAccount(AccountID... accounts){
        Request request = newRequest(Command.unsubscribe);
        JSONArray accounts_arr = new JSONArray();
        for (AccountID acc : accounts) {
            accounts_arr.put(acc);
        }
        request.json("accounts", accounts_arr);
        return request;
    }
    

    /**
     * Request for book-offers.
     * @param get Get.
     * @param pay Pay.
     * @return Request data.
     */
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

    /**
     * Request for book offers.
     * @param get Get.
     * @param pay Pay.
     * @return Request data.
     */
    public Request requestBookOffers(Issue get, Issue pay) {
        Request request = newRequest(Command.book_offers);
        request.json("taker_gets", get.toJSON());
        request.json("taker_pays", pay.toJSON());
        return request;
    }



    /**
     * Get schema_list
     * @return schema_list data.
     */
    public JSONObject getSchemaList(JSONObject params){
        Request request = newRequest(Command.schema_list);
        request.request();

        if(params.has("account")){
            request.json("account", params.getString("account"));
        }

        if(params.has("running")){
            request.json("running",params.getBoolean("running"));
        }


        waiting(request);
        return getResult(request);
    }


    /**
     * Request for schema_info.
     * @param schemaID schemaID.
     * @return Request data.
     */
    public JSONObject getSchemaInfo(String schemaID) {
        Request request = newRequest(Command.schema_info);
        request.json("schema", schemaID);

        request.request();
        waiting(request);
        return getResult(request);
    }
}
