package java8.test;

import java.util.List;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.Util;

public class TestChainsql {
	public static final Chainsql c = Chainsql.c;
	public static String sTableName,sTableName2,sReName;
	public static String sNewAccountId,sNewSecret;
	
	public static String rootAddress = "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q";
	public static String rootSecret = "ssnqAfDUjc6Bkevd1Xmz5dJS5yHdz";
	
	public static void main(String[] args) {
		c.connect("ws://139.198.11.189:6006");
		
		sTableName = "tTable1";
		sTableName2 = "tTable2";
		sReName = "tTable3";


		c.as(rootAddress, rootSecret);


		testAccount();
		testChainSql();

		c.disconnect();
	}
	
	private static void testChainSql() {
		TestChainsql test = new TestChainsql();
		//建表
		test.testCreateTable();
		//建表，用于重命名，删除
		test.testCreateTable1();
		//插入数据
		test.testinsert();
		//更新表数据
		test.testUpdateTable();
		//删除表数据
		test.testdelete();
		//重命名表
		test.testrename();
		//查询表数据
		test.testget();
		//删除表
		test.testdrop();
		//授权
		test.grant();
		//授权后使用被授权账户插入数据
		test.insertAfterGrant();
	}
	
	private static void testAccount() {
		TestChainsql test = new TestChainsql();
//		//查询根账户余额
//		test.getAccountBalance();
		//生成新账户
		test.generateAccount();
		//给新账户打钱
		test.activateAccount(sNewAccountId);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void generateAccount() {
		JSONObject obj = c.generateAddress();
		System.out.println("new account:" + obj);
		sNewAccountId = obj.getString("account_id");
		sNewSecret = obj.getString("secret");
	}

	public void activateAccount(String account) {
		JSONObject ret = c.pay(account, "200");
		System.out.println("pay result:" + ret);
	}
	
	public void getAccountBalance() {
		String balance = c.getAccountBalance("rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q");
		System.out.println(balance);
	}
	
	// 创建表
	public void testCreateTable1() {
		// 创建表字段		
		List<String> args = Util.array("{'field':'LQD_UUID','type':'int','length':16,'PK':1,'NN':1,'UQ':1}",
		            "{'field':'CIFSEQ_CHARGE','type':'varchar','length':32,'default':NULL}",
		            "{'field':'CIFSEQ_OPPONENT','type':'varchar','length':32,'default':NULL}",
		            "{'field':'PAY_MONEY','type':'decimal','length':16,'accuracy':4}",
		            "{'field':'BUSI_JNL_SEQ','type':'varchar','length':20,'default':NULL}",
		            "{'field':'BILL_APPLI_USERSEQ','type':'varchar','length':32,'default':NULL}",
		            "{'field':'AGENT_SERIAL_NO','type':'varchar','length':20,'default':NULL}",
		            "{'field':'CREATE_TIME','type':'date','default':NULL}",
		            "{'field':'CREATE_USER','type':'varchar','length':32,'default':NULL}",
		            "{'field':'UPDATE_TIME','type':'datetime','default':NULL}",
		            "{'field':'UPDATE_USER','type':'varchar','length':32,'default':NULL}",
		            "{'field':'PRD_CODE','type':'varchar','length':32,'default':NULL}",
		            "{'field':'NAME_CODE','type':'varchar','length':32,'default':NULL}",
		            "{'field':'START_RATE_DATE','type':'date','default':NULL}",
		            "{'field':'DEADLINE_DAY','type':'varchar','length':32,'default':NULL}",
		            "{'field':'MONEY_RATE','type':'decimal','length':16,accuracy:4}",
		            "{'field':'ORIGIN_MONEY','type':'decimal','length':16,accuracy:4}",
		            "{'field':'END_RATE_DATE','type':'date','default':NULL}",
		            "{'field':'FEE','type':'decimal','length':16,accuracy:4}",
		            "{'field':'EARN_MONEY','type':'decimal','length':16,accuracy:4}",
		            "{'field':'FEE_MONEY','type':'decimal','length':16,accuracy:4}"
		          );
		JSONObject obj;
		System.out.println("创建表中...");
		obj = c.createTable(sTableName2, args).submit(SyncCond.db_success);
		// obj = c.createTable("mytable4", args).submit((data)->{
		// System.out.println(data);
		// });
		if (obj.getString("status").equals("success")) {
			System.out.println("创建表成功");
		} else {
			System.out.println("创建表失败");
		}
		System.out.println(obj);
	}

	public void testts() {
		c.beginTran();

		 List<String> args =
		 Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
		 "{'field':'name','type':'varchar','length':50,'default':null}","{'field':'balance','type':'varchar','length':50,'default':null}","{'field':'age','type':'int'}"
		 );
		c.table(sTableName).insert(Util.array("{'age': 22}", "{'age': 33}"));
		c.table(sTableName).get(Util.array("{'id': 1}")).update("{'age':244}");
		JSONObject obj = c.commit(SyncCond.db_success);
		System.out.println("transaction result:" + obj);
	}

	public void testCreateTable() {
		List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
				"{'field':'name','type':'varchar','length':50,'default':null}", "{'field':'age','type':'int'}");
		JSONObject obj;
		obj = c.createTable(sTableName,args,false).submit(SyncCond.db_success);
		System.out.println("create result:" + obj);
	}

	public void testinsert() {
		List<String> orgs = Util.array("{'age': 333,'name':'hello'}","{'age': 444,'name':'sss'}","{'age': 555,'name':'rrr'}");
		JSONObject obj;
		obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
		System.out.println("insert result:" + obj);
	}

	public void insertAfterGrant(){
		c.as(sNewAccountId, sNewSecret);
		List<String> orgs = Util.array("{'age': 333,'name':'hello'}","{'age': 444,'name':'sss'}","{'age': 555,'name':'rrr'}");
		JSONObject obj;
		obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
		System.out.println("insert after grant result:" + obj);
		c.as(rootAddress, rootSecret);
	}
	
	public void testUpdateTable() {
		List<String> arr1 = Util.array("{'id': 2}");

		JSONObject obj;
		obj = c.table(sTableName).get(arr1).update("{'age':200}").submit(SyncCond.db_success);
		System.out.println("update result:" + obj);
	}

	public void testdelete() {
		List<String> arr = Util.array("{'id': '3'}");
		JSONObject obj;
		obj = c.table(sTableName).get(arr).delete().submit(SyncCond.db_success);
		System.out.println("delete result:" + obj);
	}
	
	public void testrename() {
		JSONObject obj;
		obj = c.renameTable(sTableName2, sReName).submit(SyncCond.db_success);
		System.out.println("rename result:" + obj);
	}

	public void testget() {
		
//		//查询单条数据
//		 JSONObject obj = c.table(sTableName).get(Util.array("{id:1}")).withFields("[]").submit();
		
		//查询所有数据
		 JSONObject obj = c.table(sTableName).get().submit();
		 System.out.println("get result:" + obj);
	}

	public void grant() {
		JSONObject obj = new JSONObject();
		obj = c.grant(sTableName, sNewAccountId, "{insert:true,update:true,delete:true}")
				   .submit(SyncCond.db_success);
		System.out.println("grant result:" + obj.toString());
	}

	public void testdrop() {
		JSONObject obj;
		 obj = c.dropTable(sReName).submit(SyncCond.db_success);
		 System.out.println("drop result:" + obj);
	}
}
