package com.peersafe.example.contract;


import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.chainsql.core.Ripple;
import com.peersafe.chainsql.core.Submit;
import org.json.JSONObject;

import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.Util;


public class TestContractGatewayTxs {

	public static final Chainsql c = new Chainsql();

	private static String[] sAddr = {
			"zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh", // 0
			"zKQwdkkzpUQC9haHFEe2EwUsKHvvwwPHsv", // 1
			"zPcimjPjkhQk7a7uFHLKEv6fyGHwFGQjHa", // 2
			"zGSyDNzNbQeuKS3L8gf2MBLAVsSShfm9Wi"  // 3

	};
	private static String[] sSec = {
			"xnoPBzXtMeMyMHUVTgbuqAfg1SUTb", // 0
			"xnJn5J5uYz3qnYX72jXkAPVB3ZsER", // 1
			"xxCosoAJMADiy6kQFVgq1Nz8QewkU", // 2
			"xxbUtZQWXmJgLwqqgnEFY9cJvVyfr"  // 3
	};
	public static String rootAddress = sAddr[0];
	public static String rootSecret = sSec[0];
	public static String gatewayAddress = sAddr[1];
	public static String gatewaySec = sSec[1];
	public static String user1Address = sAddr[2];
	public static String user1Sec = sSec[2];
	public static String user2Address = sAddr[3];
	public static String user2Sec = sSec[3];

	static GatewayTest myContract = null;

	//static String contractAddr = "zwvgh6WAr3GbjsopgpQmhciHjcPwC7LU8h";

	static  String sCurrency = "AAA";

	//
	public enum tagStep {
		active,
		deployContract,
		callContract
	}


	public static String contractAddress = "z9b4BZxBVwqmZiBMgf8acuZ8q3112rUpu2";

	public static void main(String[] args) throws Exception {

		c.connect("ws://192.168.29.115:6005");

		c.as(rootAddress, rootSecret);


		try {

			tagStep nStep = tagStep.callContract;

			if (nStep == tagStep.active) {

				active();
			} else if (nStep == tagStep.deployContract) {

				deployContract();
			} else {

				callContract();
			}


			System.out.print("end <<<<<<");
			System.exit(1);


		}catch (TransactionException e){
			throw e;
		}


	}


	public static void active() {
		c.as(rootAddress, rootSecret);
		System.out.print("activate >>>>>>>>>>>>>>>\n");
		JSONObject jsonObj = c.pay(user1Address, "200000").submit(SyncCond.validate_success);
		System.out.print("     user1 :" + jsonObj + "\n");
		jsonObj = c.pay(user2Address, "200000").submit(SyncCond.validate_success);
		System.out.print("     user2 :" + jsonObj + "\n");

		jsonObj = c.pay(gatewayAddress, "200000").submit(SyncCond.validate_success);
		System.out.print("     gateway:" + jsonObj + "\n");

		System.out.print("activate <<<<<<<<<<<<<<<\n");
	}


	public static void deployContract() throws TransactionException {
		c.as(rootAddress, rootSecret);
		GatewayTest contract = GatewayTest.deploy(c, Contract.GAS_LIMIT.multiply(BigInteger.valueOf(10)));
		String contractAddr = contract.getContractAddress();
		System.out.print("contract address:" + contractAddr+"\n");
	}

	public static void callContract() throws TransactionException {

		c.as(rootAddress,rootSecret);

		myContract = GatewayTest.load(c, contractAddress, Contract.GAS_LIMIT.multiply(BigInteger.valueOf(100)));


		accountSet();
		setTransferFee();
		trustSet();
		trustSetContract();
		trustLimit();
		trustLimitContract();
		payCurrency();

		payCurrencyContract();
		gatewayBalance();
		gatewayBalanceContract();


	}


	public static void accountSet() throws TransactionException {

		try {

			c.as(gatewayAddress, gatewaySec);

			JSONObject oj = myContract.accountSet(BigInteger.valueOf(8),true).submit(Submit.SyncCond.validate_success);
			System.out.println("accountSet result ="+oj);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void setTransferFee() throws TransactionException {


		try {

			c.as(gatewayAddress, gatewaySec);
			JSONObject oj = myContract.setTransferFee("1.002","10","20").submit(Submit.SyncCond.validate_success);
			System.out.println("setTransferFee result ="+oj);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void trustSet() throws TransactionException {

		try {
			c.as(user1Address, user1Sec);

			JSONObject res = myContract.trustSet("10000",sCurrency,gatewayAddress).submit(Submit.SyncCond.validate_success);
			System.out.println("user1 trustSet result =" + res);


			c.as(user2Address, user2Sec);

			res = myContract.trustSet("10000",sCurrency,gatewayAddress).submit(Submit.SyncCond.validate_success);
			System.out.println("user2 trustSet result =" + res);

		}catch (Exception e){
			e.printStackTrace();
		}

	}
	public static void trustSetContract() throws TransactionException {

		try {
			c.as(user1Address, user1Sec);

			JSONObject oj = myContract.trustSet(contractAddress,"10000",sCurrency,gatewayAddress).submit(Submit.SyncCond.validate_success);
			System.out.println("trustSetContract  result ="+oj);

		}catch (Exception e){
			e.printStackTrace();
		}

	}
	public static void trustLimit() throws TransactionException {

		try {

			c.as(user1Address, user1Sec);
			BigInteger res = myContract.trustLimit(sCurrency,BigInteger.valueOf(0),gatewayAddress);
			System.out.println("user1 trustLimit  result ="+ res);


			c.as(user2Address, user2Sec);
			res = myContract.trustLimit(sCurrency,BigInteger.valueOf(0),gatewayAddress);
			System.out.println("user2 trustLimit  result =" + res);

		}catch (Exception e){
			e.printStackTrace();
		}

	}
	public static void trustLimitContract() throws TransactionException {

		try {

			c.as(user1Address, user1Address);

			BigInteger oj = myContract.trustLimit(contractAddress,sCurrency,BigInteger.valueOf(0),gatewayAddress);
			System.out.println("trustLimitContract  result ="+oj);

		}catch (Exception e){
			e.printStackTrace();
		}


	}
	public static void payCurrency() throws TransactionException {

		try {
			c.as(gatewayAddress, gatewaySec);

			JSONObject res = myContract.pay(user1Address,"500","800",sCurrency,gatewayAddress).submit(Submit.SyncCond.send_success);
			System.out.println("gateway to user1 " + res);


			res = myContract.pay(user2Address,"500","800",sCurrency,gatewayAddress).submit(Submit.SyncCond.send_success);
			System.out.println("gateway to user2 " + res);

		}catch (Exception e){
			e.printStackTrace();
		}

	}
	public static void payCurrencyContract() throws TransactionException {

		try {
			c.as(gatewayAddress, gatewaySec);

			JSONObject res = myContract.pay(contractAddress,"500","800",sCurrency,gatewayAddress).submit(Submit.SyncCond.validate_success);
			System.out.println("gateway to contractAddress " + res );


			res = myContract.gatewayPay(contractAddress,user1Address,"50","80",sCurrency,gatewayAddress).submit(Submit.SyncCond.validate_success);
			System.out.println("contractAddress to user1 " + res );

			res = myContract.gatewayPay(contractAddress,user2Address,"50","80",sCurrency,gatewayAddress).submit(Submit.SyncCond.validate_success);

			System.out.println("contractAddress to user2 " + res );

			//gatewayBalance();

		//	gatewayBalanceContract();


		}catch (Exception e){
			e.printStackTrace();
		}

	}


	public static void gatewayBalance() throws TransactionException {

		try {
			c.as(user1Address, user1Sec);

			BigInteger res = myContract.gatewayBalance(sCurrency,BigInteger.valueOf(0),gatewayAddress);
			System.out.println("user1 余额：" + res);

			c.as(user2Address, user2Sec);
			res = myContract.gatewayBalance(sCurrency,BigInteger.valueOf(0),gatewayAddress);
			System.out.println("user2 余额：" + res);

		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public static void gatewayBalanceContract() throws TransactionException {

		try {
			c.as(gatewayAddress, gatewaySec);

			BigInteger res = myContract.gatewayBalance(contractAddress,sCurrency,BigInteger.valueOf(1),gatewayAddress);
			System.out.println("合约 余额：" + res);

		}catch (Exception e){
			e.printStackTrace();
		}

	}
}

