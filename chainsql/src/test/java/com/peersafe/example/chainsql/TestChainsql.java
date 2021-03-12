package com.peersafe.example.chainsql;

import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Ripple;
import com.peersafe.chainsql.core.Submit.SchemaOpType;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.Util;

public class TestChainsql {
	public static final Chainsql c = new Chainsql();
	public static String sTableName,sTableName2,sReName;
	public static String sNewAccountId,sNewSecret;

	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	
	public static String userSecret = "xnnUqirFepEKzVdsoBKkMf577upwT";
	public static String userAddress = "zpMZ2H58HFPB5QTycMGWSXUeF47eA8jyd4";
	
	
	public static void main(String[] args) {

		try{
			c.connect("ws://127.0.0.1:6006");
			//c.connect("ws://221.7.246.149/ws");


//			testRipple();
			testChainSql();
//			testSchema();

		}catch (Exception e){

			e.printStackTrace();
		}
	}


	private static  String readCertFile(String pemPath){


		String str="";

		File file=new File(pemPath);

		try {

			FileInputStream in=new FileInputStream(file);
			int size=in.available();

			byte[] buffer=new byte[size];

			in.read(buffer);

			in.close();

			str=new String(buffer,"GB2312");

		} catch (IOException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

			return null;

		}

		return str;

	}
	
	private static void testChainSql() {
		TestChainsql test = new TestChainsql();
		//建表
//		test.testCreateTable();
		//建表，用于重命名，删除
//		test.testCreateTable1();
//		//插入数据
		//test.testinsert();
//		//更新表数据
//		test.testUpdateTable();
//		//删除表数据
//		test.testdelete();
//		//重命名表
//		test.testrename();
//		//查询表数据
//	test.testget();
//		//删除表
//		test.testdrop();
//		//授权
//		test.grant();
//		//授权后使用被授权账户插入数据
//		test.insertAfterGrant();
		
		//根据sql语句查询，有签名检测

	//	test.testGetBySqlUser();
		
		//根据sql语句查询，admin权限，无签名检测
//		test.testGetBySqlAdmin();
		
		test.testModifyTable();
	}
	
	private static void testSchema() {
		TestChainsql test = new TestChainsql();
		c.as(rootAddress, rootSecret);
		
//		test.testSchemaList();
		
//		test.testSchemaCreate();
		
//		sTableName = "hello_123";
//		c.setSchema("57256592FD987D4256DDCE4812484BDE9B9193A70DDEC983541A182275045FE0");
//		test.testCreateTable();
		
		test.testSchemaModify();
	}
	
	
	private static void testRipple() {
		TestChainsql test = new TestChainsql();
//		//查询根账户余额
//		test.getAccountBalance();
//		//生成新账户
//		test.generateAccount();
//		//给新账户打钱
		test.activateAccount(sNewAccountId);



		//c.trustSet()

		
//		test.getTransactions();
//		test.getTransaction();
		
//		System.out.println(c.getLedger());
//		System.out.println(c.getLedger(100));
//		System.out.println(c.getLedgerVersion());
		
//		System.out.println(c.getChainInfo());
		
//		System.out.println(c.getAccountTables(rootAddress, true));
		
//		System.out.println(c.getTableNameInDB(rootAddress, "123"));
		
//		System.out.println(c.getTableAuth(rootAddress, sTableName));
//		
//		c.getTableAuth(userAddress, sTableName,new Callback<JSONObject>() {
//
//			@Override
//			public void called(JSONObject args) {
//				System.out.println(args);
//				
//			}
//			
//		});
	}
	
	public void testSchemaList() {
		JSONObject option = new JSONObject();
		JSONObject list = c.getSchemaList(option);
		System.out.println(list);
	}
	
	public void testSchemaCreate() {
		JSONObject schemaInfo = new JSONObject();
		schemaInfo.put("SchemaName","hello1");
		schemaInfo.put("WithState",false);

		schemaInfo.put("SchemaAdmin",rootAddress);

		List<String> validators = new ArrayList<String>();
		validators.add("03C53D4B7E4D558DBD8EA67E68AD97844FCCF48DD4C7A5C10E05B293A11DC9BB40");
		validators.add("021D3E9C571DF23054DBB2005E76EA5BE5227D381FB9B4A52467B5E6412ABAFBA0");
		validators.add("0317B5CAEBE6C778D133B1CA670D00E994D3AFAC2C0E6AA8F11B0DA277309F193E");
		JSONArray validatorsJsonArray = new JSONArray(validators);
		schemaInfo.put("Validators",validatorsJsonArray);

		List<String> peerList = new ArrayList<String>();
		peerList.add("127.0.0.1:5125");
		peerList.add("127.0.0.1:5126");
		peerList.add("127.0.0.1:5127");
		JSONArray peerListJsonArray = new JSONArray(peerList);

		schemaInfo.put("PeerList",peerListJsonArray);
		schemaInfo.put("SchemaAdmin",rootAddress);
		
		try {
			JSONObject ret = c.createSchema(schemaInfo).submit(SyncCond.validate_success);
			System.out.println(ret);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testSchemaModify() {
		JSONObject schemaInfo = new JSONObject();
		schemaInfo.put("SchemaID", "312242E6D2DB07BF1F856A2525A2DBA4199F9B60CF38584C5815CD79808572FD");
		
		List<String> validators = new ArrayList<String>();
		validators.add("02DB6543999A94815F7FC5F61D3AB1BA60938F15BC004E70613F7824DA70D5CCAB");
		JSONArray validatorsJsonArray = new JSONArray(validators);
		schemaInfo.put("Validators",validatorsJsonArray);

		List<String> peerList = new ArrayList<String>();
		peerList.add("127.0.0.1:5128");
		JSONArray peerListJsonArray = new JSONArray(peerList);

		schemaInfo.put("PeerList",peerListJsonArray);
		
		try {
			JSONObject obj = c.modifySchema(SchemaOpType.schema_add, schemaInfo).submit(SyncCond.validate_success);
			System.out.println(obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void generateAccount() {
		JSONObject obj = c.generateAddress();
		System.out.println("new account:" + obj);
		sNewAccountId = obj.getString("address");
		sNewSecret = obj.getString("secret");
	}

	public void activateAccount(String account) {
		Ripple ripple = new Ripple(c);
		JSONObject ret = ripple.pay(account, "200").submit(SyncCond.validate_success);
		System.out.println("pay result:" + ret);
	}
	
	public void getAccountBalance() {
		String balance = c.getAccountBalance(rootAddress);
		System.out.println(balance);
	}
	
	public void getTransactions() {
		JSONObject obj = c.getAccountTransactions(rootAddress, 20);
		System.out.println(obj);
	}
	
	public void getTransaction() {
		JSONObject obj = c.getTransaction("6B20CCA67F70F6CCECD26376EE6913E86B9B0DADD2D14CD4E7E6DF31F910EF03");
		System.out.println(obj);
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

		List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
				"{'field':'name','type':'varchar','length':50,'default':null}",
				"{'field':'balance','type':'varchar','length':50,'default':null}", "{'field':'age','type':'int'}");
		c.table(sTableName).insert(Util.array("{'age': 22}", "{'age': 33}"));
		c.table(sTableName).get(Util.array("{'id': 1}")).update("{'age':244}");
		JSONObject obj = c.commit(SyncCond.db_success);
		System.out.println("transaction result:" + obj);
	}

	public void testCreateTable() {
		List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1}",
				"{'field':'name','type':'varchar','length':50,'default':null}", "{'field':'age','type':'int'}");

		JSONObject obj;
		obj = c.createTable(sTableName,args,false).submit(SyncCond.db_success);
		System.out.println("create result:" + obj);
	}
	
	public void testModifyTable() {
		try {
			c.as("zKgETUCoBkuPpd1PmFgsdJLLhqFgWVAwS4", "xxSxPdbdiH7HVr2biczWHYVMEZuKh");
			String sTableName = "hi33";
			List<String> args = Util.array("{'field':'name','type':'varchar','length':50,'default':null}");
			JSONObject obj; 
//			obj= c.addTableFields(sTableName, args).submit(SyncCond.db_success);
//			System.out.println("addTableFields result:" + obj);
			
//			args = Util.array("{'field':'name','type':'text'}");
//			obj = c.modifyTableFields(sTableName, args).submit(SyncCond.db_success);
//			System.out.println("modifyTableFields result:" + obj);
			
//			args = Util.array("{'field':'height'}");
//			obj = c.deleteTableFields(sTableName, args).submit(SyncCond.db_success);
//			System.out.println("deleteTableFields result:" + obj);
			
//			args = Util.array("{'index':'PIndex'}","{'field':'id'}","{'field':'firmname'}");
//			obj = c.createIndex(sTableName, args).submit(SyncCond.db_success);
//			System.out.println("createIndex result:" + obj);
//			
			args = Util.array("{'index':'PIndex'}");
			obj = c.deleteIndex(sTableName, args).submit(SyncCond.db_success);
			System.out.println("deleteIndex result:" + obj);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void testinsert() {
//		List<String> orgs = Util.array("{'id':1,'age': 333,'name':'hello'}","{'id':2,'age': 444,'name':'sss'}","{'id':3,'age': 555,'name':'rrr'}");
//		JSONObject obj;
//		obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
//		System.out.println("insert result:" + obj);
		for(int i= 0; i<1; i++) {
			List<String> orgs = Util.array("{'id':1,'age': 333,'name':'你好'}");
			JSONObject obj;
			obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
			System.out.println("insert result:" + obj);
//			orgs = Util.array("{'id':2,'age': 444,'name':'sss'}");
//			obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
//			System.out.println("insert result:" + obj);
//			orgs = Util.array("{'id':3,'age': 555,'name':'rrr'}");
//			obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
//			System.out.println("insert result:" + obj);
		}
		
	}

	public void insertAfterGrant(){
		c.as(sNewAccountId, sNewSecret);
		List<String> orgs = Util.array("{'id':100,'age': 333,'name':'hello'}","{'id':101,'age': 444,'name':'sss'}","{'id':102,'age': 555,'name':'rrr'}");
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
//		List<String> arr = c.array("{'id': 3}");
		JSONObject obj;
		obj = c.table(sTableName).get(c.array("{'id': " + 3 + "}")).delete().submit(SyncCond.db_success);
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
		c.use(rootAddress);
		//查询所有数据
		 JSONObject obj = c.table(sTableName).get(Util.array("{name:hello}")).submit();
		 System.out.println("get result:" + obj);
		 
		 String name = "sss";
		 obj = c.table(sTableName).get(Util.array("{name:" + name + "}")).submit();
		 System.out.println("get result:" + obj);
		 
		 name = "rrr";
		 obj = c.table(sTableName).get(Util.array("{'name':'" + name + "'}")).submit();
		 System.out.println("get result:" + obj);
	}
	
	public void testGetBySqlAdmin() {
//		JSONObject ret = c.getTableNameInDB(userAddress, sTableName);
//		if(ret.has("nameInDB")) {
//			JSONObject obj = c.getBySqlAdmin("select * from t_" + ret.getString("nameInDB"));
//			System.out.println("get_sql_admin sync result:" + obj);
//		}else {
//			System.out.println(ret);
//		}
		c.getTableNameInDB(rootAddress, sTableName, new Callback<JSONObject>(){

			@Override
			public void called(JSONObject args) {
				System.out.println(args);
				if(args.has("nameInDB")) {
					String sql = "select * from t_" + args.getString("nameInDB");
					c.getBySqlAdmin(sql,new Callback<JSONObject>() {

						@Override
						public void called(JSONObject args) {
							System.out.println("get_sql_admin async result:" + args);
						}
						
					});
					
				}
			}
			
		});
	}
	
	public void testGetBySqlUser() {
		JSONObject ret = c.getTableNameInDB(rootAddress, sTableName);
		if(ret.has("nameInDB")) {
			JSONObject obj = c.getBySqlUser("select * from t_" + ret.getString("nameInDB"));
//			JSONObject obj = c.getBySqlUser("insert into t_" + ret.getString("nameInDB") + " values()");
			System.out.println("get_sql_user sync result:" + obj);
		}
	}
	
	public void grant() {
		JSONObject obj = new JSONObject();
		obj = c.grant(sTableName, sNewAccountId, "{insert:true,update:true}")
				   .submit(SyncCond.validate_success);
		System.out.println("grant result:" + obj.toString());
	}

	public void testdrop() {
		JSONObject obj;
		 obj = c.dropTable(sReName).submit(SyncCond.db_success);
		 System.out.println("drop result:" + obj);
	}
}
