
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.core.Table;
import com.peersafe.chainsql.net.Connection;;


public class Demo {
	Chainsql r = null;
	Connection conn =null;
	
	public static void main(String[] args) {
		Demo demo1 =new Demo("101.201.40.124",5006);
		String raw="{\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000004\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000005\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000002\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000001\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000002\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000002\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000004\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000002\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000005\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000003\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000001\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000003\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000003\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000004\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000003\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000005\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000004\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000001\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000004\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000004\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000004\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000005\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000005\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000001\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000005\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000005\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}, {\"PAYERBANKNO\":\"100000000005\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000004\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}";
		ArrayList rows = new ArrayList();
		rows.add("{\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000004\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000005\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000002\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000001\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000002\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000002\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000004\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000002\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000005\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000003\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000001\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000003\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000003\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000004\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000003\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000005\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000004\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000001\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000004\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000004\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000004\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000005\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000005\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000001\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000005\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000005\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		rows.add("{\"PAYERBANKNO\":\"100000000005\",\"LIQUIDSESSION\":\"1\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000004\",\"WORKDATE\":\"20170412\",\"LIQUIDTYPE\":\"0\"}");
		demo1.insert("T_BBPS_SESSIONHIS", rows);
		demo1.release();
		System.out.println("程序结束");
//		System.exit(0);
	}
	/**
	 * 插入操作，向一个表插入若干条记录
	 * @param tableName 要插入的表名
	 * @param rows json格式的列表，每一条数据对应一个json，格式如{'name':'xxx','age':22}
	 * */
	public void insert(String tableName,ArrayList<String> rows){
		System.out.println("rows:"+rows.toString());
		//JSONObject obj= r.table(tableName).insert(rows).submit(SyncCond.db_success);
		JSONObject obj= r.table(tableName).insert(rows).submit((data)->{
			System.out.println(data);
		});
		System.out.println("插入结果:"+obj.toString());
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
		r.table(tableName).get(whereList).update(colList).submit(SyncCond.db_success);
	}
	/**
	 * 删除操作，根据where条件，删除指定的行
	 * @param tableName 要删除数据的表名
	 * @param whereCond 要删除的where条件。json格式，如{'name':'xxx','age':22}
	 * */
	public void delete(String tableName,String whereCond){
		System.out.println("delete from "+tableName+",where:"+whereCond);
		ArrayList<String> whereList=new ArrayList<String>();
		whereList.add(whereCond);
		r.table(tableName).get(whereList).delete().submit(SyncCond.db_success);
	}
	
	/**
	 * 查询操作
	 * @param tableName 表名
	 * @param whereCond where条件，json对象，格式如{'id':'123'}*/
	public ArrayList<JSONObject> select(String tableName,String filterWith,String whereCond){
		System.out.println("whereCond:"+whereCond);
		ArrayList<String> cond=new ArrayList<String>();
		cond.add(whereCond);
		System.out.println("cond:"+cond.toString());
//		Table t=r.table(tableName).filterWith(filterWith).get(cond).submit();//这一句是ok的
		Table t=r.table(tableName).withFields(filterWith).get(cond);
		JSONObject jsonArray=t.submit();
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("看一下结果...");
//		JSONArray jsonArray=(JSONArray)t.getData();
//		ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
//		for (int j= 0;j<jsonArray.length();j++){  
//			System.out.println(jsonArray.getJSONObject(j));
//			jsonList.add(jsonArray.getJSONObject(j));  
//         
//       }  
		System.out.println("结果集？:"+jsonArray.toString());
		
//		System.out.println("json:"+json.toString());
		//TODO 回调机制有待补充
		return null;
	}

	public ArrayList<JSONObject> select2(String tableName,String filterWith,String whereCond){
		System.out.println("whereCond:"+whereCond);
		JSONArray cond1= new JSONArray(whereCond);
		ArrayList<String> cond=new ArrayList<String>();
		for(int i=0;i<cond1.length();i++){
			cond.add(cond1.get(i).toString());
		}
		System.out.println("cond:"+cond.toString());
//		Table t=r.table(tableName).filterWith(filterWith).get(cond).submit();//这一句是ok的
		Table t=r.table(tableName).withFields(filterWith).get(cond);
		JSONObject obj=t.submit(SyncCond.db_success);
		System.out.println("get  submit returns:"+obj.toString());
		JSONArray jsonArray = (JSONArray) (obj.getJSONArray("lines"));
		System.out.println("lines:"+jsonArray.toString());
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("看一下结果...");
//		JSONArray jsonArray=(JSONArray)t.getData();
//		ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
//		for (int j= 0;j<jsonArray.length();j++){  
//			System.out.println(jsonArray.getJSONObject(j));
//			jsonList.add(jsonArray.getJSONObject(j));  
//         
//       }  
//		System.out.println("结果集:"+jsonList.toString());
		
//		System.out.println("json:"+json.toString());
		//TODO 回调机制有待补充
		return null;
	}

}
