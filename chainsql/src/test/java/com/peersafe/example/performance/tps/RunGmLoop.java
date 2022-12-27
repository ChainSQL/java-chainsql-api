package com.peersafe.example.performance.tps;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.pool.ChainsqlPool;
import com.peersafe.chainsql.pool.ChainsqlUnit;
import com.peersafe.chainsql.util.Util;

class PublicVar {
	public static int AccountCount = 50;
	public static int ThreadCount = 50;
	public static int PoolSize = 50;
	public static String mTableName = "test_gm3";
	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	public static String WsUrl = "ws://192.168.29.69:6005";
	public static String HttpUrl = "";
	public static boolean NeedInitEnv = true;
	public static int SleepCount = 1000;
	public static String NameInDB = "";
	public static boolean GM = false;
	public static int TxType = 0;
	public static String Data = "";
	public static int DataSizeKb;
}

/**
 * 
	示例：
	java -jar TestGmLoop.jar ws://106.14.123.158:6006 http://106.14.123.158:5005 true 100 0 true 1
	
	参数说明 ：
	1. ws地址
	2. http地址
	3. 是否要初始化环境（建表，授权，激活账户） 
	4. 并发数（线程数、账户数） 
	5. 发送完一笔交易等等多久
	6. 是否国密算法
	7. 交易类型：0-insert 1-payment
	8. 插入数据 大小（kb）
 */
public class RunGmLoop {
	public static List<Account> mAccountList = new ArrayList<Account>();

	public static void main(String[] args) {
		parseArgs(args);

		initAccountList();

		// 初始化表及激活账户
		if(PublicVar.NeedInitEnv) {
			initEnv();
		}			

		// 测试ChainsqlPool
		testPool();
	}

	public static void parseArgs(String[] args) {
		if (args.length != 8) {
			System.out.println("参数错误,length="+args.length);
			return;
		}
		PublicVar.WsUrl = args[0];
		PublicVar.HttpUrl = args[1];
		PublicVar.NeedInitEnv = Boolean.parseBoolean(args[2]);
		int cocurrentCount = Integer.parseInt(args[3]);
		PublicVar.AccountCount = cocurrentCount;
		PublicVar.PoolSize = cocurrentCount;
		PublicVar.ThreadCount = cocurrentCount;
		PublicVar.SleepCount = Integer.parseInt(args[4]);
		PublicVar.GM = Boolean.parseBoolean(args[5]);
		PublicVar.TxType = Integer.parseInt(args[6]);
		
		if(PublicVar.GM) {
			PublicVar.rootAddress = "zN7TwUjJ899xcvNXZkNJ8eFFv2VLKdESsj";
			PublicVar.rootSecret = "p97evg5Rht7ZB7DbEpVqmV3yiSBMxR3pRBKJyLcRWt7SL5gEeBb";
		}
		PublicVar.DataSizeKb = Integer.parseInt(args[7]);
		PublicVar.Data = constructContent(PublicVar.DataSizeKb);
	}

	public static String constructContent(int contentSizeKb) {
	    String strRet = "";
	    String strKb = "7b224845414444415441223a7b22444154415f434e223a22222c22444154415f4b4559223a2261356331393363612d373066632d343137342d626234652d633238346237623264616161222c22444154415f4e4f223a22353034222c2254494d455354414d50223a313630363238353635383137332c2256455253494f4e223a2232222c2246524f4d504c4154464f524d434f4445223a2242616948616e54657374303031222c22544f504c4154464f524d434f4445223a224333323035383230303031222c22444154415f54595045223a22222c22444154415f454e223a22227d2c22444553434f4e54455854223a7b22554e49464945445f4445414c5f434f444553223a2236386133343735662d353264652d346135662d613366382d336433323263393064343039222c224e4f544943455f434f4e54454e54223a223c7020616c69676e3d5c2263656e7465725c22207374796c653d5c226d617267696e3a203070743b20746578742d616c69676e3a2063656e7465723b206c696e652d6865696768743a20323170743b20666f6e742d66616d696c793a2043616c6962723a2063656e7465723b206c696e652d6865696768743a20323170743b20666f6e742d66616d696c793a2043616c696272742d66616d696c793a2043616c696272742d66616d696c793a2043616c6962726d696c793a2043616c6962726272";
	    for(int i=0; i<contentSizeKb; i++) {
	    	strRet += strKb;
	    }
	    return strRet;
	}
	
	public static void testPool() {
//		ChainsqlPool.instance().init(PublicVar.WsUrl, PublicVar.PoolSize);

		System.out.println("开始时间：" + (new Date()).toString());
		for (int i = 0; i < PublicVar.ThreadCount; i++) {
			new Thread(new InsertThread(i, mAccountList)).start();
		}
	}

	public static void initEnv() {
		Chainsql c = new Chainsql();
		c.connect(PublicVar.WsUrl);
		c.connection.client.logger.setLevel(Level.SEVERE);
		c.as(PublicVar.rootAddress, PublicVar.rootSecret);
		// 激活
		for (int i = 0; i < mAccountList.size(); i++) {
			JSONObject obj = c.pay(mAccountList.get(i).address, "20000000").submit(SyncCond.validate_success);
			System.out.println("activate result:" + obj);
		}
		if(PublicVar.TxType == 1) {
			return;
		}
		// 建表
		List<String> args = Util.array("{'field':'id','type':'int','length':11,'NN':0}",
				"{'field':'name','type':'longtext'}", "{'field':'age','type':'int'}");
		JSONObject obj = c.createTable(PublicVar.mTableName, args).submit(SyncCond.validate_success);
		System.out.println("create table result:" + obj);
		JSONObject ret = c.getTableNameInDB(PublicVar.rootAddress, PublicVar.mTableName);
		PublicVar.NameInDB = ret.getString("nameInDB");
		

		// 授权
		obj = c.grant(PublicVar.mTableName, "zzzzzzzzzzzzzzzzzzzzBZbvji", "{insert:true,select:true}")
				.submit(SyncCond.validate_success);
		System.out.println("grant table result:" + obj);
	}

	public static void initAccountList() {
		if (PublicVar.NeedInitEnv) {
			System.out.println(PublicVar.NeedInitEnv);
			Chainsql c = new Chainsql();
			JSONObject options = new JSONObject();
			if(PublicVar.GM) {
				options.put("algorithm","softGMAlg");
			}
			
			JSONObject list = new JSONObject();

			for (int i = 0; i < PublicVar.AccountCount; i++) {
				JSONObject obj = c.generateAddress(options);
				mAccountList.add(new Account(obj.getString("address"), obj.getString("secret"),obj.getString("publicKey")));
				list.put(obj.getString("address"), obj);
			}
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("account.data"));
				out.writeObject(list.toString());
				System.out.println(list.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new FileInputStream("account.data"));
				String obj = (String) in.readObject();
				JSONObject list = new JSONObject(obj);
				Iterator<String> iter = list.keys();
				while (iter.hasNext()) {
					String key = iter.next();
					JSONObject act = list.getJSONObject(key);
					mAccountList.add(new Account(key,act.getString("secret"),act.getString("publicKey")));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class Account {
	public String address;
	public String secret;
	public String publicKey;

	public Account(String addr, String secret,String publicKey) {
		this.address = addr;
		this.secret = secret;
		this.publicKey = publicKey;
	}
}



class InsertThread implements Runnable {
	private int number = 0;
	private List<Account> accountList;

	public InsertThread(int i, List<Account> accountList) {
		this.number = i;
		this.accountList = accountList;
	}
	   public JSONObject getSendDataPayment(String account,String secret,String publicKey) {
		   JSONObject mData = new JSONObject();
	       try {
	    	   mData.put("method", "submit");
				JSONObject param = new JSONObject();
				param.put("secret", secret);
				param.put("public_key", publicKey);
				JSONObject json = new JSONObject("{ "+
					"'TransactionType': 'Payment',"+
					"'Account': '" + account + "',"+
					"'Destination':'"+ PublicVar.rootAddress + "'," +			   
					"'Fee':'1000',"+
					"'Amount': '1000000'"+
				"}");
				param.put("tx_json", json);
				mData.append("params", param);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		   return mData;
	   }
	   
	   public JSONObject getSendData(String account,String secret,String publicKey) {
		   JSONObject mData = new JSONObject();
	       try {
	    	   mData.put("method", "r_insert");
				JSONObject param = new JSONObject();
				param.put("secret", secret);
				param.put("public_key", publicKey);
				JSONObject json = new JSONObject("{ "+
					"'TransactionType': 'SQLStatement',"+
					"'Account': '" + account + "',"+
					"'Owner': '" + PublicVar.rootAddress + "',"+
					"'Tables':["+
					"	{"+
					"		'Table':{"+
					"				'TableName':'"+ PublicVar.mTableName + "'" +			
					"		}"+
					"	 }"+
					"],"+
				   "'OpType': 6,"+				   
				   "'Raw': ["+
					"	{'id':1,'name':'" + PublicVar.Data + "','age':333}"+
				   "]"+
				"}");
				param.put("tx_json", json);
				mData.append("params", param);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		   return mData;
	   }
	   public JSONObject getSignData(String account,int seq) {
		   JSONObject json = new JSONObject("{ "+
					"'TransactionType': 'SQLStatement',"+
					"'Account': '" + account + "',"+
					"'Owner': '" + PublicVar.rootAddress + "',"+
					"'Tables':["+
					"	{"+
					"		'Table':{"+
					"				'TableName':'"+ Util.toHexString(PublicVar.mTableName) + "'," +
					"				'NameInDB':'" + PublicVar.NameInDB + "'"+		
					"		}"+
					"	 }"+
					"],"+
				   "'OpType': 6,"+				   
				   "'Raw':'5b7b226e616d65223a2268656c6c6f222c22616765223a3132337d5d',"+
				   "'Fee':'60000',"+
				   "'Sequence': " + seq +
				"}");
		   
		   return json;
	   }
//	// 重写run()方法
//	public void run() {
//		// 获取当前线程的名字
//		// System.out.println("当前线程："+Thread.currentThread().getName());
//		// int countInThread = accountList.size() / PublicVar.ThreadCount;
//		while (true) {
//			int index = this.number;
//			Account a = accountList.get(index);
//			ChainsqlUnit unit = ChainsqlPool.instance().getChainsqlUnit();
//			Chainsql c = unit.getChainsql();
//
//			c.as(a.address, a.secret);
//			c.use(PublicVar.rootAddress);
//			JSONObject obj = c.table(PublicVar.mTableName)
//					.insert(Util.array("{'id':" + index + ",'age': 333,'name':'hello'}")).submit();
//
//			unit.unlock();
//			if (!obj.getString("status").equals("success"))
//				System.out
//						.println("线程 " + number + ",第 " + index + " 个插入结果 ：" + obj + "，时间：" + (new Date()).toString());
//			try {
//				Thread.sleep(PublicVar.SleepCount);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	public void run() {
		while(true) {
			int index = this.number;
			Account a = accountList.get(index);	
			JSONObject jsonData = PublicVar.TxType == 0 ? 
					getSendData(a.address,a.secret,a.publicKey):
					getSendDataPayment(a.address,a.secret,a.publicKey);
			String sr=HttpRequest.sendPost(PublicVar.HttpUrl, jsonData.toString());
			JSONObject res = new JSONObject(sr);
			if(!res.has("result") || !res.getJSONObject("result").has("engine_result")
					|| (!res.getJSONObject("result").getString("engine_result").equals("tesSUCCESS") &&
							!res.getJSONObject("result").getString("engine_result").equals("terPRE_SEQ"))) {
				if(res.getJSONObject("result").has("error_message"))
					System.out.println(res.getJSONObject("result").getString("error_message"));
				else
					System.out.println(sr);
			}
			try {
				Thread.sleep(PublicVar.SleepCount);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
   	}
	   
//		public void run() {
//			int seq = 1;
//			while(true) {
//				int index = this.number;
//				Account a = accountList.get(index);	
//				JSONObject jsonData = getSignData(a.address,seq++);
//				JSONObject obj = new JSONObject();
//				obj.put("tx_json", jsonData);
////				System.out.println(obj);
//				Chainsql c = new Chainsql();
//				JSONObject blob = c.sign(obj, a.secret);
//				JSONObject mData = new JSONObject();
//				mData.put("method", "submit");
//				JSONObject param = new JSONObject();
//				param.put("tx_blob", blob.getString("tx_blob"));
//				mData.append("params", param);
//				
//				String sr=HttpRequest.sendPost(PublicVar.HttpUrl, mData.toString());
//				JSONObject res = new JSONObject(sr);
//				if(!res.has("result") || !res.getJSONObject("result").has("engine_result")
//						|| (!res.getJSONObject("result").getString("engine_result").equals("tesSUCCESS") &&
//								!res.getJSONObject("result").getString("engine_result").equals("terPRE_SEQ"))) {
//					if(res.getJSONObject("result").has("error_message"))
//						System.out.println(res.getJSONObject("result").getString("error_message"));
//					else
//						System.out.println(sr);
//				}
//			}		
//	   	}
}
