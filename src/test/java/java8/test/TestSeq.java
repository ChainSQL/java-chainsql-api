package java8.test;

import static java8.example.Print.print;
import static java8.example.Print.printErr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

import com.peersafe.base.client.Account;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.transactions.ManagedTxn;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;
import com.peersafe.base.core.types.known.tx.txns.Payment;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.util.Util;

public class TestSeq {
	public static final Chainsql c = Chainsql.c;
	public static String sTableName,sTableName2,sReName;
	public static String sNewAccountId,sNewSecret;
	private static int mValCount = 0;
	public static void main(String[] args) {
		// c.connect("ws://192.168.0.152:6006");
		//c.connect("ws://192.168.0.14:5082");
//		c.connect("ws://139.198.11.189:6006");
		// c.connect("ws://192.168.0.220:6006");
//		c.connection.client.logger.setLevel(Level.SEVERE);

		//c.as("rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q", "ssnqAfDUjc6Bkevd1Xmz5dJS5yHdz");
//		c.as("rfVLQugNwsn4ToSBksFiQKTJphw2fU9W6Y", "snrnF2RiZWC7DRXQPykXdDHi1RgAb");
		
		
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
        
        System.out.println("before");
		TestSeq test = new TestSeq();
		MyThread th1 = test.new MyThread("rfVLQugNwsn4ToSBksFiQKTJphw2fU9W6Y","snrnF2RiZWC7DRXQPykXdDHi1RgAb","rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q");
		MyThread th2 = test.new MyThread("rfnmWkQ1jpVvWFvw7rnFNt7AUe58rQvMet","snEqBjWd2NWZK3VgiosJbfwCiLPPZ","rPbFRi7KrKBqmFqs9nCCXwNepDWCLPr8bb");
		th1.start();
		System.out.println("after");
		//th2.start();
		testSignPayment();
	}
	
	class MyThread extends Thread{
		private String src;
		private String seed;
		private String dest;
		public MyThread(String src,String seed,String dest) {
			this.seed = seed;
			this.src = src;
			this.dest = dest;
		}
        @Override
        public void run() {
        	c.connect("ws://139.198.11.189:6007");
    		c.event.subTable("hijack", "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q", (data) -> {
    			System.out.println(data);
    		});
            testSeq(src,seed,dest);       
            try {
    			Thread.sleep(100000);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
        }
    }
	
	private void testSeq(String src,String seed,String dest) {
//		Test test = new Test();
//		test.generateAccount();
//		test.activateAccount(sNewAccountId);
		
		//14上起个点连青云，然后构造100个payment，顺序发出去，看最后的成交情况
        Account account = c.connection.client.accountFromSeed(seed);
        TransactionManager tm = account.transactionManager();
        while(!account.getAccountRoot().primed()) {
        	Util.waiting();
        }
        Payment payment = new Payment();

        payment.as(AccountID.Account,     src)
             	.as(AccountID.Destination, dest)
             	.as(Amount.Fee,            "10")
             	.as(Amount.Amount, "10000000");
        //long tm1 = System.currentTimeMillis();
//        for(int i=0; i<1000; i++) {
 //       	final int index = i;
	        tm.queue(tm.manage(payment)
	            .onValidated(this::onValidated)
	                .onError(this::onError)
	                .onSubmitSuccess((data)->{
	                	//Response res = (Response)data;
	                	//System.out.println("OnSubmitSuccess:" + index);
	                }));
	      	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//        }
        //System.out.println(System.currentTimeMillis() - tm1);
	}

	private synchronized void  onValidated(ManagedTxn managed) {
		mValCount++;
		if(mValCount % 200 == 0) {
			 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
		        System.out.println(mValCount);
		}
        TransactionResult tr = managed.result;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
        //print("Result:\n{0}", tr.toJSON().toString(2));
        print("Transaction result was: {0},{1},{2},{3}", tr.engineResult,tr.txn.sequence(),tr.ledgerIndex,tr.hash);
        //System.exit(0);
    }

    private synchronized void onError(Response res) {
        printErr("Transaction failed! {0}", res.message);
//        managed.submissions.forEach(sub ->
//                printErr("{0}", sub.hash) );
        //System.exit(1);
    }
    
	public static  void testSignPayment(){
		
		JSONObject obj = new JSONObject();
		JSONObject tx_json = new JSONObject();
		String tx_blob = "";
		try{
			JSONObject address = c.generateAddress();
			//获取账户信息，得到当前Sequence
			AccountID account = AccountID.fromAddress(address.getString("account_id"));
			Request request = c.connection.client.accountInfo(account);
			if(request.response.result!=null){
				Integer sequence = (Integer)request.response.result.optJSONObject("account_data").get("Sequence");
				System.out.println("request.response.result != null & sequence = " + sequence);
				tx_json.put("Sequence", sequence.intValue());
			}
			tx_json.put("Account", address.getString("account_id"));
			tx_json.put("Amount", "200000000");
			tx_json.put("Destination", "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q");
			tx_json.put("TransactionType", "Payment");
			obj.put("tx_json", tx_json);
			
			//签名，内部调用了EccSign接口
			JSONObject res = c.sign(obj, "");
			tx_blob = res.getString("tx_blob");
		}catch(JSONException e){
		}catch(Exception e){
		}
		
		//发送交易等待
        try{
            Request r = c.connection.client.submit(tx_blob, true);
            r.request();
            while(r.response == null){
            	Thread.sleep(50);
            }
            System.out.println("submit payment result:" + r.response.message);
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
}
