

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
		Demo demo =new Demo("192.168.0.151",6006);
		JSONObject wherejson=new JSONObject();wherejson.put("name", "peera1");
		String where=wherejson.toString();
		String filterWith = "[]";//要查询的字段，例如:"['name','age']";如果为空"[]",则查询所有字段；
		demo.select("testaa",filterWith, where);
		String tableName="testaa";
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
		demo.release();//这里调用完成后，java进程不会退出，why？
//		System.out.println("程序结束");
//		System.exit(0);
	}
	public void createTable(String table ,ArrayList<String> raw){
		
		r.createTable(table,raw);
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
		r.table(tableName).insert(rows).submit();
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
		r.table(tableName).get(whereList).update(colList).submit();
	}
	/**
	 * 删除操作，根据where条件，删除指定的行
	 * @param tableName 要删除数据的表名
	 * @param whereCond 要删除的where条件。json格式，如{'name':'xxx','age':22}
	 * */
	public void delete(String tableName,String whereCond){
		ArrayList<String> whereList=new ArrayList<String>();
		whereList.add(whereCond);
		r.table(tableName).get(whereList).delete().submit();
	}
	/**
	 * 查询操作
	 * @param tableName 表名
	 * @param whereCond where条件，json对象，格式如{'id':'123'}*/
	public ArrayList<JSONObject> select(String tableName,String filterWith,String whereCond){
		ArrayList<String> cond=new ArrayList<String>();
		cond.add(whereCond);
		Table t=r.table(tableName).filterWith(filterWith).get(cond).submit();
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


}
