package com.peersafe.example.contract;


import java.math.BigInteger;

import org.json.JSONObject;

import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;

public class TestPreCompiledContractTableTxs {

	public static final Chainsql c = new Chainsql();
	//
	//account,secret
	private static String[] sAddr = {
			"zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh", // 0
			"zKQwdkkzpUQC9haHFEe2EwUsKHvvwwPHsv", // 1
			"zPcimjPjkhQk7a7uFHLKEv6fyGHwFGQjHa" // 2

	};
	private static String[] sSec = {
			"xnoPBzXtMeMyMHUVTgbuqAfg1SUTb", // 0
			"xnJn5J5uYz3qnYX72jXkAPVB3ZsER", // 1
			"xxCosoAJMADiy6kQFVgq1Nz8QewkU" // 2

	};
	public static String rootAddress = sAddr[0];
	public static String rootSecret = sSec[0];
	public static String sOwner = sAddr[1];
	public static String sOwnerSec = sSec[1];
	public static String sUserOper = sOwner;
	public static String sUserOperSec = sOwnerSec;
	public static String sUser = sAddr[2];
	public static String sUserSec = sSec[2];

	public static String grantAddr = "zzzzzzzzzzzzzzzzzzzzBZbvji";
	public static String flag = "[{\"insert\":true,\"update\":true,\"delete\":true,\"select\":true}]";

	public static String sTableName = "hello";
	public static String sTableNameNew = "table_new";
	public static String rawTable = "["
			+ "{ \"field\": \"id\", \"type\": \"int\" },"
			+ "{ \"field\": \"time\", \"type\": \"datetime\" }, "
			+ "{ \"field\": \"txHash\", \"type\": \"varchar\", \"length\": 100 }, "
			+ "{ \"field\": \"name\", \"type\": \"varchar\", \"length\": 100 }, "
			+ "{ \"field\": \"email\", \"type\": \"varchar\", \"length\": 100 }, "
			+ "{ \"field\": \"account\", \"type\": \"varchar\", \"length\": 40 }"
			+ "]";

	public static String rawInsert = "[ "
			+ "{ \"id\": 1, \"name\": \"zhangsan\", \"email\": \"123\", \"account\": \"zhd8rfb9dyoq7b8vMBqSm3dbzJpUNFNtRt\", \"time\": \"2018-10-18 14:31:00\" }, "
			+ "{ \"id\": 2, \"name\": \"lisi\", \"email\": \"124\", \"account\": \"zhd8rfb9dyoq7b8vMBqSm3dbzJpUNFNtRt\", \"time\": \"2018-10-18 14:31:00\" }, "
			+ "{ \"id\": 3, \"name\": \"wangwu\", \"email\": \"125\", \"account\": \"zhd8rfb9dyoq7b8vMBqSm3dbzJpUNFNtRt\", \"time\": \"2018-10-18 14:31:00\" }, "
			+ "{ \"id\": 4, \"name\": \"zhaoliu\", \"email\": \"126\", \"account\": \"zhd8rfb9dyoq7b8vMBqSm3dbzJpUNFNtRt\", \"time\": \"2018-10-18 14:31:00\" } "
			+ "]";

	public static String rawDelete = "[{\"id\":1}]";
	public static String rawUpdate = "[{\"account\": \"134\" },{\"id\": 2}]";
	public static String rawAddFeild = "[{\"field\":\"age\",\"type\":\"int\"}]";
	public static String rawDeleteFeild = "[{\"field\":\"age\"}]";
	public static String rawModifyFeild = "[{\"field\":\"age\",\"type\":\"varchar\",\"length\":10}]";
	public static String rawCreateIndex = "[{\"index\":\"AcctLgrIndex\"},{\"field\":\"email\"},{\"field\":\"Account\"}]";
	public static String rawDeleteIndex = "[{\"index\":\"AcctLgrIndex\"}]";
	
	public static String autoFillField = "txHash";
	static PreCompiledDBTest myContract = null;
	static String contractAddr = "zPzB627o24cAXFrjMutqccxQM2nEAECucU";
	//
	public enum tagStep {
		active, table_create, table_create_operationRule,
		table_rename, table_grant, table_drop,
		table_insert, table_insert_operationRule, table_delete,
		table_update, table_get, table_transaction, deployContract,
		table_insert_Hash,payToContract,add_fields,delete_fields,modify_fields,
		create_index,delete_index
		
	}
	//
	public static void main(String[] args) throws Exception
	{		
		//
		c.connect("ws://127.0.0.1:5510");
		//c.connect("ws://10.100.0.78:25510");
		//
		c.as(rootAddress, rootSecret);
		//
		/**************************************/

		
		// */
		boolean isContract = false;
		tagStep nStep = tagStep.delete_index;
		//
		if (nStep != tagStep.active && nStep != tagStep.deployContract) {
			myContract = PreCompiledDBTest.load(c, contractAddr, Contract.GAS_LIMIT);
		}
		System.out.print("start >>>>>>\n");
		if(isContract) {
			switch (nStep) {
			case active: active(); break;
			case deployContract: deployContract(); break;
			case table_create: table_create_by_contract(); break;
			case table_rename: table_rename_by_contract(); break;
			case table_grant: table_grant_by_contract(); break;
			case table_drop: table_drop_by_contract(); break;
			case table_insert: table_insert_by_contract(); break;
			case table_insert_Hash: table_insert_hash_by_contract(); break;
			case table_delete: table_delete_by_contract(); break;
			case table_update: table_update_by_contract(); break;
			case table_get: table_get_by_contract(); break;
			case add_fields: add_fields_by_contract(); break;
			case delete_fields: delete_fields_by_contract(); break;
			case modify_fields: modify_fields_by_contract(); break;
			case create_index: create_index_by_contract(); break;
			case delete_index: delete_index_by_contract(); break;
			
			case payToContract:payToContract();break;
			default: break;
			}
		}else {
			switch (nStep) {
			case active: active(); break;
			case table_create: table_create(); break;
			case table_rename: table_rename(); break;
			case table_grant: table_grant(); break;
			case table_drop: table_drop(); break;
			case table_insert: table_insert(); break;
			case table_insert_Hash: table_insert_hash(); break;
			case table_delete: table_delete(); break;
			case table_update: table_update(); break;
			case table_get: table_get(); break;
			case add_fields: add_fields(); break;
			case delete_fields: delete_fields(); break;
			case modify_fields: modify_fields(); break;
			case create_index: create_index(); break;
			case delete_index: delete_index(); break;
			
			case payToContract:payToContract();break;
			default: break;
		}
		}
		//
		System.out.print("end <<<<<<");
		System.exit(1);
	}

	public static void deployContract() throws TransactionException {
		c.as(sUser, sUserSec);
		PreCompiledDBTest contract = PreCompiledDBTest.deploy(c, Contract.GAS_LIMIT.multiply(BigInteger.valueOf(10)), BigInteger.valueOf(30000000), sTableName, rawTable);
		String contAddr = contract.getContractAddress();
		System.out.print("contract address:" + contAddr+"\n");
	}

	public static void payToContract() {
		c.as(rootAddress, rootSecret);
		JSONObject jsonObj = c.payToContract(contractAddr, "523", 30000000).submit(SyncCond.validate_success);
		 System.out.print("     user:" + jsonObj + "\n");
	}
	public static void active() {
		c.as(rootAddress, rootSecret);
		System.out.print("activate >>>>>>>>>>>>>>>\n");
		JSONObject jsonObj = c.pay(sUser, "2000").submit(SyncCond.validate_success);
		System.out.print("     user:" + jsonObj + "\n");
		System.out.print("activate <<<<<<<<<<<<<<<\n");
	}
	
	
	public static void table_create_by_contract() {
		c.as(sUser, sUserSec);
		//发交易调用合约
		try {
				JSONObject ret = myContract.createByContract(sTableName, rawTable).submit(SyncCond.db_success);
				System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void table_rename_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.renameByContract(sTableName, sTableNameNew).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void table_grant_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.grantByContract(grantAddr, sTableName, flag).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void table_drop_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.dropByContract(sTableName).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void table_insert_by_contract() {
		c.as(sUser, sUserSec);
		try {
		
			JSONObject ret = myContract.insertByContract(contractAddr, sTableName, rawInsert).submit(SyncCond.db_success); //no support autoFillField
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void table_insert_hash_by_contract() {
		c.as(sUser, sUserSec);
		try {
		
			JSONObject ret = myContract.insertHashByContract(contractAddr, sTableName, rawInsert, autoFillField).submit(SyncCond.db_success); //no support autoFillField
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void table_delete_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret =  myContract.deletexByContract(contractAddr, sTableName, rawDelete).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void table_get_by_contract() {
		c.as(sUser, sUserSec);
		try {
			String res = myContract.getByContract(contractAddr, sTableName, "");
			System.out.println("get result:" + res);
			//String raw = "[[],{\"id\":\"2\"}]";
	        //String raw = "[[],{\"$or\":[{\"email\":\"126\"}, {\"name\": \"zhangsan\"}]}]";
	        String raw = "[[],{\"name\": { \"$regex\": \"/wangwu/\" }}]";
			res = myContract.getByContract(contractAddr, sTableName, raw,"name");
			System.out.println("get result:" + res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void table_update_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret =  myContract.updateByContract(contractAddr, sTableName, rawUpdate).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void add_fields_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.addFieldsByContract(sTableName, rawAddFeild).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void delete_fields_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.deleteFieldsByContract(sTableName, rawDeleteFeild).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void modify_fields_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.modifyFieldsByContract(sTableName, rawModifyFeild).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void create_index_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.createIndexByContract(sTableName, rawCreateIndex).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void delete_index_by_contract() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.deleteIndexByContract(sTableName, rawDeleteIndex).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void table_create() {
		c.as(sUser, sUserSec);
		//发交易调用合约
		try {
				JSONObject ret = myContract.create(sTableName, rawTable).submit(SyncCond.db_success);
				System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_rename() {
		c.as(sUser, sUserSec);
		try {
			JSONObject obj = myContract.rename(sTableName, sTableNameNew).submit(SyncCond.db_success);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_grant() {
		c.as(sUser, sUserSec);
		try {
			JSONObject obj = myContract.grant(grantAddr, sTableName, flag).submit(SyncCond.db_success);
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_drop() {
		c.as(sUser, sUserSec);
		try {
			JSONObject obj = myContract.drop(sTableName).submit(SyncCond.db_success);
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_insert() {
		c.as(sUser, sUserSec);
		try {
			JSONObject obj = myContract.insert(sUser, sTableName, rawInsert).submit(SyncCond.db_success); 
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void table_insert_hash() {
		c.as(sUser, sUserSec);
		try {
		
			JSONObject obj = myContract.insertHash(sUser, sTableName, rawInsert, autoFillField).submit(SyncCond.db_success); 
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void table_delete() {
		c.as(sUser, sUserSec);
		try {
			JSONObject obj = myContract.deletex(sUser, sTableName, rawDelete).submit(SyncCond.db_success);
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void table_update() {
		c.as(sUser, sUserSec);
		try {
			JSONObject obj = myContract.update(sUser, sTableName, rawUpdate).submit(SyncCond.db_success);
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_get() {
		c.as(sUser, sUserSec);
		try {
			String res = myContract.get(sUser, sTableName, "");
			System.out.println("get result:" + res);
			//String raw = "[[],{\"id\":\"2\"}]";
	        //String raw = "[[],{\"$or\":[{\"email\":\"126\"}, {\"name\": \"zhangsan\"}]}]";
	        String raw = "[[],{\"name\": { \"$regex\": \"/wangwu/\" }}]";
			res = myContract.get(sUser, sTableName, raw,"name");
			System.out.println("get result:" + res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void add_fields() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.addFields(sTableName, rawAddFeild).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void delete_fields() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.deleteFields(sTableName, rawDeleteFeild).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void modify_fields() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.modifyFields(sTableName, rawModifyFeild).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void create_index() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.createIndex(sTableName, rawCreateIndex).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void delete_index() {
		c.as(sUser, sUserSec);
		try {
			JSONObject ret = myContract.deleteIndex(sTableName, rawDeleteIndex).submit(SyncCond.db_success);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
