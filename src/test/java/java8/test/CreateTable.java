package java8.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;

public class CreateTable {

	private static final Chainsql c = Chainsql.c;
	private static String Address = null;
	private static String Secret = null;
	private static String IpPort = null;
	private static JSONArray tables = null;
	private static String DefaultPort = "6006";
	public static void main(String[] args) {
		parseArgs(args);
		if(IpPort == null || Address == null || Secret == null || tables == null || tables.length() == 0 ){
			return;
		}
		
		c.connect("ws://" + IpPort);
		
		//设置日志级别
		c.connection.client.logger.setLevel(Level.SEVERE);
		c.as(Address, Secret);
		
		//建表
		boolean ret = createTable(Address,tables);
		
		//建表不成功，将钱打回原始根账户
		if(ret){
			System.out.println("全部执行成功，程序将退出！");
		}
		c.disconnect();
	}
	
	private static boolean createTable(String address,JSONArray tables){
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
					System.out.println("表" + tableName + "创建成功！");
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
		if(args.length == 0){  
	        if (!hasFile) {  
	        	createJSONFile();
	        	System.out.println("已生成data.json文件，请配置之后重新执行初始化程序");
	        }
	        printHelpMessages();
		}else if(args.length == 1){
			if(!args[0].equals("-h")){
				System.out.println("参数个数不正确");
			}
			printHelpMessages();
		}else if(args.length == 3){
			if(!hasFile){
				createJSONFile();
	        	System.out.println("已生成data.json文件，请配置之后重新执行初始化程序");
	        	return;
			}
			String str = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh";
			if(args[1].length() != str.length() || args[1].charAt(0) != 'r'){
				System.out.println("根账户地址无效");
				printHelpMessages();
				return;
			}
			str = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";
			if(args[2].length() != str.length() || args[2].charAt(0) != 's'){
				System.out.println("根账户私钥无效");
				printHelpMessages();
				return;
			}
			
			String arg = args[0];
			if(arg.contains(":")){
				IpPort = args[0]; 
			}else if(Helper.isIpv4(args[0])){
				IpPort = args[0] + ":" + DefaultPort;
			}else{
				return;
			}
			
			Address = args[1];
			Secret = args[2];
			tables = getTables();
		}else{
			System.out.println("参数个数不正确");
		}
	}
	
	private static void createJSONFile(){
		String fileName = "data.json";

    	writeDataJson(fileName);
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
	    	String strContent = Helper.readFile("/config/data.json");
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
		System.out.println("CreateTable ip:port address secret");
		System.out.println("\t-h\t\t帮助");
		System.out.println("\tip:port\t\t初始化操作要连接的chainsql节点ip及端口号(默认为6006)，节点要求配置数据库");
		System.out.println("\taddress\t\t根账户地址");
		System.out.println("\tsecret\t\t根账户私钥");
		System.out.println("\t\t\t注：同级目录下不存在data.json文件时，运行会自动生成data.json配置文件\n");
		System.out.println("data.json Format：");
		printExplainInfo();
	}
}