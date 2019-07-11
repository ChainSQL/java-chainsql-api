package com.peersafe.example.chainsql;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.pool.ChainsqlPool;
import com.peersafe.chainsql.pool.ChainsqlUnit;
import com.peersafe.chainsql.util.Util;

class PublicVar{
	public static  int AccountCount = 30;
	public static int ThreadCount = 10;
	public static String mTableName = "testDDD";
	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	public static String wsUrl = "ws://127.0.0.1:6006";
}

/**
 * 
 * @author Jerry
 * 调用方法：
 * 	第一步：先激活一批账户及初始化表
 * 	第二步：多线程循环获取Chainsql对象并向表中插入数据
 * 注：
 * 	获取到ChainsqlUnit，用完后要调用 unlock 方法释放资源
 */
public class TestChainsqlPool {
	public static List<Account> mAccountList = new ArrayList<Account>();

	public static void main(String[] args) {
		initAccountList();

		System.out.println("开始时间：" + (new Date()).toString());
		//初始化表及激活账户
//		initEnv();
		
		//测试ChainsqlPool
		testPool();
	}
	
	public static void testPool() {
		ChainsqlPool.instance().init(PublicVar.wsUrl,10);

		System.out.println("开始时间：" + (new Date()).toString());
		for(int i=0; i<PublicVar.ThreadCount; i++) {
			new Thread(new InsertThread(i,mAccountList)).start();
		}
	}

	public static void initEnv() {
		Chainsql c = new Chainsql();
		c.connect(PublicVar.wsUrl);
		c.connection.client.logger.setLevel(Level.SEVERE);
		c.as(PublicVar.rootAddress, PublicVar.rootSecret);
		//激活
		for(int i=0; i<mAccountList.size(); i++) {
			JSONObject obj = c.pay(mAccountList.get(i).address, "2000").submit();
			System.out.println("activate result:" +obj);
		}
		//建表
		List<String> args = Util.array(	"{'field':'id','type':'int','length':11,'NN':1}",
										"{'field':'name','type':'varchar','length':50,'default':null}", 
										"{'field':'age','type':'int'}");
		JSONObject obj = c.createTable(PublicVar.mTableName, args).submit(SyncCond.validate_success);
		System.out.println("create table result:" + obj);
		
		//授权
		obj = c.grant(PublicVar.mTableName, "zzzzzzzzzzzzzzzzzzzzBZbvji", "{insert:true,select:true}").submit(SyncCond.validate_success);
		System.out.println("grant table result:" + obj);
	}
	
	public static void initAccountList() {
		//30 个账户
				mAccountList.add(new Account("z3YsJtfssndPP5goGk8M8kwGS97HKfc7o3","xx699r4DY63RDmYiA8SGdTx5jkLHB"));
				mAccountList.add(new Account("zwPTrmB5XQCYtJZuDMaE8QEVnaYuguTMbz","xnooj9uAJeuF13wVKsLN1iCVwSuXm"));
				mAccountList.add(new Account("z9ys9JGiTHDuChojUWrLZKNXAdDiAF3SkL","xxheeL4ao4GXAQZ2H8FP1C2WfAsZd"));
				mAccountList.add(new Account("zhtcBFqucvcZ9HdF9AZLkdB8x3a1qadAN3","xhJXp81KdvHxQhNpXyKDME5tDSb91"));
				mAccountList.add(new Account("zHRzYZnPW6BtqYxUZGUkdYkxBntnsYxwcv","xxqCLBmxRd279e3mtA9kJkJ5afNZG"));
				mAccountList.add(new Account("zc4ozg92fkcr7p36QeLhpCX1BAxt1rzAAz","xx1yHc5ENvWhmjSUTeRAyzUmYbJPD"));
				mAccountList.add(new Account("zzpjAfc7rneXXHsDnfGscqnr7QLquRhX1T","xhk3ghW74qhZ7pXGpD24EsBkCtuWT"));
				mAccountList.add(new Account("zKqMDdi5aWJ2Z8z3C2x5LAp4hbmCKNz7Qd","xp6MDkBui29TMtBSn4gmABnHREQoS"));
				mAccountList.add(new Account("zErvwsAUf2kCNZNkMoxWdUdAeXnyTTQm9s","xhgaKSTADjdif6NgPPgPyx4BwVibM"));
				mAccountList.add(new Account("zNFTUNaNeydFosN9fXHuiF6c4NJGGRygqc","xxPLm9K9zmDPRxB17WCzyfnanbsuP"));
				mAccountList.add(new Account("zpwT3xjknxnSm6FH7aNg5bNfmMpV2BwBJV","xnsce45Za89Dm3NBxFq7PfDoFBwRp"));
				mAccountList.add(new Account("zns9ZDWtyaLCxPZBdqKcBcamKFHfVzUzXh","xxVXnwsSQxqwpNtune95DXKaCzWch"));
				mAccountList.add(new Account("zGc7QJTfGEfGERwLiijxuKX9CcVTWLS9oG","xxLyB72t1LRsLZrjAvDWfZdnfcdSY"));
				mAccountList.add(new Account("zMH4vg6WVDfKvNZKYhRQ3rCtUbA6C4joKs","xnZDwFG5nuBJAWjHEyizRhm1Fpjw6"));
				mAccountList.add(new Account("zMTLt9P6f1veyignrucrbbXJHsN44F9ePD","xxfPXwbe6oYsuFAzgGWVzcHwY3GPw"));
				mAccountList.add(new Account("zBBSudupa9ndqAdjd5ARobXZ4HNT8b6AwP","xnAJyPRuNvdX3XJpARXHLnrwxke7D"));
				mAccountList.add(new Account("zLtu2Cj8akpDJ377nHNNPtCndkASS1B4Mo","xhhCUuUeNw53zp6B6SYRtravyYQfz"));
				mAccountList.add(new Account("zcnQ9D7AGtovHDypuRH6X5WM63a6A8Kero","xnhJSnvb61NZAipoeK3RrtG1B2Ha1"));
				mAccountList.add(new Account("zwnj33VGZj3YhYrhnSsiK4fLffAS8GiNbq","xxJqMdKwPyjEuhcAyMjmg6YAuRsK8"));
				mAccountList.add(new Account("zH2oQhYGq43Rbr2AXHHRSMFXPcJ3r7EQAZ","xhY2mVPDAq2YKzNMgWiWdHAHmuiru"));
				mAccountList.add(new Account("z4FAta3ys4Tv9u2AzCoSQ5eEjjfh1mc5z8","xxB1VtZrPQuWbprqqhvYQuSPHAQk1"));
				mAccountList.add(new Account("zKP8WRxpJwMAqTWiPxJWRW8vex6DFfJMdX","xcE4xfx6AqAcUpspuL1mdUhKRxr2E"));
				mAccountList.add(new Account("z9Wm5e16vQDD5akUoJFXKqRHseKQVGRkKS","xxM1gGBSbUD2cTxdiJyV2NHp3nPhR"));
				mAccountList.add(new Account("zpipRJU95JFFVtDRStRKuo9818z5VZw8vR","xndif8oZdTVAHYXLmrYp9S8pW18Zr"));
				mAccountList.add(new Account("zcR5PpkCXRihupzy4N7tbn6YDRT3jdf1Uk","xnwiBDEwJQF3FGc6cimdVVCAdp1oK"));
				mAccountList.add(new Account("z3RjkS8S5xHu2uaUsBtJ7J7dDcjuaprSZS","xxPCP4ZvU7MXwuCMnv8o7Sw6DvYxX"));
				mAccountList.add(new Account("zJy6PaGrJDuJBjUU3YFHUbcxa8SdfpreVd","xhAUi1znPYQfTStq5x6vCqtvtpPrB"));
				mAccountList.add(new Account("zGEfgX8U1qWyEtBYLBWZ4hdBC7ioPv22yq","xhE9R2TjFXVS3gkCpvmN9yTbjqzgj"));
				mAccountList.add(new Account("zEVuZQtP2zWNMwPLcUjuyDPv9SyGC3Unk1","xnJEtyu4KPNRsNd9A9FmRLhCyWGCt"));
				mAccountList.add(new Account("zLfMaKdZkq8sNuQfPP8Sa3MvhV48xspvx6","xxJymAugi4aa6YUg8Nc47fNGR7BE1"));
	}
}

class Account{
	public String address;
	public String secret;
	public Account(String addr,String secret) {
		this.address = addr;
		this.secret = secret;
	}
}

class InsertThread implements Runnable{
	private int number = 0;
	private List<Account> accountList;
	public InsertThread(int i,List<Account> accountList) {
		this.number = i;
		this.accountList = accountList;
	}
	//重写run()方法
	public void run()
	{
		//获取当前线程的名字
//		System.out.println("当前线程："+Thread.currentThread().getName());
		int countInThread = accountList.size() / PublicVar.ThreadCount;
		for(int i=0;i<countInThread;i++)
		{
			int index = number*countInThread + i;
			Account a = accountList.get(index);
			ChainsqlUnit unit = ChainsqlPool.instance().getChainsqlUnit();
			Chainsql c = unit.getChainsql();
			
			c.as(a.address, a.secret);
			c.use(PublicVar.rootAddress);
			JSONObject obj = c.table(PublicVar.mTableName).insert(Util.array("{'id':" + index + ",'age': 333,'name':'hello'}")).submit();
			
			unit.unlock();
			System.out.println("线程 " + number + ",第 " + index + " 个插入结果 ：" + obj + "，时间：" + (new Date()).toString());
		}
	}
}
