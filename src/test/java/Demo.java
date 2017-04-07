

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Table;
import com.peersafe.chainsql.net.Connection;

public class Demo {
	Chainsql r = null;
	Connection conn =null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Demo demo =new Demo("192.168.0.228",6006);
		demo.test();
		/*JSONObject wherejson=new JSONObject();wherejson.put("name", "peera1");
		String where=wherejson.toString();
		String filterWith = "[]";//要查询的字段，例如:"['name','age']";如果为空"[]",则查询所有字段；
		demo.select("testaa",filterWith, where);
		String tableName="testaa";*/
		//建表
//		ArrayList<String> raw=new ArrayList<String>();
//		JSONObject col=new JSONObject();
//		col.put("field", "id");col.put("type", "int");col.put("length", 11);col.put("PK",1);col.put("NN", 1);col.put("UQ", 1);col.put("A1", 1);
//		raw.add(col.toString());
//		System.out.println("col:"+col.toString());;
//		JSONObject col2=new JSONObject();
//		col2.put("field", "name");col2.put("type", "varchar");col2.put("length", 50);col2.put("default","");
//		raw.add(col2.toString());
//		JSONObject col3=new JSONObject();
//		col3.put("field", "age");col3.put("type", "int");
//		raw.add(col3.toString());
//		
//		String tableName="dc_universe1";
//		System.out.println("开始建表...");
//		demo.createTable(tableName, raw);
//		System.out.println("建表，完成");
		
		//插入数据
//		ArrayList<String> rows=new ArrayList<String>();
//		JSONObject row=new JSONObject();
//		row.put("id",2);
//		row.put("name", "lisi");
//		row.put("age", "22");
//		rows.add(row.toString());
//		demo.insert(tableName,rows);
//		demo.release();//这里调用完成后，java进程不会退出，why？
//		System.out.println("程序结束");
//		System.exit(0);
	}
	public void createTable(String table ,ArrayList<String> raw){
		
		r.createTable(table,raw, null);
		System.out.println("建表，成功");
	}
	/**构造方法*/
	public Demo(String ip,int port){
		r = Chainsql.c;
		conn =r.connect("ws://"+ip+":"+port);
		r.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		if(conn==null){
			System.out.println("无法获取connection对象");
		}
		
	}
	/**
	 * 释放对象*/
	public void release(){
		this.r.disconnect();
		System.out.println("disconnected!");
	}
	/**
	 * 插入操作，向一个表插入若干条记录
	 * @param tableName 要插入的表名
	 * @param rows json格式的列表，每一条数据对应一个json，格式如{'name':'xxx','age':22}
	 * */
	public void insert(String tableName,ArrayList<String> rows){
		r.table(tableName).insert(rows).submit(null);
		System.out.println("插入成功");
	}
	/**
	 * 更新操作，根据where条件，更新指定的行的指定列数据
	 * @param tableName 要更新的表名
	 * @param col 要更新的列及值。json格式，如{'name':'xxx','age':22}
	 * @param whereCond 要更新的列及值。json格式，如{'name':'xxx','age':22}
	 * */
	public void update(String tableName,String col,String whereCond){
		ArrayList<String> whereList=new ArrayList<String>();
		whereList.add(whereCond);
		ArrayList<String> colList=new ArrayList<String>();
		colList.add(col);
		r.table(tableName).get(whereList).update(colList).submit(null);
	}
	/**
	 * 删除操作，根据where条件，删除指定的行
	 * @param tableName 要删除数据的表名
	 * @param whereCond 要删除的where条件。json格式，如{'name':'xxx','age':22}
	 * */
	public void delete(String tableName,String whereCond){
		ArrayList<String> whereList=new ArrayList<String>();
		whereList.add(whereCond);
		r.table(tableName).get(whereList).delete().submit(null);
	}
	/**
	 * 查询操作
	 * @param tableName 表名
	 * @param whereCond where条件，json对象，格式如{'id':'123'}*/
	public ArrayList<JSONObject> select(String tableName,String filterWith,String whereCond){
		ArrayList<String> cond=new ArrayList<String>();
		cond.add(whereCond);
		Table t=r.table(tableName).withFields(filterWith).get(cond).submit(null);
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println("看一下结果..."+t.getData());
		JSONArray jsonArray=(JSONArray)t.getData();
		ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
		System.out.println("结果集:");
		for (int j= 0;j<jsonArray.length();j++){  
			System.out.println(jsonArray.getJSONObject(j));
			jsonList.add(jsonArray.getJSONObject(j));  
         
       }  
		if(jsonList.size()<0){
			System.out.println("查询结果集为空");
			return null;
		}
//		System.out.println("json:"+json.toString());
		//TODO 回调机制有待补充
		return null;
	}
	public void test(){
		
		// TODO Auto-generated method stub

//			System.out.println("开始查询......");
//			JSONObject wherejson=new JSONObject();wherejson.put("name", "lisi");
//			String where=wherejson.toString();
//			String filterWith = "[]";//要查询的字段，例如:"['name','age']";如果为空"[]",则查询所有字段；
//			demo.select("dc_universe1",filterWith, where);
//			System.out.println("查询结束");
//			System.out.println("开始建表...");
//			String tableName="dc_universe1";
//			ArrayList<String> raw=new ArrayList<String>();
//			JSONObject col1=new JSONObject();
//			col1.put("field", "id");col1.put("type", "int");col1.put("length", 11);col1.put("PK",1);col1.put("NN", 1);col1.put("UQ", 1);col1.put("A1", 1);
//			raw.add(col1.toString());
//			System.out.println("col1:"+col1.toString());
//			JSONObject col2=new JSONObject();
//			col2.put("field", "name");col2.put("type", "varchar");col2.put("length", 50);col2.put("default","");
//			raw.add(col2.toString());
//			System.out.println("col2:"+col2.toString());
//			JSONObject col3=new JSONObject();
//			col3.put("field", "age");col3.put("type", "int");
//			raw.add(col3.toString());
//			System.out.println("col3:"+col3.toString());
//			demo.createTable(tableName, raw);
//			System.out.println("建表，完成");
			
			System.out.println("开始建表...");
			String tableName="T_BBPS_LEDGER_CHAIN";
			ArrayList<String> raw=new ArrayList<String>();
//			SENDBANKNO.put("field", "SENDBANKNO");SENDBANKNO.put("type", "char");SENDBANKNO.put("length", 12);SENDBANKNO.put("PK",1);SENDBANKNO.put("NN", 1);SENDBANKNO.put("UQ", 1);SENDBANKNO.put("AI", 1);
			JSONObject ID=new JSONObject();
			ID.put("field", "ID");ID.put("type", "int");ID.put("length", 11);ID.put("NN", 1);ID.put("PK",1);ID.put("AI",1);
			raw.add(ID.toString());
			System.out.println("ID:"+ID.toString());
			JSONObject SENDBANKNO=new JSONObject();
			SENDBANKNO.put("field", "SENDBANKNO");SENDBANKNO.put("type", "varchar");SENDBANKNO.put("length", 12);SENDBANKNO.put("NN", 1);
			raw.add(SENDBANKNO.toString());
			System.out.println("SENDBANKNO:"+SENDBANKNO.toString());
			JSONObject AGENTSERIALNO=new JSONObject();
			AGENTSERIALNO.put("field", "AGENTSERIALNO");AGENTSERIALNO.put("type", "varchar");AGENTSERIALNO.put("length", 24);AGENTSERIALNO.put("default","");
			raw.add(AGENTSERIALNO.toString());
			System.out.println("AGENTSERIALNO:"+AGENTSERIALNO.toString());
			JSONObject WORKDATE=new JSONObject();
			WORKDATE.put("field", "WORKDATE");WORKDATE.put("type", "varchar");WORKDATE.put("length", 8);WORKDATE.put("default","");
			raw.add(WORKDATE.toString());
			System.out.println("WORKDATE:"+WORKDATE.toString());
			JSONObject RECVBANKNO=new JSONObject();
			RECVBANKNO.put("field", "RECVBANKNO");RECVBANKNO.put("type", "varchar");RECVBANKNO.put("length", 12);RECVBANKNO.put("default","");
			raw.add(RECVBANKNO.toString());
			System.out.println("RECVBANKNO:"+RECVBANKNO.toString());
			JSONObject PAYERBANKNO=new JSONObject();
			PAYERBANKNO.put("field", "PAYERBANKNO");PAYERBANKNO.put("type", "varchar");PAYERBANKNO.put("length", 12);PAYERBANKNO.put("default","");
			raw.add(PAYERBANKNO.toString());
			System.out.println("PAYERBANKNO:"+PAYERBANKNO.toString());
			JSONObject PAYERACCT=new JSONObject();
			PAYERACCT.put("field", "PAYERACCT");PAYERACCT.put("type", "varchar");PAYERACCT.put("length", 30);PAYERACCT.put("default","");
			raw.add(PAYERACCT.toString());
			System.out.println("PAYERACCT:"+PAYERACCT.toString());
			JSONObject PAYERNAME=new JSONObject();
			PAYERNAME.put("field", "PAYERNAME");PAYERNAME.put("type", "varchar");PAYERNAME.put("length", 255);PAYERNAME.put("default","");
			raw.add(PAYERNAME.toString());
			System.out.println("PAYERNAME:"+PAYERNAME.toString());
			JSONObject PAYEEBANKNO=new JSONObject();
			PAYEEBANKNO.put("field", "PAYEEBANKNO");PAYEEBANKNO.put("type", "varchar");PAYEEBANKNO.put("length", 12);PAYEEBANKNO.put("default","");
			raw.add(PAYEEBANKNO.toString());
			System.out.println("PAYEEBANKNO:"+PAYEEBANKNO.toString());
			JSONObject PAYEEACCT=new JSONObject();
			PAYEEACCT.put("field", "PAYEEACCT");PAYEEACCT.put("type", "varchar");PAYEEACCT.put("length", 30);PAYEEACCT.put("default","");
			raw.add(PAYEEACCT.toString());
			System.out.println("PAYEEACCT:"+PAYEEACCT.toString());
			JSONObject PAYEENAME=new JSONObject();
			PAYEENAME.put("field", "PAYEENAME");PAYEENAME.put("type", "varchar");PAYEENAME.put("length", 255);PAYEENAME.put("default","");
			raw.add(PAYEENAME.toString());
			System.out.println("PAYEENAME:"+PAYEENAME.toString());
			JSONObject CURRTYPE=new JSONObject();
			CURRTYPE.put("field", "CURRTYPE");CURRTYPE.put("type", "varchar");CURRTYPE.put("length", 3);CURRTYPE.put("default","");
			raw.add(CURRTYPE.toString());
			System.out.println("CURRTYPE:"+CURRTYPE.toString());
			JSONObject AMOUNT=new JSONObject();
			AMOUNT.put("field", "AMOUNT");AMOUNT.put("type", "int");AMOUNT.put("length", 20);
			raw.add(AMOUNT.toString());
			System.out.println("AMOUNT:"+AMOUNT.toString());
			JSONObject DEBITCREDITFLAG=new JSONObject();
			DEBITCREDITFLAG.put("field", "DEBITCREDITFLAG");DEBITCREDITFLAG.put("type", "varchar");DEBITCREDITFLAG.put("length", 1);DEBITCREDITFLAG.put("default","");
			raw.add(DEBITCREDITFLAG.toString());
			System.out.println("DEBITCREDITFLAG:"+DEBITCREDITFLAG.toString());
			JSONObject PAYBACKFLAG=new JSONObject();
			PAYBACKFLAG.put("field", "PAYBACKFLAG");PAYBACKFLAG.put("type", "varchar");PAYBACKFLAG.put("length", 2);PAYBACKFLAG.put("default","");
			raw.add(PAYBACKFLAG.toString());
			System.out.println("PAYBACKFLAG"+PAYBACKFLAG.toString());
			JSONObject PAYBACKREASON=new JSONObject();
			PAYBACKREASON.put("field", "PAYBACKREASON");PAYBACKREASON.put("type", "varchar");PAYBACKREASON.put("length", 255);PAYBACKREASON.put("default","");
			raw.add(PAYBACKREASON.toString());
			System.out.println("PAYBACKREASON:"+PAYBACKREASON.toString());
			JSONObject ORISENDBANKNO=new JSONObject();
			ORISENDBANKNO.put("field", "ORISENDBANKNO");ORISENDBANKNO.put("type", "varchar");ORISENDBANKNO.put("length", 12);ORISENDBANKNO.put("default","");
			raw.add(ORISENDBANKNO.toString());
			System.out.println("ORISENDBANKNO:"+ORISENDBANKNO.toString());
			JSONObject ORIAGENTSERIALNO=new JSONObject();
			ORIAGENTSERIALNO.put("field", "ORIAGENTSERIALNO");ORIAGENTSERIALNO.put("type", "varchar");ORIAGENTSERIALNO.put("length", 24);ORIAGENTSERIALNO.put("default","");
			raw.add(ORIAGENTSERIALNO.toString());
			System.out.println("ORIAGENTSERIALNO:"+ORIAGENTSERIALNO.toString());
			JSONObject ORIWORKDATE=new JSONObject();
			ORIWORKDATE.put("field", "ORIWORKDATE");ORIWORKDATE.put("type", "varchar");ORIWORKDATE.put("length", 8);ORIWORKDATE.put("default","");
			raw.add(ORIWORKDATE.toString());
			System.out.println("ORIWORKDATE:"+ORIWORKDATE.toString());
			JSONObject LIQUIDDATE=new JSONObject();
			LIQUIDDATE.put("field", "LIQUIDDATE");LIQUIDDATE.put("type", "varchar");LIQUIDDATE.put("length", 8);LIQUIDDATE.put("default","");
			raw.add(LIQUIDDATE.toString());
			System.out.println("LIQUIDDATE:"+LIQUIDDATE.toString());
			JSONObject SESSIONID=new JSONObject();
			SESSIONID.put("field", "SESSIONID");SESSIONID.put("type", "int");
			raw.add(SESSIONID.toString());
			System.out.println("SESSIONID:"+SESSIONID.toString());
			JSONObject LIQUIDSTATUS=new JSONObject();
			LIQUIDSTATUS.put("field", "LIQUIDSTATUS");LIQUIDSTATUS.put("type", "varchar");LIQUIDSTATUS.put("length", 1);LIQUIDSTATUS.put("default","");
			raw.add(LIQUIDSTATUS.toString());
			System.out.println("LIQUIDSTATUS:"+LIQUIDSTATUS.toString());
			
			
//			JSONObject dd=new JSONObject();
//			dd.put("field", "WORKDATE");dd.put("type", "varchar");dd.put("length", 8);dd.put("default","");
//			raw.add(dd.toString());
//			System.out.println("dd:"+dd.toString());

			//		JSONObject col3=new JSONObject();
//			col3.put("field", "age");col3.put("type", "int");
//			raw.add(col3.toString());
//			System.out.println("col3:"+col3.toString());
			String table="testaaaa";
			r.createTable(table,raw, null);
			System.out.println("建表，完成");
			//插入数据
//			ArrayList<String> rows=new ArrayList<String>();
//			JSONObject row=new JSONObject();
//			row.put("id",2);
//			row.put("name", "lisi");
//			row.put("age", "22");
//			rows.add(row.toString());
//			demo.insert(tableName,rows);
			//r.release();//这里调用完成后，java进程不会退出，why？
			System.out.println("程序结束");
//			System.exit(0);
		}
		
}
