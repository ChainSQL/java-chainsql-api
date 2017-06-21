package java8.test;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.core.Table;
import com.peersafe.chainsql.util.Util;


public class test1 {
	public static final Chainsql c = Chainsql.c;
	public Table table;
	public static String TableName;
	private static String IpPort = "";
	private static String Root_Address = "";
	private static String Root_Secret = "";
	private static String DefaultPort = "6007";
	public static void main(String[] args) {
		boolean ret = parseArgs(args);
		if(!ret) return;
		
		//连接
		c.connect("ws://" + IpPort);
		
		//设置日志级别
		//c.connection.client.logger.setLevel(Level.SEVERE);
		
		//操作者为根账户
		c.as(Root_Address,Root_Secret);
		
//	  c.connect("ws://139.198.11.189:6006");
//	  
//	  //sTableName = "hiyou";
//	  TableName = "T_BBPS_LEDGER_ONCHAIN";
//	 
//
//	  c.as("rw4ZyNJSU9UazgB6y8Ft44w2FPvPTUAKLM", "snc4uBhbwzGQKYSXyKr5oMPUdWGzY");
//	  c.use("rw4ZyNJSU9UazgB6y8Ft44w2FPvPTUAKLM");	  
	  

	  
	  test1 test =new test1();

	  test.testCreateTable();

	  test.testinsert();

	  test.testget();
  
	  c.disconnect();
    }
	
  public void testCreateTable() {
  	List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
	    		  "{'field':'name','type':'varchar','length':50,'default':null}","{'field':'balance','type':'varchar','length':50,'default':null}","{'field':'age','type':'int'}"
	    		 );
  	JSONObject obj;
  	
//  	obj = c.createTable(sTableName,args).submit();
//  	System.out.println(obj);
  	
//  	obj = c.createTable(sTableName,args,true).submit((data)->{
//  		System.out.println("creat------"+data);
//  	});
//  	System.out.println(obj);
  	
  	obj = c.createTable(TableName, args).submit(SyncCond.db_success);
  	if(obj.getString("status").equals("success")){
  		System.out.println("创建表成功！");
  	}else{
  		System.out.println("创建表失败！");
  	}
  	System.out.println(obj);
  }
	 
	 public void testinsert(){
//		 List<String> orgs = Util.array("{'age': 333}");
//		 List<String> orgs = Util.array("{'PAYERNAME':'李伟华','ORISENDBANKNO':'', 'PAYERBANKNO':'100000000001', 'SESSIONID':'1', 'LIQUIDSTATUS':'0', 'DEBITCREDITFLAG':'0', 'PAYBACKREASON':'', 'PAYERACCT':'1000000000000111', 'RECVBANKNO':'100000000002', 'CURRTYPE':'156', 'AMOUNT':'12000', 'SENDBANKNO':'100000000001', 'AGENTSERIALNO':'201704050000000000000314', 'ORIAGENTSERIALNO':'', 'ORIWORKDATE':'', 'LIQUIDDATE':'20170405', 'PAYEEACCT':'2000000000000123', 'PAYEEBANKNO':'100000000002', 'PAYBACKFLAG':'00', 'PAYEENAME':'李伟华', 'WORKDATE':'20170405'}");
		 List<String> orgs = Util.array("{'age': 27,'name':'test_re','balance':'124'}"
				 					    //,"{'age': 33,'name':'李宇','balance':'300'}"
				 );
		 JSONObject obj;
//		 obj = c.table(sTableName).insert(orgs).submit();
//		 System.out.println(obj);
		 
//		 obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
//		 System.out.println(obj);
		 
		 obj = c.table(TableName).insert(orgs).submit(SyncCond.db_success);
		 if(obj.getString("status").equals("success")){
			 System.out.println("插入表成功！");
		 }else{
			 System.out.println("插入表失败！");
		 }
		 System.out.println(obj);
	 }

	  
	  public void testget(){
		 // table = c.table("testcas").get(Util.array("{'name':'peerb1'}")).limit("{index:0,total:10}").filterWith("[]").submit();
		  
		/*  table = c.table("testcas").get(Util.array("{'name':'peerb1'}")).order(Util.array("{age:-1}")).filterWith("[]").submit();*/
		  JSONObject obj = c.table(TableName).get(Util.array("{age:{$ne:232}}")).order(Util.array("{age:-1}")).withFields("[]").submit();
		  System.out.println("查询表成功！");
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

    public static boolean isIpv4(String ipAddress) {
        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
    
	private static boolean parseArgs(String[] args){
		if(args.length == 1 && args[0].equals("-h")){
			printHelpMessages();
			return false;
		}else if(args.length != 4){
			System.out.println("参数个数不正确");
			printHelpMessages();
			return false;
		}
		
		if(!isIpv4(args[0])){
			System.out.println("IP地址地址无效");
			printHelpMessages();
			return false;
		}
		
		String str = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh";
		if(args[2].length() != str.length() || args[2].charAt(0) != 'r'){
			System.out.println("根账户地址无效");
			printHelpMessages();
			return false;
		}
		str = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";
		if(args[3].length() != str.length() || args[3].charAt(0) != 's'){
			System.out.println("根账户私钥无效");
			printHelpMessages();
			return false;
		}
		

		IpPort = args[0] + ":" + DefaultPort;
		TableName = args[1];
		Root_Address = args[2];
		Root_Secret = args[3];
		
		return true;
	}
	private static void printHelpMessages(){
		System.out.println("TestChainsql elastic_ip table_name root_account root_secret");
		System.out.println("eg: TestChainsql 192.168.0.101 t_hello rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("\t-h\t\t帮助");		
		System.out.println("\tip:port\t\t要连接的chainSql节点ip地址");
		System.out.println("\ttable_name\t表名");
		System.out.println("\taddress\t\t账户地址");
		System.out.println("\tsecret\t\t账户私钥");
	}
}

