package com.peersafe.chainsql.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.EventManager;
import com.peersafe.chainsql.util.JSONUtil;
import com.peersafe.chainsql.util.Validate;
import com.ripple.client.pubsub.Publisher.Callback;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.TableListSet;

public class Chainsql extends Submit {
	private String owner;
	private String[] query;
	private String exec;
	public	EventManager event;
	public List<JSONObject> cache = new ArrayList<JSONObject>();
	private boolean strictMode = false;
	private boolean transaction = false;
	private Integer needVerify = 1;
	private JSONObject json;
	
	private SignedTransaction signed;

	 public List array(Object val0, Object... vals){
		 	List res = new ArrayList();
		 	if(val0.getClass().isArray()){
		 		String[] a = (String[]) val0; 
		 		for(String s:a){
		 			res.add(s);
		 		}
		 		
		 	}else{
		 		  res.add(val0);
			      res.addAll(Arrays.asList(vals));
		 	}
	        return res;
	 }
	 
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
		 if (this.transaction) {
			   	tab.transaction = this.transaction;
			    tab.cache = this.cache;
			  }
			  tab.strictMode = this.strictMode;
			  tab.event = this.event;
			  tab.connection = this.connection;
		return tab;
	}
	
	@Override
	JSONObject doSubmit() {
		return doSubmit(signed);
	}
	
	private boolean mapError(Map map){
		if(map.get("Sequence") == null || map.get("NameInDB") == null){
	    	return true;
	    }else{
	    	return false;
	    }
	}
	public Chainsql createTable(String name, List<String> raw) {
		return createTable(name, raw , false);
	}
	
	public Chainsql createTable(String name, List<String> raw ,boolean confidential) {
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
		if(this.transaction){
			json.put("OpType", 1);
			json.put("TableName", name);
			json.put("Raw", strraw);
			json.put("confidential",confidential);
			
			this.cache.add(json);
			return this;
		}
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, name);
		
		if(mapError(map)){
			return this;
		}else{
			return create(name, strraw.toString(), map);
		}		
	}

	private Chainsql create(String name, String raw, Map map) {
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

		signed = payment.sign(this.connection.secret);

		return this;
	}

	public Chainsql dropTable(String name) {
		if(this.transaction){
			json.put("OpType", 2);
			json.put("TableName", name);
			this.cache.add(json);
			return this;
		}
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, name);
		if(mapError(map)){
			return this;
		}else{
			return drop(name, map);
		}
	}

	private Chainsql drop(String name, Map map) {
		String str = "{\"Table\":{\"TableName\":\"" + JSONUtil.toHexString(name) + "\",\"NameInDB\":\"" + map.get("NameInDB") + "\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		TableListSet payment = new TableListSet();
		payment.as(AccountID.Account, this.connection.address);
		payment.as(STArray.Tables, arr);
		payment.as(UInt16.OpType, 2);
		payment.as(UInt32.Sequence, map.get("Sequence"));
		payment.as(Amount.Fee, fee);
		signed = payment.sign(this.connection.secret);

		return this;
	}

	public Chainsql renameTable(String oldName, String newName) {
		if(this.transaction){
			json.put("OpType", 3);
			json.put("oldName", oldName);
			json.put("newName", newName);
			this.cache.add(json);
			return this;
		}
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, oldName);
		if(mapError(map)){
			return this;
		}else{
			return rename(oldName, newName, map);
		}
	}

	private Chainsql rename(String oldName, String newName, Map map) {
		String str = "{\"Table\":{\"TableName\":\"" + JSONUtil.toHexString(oldName) + "\",\"NameInDB\":\"" + map.get("NameInDB") + "\",\"TableNewName\":\"" + JSONUtil.toHexString(newName) + "\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		TableListSet payment = new TableListSet();
		payment.as(AccountID.Account, this.connection.address);
		payment.as(STArray.Tables, arr);
		payment.as(UInt16.OpType, 3);
		payment.as(UInt32.Sequence, map.get("Sequence"));
		payment.as(Amount.Fee, fee);

		signed = payment.sign(this.connection.secret);
		return this;
	}

	public Chainsql grant(String name, String user, List<String> flag) {
		List<JSONObject> flags = new ArrayList<JSONObject>();
		for (String s : flag) {
			JSONObject json = JSONUtil.StrToJson(s);
			flags.add(json);
		}
		if(this.transaction){
			json.put("OpType", 11);
			json.put("TableName", name);
			json.put("Raw", flags);
			this.cache.add(json);
			return this;
		}
		AccountID account = AccountID.fromAddress(this.connection.address);
		Map map = Validate.rippleRes(this.connection.client, account, name);
		if(mapError(map)){
			return this;
		}else{
			return grant(name, user, flags.toString(), map);
		}
	}

	private Chainsql grant(String name, String user, String flag, Map map) {
		String str = "{\"Table\":{\"TableName\":\"" + JSONUtil.toHexString(name) + "\",\"NameInDB\":\"" + map.get("NameInDB") + "\"}}";
		STArray arr = Validate.fromJSONArray(str);
		String fee = this.connection.client.serverInfo.fee_ref + "";
		TableListSet payment = new TableListSet();
		payment.as(AccountID.Account, this.connection.address);
		payment.as(STArray.Tables, arr);
		payment.as(UInt16.OpType, 11);
		payment.as(AccountID.User, user);
		payment.as(Blob.Raw, JSONUtil.toHexString(flag.toString()));
		payment.as(UInt32.Sequence, map.get("Sequence"));
		payment.as(Amount.Fee, fee);

		signed = payment.sign(this.connection.secret);
		return this;
	}

	public void beginTran(){
		 if (this.connection!=null && this.connection.address!=null) {
		    this.transaction = true;
		    return;
		  }
		
	}
	
	public void commit(Callback cb){
		HashMap secretMap = new HashMap();
		List ary = new ArrayList();
		for(int i = 0;i<this.cache.size();i++){
			if(this.cache.get(i).get("OpType").toString().indexOf("2,3,5,7") == -1){
				if(cache.get(i).getInt("OpType")== 1 && cache.get(i).getBoolean("confidential")==true){
					
				}
				if (this.cache.get(i).get("OpType").toString().indexOf("6,8,9,10") != -1) {
			        this.needVerify = 0;
			    }
			    if (cache.get(i).getInt("OpType") != 1) {
			        ary.add(Validate.getUserToken(this, this.cache.get(i).getString("TableName")));
			    }
				
			}
		}
	}
	
	public void getUserToken(String name){
		Validate.getUserToken(this, name);
	}

	public void getLedger(JSONObject option,Callback cb){
		this.connection.client.getLedger(option,cb);
	}
	
	public void getLedgerVersion(Callback cb){
		this.connection.client.getLedgerVersion(cb);
	}
	
	public void getTransactions(String address,Callback cb){
		this.connection.client.getTransactions(address,cb);	
	}
    
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
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
