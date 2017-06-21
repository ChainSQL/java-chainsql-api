package java8.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.Util;


public class InitMain {
	private static final Chainsql c = Chainsql.c;
	private static String ROOT_ADDRESS = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh";
	private static String ROOT_SECRET = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";
	private static String Ip = "";
	private static JSONArray tables = null;
	public static void main(String[] args) {
		parseArgs(args);
		if(Ip == null || tables == null || tables.length() == 0 ){
			return;
		}
		
		c.connect("ws://" + Ip + ":5008");
//		c.connect("ws://192.168.0.221:6006");
		
		//插入自定义柜员-账户表
		tables = insertAccountTable(tables);
		
		//生成根账户
		JSONObject account = c.generateAddress();
		System.out.println("创建账户成功！");
		
		//转账激活根账户
		boolean ret = activateAccount(account);
		if(!ret){
			c.disconnect();
			return;
		}
		
		//建表
		ret = createTable(account,tables);
		
		//建表不成功，将钱打回原始根账户
		if(!ret){
			payBackMoney(account);
			//c.disconnect();
		}else{
			System.out.println("表初始化完成！");
			System.out.println("根账户地址："+account.getString("account_id"));
			System.out.println("根账户私钥："+account.getString("secret"));
		}
	}
	
	private static JSONArray insertAccountTable(JSONArray tables){
		JSONArray newTables = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("TableName", "t_account");
		JSONArray raw = new JSONArray();
		raw.put(Util.StrToJson("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}"));
		raw.put(Util.StrToJson("{'field':'cid','type':'varchar','length':20,'NN':1,'UQ':1}"));
		raw.put(Util.StrToJson("{'field':'account_id','type':'varchar','length':100}"));
		raw.put(Util.StrToJson("{'field':'secret','type':'varchar','length':50}"));
		obj.put("Raw", raw);
		newTables.put(obj);
		for(int i=0; i<tables.length(); i++){
			newTables.put(tables.getJSONObject(i));
		}
		return newTables;
	}
	
	private static String getPayAmount(String amount){
		BigInteger bal = new BigInteger(amount);
		bal = bal.subtract(BigInteger.valueOf(300));
		System.out.print(bal.toString());
		return bal.toString();
	}
	private static void payBackMoney(JSONObject account){
		c.as(account.getString("account_id"),account.getString("secret"));
		String balance = c.getAccountBalance(account.getString("account_id"));
		if(balance != null){
			c.pay(ROOT_ADDRESS, getPayAmount(balance));
		}
	}
	
	private static boolean activateAccount(JSONObject account){
		c.as(ROOT_ADDRESS, ROOT_SECRET);
		String balance = c.getAccountBalance(ROOT_ADDRESS);
		if(balance == null){
			return false;
		}
		System.out.println("等待账户激活");
		JSONObject ret = c.pay(account.getString("account_id"), getPayAmount(balance));
		if(!ret.getString("status").equals("success")){
			System.out.println(ret.toString());
			return false;
		}else{
			System.out.println("等待账户被激活...");
			Helper.wait(3000);
			System.out.println("账户激活成功！");
			return true;
		}
	}
	
	private static boolean createTable(JSONObject account,JSONArray tables){
		c.as(account.getString("account_id"),account.getString("secret"));
		System.out.println("开始初始化表");
		try{
//			c.beginTran();

			for(int i=0; i<tables.length(); i++){
				JSONObject table = tables.getJSONObject(i);
				String tableName = table.getString("TableName");
				JSONArray rawArray = table.getJSONArray("Raw");
				List<String> rawList = new ArrayList<String>();
				for(int j=0; j<rawArray.length(); j++){
					rawList.add(rawArray.getJSONObject(j).toString());
				}
				JSONObject obj = c.createTable(tableName,rawList).submit(SyncCond.db_success);
				if(obj.getString("status").equals("success")){
					System.out.println("表" + tableName + "初始化成功！");
				}else{
					System.out.println(obj);
					return false;
				}
			}		  
			return true;
//			JSONObject obj = c.commit(SyncCond.db_success);
//			if(obj.getString("status").equals("success")){
//				System.out.println("表初始化成功！");
//				return true;
//			}else{
//				System.out.println(obj);
//				return false;
//			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

	}
	private static void parseArgs(String[] args){
		String fileName = "data.json";
		File file = new File(fileName); 
		boolean hasFile = file.exists();
        if (!hasFile) {  
        	createJSONFile();
        	System.out.println("已生成data.json文件，请配置之后重新执行初始化程序");
        }
		if(args.length == 0){  
			printHelpMessages();
		}else if(args.length == 1){
			if(args[0].equals("-h")){
				printHelpMessages();
			}else{
				if(hasFile && Helper.isIpv4(args[0])){
					Ip = args[0];
					tables = getTables();
				}
			}
		}else{
			System.out.println("参数个数不正确");
		}
	}
	
	private static void createJSONFile(){
		String fileName = "data.json";

    	writeDataJson(fileName);
		System.out.println("已生成data.json文件，请配置之后重新执行初始化程序");
		printHelpMessages();
	}
	
	private static JSONArray getTables(){
		String fileName = "data.json";
		File file = new File(fileName);  
        if (!file.exists()) {  
        	writeDataJson(fileName);
			System.out.println("已生成data.json文件，请配置之后重新执行初始化程序");
			printHelpMessages();
            return null;  
        }
        
		String content = Helper.readFile(fileName);
		JSONObject json = new JSONObject(content);
		if(json.has("Tables")){
			JSONArray tables = json.getJSONArray("Tables");
			return tables;
		}else{
			System.out.println("文件" + fileName + "配置错误，未找到Tables字段");
			return null;
		}
	}
    
	private static void writeDataJson(String fileName){
		try {
			FileOutputStream oFile = new FileOutputStream(fileName, false);//true表示追加打开   
	    	String strContent = Helper.readFile("config/data.json");
	        oFile.write(strContent.getBytes());
			oFile.close();
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void printExplainInfo(){
		System.out.println("\tTables:\t\t" + "需要创建表的信息。可以指定多个表");
		System.out.println("\tTablename:\t" + "表名");
		System.out.println("\tfield:\t\t" + "表字段名");
		System.out.println("\ttype:\t\t" + "字段名类型，支持int/float/double/decimal/varchar/blob/text/datetime");
		System.out.println("\tlength:\t\t" + "字段值的字节长度");
		System.out.println("\tPK:\t\t" + "值为1表示字段文件主键");
		System.out.println("\tNN:\t\t" + "值为1表示值不能为NOT NULL");
		System.out.println("\tUQ:\t\t" + "值为1表示值唯一");
		System.out.println("\tAI:\t\t" + "值为1表示值有自增特性");
		System.out.println("\tFK:\t\t" + "值为1表示字段为某表的外键，必须配合REFERENCES使用");
		System.out.println("\tREFERENCES:\t" + "值的格式为 {'table':'user','field':'id'}");
	}
	
	private static void printHelpMessages(){
		System.out.println("Initialize ip");
		System.out.println("\t-h\t\t帮助");
		System.out.println("\tip\t\t初始化操作要连接的chainsql节点ip，节点要求配置数据库\n");
		System.out.println("data.json配置格式如下：");
		printExplainInfo();
	}
}
