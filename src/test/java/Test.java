
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Table;
import com.peersafe.chainsql.util.Util;


public class Test {
	  public static final Chainsql c = Chainsql.c;
	  public Table table;
	  public static String sTableName;
	  public static void main(String[] args) {
		  //c.connect("ws://192.168.0.193:6006");
		  c.connect("ws://192.168.0.197:6007");
		  //c.connect("ws://192.168.0.230:6006");
		  
		  sTableName = "ctesss";
		 
		/* conn.address="rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr";
		  conn.secret="snrJRLBSkThBXtaBYZW1zVMmThm1d";*/
		  c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
//		  c.as("rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q", "ssnqAfDUjc6Bkevd1Xmz5dJS5yHdz");
//		  c.use("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
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
		  
		  Test test =new Test();
//		  test.testts();
//		  	test.getTransactions();
//		  	test.getLedgerVersion();
//		  	test.getLedge();
//		  test.getUserToken();
		  test.testCreateTable();
		  //test.testinsert();
//		  test.testUpdateTable();
//		  test.testdelete();
//		  test.testrename();
//		  test.testget();
//		  test.testdrop();
//			 test.grant();

//		  try {
//			Thread.sleep(10000);
//		  } catch (InterruptedException e) {
//			e.printStackTrace();
//		  }
//		  
//		  c.disconnect();
			 //System.out.println("```````````");
			// test.testget();
			/* try {
				Runtime.getRuntime().exec("taskkill /F /IM javaw.exe");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 */
		
	    }
	  public void testts(){
		  c.beginTran();
//		  c.grant(sTableName, "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q","{insert:true,update:true}");
		  c.table(sTableName).insert(Util.array("{'age': 23,'name':'adsf','balance':'124'}","{'age': 33,'name':'小sr','balance':'300'}"));
		  c.table(sTableName).get(Util.array("{'id': 2}")).update("{'balance':200}");
		  JSONObject obj = c.commit((data)->{
			  System.out.println("commit------"+data);
		  });
		  System.out.println(obj);
	  } 
	public void getUserToken(){
		c.getUserToken(sTableName);
	}
	  
	public void getLedge(){
		JSONObject option = new JSONObject();
		option.put("ledger_index", 766);
		c.getLedger(option,(data)->{
			System.out.println("creat------"+data);
		 });
	}
	public void getLedgerVersion(){
		c.getLedgerVersion((data)->{
			 System.out.println("creat------"+data);
		 });
	}
	public void getTransactions(){
		c.getTransactions("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",(data)->{
			 System.out.println("creat------"+data);
		 });
	}	
    public void testCreateTable() {
    	List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
	    		  "{'field':'name','type':'varchar','length':50,'default':null}","{'field':'balance','type':'varchar','length':50,'default':null}","{'field':'age','type':'int'}"
	    		 );
    	JSONObject obj;
    	
//    	obj = c.createTable(sTableName,args).submit();
//    	System.out.println(obj);
    	
    	obj = c.createTable(sTableName,args,true).submit((data)->{
    		System.out.println("creat------"+data);
    	});
    	System.out.println(obj);
    	
//    	obj = c.createTable(sTableName, args).submit(SyncCond.db_success);
//    	System.out.println(obj);
    }
	 
	 public void testinsert(){
//		 List<String> orgs = Util.array("{'id':1,'age': 333}");
		 List<String> orgs = Util.array("{'age': 23,'name':'adsf','balance':'124'}","{'age': 33,'name':'小sr','balance':'300'}");
		 JSONObject obj;
//		 obj = c.table(sTableName).insert(orgs).submit();
//		 System.out.println(obj);
		 
//		 obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
//		 System.out.println(obj);
		 
		 obj = c.table(sTableName).insert(orgs).submit((data)->{
 			 System.out.println("insert------"+data);
 		 });
		 System.out.println(obj);
	 }

	  public void testUpdateTable(){
		  List<String> arr1 = Util.array("{'id': 2}");
		  String arr2 ="{'balance':200}";
		  
		  JSONObject obj;
		  obj = c.table(sTableName).get(arr1).update(arr2).submit((data)->{
	 			 System.out.println("update------"+data);
	 		 });
		  System.out.println(obj);
		  
//		  obj = c.table(sTableName).get(arr1).update(arr2).submit();
//		  System.out.println(obj);
//		  
//		  obj = c.table(sTableName).get(arr1).update(arr2).submit(SyncCond.db_success);
//		  System.out.println(obj);
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
		 // table = c.table("testcas").get(Util.array("{'name':'peerb1'}")).limit("{index:0,total:10}").filterWith("[]").submit();
		  
		/*  table = c.table("testcas").get(Util.array("{'name':'peerb1'}")).order(Util.array("{age:-1}")).filterWith("[]").submit();*/
		  JSONObject obj = c.table(sTableName).get(Util.array("{age:{$ne:232}}")).order(Util.array("{age:-1}")).withFields("[]").submit((data)->{
	 			 System.out.println("testget------"+data);
		  });
		  
		  System.out.println(obj.toString());
	  }
	  
	  public void grant(){
		  JSONObject obj = c.grant(sTableName, "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q","{insert:true,update:true}").submit((data)->{
	 			 System.out.println("grant------"+data);
		  });
		  System.out.println(obj.toString());
	  }
	  /*public void testcelassign(){
		  c.grant("tabke", "rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr",Util.array("{insert:true}","{lsfSelect:true}","{lsfUpdate:false}"),(data)->{
	 			 System.out.println("test1wqw------"+data);
	 		 });
	  }*/
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
			System.out.println("查询结果集为空");
			return null;
		}
		System.out.println("json:"+json.toString());
		//TODO 回调机制有待补充
		return null;
	}

}
