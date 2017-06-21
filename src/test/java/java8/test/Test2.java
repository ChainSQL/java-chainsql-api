//*****************实例说明*******************
//---1、本实例包含三个操作：(1)创建表testCreateTable()、(2)插入数据testinsert()、(3)查询数据testget()。
//---2、使用时须先单独执行 创建表操作。将test.testinsert()和test.testget()进行注解。
//---3、创建表完成后，再执行 插入数据操作和查询数据操作。将testCreateTable()注解。

//以下为代码行：
package java8.test;
import java.util.List;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.core.Table;
import com.peersafe.chainsql.util.Util;

public class Test2 {
public static final Chainsql c = Chainsql.c;
public Table table;
public static String sTableName;
public static void main(String[] args) {
	//将IP替换为需要链接的服务IP
	c.connect("ws://192.168.0.148:5008");
	
	sTableName = "T_BBPS_LEDGER_ONCHAIN";
	
	//c.as(address,secret) 替换address,secret信息
	c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
	
	//c.use(address); 替换address信息
	c.use("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

	Test2 test = new Test2();
	//创建表
	test.testCreateTable();

	//插入数据
	test.testinsert();

	//查询数据
	test.testget();

	//释放连接		  
	c.disconnect();
}
		
//创建表
public void testCreateTable() {
	List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
		"{'field':'name','type':'varchar','length':50,'default':null}",
		"{'field':'balance','type':'varchar','length':50,'default':null}",
		"{'field':'age','type':'int'}"    		 
	);
	JSONObject obj;
	obj = c.createTable(sTableName,args).submit(SyncCond.db_success);
	System.out.println("create result:" + obj);
}

//插入数据
public void testinsert(){
	List<String> orgs = Util.array("{'age': 27,'name':'交易者1','balance':'124'}","{'age': 33,'name':'交易者2','balance':'300'}");
	JSONObject obj;
	obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
	System.out.println("insert result:" + obj);
}

//查询数据
public void testget(){
	JSONObject obj = c.table(sTableName).get(Util.array("{age:{$ne:232}}")).order(Util.array("{age:-1}")).withFields("[]").submit();
	System.out.println("get result:" + obj.toString());
  }
}