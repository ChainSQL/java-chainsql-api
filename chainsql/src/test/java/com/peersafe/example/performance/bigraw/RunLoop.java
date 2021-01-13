package com.peersafe.example.performance.bigraw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import org.w3c.dom.css.Counter;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.pool.ChainsqlPool;
import com.peersafe.chainsql.pool.ChainsqlUnit;
import com.peersafe.chainsql.util.Util;
import com.peersafe.example.performance.tps.HttpRequest;

class PublicVars {
	public static int AccountCount = 50;
	public static int ThreadCount = 50;
	public static int PoolSize = 50;
	public static String TableName = "tTable522";
	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	public static String WsUrl = "ws://192.168.29.69:6005";
	public static String Data = "";
	public static boolean NeedInitEnv = true;
	public static String NameInDB = "";
	public static boolean GM = false;
	public static int SuccessCount = 0;
	public static int TotalCount = 0;
}

/**
 * 
 * @author Jerry 调用方法： 第一步：先激活一批账户及初始化表 第二步：多线程循环获取Chainsql对象并向表中插入数据 注：
 *         获取到ChainsqlUnit，用完后要调用 unlock 方法释放资源
 */
public class RunLoop {
	public static List<Account2> mAccountList = new ArrayList<Account2>();

	public static void main(String[] args) {
		parseArgs(args);

		initAccountList();

		// 初始化表及激活账户
		if(PublicVars.NeedInitEnv) {
			initEnv();
		}			

		// 测试ChainsqlPool
		testPool();
	}

	public static void parseArgs(String[] args) {
		if (args.length != 7) {
			System.out.println("参数错误,length="+args.length);
			return;
		}
		PublicVars.WsUrl = args[0];
		PublicVars.NeedInitEnv = Boolean.parseBoolean(args[1]);
		int cocurrentCount = Integer.parseInt(args[2]);
		PublicVars.AccountCount = cocurrentCount;
		PublicVars.PoolSize = cocurrentCount;
		PublicVars.ThreadCount = cocurrentCount;
		PublicVars.GM = Boolean.parseBoolean(args[3]);
		PublicVars.TableName = args[4];
		PublicVars.NameInDB = args[5];
		
		if(PublicVars.GM) {
			PublicVars.rootAddress = "zN7TwUjJ899xcvNXZkNJ8eFFv2VLKdESsj";
			PublicVars.rootSecret = "p97evg5Rht7ZB7DbEpVqmV3yiSBMxR3pRBKJyLcRWt7SL5gEeBb";
		}
		
		PublicVars.Data = readFileContent(args[6]);
	}
	
	
	public static String readFileContent(String fileName) {
	    File file = new File(fileName);
	    BufferedReader reader = null;
	    StringBuffer sbf = new StringBuffer();
	    try {
	        reader = new BufferedReader(new FileReader(file));
	        String tempStr;
	        while ((tempStr = reader.readLine()) != null) {
	            sbf.append(tempStr);
	        }
	        reader.close();
	        return sbf.toString();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException e1) {
	                e1.printStackTrace();
	            }
	        }
	    }
	    return sbf.toString();
	}


	public static void testPool() {
		ChainsqlPool.instance().init(PublicVars.WsUrl, PublicVars.PoolSize);

		long startTime = System.currentTimeMillis(); 
		System.out.println("开始时间：" + (new Date()).toString());
		List<Thread> thList = new ArrayList<Thread>();
		for (int i = 0; i < PublicVars.ThreadCount; i++) {
			thList.add(new Thread(new InsertThread2(i, mAccountList)));
		}
		for(int i=0; i<PublicVars.ThreadCount; i++) {
			thList.get(i).start();
		}
		for(int i=0; i<PublicVars.ThreadCount; i++) {
			try {
				thList.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("结束时间：" + (new Date()).toString() + ",总用时:" + (System.currentTimeMillis() - startTime) + 
				",\n平均响应时间：" + InsertThread2.counter.TimeUsedMs / InsertThread2.counter.Total + "ms" +
				",\n最小响应时间：" + InsertThread2.counter.MinTimeUsedMs +"ms" + 
				",\n最大响应时间：" + InsertThread2.counter.MaxTimeUsedMs +"ms");
	}

	public static void initEnv() {
		Chainsql c = new Chainsql();
		c.connect(PublicVars.WsUrl);
		c.connection.client.logger.setLevel(Level.SEVERE);
		c.as(PublicVars.rootAddress, PublicVars.rootSecret);
		// 激活
		for (int i = 0; i < mAccountList.size(); i++) {
			JSONObject obj = c.pay(mAccountList.get(i).address, "200000").submit(SyncCond.send_success);
			System.out.println("activate result:" + obj);
		}

		c.disconnect();
//		Chainsql.shutdown();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void initAccountList() {
		if (PublicVars.NeedInitEnv) {
			Chainsql c = new Chainsql();
			JSONObject options = new JSONObject();
			if(PublicVars.GM) {
				options.put("algorithm","softGMAlg");
			}
			
			JSONObject list = new JSONObject();

			for (int i = 0; i < PublicVars.AccountCount; i++) {
				JSONObject obj = c.generateAddress(options);
				mAccountList.add(new Account2(obj.getString("address"), obj.getString("secret"),obj.getString("publicKey")));
				list.put(obj.getString("address"), obj);
			}
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("account.data"));
				out.writeObject(list.toString());
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
					mAccountList.add(new Account2(key,act.getString("secret"),act.getString("publicKey")));
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

class Account2 {
	public String address;
	public String secret;
	public String publicKey;

	public Account2(String addr, String secret,String publicKey) {
		this.address = addr;
		this.secret = secret;
		this.publicKey = publicKey;
	}
}

class Count{
	public int Total = 0;
	public int Success = 0;
	public int TimeUsedMs = 0;
	public long MaxTimeUsedMs = 0;
	public long MinTimeUsedMs = 2000;
}

class InsertThread2 implements Runnable {
	private int number = 0;
	private List<Account2> accountList;
	public static Count counter = new Count();
	public InsertThread2(int i, List<Account2> mAccountList) {
		this.number = i;
		this.accountList = mAccountList;
	}
	// 重写run()方法
	public void run() {
		// 获取当前线程的名字
		ChainsqlUnit unit = ChainsqlPool.instance().getChainsqlUnit();
		Chainsql c = unit.getChainsql();
		int index = this.number;
		Account2 a = accountList.get(index);
		c.as(a.address, a.secret);
		c.use(PublicVars.rootAddress);
		JSONObject opt = new JSONObject();
		opt.put("nameInDB",PublicVars.NameInDB);
		opt.put("confidential",false);
		for(int i=0; i<50; i++) {
			JSONObject obj = new JSONObject();
			long start = System.currentTimeMillis(); 
			obj = c.table(PublicVars.TableName)
					.tableSet(opt)
					.insert(Util.array("{'id':" + index + ",'data':'" + PublicVars.Data + "'}")).submit(SyncCond.db_success);
			long timeUsed = System.currentTimeMillis() - start;
			synchronized(counter) {
				counter.TimeUsedMs += timeUsed;
				counter.Total++;
				if(timeUsed > counter.MaxTimeUsedMs)
					counter.MaxTimeUsedMs = timeUsed;
				if(timeUsed < counter.MinTimeUsedMs)
					counter.MinTimeUsedMs = timeUsed;
				if(obj.has("status") && obj.getString("status").equals("db_success"))
					counter.Success++;
				else {
					System.out.println(obj);
					if(obj.has("error") && obj.getString("error").equals("tefMAX_LEDGER")) {
						System.out.println("Thread number:" + this.number+",serverInfo.primed=" + c.connection.client.serverInfo.primed());
						System.out.println("We ar on " + c.connection.client.serverInfo.ledger_index + " and they are on " + c.getLedgerVersion().getInt("ledger_current_index"));
					}
				}
				
				if(counter.Total % PublicVars.PoolSize == 0) {
					System.out.println("已完成 ：" + counter.Total + "成功数：" + counter.Success);
				}
			}			
		}
		unit.unlock();
	}
//	public void run() {
//		while(true) {
//			int index = this.number;
//			Account2 a = accountList.get(index);	
//			JSONObject jsonData = PublicVars.TxType == 0 ? 
//					getSendData(a.address,a.secret,a.publicKey):
//					getSendDataPayment(a.address,a.secret,a.publicKey);
//			String sr=HttpRequest.sendPost(PublicVars.HttpUrl, jsonData.toString());
//			JSONObject res = new JSONObject(sr);
//			if(!res.has("result") || !res.getJSONObject("result").has("engine_result")
//					|| (!res.getJSONObject("result").getString("engine_result").equals("tesSUCCESS") &&
//							!res.getJSONObject("result").getString("engine_result").equals("terPRE_SEQ"))) {
//				if(res.getJSONObject("result").has("error_message"))
//					System.out.println(res.getJSONObject("result").getString("error_message"));
//				else
//					System.out.println(sr);
//			}
//			try {
//				Thread.sleep(PublicVars.SleepCount);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}		
//   	}
	   
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
//				String sr=HttpRequest.sendPost(PublicVars.HttpUrl, mData.toString());
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
