package com.peersafe.example.contract;


import java.math.BigInteger;
import java.util.List;

import org.json.JSONObject;

import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.RemoteCall;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.Util;

public class TestContractTableTxs {

	public static final Chainsql c = Chainsql.c;
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
	public static String flag = "{\"insert\":true,\"update\":true,\"delete\":true,\"select\":true}";

	public static String sTableName = "table";
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

	public static String rawDelete = "{\"id\":1}";
	public static String rawUpdate = "{ \"account\": \"134\" }";
	public static String rawGet = "{\"id\": 2}";
	static TableTxsContract myContract = null;

	//
	public enum tagStep {
		active, table_create, table_create_operationRule,
		table_rename, table_grant, table_drop,
		table_insert, table_insert_operationRule, table_delete,
		table_update, table_get, table_transaction, deployContract
	}
	//
	public static void main(String[] args) throws Exception
	{		
		//
		c.connect("ws://127.0.0.1:6006");
		//
		c.as(rootAddress, rootSecret);
		//
		/**************************************/

		/*
		sUserOper = sUser;
		sUserOperSec = sUserSec;
		// */
		String contractAddr = "zc6kNuC9HaN14ic3csSySmzkkFLuCrHLLZ";
		tagStep nStep = tagStep.active;
		sTableName = sTableNameNew;
		//
		if (nStep != tagStep.active && nStep != tagStep.deployContract) {
			myContract = TableTxsContract.load(c, contractAddr, Contract.GAS_LIMIT);
		}
		System.out.print("start >>>>>>\n");
		switch (nStep) {
		case active: active(); break;
		case deployContract: deployContract(); break;
		case table_create: table_create(); break;
		case table_create_operationRule: table_create_operationRule(); break;
		case table_rename: table_rename(); break;
		case table_grant: table_grant(); break;
		case table_drop: table_drop(); break;
		case table_insert: table_insert(); break;
		case table_insert_operationRule: table_insert_operationRule(); break;
		case table_delete: table_delete(); break;
		case table_update: table_update(); break;
		case table_get: table_get(); break;
		case table_transaction: table_transaction(); break;
		default: break;
		}
		//
		System.out.print("end <<<<<<");
		System.exit(1);
	}

	public static void deployContract() {
		c.as(sOwner, sOwnerSec);
		RemoteCall<TableTxsContract> remoteCallContract = TableTxsContract.deploy(c, Contract.GAS_LIMIT);
		TableTxsContract contract = null;
		try {
			contract = remoteCallContract.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String contractAddr = contract.getContractAddress();
		System.out.print("contract address:" + contractAddr+"\n");
	}

	public static void active() {
		c.as(rootAddress, rootSecret);
		System.out.print("activate >>>>>>>>>>>>>>>\n");
		JSONObject jsonObj = c.pay(sOwner, "20000").submit(SyncCond.validate_success);
		System.out.print("     sOwner:" + jsonObj + "\n");
		jsonObj = c.pay(sUser, "20000").submit(SyncCond.validate_success);
		System.out.print("     user:" + jsonObj + "\n");
		System.out.print("activate <<<<<<<<<<<<<<<\n");
	}

	public static void table_create() {
		c.as(sOwner, sOwnerSec);
		//发交易调用合约
		try {
			myContract.create(sTableName, rawTable).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_create_operationRule() {
		System.out.print("    no support in this version");
		/*
		c.as(sOwner, sOwnerSec);
		String rule = "{"
				+ "'Insert': {"
				+ "'Condition': { 'txHash': '$tx_hash' }"	//Condition:指定插入操作可设置的默认值
				+ "}"
				+ "}"; 
		JSONObject option = new JSONObject("{"
				+ "'confidential': false,"
				+ "'operationRule': rule"
				+ "}");
		try {
			myContract.create(sTableName, rawTable, option).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// */
	}
	public static void table_rename() {
		c.as(sOwner, sOwnerSec);
		try {
			myContract.rename(sTableName, sTableNameNew).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_grant() {
		c.as(sOwner, sOwnerSec);
		try {
			myContract.grant(grantAddr, sTableName, flag).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_drop() {
		c.as(sOwner, sOwnerSec);
		try {
			myContract.drop(sTableName).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_insert() {
		c.as(sUserOper, sUserOperSec);
		try {
			if(sUserOper != sOwner)
			{
				myContract.insert(sOwner, sTableName, rawInsert/*, "txHash"*/).send(); //no support autoFillField
			}
			else
			{
				myContract.insert(sTableName, rawInsert/*, "txHash"*/).send(); //no support autoFillField
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_insert_operationRule() {
		System.out.print("    no support in this version");
		/*
		c.as(sUserOper, sUserOperSec);
		try {
			if(sUserOper != sOwner)
			{
				myContract.insert(sOwner, sTableName, rawInsert).send(); //no support autoFillField
			}
			else
			{
				myContract.insert(sTableName, rawInsert).send(); //no support autoFillField
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// */
	}
	public static void table_delete() {
		c.as(sUserOper, sUserOperSec);
		try {
			if(sUserOper != sOwner)
			{
				myContract.deletex(sOwner, sTableName, rawDelete).send();
			}
			else
			{
				myContract.deletex(sTableName, rawDelete).send();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void tableUpdate(int i) {
		try {
			List<String> arrayRawUpdate = Util.array(
					"{ \"account\": \"update id==2\" }",
					"{ \"account\": \"update email==123\" }",
					"{ \"account\": \"update email == 126 || name == wangwu\" }",
					"{ \"account\": \"update email == 124 && name == lisi\" }",
					"{ \"account\": \"update all\" }"
					);
			List<String> arrayRawGet = Util.array(
					"{\"id\": 2}",
					"{\"email\": \"123\"}",
					"{ \"email\": \"126\" }, { \"name\": \"wangwu\" }", //{ $or: [{ \"email\": \"126\" }, { \"name\": \"wangwu\" }] }
					"{ \"email\": \"124\" , \"name\": \"lisi\" }", //{ $and: [{ \"email\": \"124\" }, { \"name\": \"lisi\" }] }
					""      //C++ dispose 2 avoid update all, so no support update a table without a WHERE that uses a KEY column
					);
			int length = arrayRawGet.size();
			if (i < length) {
				rawUpdate = arrayRawUpdate.get(i);
				rawGet = arrayRawGet.get(i);
				if(sUserOper != sOwner)
				{
					System.out.print("update " + i + " Res: " + myContract.update(sOwner, sTableName, rawUpdate, rawGet).send());
				}
				else
				{
					System.out.print("update " + i + " Res: " + myContract.update(sTableName, rawUpdate, rawGet).send());
				}
				tableUpdate(i+1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_update() {
		c.as(sUserOper, sUserOperSec);
		tableUpdate(0);
	}
	public static void table_get() {
		c.as(sUserOper, sUserOperSec);
		try {
			BigInteger handle = BigInteger.valueOf(0);
			if(sUserOper != sOwner)
			{
				handle = myContract.get(sOwner, sTableName, "").send();
			}
			else
			{
				handle = myContract.get(sTableName, "").send();
			}
			if (handle.intValue() != 0) {
				BigInteger rowSize = myContract.getRowSize(handle).send();
				BigInteger colSize = myContract.getColSize(handle).send();
				String value = myContract.getValueByKey(handle, BigInteger.valueOf(rowSize.intValue() - 1), "email").send();
				String value1 = myContract.getValueByIndex(handle, BigInteger.valueOf(rowSize.intValue() - 1), BigInteger.valueOf(colSize.intValue() - 1)).send();
				System.out.print("    handle:" + handle + "rowSize: " + rowSize + ";colSize: " + colSize + ";id value: " + value + ";last value: " + value1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void table_transaction() {
		c.as(sOwner, sOwnerSec);
		try {
			myContract.sqlTransaction(sTableName).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
