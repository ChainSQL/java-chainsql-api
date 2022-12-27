package com.peersafe.example.performance.bigraw;

import java.util.List;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.Util;

public class CreateAndGrant {
	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	
	public static final Chainsql c = new Chainsql();
	
	public static void main(String[] args) {
		
		if(args.length != 2)
			System.out.println("参数错误,length="+args.length);
		
		c.connect(args[0]);
		c.as(rootAddress, rootSecret);
		
		createTable(args[1]);
		grant(args[1]);
	}
	
	public static void createTable(String sTableName) {
		List<String> args = Util.array("{'field':'id','type':'int','length':11}",
				"{'field':'data','type':'longtext'}");
		System.out.println("创建表 : " + sTableName + "...");
		JSONObject obj;
		obj = c.createTable(sTableName,args,false).submit(SyncCond.db_success);
		System.out.println(obj);
		if (obj.getString("status").equals("db_success")) {
			System.out.println(sTableName + " 创建表成功");
		} else {
			System.out.println(sTableName + " 创建表失败");
		}
	}
	
	public static void grant(String sTableName) {
		JSONObject obj = new JSONObject();
		obj = c.grant(sTableName, "zzzzzzzzzzzzzzzzzzzzBZbvji", "{insert:true,update:true,delete:true}")
				   .submit(SyncCond.db_success);
		System.out.println("grant result:" + obj.toString());
	}
}
