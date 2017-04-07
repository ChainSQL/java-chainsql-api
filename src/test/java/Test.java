
import java.util.ArrayList;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Table;




public class Test {
	  public static final Chainsql c = Chainsql.c;
	  public Table table;
	  public static void main(String[] args) {
		  c.connect("ws://192.168.0.197:6007");
		  
		  
		 
		/* conn.address="rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr";
		  conn.secret="snrJRLBSkThBXtaBYZW1zVMmThm1d";*/
		  c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		//  c.use("rLQcU7QYrKtuLLj481XZq5M7m89TkoEG2z");
		  
		// c.event.subTable("testcssas", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
		  
		  Test test =new Test();
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
    public void testCreateTable() {
    	 c.createTable("test1a",c.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
	    		  "{'field':'name','type':'varchar','length':50,'default':null}","{'field':'age','type':'int'}"
	    		 ),(data)->{
	    			 System.out.println("creat------"+data);
	    		 });
	   }
	 
	 public void testinsert(){
		 c.table("test1a").insert(c.array("{'name': 'peera1','age': 222}","{'name': 'peerb1','age': 231}")).submit((data)->{
 			 System.out.println("creat------"+data);
 		 });
	 }
	 

	  public void testUpdateTable(){
		  c.table("tabke").get(c.array("{'name':'peera'}")).update(c.array("{'age':'24'}")).submit((data)->{
	 			 System.out.println("creat------"+data);
	 		 });
	        
	  }
	  public void testdelete(){
		  c.table("tabke").get(c.array("{'name': 'peera'}")).delete().submit((data)->{
 			 System.out.println("creat------"+data);
 		 });
	        
	  }
	  
	  public void testrename(){
		  c.reName("test", "TableBww",(data)->{
	 			 System.out.println("test1wqw------"+data);
	 		 });
	  }
	  
	  public void testget(){
		 // table = c.table("testcas").get(c.array("{'name':'peerb1'}")).limit("{index:0,total:10}").filterWith("[]").submit();
		  
		/*  table = c.table("testcas").get(c.array("{'name':'peerb1'}")).order(c.array("{age:-1}")).filterWith("[]").submit();*/
		  table = c.table("test1wqw").get(c.array("{age:{$ne:232}}")).order(c.array("{age:-1}")).withFields("[]").submit((data)->{
	 			 System.out.println("test1wqw------"+data);
	 		 });
		  //System.out.println(table.getData());
		  //table = c.table("test3").select(c.array("{'name':'peerb1'}")).submit();
		  System.out.println(table.getData());
	  }
	  
	  public void testassign(){
		  c.assign("test", "rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr",c.array(c.perm.lsfDelete, c.perm.lsfSelect,c.perm.lsfUpdate),(data)->{
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
		Table t=c.table(tableName)
				.withFields(filterWith).get(cond).submit((data)->{
		 			 System.out.println("creat------"+data);
		 		 });
		System.out.println(t.getData().getClass());
		JSONObject json=(JSONObject)t.getData();
		if(json==null){
			System.out.println("查询结果集为空");
			return null;
		}
		System.out.println("json:"+json.toString());
		//TODO 回调机制有待补充
		return null;
	}

}
