package java8.test;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.core.Table;
import com.peersafe.chainsql.util.Util;


public class Test {
	  public static final Chainsql c = Chainsql.c;
	  public Table table;
	  public static String sTableName;
	  public static void main(String[] args) {
		  //c.connect("ws://192.168.0.193:6006");
		 // c.connect("ws://192.168.0.110:6008");
		 //c.connect("ws://139.198.11.189:6006");

//		   c.connect("ws://192.168.0.152:6006");
		   c.connect("ws://192.168.0.148:5008");
//		    c.connect("ws://192.168.0.195:6007");
//		    c.connect("ws://192.168.0.151:6006");
//		   c.connect("ws://101.201.40.124:5006");
//		  c.connect("ws://139.198.188.132:6006");
		  sTableName = "abc";
		 
		/* conn.address="rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr";
		  conn.secret="snrJRLBSkThBXtaBYZW1zVMmThm1d";*/
		  c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
//		  c.as("rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q", "ssnqAfDUjc6Bkevd1Xmz5dJS5yHdz");
//		  c.use("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		  
//		  c.as("rQDG465k2ER9S7YoUYhW1gatt4hAuX7kG1", "snG5YZ3gZgt4aue9JzaVHHKMyBP9B");
//		  System.out.println(c.getUnlList());
		  
		  //c.disconnect();
//		  
		// c.event.subTable("testcssas", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		  
//		  JSONObject obj = new JSONObject();
//		  JSONObject subObj = new JSONObject();
//		  subObj.put("table", "hello");
//		  obj.put("val", "123");
//		  obj.put("tables", subObj);
//		  System.out.println(obj);		  
//		  JSONObject table = obj.getJSONObject("tables");
//		  table.put("table", "nihao");
//		  System.out.println(obj);
		  
		  
//		  c.event.subTable("hiyou", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", (data)->{
//			  System.out.println(data);
//		  });
//		  c.onReconnecting((data)->{
//			  System.out.println("Reconnecting started");
//		  });
//		  c.onReconnected((data)->{
//			  System.out.println("Reconnected");
//		  });
		  
		  //AccountID.fromAddress("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		  Test test =new Test();
//		  test.testCreateTable1();
//		  test.testValidationCreate();
//		  test.testts();
//		  	test.getLedgerVersion();
//		  	test.getLedge();
//		  test.getUserToken();
//		 test.testCreateTable();
		 // c.connection.client.getAccountInfo(null);

//		  test.testRecreateTable();
		  test.testinsert();
//		  test.testUpdateTable();
//		  test.testdelete();
//		  test.testrename();
//		  test.testget();
//		  test.testdrop();
//		  test.grant();		 
		  
//		  	test.getTransactions();
//		  	 test.getTransaction();
//		  test.getCrossChainTxs();
//		  test.getChainInfo();
//		  test.generateAccount();
//		  test.activateAccount("r35d4GNRrMexdC3fzkKh1GawLEQxTSGATx");
//		  test.getServerInfo();
//		  try {
//			Thread.sleep(10000);
//		  } catch (InterruptedException e) {
//			e.printStackTrace();
//		  }
//		  
//		  c.disconnect();
		 
//		 JSONObject obj = c.generateAddress();
//		 activateAccount(obj);
//		  c.recreateTable("");
		
	    }
		private static boolean activateAccount(JSONObject account){
			c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
			JSONObject ret = c.pay(account.getString("account_id"), "20");
			if(!ret.getString("status").equals("success")){
				System.out.println(ret.toString());
				return false;
			}
			return true;
		}
		
		//创建表
		public void testCreateTable1() {
		  //创建表字段
		    List<String> args = Util.array("{'field':'LQD_UUID','type':'int','length':16,'PK':1,'NN':1,'UQ':1,'AI':0}",
		            "{'field':'CIFSEQ_CHARGE','type':'varchar','length':32,'default':null}",
		            "{'field':'CIFSEQ_OPPONENT','type':'varchar','length':32,'default':NULL}",
		            "{'field':'PAY_MONEY','type':'decimal','length':16,accuracy:4}",
		            "{'field':'BUSI_JNL_SEQ','type':'varchar','length':20,'default':NULL}",
		            "{'field':'BILL_APPLI_USERSEQ','type':'varchar','length':32,'default':NULL}",
		            "{'field':'AGENT_SERIAL_NO','type':'varchar','length':20,'default':NULL}",
		            "{'field':'CREATE_TIME','type':'datetime','default':NULL}",
		            "{'field':'CREATE_USER','type':'varchar','length':32,'default':NULL}",
		            "{'field':'UPDATE_TIME','type':'datetime','default':NULL}",
		            "{'field':'UPDATE_USER','type':'varchar','length':32,'default':NULL}",
		            "{'field':'PRD_CODE','type':'varchar','length':32,'default':NULL}",
		            "{'field':'NAME_CODE','type':'varchar','length':32,'default':NULL}",
		            "{'field':'START_RATE_DATE','type':'datetime','default':NULL}",
		            "{'field':'DEADLINE_DAY','type':'varchar','length':32,'default':NULL}",
		            "{'field':'MONEY_RATE','type':'decimal','length':16,accuracy:4}",
		            "{'field':'ORIGIN_MONEY1','type':'decimal','length':16,accuracy:4}",
		            "{'field':'END_RATE_DATE','type':'datetime','default':NULL}",
		            "{'field':'FEE','type':'decimal','length':16,accuracy:4}",
		            "{'field':'ORIGIN_MONEY2','type':'decimal','length':16,accuracy:4}",
		            "{'field':'EARN_MONEY','type':'decimal','length':16,accuracy:4}",
		            "{'field':'FEE_MONEY','type':'decimal','length':16,accuracy:4}"
		          );
		    JSONObject obj;
		    System.out.println("创建表中...");
//		    obj = c.createTable("mytable333", args).submit(SyncCond.db_success);
		    obj = c.createTable("mytable4", args).submit((data)->{
		    	System.out.println(data);
		    });
		    if(obj.getString("status").equals("success")){
		      System.out.println("创建表成功");
		    }else{
		      System.out.println("创建表失败");
		    }
		    System.out.println(obj);
		  }
	  public void testValidationCreate(){
		  JSONArray ret = c.validationCreate(1);
		  System.out.println(ret);
	  }
	  public void testts(){
//		  c.setNeedVerify(false);
		  c.beginTran();
		  
//		  List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
//	    		  "{'field':'name','type':'varchar','length':50,'default':null}","{'field':'balance','type':'varchar','length':50,'default':null}","{'field':'age','type':'int'}"
//	    		 );
//		  c.createTable(sTableName,args,true);
//		  c.grant(sTableName, "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q","{insert:true,update:true}");
//		  c.table(sTableName).insert(Util.array("{'age': 23,'name':'adsf','balance':'124'}","{'age': 33,'name':'小sr','balance':'300'}"));
		  c.table(sTableName).insert(Util.array("{'age': 23,'name':'adsf'}","{'age': 33,'name':'小sr'}"));
		  c.table(sTableName).get(Util.array("{'id': 2}")).update("{'age':500}");
//		  c.table(sTableName).get(Util.array("{'id': 2}")).sqlAssert(c.array("{'age':200}"));
		  JSONObject obj = c.commit((data)->{
				 System.out.println("creat------"+data);
			 });
		  System.out.println(obj);
	  } 
	  
	public void getLedge(){
		c.getLedger(766,(data)->{
			System.out.println("creat------"+data);
		 });
	}
	public void getLedgerVersion(){
		c.getLedgerVersion((data)->{
			 System.out.println("creat------"+data);
		 });
	}
	public void getTransactions(){
//		c.getTransactions("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",(data)->{
//			 System.out.println("creat------"+data);
//		 });
		
		JSONObject obj = c.getTransactions("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
//		JSONObject obj = c.getTransactions("rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q");
		 System.out.println("getTransactions------"+obj);
	}	
	public void getTransaction(){
		String hash = "2D3EBDCE852864DF57A48A9E2ED361B67DD5059F3A3EC79134855C1C59026C27";
		JSONObject obj = c.getTransaction(hash);
		System.out.println("getTransaction------"+obj);
//		c.getTransaction(hash, (data)->{
//			System.out.println(data);
//		});
		
	}
	
	public void getCrossChainTxs(){
    	long tm1 = System.currentTimeMillis();
		JSONObject obj = c.getCrossChainTxs("", 10,true);
		System.out.println(obj);
    	System.out.println(System.currentTimeMillis() - tm1);
    	

	}
	
	public void testRecreateTable(){
		c.recreateTable("abcde").submit((data)->{
			System.out.println(data);
		});
	}
	
    public void testCreateTable() {
    	List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
    			"{'field':'name','type':'varchar','length':50,'default':null}",
	    		  "{'field':'age','type':'int'}"
	    		 );
    	JSONObject obj;
    	
    	//obj = c.createTable(sTableName,args).submit();
    	//System.out.println(obj);
    	

//    	obj = c.createTable(sTableName,args,false).submit((data)->{
//    		System.out.println("creat------"+data);
//    	});
//    	System.out.println(obj);
    	long tm1 = System.currentTimeMillis();
 //   	obj = c.createTable(sTableName, args,true).submit(SyncCond.db_success);
    	obj = c.createTable(sTableName, args).submit(SyncCond.validate_success);
    	System.out.println(obj);
    	System.out.println(System.currentTimeMillis() - tm1);
//    	List<String> raw = new ArrayList<String>();
//		raw =(Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
//				"{'field':'cid','type':'varchar','length':20,'NN':1,'UQ':1}",
//				"{'field':'account_id','type':'varchar','length':100}",
//				"{'field':'secret','type':'varchar','length':50}"));
//    	obj = c.createTable("t_account1",args).submit((data)->{
//    		System.out.println("creat------"+data);
//    	});
//    	System.out.println(obj);
    }
	 
	 public void testinsert(){
//		 List<String> orgs = Util.array("{'age': 333}");
//		 List<String> orgs = Util.array("{'account':'rUhiKPaoqgLFqj1mjTuxVANanMZFyKPSqy','name':'lu','gender':0,'phone':18911163291,'birthDate':'2017/06/03','idNo':37142519870921464,'mailAddr':'luleigreat@163.com','status':'本科','profession':'IT'}");
//		 List<String> orgs = Util.array("{'PAYERNAME':'张三1_111','ORISENDBANKNO':'', 'PAYERBANKNO':'100000000001', 'SESSIONID':'1', 'LIQUIDSTATUS':'0', 'DEBITCREDITFLAG':'0', 'PAYBACKREASON':'', 'PAYERACCT':'1000000000000111', 'RECVBANKNO':'100000000002', 'CURRTYPE':'156', 'AMOUNT':'12000', 'SENDBANKNO':'100000000001', 'AGENTSERIALNO':'201704050000000000000314', 'ORIAGENTSERIALNO':'', 'ORIWORKDATE':'', 'LIQUIDDATE':'20170405', 'PAYEEACCT':'2000000000000123', 'PAYEEBANKNO':'100000000002', 'PAYBACKFLAG':'00', 'PAYEENAME':'李四2_123', 'WORKDATE':'20170405'}");
		 List<String> orgs = Util.array("{'age': 23,'name':'你好'}",
				 						"{'age': 33,'name':'小sr'}");
		 JSONObject obj;
//		 long tm1 = System.currentTimeMillis();
//		 obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
//		 System.out.println(obj);
//		 System.out.println(System.currentTimeMillis() - tm1);
//		 orgs = Util.array("{'BANKNO':'100000000006'}");
//		 obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
//		 System.out.println(obj);
		 
		 obj = c.table(sTableName).insert(orgs).submit((data)->{
 			 System.out.println("insert------"+data);
 		 });
		 System.out.println(obj);
	 }

	  public void testUpdateTable(){
		  List<String> arr1 = Util.array("{'id': 2}");
		  
		  JSONObject obj;
//		  obj = c.table(sTableName).get(arr1).update("{'age':200}").submit((data)->{
//	 			 System.out.println("update------"+data);
//	 		 });
//		  System.out.println(obj);
		  
//		  obj = c.table(sTableName).get(arr1).update(arr2).submit();
//		  System.out.println(obj);
//		  
		  obj = c.table(sTableName).get(arr1).update("{'age':200}").submit(SyncCond.db_success);
		  System.out.println(obj);
	  }
	  public void testdelete(){
		  List<String> arr = Util.array("{'id': '3'}");
		  JSONObject obj;
		  obj = c.table(sTableName).get(arr).delete().submit((data)->{
			  System.out.println("delete------"+data);
		  });
		  System.out.println(obj);
//		  
//		  obj = c.table(sTableName).get(arr).delete().submit();
//		  System.out.println(obj);
//		  
//		  obj = c.table(sTableName).get(arr).delete().submit(SyncCond.db_success);
//		  c.table(sTableName).get(arr).delete().submit();
	  }
	  
	  public void testrename(){
		  JSONObject obj;
		  obj = c.renameTable(sTableName, "aaaaaa").submit((data)->{
			  System.out.println("rename------"+data);
		  });
		  System.out.println(obj);
//		  
//		  obj = c.renameTable(sTableName, "TableBww").submit();
//		  System.out.println(obj);
//		  
//		  obj = c.renameTable(sTableName, "TableBww").submit(SyncCond.db_success);
//		  System.out.println(obj);
	  }
	  
	  public void testget(){
		//  JSONObject obj = c.table("testcas").get(Util.array("{'name':'peerb1'}")).limit("{index:0,total:10}").withFields("[]").submit();
		  
		/*  table = c.table("testcas").get(Util.array("{'name':'peerb1'}")).order(Util.array("{age:-1}")).filterWith("[]").submit();*/
		  JSONObject obj = c.table(sTableName).get(Util.array("{age:{$ne:232}}")).order(Util.array("{age:-1}")).withFields("[]").submit((data)->{
	 			 System.out.println("testget------"+data);
		  });
		  
		  System.out.println(obj.toString());
	  }
	  
	  public void grant(){
//		  JSONObject obj = c.grant(sTableName, "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q","aBP8JEiNXr3a9nnBFDNKKzAoGNezoXzsa1N8kQAoLU5F5HrQbFvs","{insert:true,update:true,delete:true}").submit((data)->{
//	 			 System.out.println("grant------"+data);
//		  });
		  JSONObject obj = c.grant(sTableName, "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q","{insert:true,update:true,delete:true}").submit((data)->{
	 			 System.out.println("grant------"+data);
		  });
		  System.out.println(obj.toString());
	  }
	  
	  public void getChainInfo(){
		  JSONObject ret = c.getChainInfo();
		  System.out.println(ret);
	  }
	  public void getServerInfo(){
		  JSONObject ret = c.getServerInfo();
		  System.out.println(ret);
	  }
	  public void generateAccount(){
		  System.out.println(c.generateAddress());
	  }
	  
	  public void activateAccount(String account){
		  JSONObject ret = c.pay(account,"20");
		  System.out.println(ret);
	  }
	public void testdrop(){
		JSONObject obj;
		obj = c.dropTable(sTableName).submit((data)->{
			 System.out.println("drop------"+data);
		});
		System.out.println(obj);
//		
//		obj = c.dropTable(sTableName).submit();
//		System.out.println(obj);
//		
//		obj = c.dropTable(sTableName).submit(SyncCond.db_success);
//		System.out.println(obj);
	}
	
	
	public ArrayList<String> select(String tableName,String filterWith,String whereCond){
		ArrayList<String> cond=new ArrayList<String>();
		cond.add(whereCond);
		JSONObject json = c.table(tableName).withFields(filterWith).get(cond).submit((data)->{
		 			 System.out.println("creat------"+data);
		 		 });
		if(json==null){
			System.out.println("鏌ヨ缁撴灉闆嗕负绌�");
			return null;
		}
		System.out.println("json:"+json.toString());
		//TODO 鍥炶皟鏈哄埗鏈夊緟琛ュ厖
		return null;
	}

}
