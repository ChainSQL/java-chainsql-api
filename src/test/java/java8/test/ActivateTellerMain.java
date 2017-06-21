package java8.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.Util;

public class ActivateTellerMain {
	private static final Chainsql c = Chainsql.c;
	private static String ip = "";
	private static String cid = "";
	private static String root_address = "";
	private static String root_secret = "";
	
	public static void main(String[] args){
		boolean ret = parseArgs(args);
		if(!ret) return;
		
		//生成柜员账户
		JSONObject account = c.generateAddress();
		System.out.println("创建账户成功！");
		
		//连接
		c.connect("ws://" + ip + ":5008");
		
		//操作者为根账户
		c.as(root_address,root_secret);
		
		//转账激活账户
		ret = activateAccount(account);
		if(!ret) {
			c.disconnect();
			return;
		}
		System.out.println("激活账户成功！");
		
		//给用户授权
		ret = grantTable(root_address,account);
		if(!ret){
			c.disconnect();
			return;
		}
		System.out.println("账户授权成功！");
		
		//绑定账户与cid，在t_account表中插入记录
		ret = bindCid(cid,account);
		if(!ret){
			c.disconnect();
			return;
		}
		System.out.println("账户与柜员号绑定成功！");
		c.disconnect();
	}
	
	private static boolean grantTable(String rootAddress,JSONObject account){
		JSONObject account_txs = c.getTransactions(rootAddress);
		JSONArray txs = account_txs.getJSONArray("transactions");
		
		List<String> listTables = new ArrayList<String>();
		for(int i=0; i<txs.length(); i++){
			JSONObject tx = txs.getJSONObject(i).getJSONObject("tx");
			if(tx.getString("TransactionType") == "SQLTransaction"){
				JSONArray obj = tx.getJSONArray("Statements");
				JSONObject first = obj.getJSONObject(0);
				if(first.getInt("OpType") != 1 || !getTableName(first).equals("t_account")){
					System.out.println("获取到的初始化交易有误！！！");
					return false;
				}
				for(int j=1; j<obj.length(); j++){
					JSONObject tx2 = obj.getJSONObject(j);
					listTables.add(getTableName(tx2));
				}
				break;
			}
		}
		String auth = "{insert:true,update:true,delete:true,select:true}";
		for(int i=0; i<listTables.size(); i++){
			JSONObject obj = c.grant(listTables.get(i), account.getString("accont_id"),auth).submit(SyncCond.db_success);
			if(!obj.getString("status").equals("success")){
				System.out.println(obj.toString());
				return false;
			}
			System.out.println(obj.toString());
		}
		
		return true;
	}
	
	private static String getTableName(JSONObject tx){
		return tx.getJSONArray("Tables").getJSONObject(0).getJSONObject("Table").getString("TableName");
	}
	
	private static boolean activateAccount(JSONObject account){
		JSONObject ret = c.pay(account.getString("account_id"), "100000");
		System.out.println(ret.toString());
		if(!ret.getString("status").equals("success")){
			System.out.println(ret.toString());
			return false;
		}
		System.out.println("等待账户被激活...");
		Helper.wait(3000);
		return true;
	}
	
	private static boolean bindCid(String cid,JSONObject account){
		String accountId = account.getString("account_id");
		String secret = account.getString("secret");
		String strIns = "{'cid':" + cid + ",'account_id':" + accountId + ",'secret':" + secret + "}";
		List<String> orgs = Util.array(strIns);
		JSONObject obj;
		obj = c.table("t_account").insert(orgs).submit(SyncCond.db_success);
		if(obj.getString("status").equals("success")){
			return true;
		}else{
			System.out.println(obj);
			return false;
		}
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
		
		if(!Helper.isIpv4(args[0])){
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
		
		ip = args[0];
		cid = args[1];
		root_address = args[2];
		root_secret = args[3];
		
		return true;
	}
	
	private static void printHelpMessages(){
		System.out.println("运行格式：");
		System.out.println("activate ip tellerId address secret");
		System.out.println("eg: activate 192.168.0.101 N001 rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("\t-h\t\t帮助");		
		System.out.println("\tip\t\t要连接的chainSql节点ip地址，节点要求配置数据库");
		System.out.println("\ttellerId\t柜员标识");
		System.out.println("\taddress\t\t根账户地址");
		System.out.println("\tsecret\t\t根账户私钥");
	}
}
