package com.peersafe.chainsql.core;

import static com.ripple.java8.utils.Print.print;
import static com.ripple.java8.utils.Print.printErr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.resources.Operate;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.JSONUtil;
import com.peersafe.chainsql.util.TopLevel;
import com.peersafe.chainsql.util.Validate;
import com.ripple.client.Account;
import com.ripple.client.pubsub.Publisher.Callback;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.TableListSet;

public class Chainsql extends TopLevel {
	public Connection connection;
	private String owner;
	private String[] query;
	private String exec;
	public Operate perm;
	public EventManager event;
	public boolean strictMode;

	public void as(String address, String secret) {
		this.connection.address = address;
		this.connection.secret = secret;
		if (this.connection.scope == null) {
			this.connection.scope = address;
		}
	}

	public void use(String address) {
		this.connection.scope = address;
	}

	public static final Chainsql c = new Chainsql();

	public Connection connect(String url) {
		connection = new Connection().connect(url);
		while (!connection.client.connected) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.event = new EventManager(this.connection);
		return connection;
	}

	public void disconnect() {
		this.connection.disconnect();
	}

	public void setRestrict(boolean falg) {
		this.strictMode = falg;
	}

	public Table table(String name) {
		Table tab = new Table(name);
		tab.connection = this.connection;
		return tab;
	}

	public Chainsql createTable(String name, List<String> raw, Callback<JSONObject> cb) {
		use(this.connection.address);
		List<JSONObject> strraw = new ArrayList<JSONObject>();
		for (String s : raw) {
			JSONObject json = JSONUtil.StrToJson(s);
			strraw.add(json);
		}
		try {
			JSONUtil.checkinsert(strraw);
		} catch (Exception e) {
			//table.message = e.getLocalizedMessage();
			System.out.println("Exception:" + e.getLocalizedMessage());
			//e.printStackTrace();
		}
		return create(name, strraw.toString(), cb);

	}

	private Chainsql create(String name, String raw, Callback<JSONObject> cb) {
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, name);
		return create(name, raw, map, cb);
	}

	private Chainsql create(String name, String raw, Map map, Callback<JSONObject> cb) {
		Account account = this.connection.client.accountFromSeed(this.connection.secret);
		TransactionManager tm = account.transactionManager();
		TableListSet payment = new TableListSet();
		String str = "{\"Table\":{\"TableName\":\"" + JSONUtil.toHexString(name) + "\",\"NameInDB\":\"" + map.get("NameInDB") + "\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		payment.as(AccountID.Account, this.connection.address);
		payment.as(STArray.Tables, arr);
		payment.as(Blob.Raw, JSONUtil.toHexString(raw));
		payment.as(UInt16.OpType, 1);
		payment.as(UInt32.Sequence, map.get("Sequence"));
		payment.as(Amount.Fee, fee);

		SignedTransaction signed = payment.sign(this.connection.secret);
		submit(tm, signed, cb);

		return this;
	}

	public Chainsql drop(String name,Callback<JSONObject> cb) {
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, name);
		return drop(name, map,cb);

	}

	private Chainsql drop(String name, Map map,Callback<JSONObject> cb) {
		Account account = this.connection.client.accountFromSeed(this.connection.secret);
		TransactionManager tm = account.transactionManager();
		String str = "{\"Table\":{\"TableName\":\"" + JSONUtil.toHexString(name) + "\",\"NameInDB\":\"" + map.get("NameInDB") + "\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		TableListSet payment = new TableListSet();
		payment.as(AccountID.Account, this.connection.address);
		payment.as(STArray.Tables, arr);
		payment.as(UInt16.OpType, 2);
		payment.as(UInt32.Sequence, map.get("Sequence"));
		payment.as(Amount.Fee, fee);
		SignedTransaction signed = payment.sign(this.connection.secret);
		submit(tm, signed, cb);
		return this;
	}

	public Chainsql reName(String oldName, String newName,Callback<JSONObject> cb) {
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, oldName);
		return reName(oldName, newName, map,cb);
	}

	private Chainsql reName(String oldName, String newName, Map map,Callback<JSONObject> cb) {
		Account account = this.connection.client.accountFromSeed(this.connection.secret);
		TransactionManager tm = account.transactionManager();
		String str = "{\"Table\":{\"TableName\":\"" + JSONUtil.toHexString(oldName) + "\",\"NameInDB\":\"" + map.get("NameInDB") + "\",\"TableNewName\":\"" + JSONUtil.toHexString(newName) + "\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		TableListSet payment = new TableListSet();
		payment.as(AccountID.Account, this.connection.address);
		payment.as(STArray.Tables, arr);
		payment.as(UInt16.OpType, 3);
		payment.as(UInt32.Sequence, map.get("Sequence"));
		payment.as(Amount.Fee, fee);

		SignedTransaction signed = payment.sign(this.connection.secret);
		submit(tm, signed, cb);
		return this;
	}

	public Chainsql assign(String name, String user, List flag,Callback<JSONObject> cb) {
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, name);
		return assign(name, user, flag, map,cb);
	}

	private Chainsql assign(String name, String user, List flag, Map map,Callback<JSONObject> cb) {
		Account account = this.connection.client.accountFromSeed(this.connection.secret);
		TransactionManager tm = account.transactionManager();
		String str = "{\"Table\":{\"TableName\":\"" + JSONUtil.toHexString(name) + "\",\"NameInDB\":\"" + map.get("NameInDB") + "\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		Integer flags = Validate.assign(flag);
		TableListSet payment = new TableListSet();
		payment.as(AccountID.Account, this.connection.address);
		payment.as(STArray.Tables, arr);
		payment.as(UInt16.OpType, 4);
		payment.as(AccountID.User, user);
		payment.as(UInt32.Flags, flags);
		payment.as(UInt32.Sequence, map.get("Sequence"));
		payment.as(Amount.Fee, fee);

		SignedTransaction signed = payment.sign(this.connection.secret);
		submit(tm, signed, cb);
		return this;
	}

	public Chainsql assignCancle(String name, String raw, List flag,Callback<JSONObject> cb) {
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, name);
		return assignCancle(name, raw, flag, map,cb);
	}

	private Chainsql assignCancle(String name, String user, List flag, Map map,Callback<JSONObject> cb) {
		Account account = this.connection.client.accountFromSeed(this.connection.secret);
		TransactionManager tm = account.transactionManager();
		String str = "{\"Table\":{\"TableName\":\"" + JSONUtil.toHexString(name) + "\",\"NameInDB\":\"" + map.get("NameInDB") + "\"}}";
		STArray arr = Validate.fromJSONArray(str);
		Integer flags = Validate.assign(flag);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		TableListSet payment = new TableListSet();
		payment.as(AccountID.Account, this.connection.address);
		payment.as(STArray.Tables, arr);
		payment.as(UInt16.OpType, 5);
		payment.as(AccountID.User, user);
		payment.as(UInt32.Flags, flags);
		payment.as(UInt32.Sequence, map.get("Sequence"));
		payment.as(Amount.Fee, fee);

		SignedTransaction signed = payment.sign(this.connection.secret);
		submit(tm, signed, cb);
		return this;
	}
	public void getLedger(JSONObject option,Callback cb){
		Request req = this.connection.client.getLedger(option,(data)->{
			Response response = (Response) data;
			if( !"error".equals(response.status)){
				//System.out.println(response.result);
				cb.called(response.message);
			}else{
				
				//System.out.println("error_message :"+ response.error_message);
				cb.called(response.error_message);
			}
		});
		
	}
	
	public void getLedgerVersion(Callback cb){
		Request req = this.connection.client.getLedgerVersion((data)->{
			Response response = (Response) data;
			if( !"error".equals(response.status)){
				cb.called(response.result.get("ledger_current_index"));
			}else{
				cb.called(response.error_message);
			}
		});
		
	}
	
	private void onValidated(ManagedTxn managed) {
		TransactionResult tr = managed.result;
		//cb.called(tr.toJSON());
		//print("Result:\n{0}", tr.toJSON().toString(2));
		//print("Transaction result was: {0}", tr.engineResult);
		//System.exit(0);
	}

	private void onError(ManagedTxn managed) {
		printErr("Transaction failed!");
		managed.submissions.forEach(sub ->
				printErr("{0}", sub.hash));
		// System.exit(1);
	}

	public void submit(TransactionManager tm, SignedTransaction signed, Callback cb) {
		tm.queue(tm.manage(signed.txn)
				.onValidated(cb)
				.onError(this::onError));

	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Operate getPerm() {
		return perm;
	}

	public void setPerm(Operate perm) {
		this.perm = perm;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public static Chainsql getC() {
		return c;
	}

	public String[] getQuery() {
		return query;
	}

	public void setQuery(String[] query) {
		this.query = query;
	}

	public String getExec() {
		return exec;
	}

	public void setExec(String exec) {
		this.exec = exec;
	}


}
