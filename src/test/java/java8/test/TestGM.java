package java8.test;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.sansec.key.exception.SDKeyException;

import com.peersafe.base.client.requests.Request;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.crypto.sm.SMDevice;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.crypto.EncryptCommon;
import com.peersafe.chainsql.util.Util;

public class TestGM {
	private static Chainsql c = Chainsql.c;
	private static String sTableName = "HelloWorld";
	public static void main(String[] args){
		//设置包名
		/*
		int version = android.os.Build.VERSION.SDK_INT;
		if (version >= 19) {
			String strAPPFileDirs = null;
			this.getExternalFilesDirs(strAPPFileDirs);
			// set sd path and package name
			// SWJAPI.setSDPathAndAppName(path, SWSDDemo.this.getPackageName());
			SWJAPI.setPackageName(MainActivity.this.getPackageName());
		}
		//设置开启GM
		try {
			c.setUseGM(true, "");
			c.connect("ws://192.168.0.126:6005");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		*/
		//生成地址
		/*
		Button btn = (Button) findViewById(R.id.buttonTest);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Chainsql c = Chainsql.c;
				try {
					JSONObject obj = c.generateAddress();
					if (obj != null) {
						System.out.println(obj);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		*/
		
		//签名测试
		/*
		Button btn2 = (Button) findViewById(R.id.buttonTestSign);
		btn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				testSignPayment();
			}
		});*/
		
		//加密测试
		/*
		Button btn3 = (Button)findViewById(R.id.buttonTestCrypt);
		btn3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				testCrypt();
			}
		});*/
		
		//加密表测试（建表，授权，插入）
		/*
		Button btn4 = (Button)findViewById(R.id.buttonTestChainsql);
		btn4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				testChainsql();
			}
		});
		*/
	}
	
	private void testCrypt(){
		String plain = "test";
		byte[] cipherBytes = EncryptCommon.asymEncrypt(plain.getBytes(), null);
		byte[] decrypted = EncryptCommon.asymDecrypt(cipherBytes, null);		
		System.out.println(new String(decrypted));
		
		String pass = "1234567890123456";
		cipherBytes = EncryptCommon.symEncrypt(plain.getBytes(),pass.getBytes());
		decrypted = EncryptCommon.symDecrypt(cipherBytes, pass.getBytes());
		System.out.println(new String(decrypted));
		
		try {
			System.out.println("Hash result:");
			byte[] bts = SMDevice.sdkey.Hash(4, pass.getBytes());
			System.out.println(Util.bytesToHex(bts));
		} catch (SDKeyException e) {
			e.printStackTrace();
		}
	}
	
	private  void testSignPayment(){
		
		JSONObject obj = new JSONObject();
		JSONObject tx_json = new JSONObject();
		Chainsql c = Chainsql.c;
		String tx_blob = "";
		try{
			AccountID account = AccountID.fromAddress("rwCThrZYQEmiXwr2Jzfi9vNfbHq565oJk5");
			Request request = c.connection.client.accountInfo(account);
			if(request.response.result!=null){
				Integer sequence = (Integer)request.response.result.optJSONObject("account_data").get("Sequence");
				tx_json.put("Sequence", sequence.intValue());
			}
			tx_json.put("Account", "rwCThrZYQEmiXwr2Jzfi9vNfbHq565oJk5");
			tx_json.put("Amount", "10000000000");
			tx_json.put("Destination", "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q");
			tx_json.put("TransactionType", "Payment");
			obj.put("tx_json", tx_json);
			
			JSONObject res = c.sign(obj, "");
			System.out.println("sign payment result:" + res);
			tx_blob = res.getString("tx_blob");
			

		}catch(JSONException e){
			e.printStackTrace();
		}
		
        try{
            Request r = c.connection.client.submit(tx_blob, true);
            r.request();
            while(r.response == null){
            	Thread.sleep(50);
            }
            System.out.println(r.response.message);
        }catch(Exception e){
        	e.printStackTrace();
        }

	}
	
	private void testChainsql(){
		Chainsql c = Chainsql.c;
		try {
			JSONObject address = c.generateAddress();
			c.as(address.getString("account_id"), "");
			System.out.println(c.connection.address);
		} catch (JSONException e) {
			e.printStackTrace();
		}

//		testCreateTable();
//		testGrantTable();
//		testInsert();
	}

	public void testCreateTable(){
		List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1,'AI':1}",
		"{'field':'name','type':'varchar','length':50,'default':null}", "{'field':'age','type':'int'}");
		JSONObject obj =	c.createTable(sTableName,args,false).submit(SyncCond.db_success);

		System.out.println("create result:" + obj);
	}
	
	public void testGrantTable(){
		JSONObject obj = c.grant(sTableName, "rN7TwUjJ899savNXZkNJ8eFFv2VLKdESxj", "pYvWhW4crFwcnovo5MhL71j5PyTWSJi2NVuzPYUzE9UYcSVLp29RhtssQB7seGvFmdjbtKRrBQ4g9bCW5hjBQSeb7LePMwFM", 
				"{'insert':true,'select':true}").submit(SyncCond.validate_success);
		System.out.println("grant result:" + obj);	
	}
	
	public void testInsert() {
		List<String> orgs = Util.array("{'age': 333,'name':'hello'}","{'age': 444,'name':'sss'}","{'age': 555,'name':'rrr'}");
		JSONObject obj;
		obj = c.table(sTableName).insert(orgs).submit(SyncCond.db_success);
		System.out.println("insert result:" + obj);
	}
	
}
