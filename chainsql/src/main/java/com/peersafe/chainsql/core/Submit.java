package com.peersafe.chainsql.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.peersafe.base.client.Account;
import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.transactions.ManagedTxn;
import com.peersafe.base.client.transactions.ManagedTxn.OnSubmitSuccess;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.core.types.known.tx.signed.SignedTransaction;
import com.peersafe.chainsql.manager.EventManager;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.GenericPair;
import com.peersafe.chainsql.util.Util;

public abstract class Submit {
	public Connection connection;
	protected Callback<JSONObject> cb;
	private SubmitState submit_state;
	private SyncState sync_state;
	private JSONObject submitRes;
	private JSONObject syncRes;

	private boolean sync = false;
	protected SyncCond condition;
	protected SignedTransaction signed;

	protected CrossChainArgs crossChainArgs = null;

	protected EventManager eventManager = new EventManager();

	//事务相关
	protected List<JSONObject> cache = new ArrayList<JSONObject>();
	protected Map<GenericPair<String,String>,String> mapToken =
			new HashMap<GenericPair<String,String>,String>();
	protected boolean transaction = false;

	protected Integer needVerify = 1;
	//严格模式
	protected boolean strictMode = false;

	protected int extraDrop = 0;

	public enum SyncCond {
		send_success,
		validate_success,
		db_success,
	}

	public enum SubmitState{
		waiting_submit,
		send_success,
		submit_error,
	}

	public enum SyncState{
		waiting_sync,
		sync_response,
	}

	public enum SchemaOpType {
		schema_add,
		schema_del,
	}
	
	public class CrossChainArgs{
		public String originalAddress;
		public int 	  txnLedgerSeq;
		public String curTxHash;
		public String futureHash;
	}
	private static final int wait_milli = 50;
	private static final int submit_wait = 10000;
	private static final int sync_maxtime = 30000;

	/**
	 * Set restrict mode.
	 * If restrict mode enabled,transaction will fail when user executing a consecutive operation
	 * to a table and some other user interrupts this by making an operation to this identical table.
	 * @param falg True to enable restrict mode and false to disable restrict mode.
	 */
	public void setRestrict(boolean falg) {
		this.strictMode = falg;
	}

	public void setNeedVerify(boolean flag){
		this.needVerify = flag ? 1 : 0;
	}

	/**
	 * asynchronous,callback trigger with all possible status
	 * @param cb Callback.
	 * @return submit result
	 */
	public JSONObject submit(Callback<JSONObject> cb){
		this.cb = cb;

		return doSubmit();
	}

	/**
	 * synchronous,return when condition satisfied or submit failed
	 * @param cond,return condition
	 * @return Submit result.
	 */
	public JSONObject submit(SyncCond cond){
		sync = true;
		condition = cond;

		return doSubmit();
	}


	/**
	 * submit a transaction,return immediately
	 * @return submit result
	 */
	public JSONObject submit(){
		sync = false;
		cb = null;
		return doSubmit();
	}

	public void setCrossChainArgs(String originalAddress,int txnLedgerSeq,String curTxHash,String futureHash){
		crossChainArgs = new CrossChainArgs();
		crossChainArgs.originalAddress = originalAddress;
		crossChainArgs.txnLedgerSeq = txnLedgerSeq;
		crossChainArgs.curTxHash = curTxHash;
		crossChainArgs.futureHash = futureHash;
	}

	public void setCrossChainArgs(CrossChainArgs args){
		this.crossChainArgs = args;
	}
	public boolean isCrossChainArgsSet(){
		return this.crossChainArgs != null;
	}

	abstract protected JSONObject prepareSigned();
//	protected JSONObject prepareSigned() {
//		return null;
//	}

	private JSONObject getError(String err){
		JSONObject obj = new JSONObject();
		obj.put("status", "error");
		obj.put("error_message", err);
		return obj;
	}

	private JSONObject getError(String err,String hash){
		JSONObject obj = new JSONObject();
		obj.put("status", "error");
		obj.put("tx_hash",hash);
		obj.put("error_message", err);
		return obj;
	}


	public EventManager eventManager() {
		return eventManager;
	}

	protected JSONObject doSubmit(){
		JSONObject obj = prepareSigned();
		if(obj.has("final_result") || obj.has("error")){
			return obj;
		}
		return doSubmitNoPrepare();
	}

	protected JSONObject doSubmitNoPrepare(){
		if(signed == null){
			return getError("Signing failed,maybe ripple node error");
		}
		
        submit_state = SubmitState.waiting_submit;
        sync_state = SyncState.waiting_sync;

		Account account = connection.client.accountFromSeed(connection.secret);
		TransactionManager tm = account.transactionManager();
		ManagedTxn tx = new ManagedTxn(signed);

		//subscribe tx
		if(sync || cb != null){
			if(tx == null || tx.hash == null){
				return getError("Submit failed,transaction hash is null.");
			}
			subscribeTx(tx.hash.toString());
		}

		tm.submitSigned(tx.onSubmitSuccess(new OnSubmitSuccess(){
			@Override
			public void called(Response args) {
				onSubmitSuccess(args);
			}
		}).onError(new Callback<Response>(){
			@Override
			public void called(Response args) {
				onSubmitError(args);
			}
		}), !tx.isOnlySubmitSigned());

//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//        System.out.println("time start waiting:" + df.format(new Date()));// new Date()为获取当前系统时间
		//wait until submit return
		int count = submit_wait / wait_milli;
		while(submit_state == SubmitState.waiting_submit){
			Util.waiting();
			if(--count <= 0){
				submit_state = SubmitState.submit_error;
				submitRes = getError("waiting submit result timeout",tx.hash.toString());
				break;
			}
		}

		if (sync) {
			if (submit_state == SubmitState.submit_error) {
				return submitRes;
			} else if (submit_state == SubmitState.send_success && condition == SyncCond.send_success) {
				unSubscribeTx(tx.hash.toString());
				return submitRes;
			} else {
				count = sync_maxtime / wait_milli;
				while (sync_state != SyncState.sync_response) {
					Util.waiting();
					if (--count <= 0) {
						syncRes = getError("waiting sync result timeout", tx.hash.toString());
						break;
					}
				}
				unSubscribeTx(tx.hash.toString());
				return syncRes;
			}
		} else {
			return submitRes;
		}
	}
	public JSONObject submitSigned(JSONObject signedRet) {
		return submitSigned(signedRet, SyncCond.send_success);
	}
	public JSONObject submitSigned(JSONObject signedRet, SyncCond cond) {
		sync = true;
		condition = cond;
		signed = new SignedTransaction(signedRet);

		return doSubmitNoPrepare();
	}

	private void subscribeTx(String txId){
		this.eventManager.subscribeTx(txId,new Callback<JSONObject>(){
			@Override
			public void called(JSONObject data) {
				if(cb != null){
					if(!data.getString("status").equals("success"))
						cb.called((JSONObject)data);
				}else if(sync){
					if(!data.has("transaction"))
						return;
					JSONObject obj = (JSONObject)data;
					JSONObject res = new JSONObject();
					JSONObject tx = (JSONObject) obj.get("transaction");
					String hash = tx.get("hash").toString();

					//only deal with subscribed tx
					if(!hash.equals(txId)) {
						return;
					}

					res.put("tx_hash", hash);

					String status = obj.getString("status");
					switch (status) {
						case "validate_success":
							if (condition.compareTo(SyncCond.db_success) < 0) {
								res.put("status", condition.toString());
							}
							break;
						case "db_success":
							res.put("status", condition.toString());
							break;
						default:	// validate_(*error) 或者 db_(*error) 错误情况
							if (status.startsWith("validate_") &&
									condition.compareTo(SyncCond.validate_success) < 0) {
								res.put("status", condition.toString());
							} else if (status.startsWith("db_") &&
									condition.compareTo(SyncCond.db_success) < 0) {
								res.put("status", condition.toString());
							} else {
								res.put("status", status);
								if (obj.has("error"))
									res.put("error", obj.get("error"));
								if (obj.has("error_message"))
									res.put("error_message", obj.get("error_message"));
							}
							break;
					}

					if (!res.isNull("status") && sync_state == SyncState.waiting_sync) {
						syncRes = res;
						sync_state = SyncState.sync_response;
						submit_state = SubmitState.send_success;
					}
				}
			}
		});
	}

	private void unSubscribeTx(String txId) {
		this.eventManager.unsubscribeTx(txId,null);
	}

	private void onSubmitSuccess(Response res){
		JSONObject obj = new JSONObject();
		obj.put("status", "send_success");
		JSONObject tx_json = (JSONObject) res.result.get("tx_json");
		obj.put("tx_hash", tx_json.get("hash").toString());

		submitRes = obj;
		submit_state = SubmitState.send_success;
	}

	private void onSubmitError(Response res) {
		JSONObject obj = new JSONObject();
		obj.put("status", "error");

		if(res.result == null){

			res.result = new JSONObject();
			if (res.message.has("result") && res.message.get("result") instanceof JSONObject ) {
				res.result = res.message.getJSONObject("result");
			} else {
				if(res.message.has("error_message"))
					obj.put("error_message", res.message.getString("error_message"));

				if(res.message.has("error"))
					obj.put("error", res.message.getString("error"));

				if(res.message.has("error_code"))
					obj.put("error_code", res.message.getInt("error_code"));
			}

		}

		if(res.result.has("engine_result_message"))
			obj.put("error_message", res.result.getString("engine_result_message"));
		if(res.result.has("engine_result_message_detail"))
			obj.put("error_message",res.result.getString("engine_result_message_detail"));

		if(res.result.has("engine_result")) {
			obj.put("error", res.result.getString("engine_result"));
		}

		if(res.result.has("engine_result_code")){
			obj.put("error_code", res.result.getInt("engine_result_code"));
		}
		if(res.result.has("tx_json")){
			obj.put("tx_json", res.result.getJSONObject("tx_json"));
		}

		if(sync || cb != null) {
			unSubscribeTx(signed.hash.toString());
			if(cb != null) {
				cb.called(obj);
			}
		}

		submitRes = obj;
		submit_state = SubmitState.submit_error;
	}

	protected JSONArray getTableArray(String tableName){
		String tablestr = "{\"Table\":{\"TableName\":\"" + Util.toHexString(tableName) + "\"}}";
		return Util.strToJSONArray(tablestr);
	}

	protected boolean mapError(Map<String,Object> map){
		if(map.get("Sequence") == null){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Translate to transaction type.
	 * @param json tx_json.
	 * @param type Transaction type.
	 * @return Transaction object.
	 * @throws Exception Exception to be throws.
	 */
	protected Transaction toTransaction(JSONObject json,TransactionType type) throws Exception{
		Transaction tx = new Transaction(type);
		Amount fee;
		int drops_per_byte = 1000;
		if(connection.client.serverInfo.primed()) {
			drops_per_byte = connection.client.serverInfo.drops_per_byte;
			fee = connection.client.serverInfo.transactionFee(tx);

			if(!json.has(UInt32.LastLedgerSequence.toString())) {
				tx.put(UInt32.LastLedgerSequence, new UInt32(connection.client.serverInfo.ledger_index + 20));
			}
		}else {
			fee = Amount.fromString("50");
			JSONObject ledger = connection.client.getLedgerVersion();
			if(ledger.has("ledger_current_index")) {
				tx.put(UInt32.LastLedgerSequence, new UInt32(ledger.getInt("ledger_current_index") + 5));
			}
		}

		//chainsql type tx needs higher fee
		Amount extraFee = Util.getExtraFee(json,drops_per_byte,type);
		fee = fee.add(extraFee);
		fee = fee.add(Amount.fromString(String.valueOf(extraDrop)));

		tx.as(Amount.Fee, fee);

  		AccountID account = AccountID.fromAddress(this.connection.address);
  		JSONObject obj = connection.client.accountInfo(account);
  		if(obj.has("error")) {
  			throw new Exception(obj.getString("error_message"));
  		}else {
  			tx.as(UInt32.Sequence, obj.getJSONObject("account_data").getInt("Sequence"));
  		}
 		
		try {  
		   tx.parseFromJson(json);
		} catch (JSONException e) {
		   e.printStackTrace();
		}
		return tx;
	}
}
