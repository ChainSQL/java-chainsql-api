
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Table;


public class Test {
	  public static final Chainsql c = Chainsql.c;
	  public Table table;
	  public static String sTableName;
	  public static void main(String[] args) {
		  //c.connect("ws://192.168.0.193:6006");
		  c.connect("ws://192.168.0.197:6007");
		  //c.connect("ws://192.168.0.230:6006");
		  
		  sTableName = "hijack2";
		 
		/* conn.address="rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr";
		  conn.secret="snrJRLBSkThBXtaBYZW1zVMmThm1d";*/
		  c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		//  c.use("rLQcU7QYrKtuLLj481XZq5M7m89TkoEG2z");
		  
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
		  	test.getTransactions();
//		  	test.getLedgerVersion();
//		  	test.getLedge();
//		  test.testCreateTable();
//		  test.getTransaction();
//		  test.testinsert();
		  //test.testUpdateTable();
		  //test.testdelete();
		  //test.testrename();
			//test.testget();
		  //test.testdrop();
//			 test.testassign();
		     //test.testcelassign();

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
	  
	public void getLedge(){

		JSONObject obj = c.getLedger();
		System.out.println("getLedger---" + obj);
	}
	public void getLedgerVersion(){
		
//		c.getLedgerVersion((data)->{
//			System.out.println(data);
//		});
		JSONObject obj = c.getLedgerVersion();
		System.out.println("getLedgerVersion---" + obj);
		
	}
	public void getTransactions(){
//		c.getTransactions("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",(data)->{
//			 System.out.println("creat------"+data);
//		 });
		
		JSONObject obj = c.getTransactions("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		 System.out.println("getTransactions------"+obj);
	}	
	public void getTransaction(){
		String hash = "C637A383BA850F6D487438A2590996CF09ED31E9DB856C9AD881C73E1FF483AA";
//		JSONObject obj = c.getTransaction(hash);
//		System.out.println("getTransaction------"+obj);
		c.getTransaction(hash, (data)->{
			System.out.println(data);
		});
		
	}
	
    public void testCreateTable() {
    	List<String> args = c.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
	    		  "{'field':'name','type':'varchar','length':50,'default':null}","{'field':'age','type':'int'}"
	    		 );
    	JSONObject obj;
    	
//    	obj = c.createTable(sTableName,args).submit();
//    	System.out.println(obj);
    	
    	obj = c.createTable(sTableName,args).submit((data)->{
    		System.out.println("creat------"+data);
    	});
    	System.out.println(obj);
    	
//    	obj = c.createTable(sTableName, args).submit(SyncCond.db_success);
//    	System.out.println(obj);
    }
	 
	 public void testinsert(){
//		 List<String> orgs = c.array("{'id':1,'age': 333}");
		 List<String> orgs = c.array("{'age': 53,'name':'灏忚儭'}","{'age': 33,'name':'灏忔槑'}");
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
		  List<String> arr1 = c.array("{'id': 1}");
		  List<String> arr2 = c.array("{'age': 226}");
		  
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
		  List<String> arr = c.array("{'name': 'lucy'}");
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
		  obj = c.renameTable(sTableName, "hijack").submit((data)->{
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
		 // table = c.table("testcas").get(c.array("{'name':'peerb1'}")).limit("{index:0,total:10}").filterWith("[]").submit();
		  
		/*  table = c.table("testcas").get(c.array("{'name':'peerb1'}")).order(c.array("{age:-1}")).filterWith("[]").submit();*/
		  JSONObject obj = c.table(sTableName).get(c.array("{age:{$ne:232}}")).order(c.array("{age:-1}")).withFields("[]").submit((data)->{
	 			 System.out.println("testget------"+data);
		  });
		  System.out.println(table.getData());
		  
		  obj =  c.table(sTableName).get(null).submit();
		  System.out.println(obj.toString());
	  }
	  
	  public void testassign(){
		  c.grant(sTableName, "rMgoRgBsh2NRUbEvFLRXHDVniYHS81JC3d",c.array("{insert:false}")).submit((data)->{
	 			 System.out.println("test1wqw------"+data);
		  });
	  }
	  /*public void testcelassign(){
		  c.grant("tabke", "rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr",c.array("{insert:true}","{lsfSelect:true}","{lsfUpdate:false}"),(data)->{
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
			System.out.println("鏌ヨ缁撴灉闆嗕负绌�");
			return null;
		}
		System.out.println("json:"+json.toString());
		//TODO 鍥炶皟鏈哄埗鏈夊緟琛ュ厖
		return null;
	}

}
