package java8.test;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;

public class AddPointMain {
	private static final Chainsql c = Chainsql.c;
	private static String ip = "";
	private static String root_address = "";
	private static String root_secret = "";
	
	public static void main(String[] args){
		boolean ret = parseArgs(args);
		if(!ret) return;
		//生成根账户
		JSONObject account = c.generateAddress();
		
		//连接
		c.connect("ws://" + ip + ":5008");
		
		//操作者为根账户
		c.as(root_address,root_secret);
		
		//转账激活根账户
		ret = activateAccount(account);
		if(ret){
			System.out.println("账户生成并激活成功");
			System.out.println("账户地址："+account.getString("account_id"));
			System.out.println("账户私钥："+account.getString("secret"));
		}
	}
	
	private static boolean activateAccount(JSONObject account){
		JSONObject ret = c.pay(account.getString("account_id"), "100000");
		if(!ret.getString("status").equals("success")){
			System.out.println(ret.toString());
			return false;
		}
		return true;
	}
	
	private static boolean parseArgs(String[] args){
		if(args.length == 1 && args[0].equals("-h")){
			printHelpMessages();
			return false;
		}else if(args.length != 3){
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
		if(args[1].length() != str.length() || args[1].charAt(0) != 'r'){
			System.out.println("根账户地址无效");
			printHelpMessages();
			return false;
		}
		str = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";
		if(args[2].length() != str.length() || args[2].charAt(0) != 's'){
			System.out.println("根账户私钥无效");
			printHelpMessages();
			return false;
		}
		
		ip = args[0];
		root_address = args[1];
		root_secret = args[2];
		
		return true;
	}
	
	private static void printHelpMessages(){
		System.out.println("运行格式：");
		System.out.println("AddPoint ip address secret");
		System.out.println("eg: activate 192.168.0.101 rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		System.out.println("\t-h\t\t帮助");		
		System.out.println("\tip\t\t要连接的chainSql节点ip地址");
		System.out.println("\taddress\t\t根账户地址");
		System.out.println("\tsecret\t\t根账户私钥");
	}
}
