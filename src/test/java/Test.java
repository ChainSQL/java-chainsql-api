
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Table;
import com.peersafe.chainsql.core.Table.SyncCond;


public class Test {
	  public static final Chainsql c = Chainsql.c;
	  public Table table;
	  public static String sTableName;
	  public static void main(String[] args) {
		  c.connect("ws://192.168.0.197:6007");
		  
		  sTableName = "hijack";
		 
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
		  	//test.getTransactions();
		  	//test.getLedgerVersion();
		  	//test.getLedge();
			// test.testCreateTable();
			 test.testinsert();
			 //test.testUpdateTable();
			 //test.testdelete();
			 // test.testrename();
			//test.testget();
			  //test.testdrop();
			 //test.testassign();
		     //test.testcelassign();
		  	
			 //c.disconnect();
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
		JSONObject option = new JSONObject();
		option.put("ledger_index", "validated");
		option.put("expand", false);
		option.put("transactions", true);
		option.put("accounts", true);
		
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
    	 c.createTable(sTableName,c.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
	    		  "{'field':'name','type':'varchar','length':50,'default':null}","{'field':'age','type':'int'}"
	    		 ),(data)->{
	    			 System.out.println("creat------"+data);
	    		 });
	   }
	 
	 public void testinsert(){
//		 List<String> orgs = c.array("{'id':1,'age': 333}");
		 List<String> orgs = c.array("{'age': 333}");
		 JSONObject obj;
//		 obj = c.table(sTableName).insert(orgs).submit();
//		 System.out.println(obj);
		 
//		 obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
//		 System.out.println(obj);
		 
		 obj = c.table(sTableName).insert(orgs).submit((data)->{
 			 System.out.println("creat------"+data);
 		 });
		 System.out.println(obj);
	 }
	 

	  public void testUpdateTable(){
		  c.table(sTableName).get(c.array("{'name':'peera'}")).update(c.array("{'age':'24'}")).submit((data)->{
	 			 System.out.println("creat------"+data);
	 		 });
	        
	  }
	  public void testdelete(){
		  c.table(sTableName).get(c.array("{'name': 'peera'}")).delete().submit((data)->{
 			 System.out.println("creat------"+data);
 		 });
	        
	  }
	  
	  public void testrename(){
		  c.rename(sTableName, "TableBww",(data)->{
	 			 System.out.println("test1wqw------"+data);
	 		 });
	  }
	  
	  public void testget(){
		 // table = c.table("testcas").get(c.array("{'name':'peerb1'}")).limit("{index:0,total:10}").filterWith("[]").submit();
		  
		/*  table = c.table("testcas").get(c.array("{'name':'peerb1'}")).order(c.array("{age:-1}")).filterWith("[]").submit();*/
//		  c.table(sTableName).get(c.array("{age:{$ne:232}}")).order(c.array("{age:-1}")).withFields("[]").submit((data)->{
//	 			 System.out.println("test1wqw------"+data);
//	 		 });
		  //System.out.println(table.getData());
//		  JSONObject obj =  c.table(sTableName).get().submit();
//		  System.out.println(obj.toString());
	  }
	  
	  public void testassign(){
		  c.assign(sTableName, "rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr",c.array(c.perm.lsfDelete, c.perm.lsfSelect,c.perm.lsfUpdate),(data)->{
	 			 System.out.println("test1wqw------"+data);
	 		 });
	  }
	  public void testcelassign(){
		  c.assignCancle("tabke", "rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr",c.array("lsfDelete", "lsfSelect","lsfUpdate"),(data)->{
	 			 System.out.println("test1wqw------"+data);
	 		 });
	  }
	public void testdrop(){
		c.drop("dc_universe1",(data)->{
			 System.out.println("test1wqw------"+data);
		 });
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
