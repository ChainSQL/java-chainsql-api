package com.peersafe.example.chainsql;


import org.json.JSONObject;

import com.peersafe.base.core.coretypes.RippleDate;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;

public class TestRipple {
	
	public static final Chainsql c = Chainsql.c;
	//
	//account,secret
	private static String[] sAddr = {
			"zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh", // 0
			"zKQwdkkzpUQC9haHFEe2EwUsKHvvwwPHsv", // 1
			"zPcimjPjkhQk7a7uFHLKEv6fyGHwFGQjHa", // 2
			"z4ypskpHPpMDtHsZvFHg8eDEdTjQrYYYV6"  // 3

	};
	private static String[] sSec = {
			"xnoPBzXtMeMyMHUVTgbuqAfg1SUTb", // 0
			"xnJn5J5uYz3qnYX72jXkAPVB3ZsER", // 1
			"xxCosoAJMADiy6kQFVgq1Nz8QewkU", // 2
			"xxXvas5HTwVwjpmGNLQDdRyYe2H6t"  // 3

	};
	public static String rootAddress = sAddr[0];
	public static String rootSecret = sSec[0];
	public static String sUser1 = sAddr[1];
	public static String sUserSec1 = sSec[1];
	public static String sUser = sAddr[2];
	public static String sUserSec = sSec[2];
	public static String sGateWay = sAddr[3];
	public static String sGateWaySec = sSec[3];

	//
	public static void main(String[] args) throws Exception
	{		
		//
		c.connect("ws://127.0.0.1:6006");
		//
		String sCurrency = "aaa";
		JSONObject jsonObj;
		boolean bGateWay = true;
		boolean bEscrow = false;
		if(bGateWay)
		{
			//
			c.as(rootAddress, rootSecret);
			//
			boolean bActive = true;
			boolean bTrust = true;
			boolean bPay = true;
			if(bActive)
			{
				System.out.print("activate >>>>>>>>>>>>>>>\n");
				jsonObj = c.pay(sGateWay, "100000000").submit(SyncCond.validate_success);
				System.out.print("     gateWay:" + jsonObj + "\n");
				jsonObj = c.pay(sUser, "100000000").submit(SyncCond.validate_success);
				System.out.print("     user:" + jsonObj + "\n");
				jsonObj = c.pay(sUser1, "100000000").submit(SyncCond.validate_success);
				System.out.print("     user1:" + jsonObj + "\n");
				System.out.print("activate <<<<<<<<<<<<<<<\n");
			}
			if(bTrust)
			{
				System.out.print("trust >>>>>>>>>>>>>>>\n");
				c.as(sGateWay, sGateWaySec);
				jsonObj = c.accountSet(8, true).submit(SyncCond.validate_success);
				System.out.print("set gateWay:" + jsonObj + "\ntrust gateWay ...\n");
				jsonObj = c.accountSet("1.005", "0.2", "0.3").submit(SyncCond.validate_success);
				System.out.print("set gateWay:" + jsonObj + "\ntrust gateWay ...\n");
				c.as(sUser, sUserSec);
				jsonObj = c.trustSet("1000000000", sCurrency, sGateWay).submit(SyncCond.validate_success);
				System.out.print("     user: " + jsonObj + "\n");
				c.as(sUser1, sUserSec1);
				jsonObj = c.trustSet("1000000000", sCurrency, sGateWay).submit(SyncCond.validate_success);
				System.out.print("     user1: " + jsonObj + "\n");
				//check accountline
				System.out.print("acountLines ...\n");
				jsonObj = c.connection.client.GetAccountLines(sUser);
				System.out.print("     user: " + jsonObj + "\n");
				jsonObj = c.connection.client.GetAccountLines(sUser1);
				System.out.print("     user1 " + jsonObj + "\n");
				System.out.print("trust <<<<<<<<<<<<<<<\n");
			}
			if(bPay)
			{
				System.out.print("pay >>>>>>>>>>>>>>>\n");
				System.out.print("transter issue coin hello:\n");
				c.as(sGateWay, sGateWaySec);
				jsonObj = c.pay(sUser, "1000000000", sCurrency, sGateWay).submit(SyncCond.validate_success);
				System.out.print("    user:\n     " + jsonObj + "\n");
				jsonObj = c.connection.client.GetAccountLines(sUser);
				System.out.print("    lines: " + jsonObj + "\n");
				c.as(sUser, sUserSec);
				jsonObj  = c.pay(sUser1, "10000", sCurrency, sGateWay).submit(SyncCond.validate_success);
				System.out.print("    user1:\n     " + jsonObj + "\n");
				jsonObj = c.connection.client.GetAccountLines(sUser1);
				System.out.print("    lines: " + jsonObj + "\n");
				System.out.print("pay <<<<<<<<<<<<<<<\n");
			}
		}
		if(bEscrow)
		{
			boolean bTestCreate = false;
			boolean bTestFin = true;
			boolean bTestCancel = false;
			boolean bTime = false;
			int nCreateEscrowSeq = 41;
			if(bTestCreate)
			{
				c.as(sUser, sUserSec);
				jsonObj = c.escrowCreate(sUser1, "100", sCurrency, sGateWay, "2018-09-12 16:42:00", "2018-09-12 16:43:00").submit(SyncCond.validate_success);
				System.out.print("escrowCreate res: " + jsonObj + "\n");
				jsonObj = c.getTransaction(jsonObj.getString("tx_hash"));
				nCreateEscrowSeq = jsonObj.getInt("Sequence");
				System.out.print("nCreateEscrowSeq: " + nCreateEscrowSeq + "\n");
				System.exit(1);
			}
			if(bTestFin)
			{
				c.as(sUser1, sUserSec1);
				jsonObj = c.escrowExecute(sUser, nCreateEscrowSeq).submit(SyncCond.validate_success);
				System.out.print("escrowExecute res: " + jsonObj + "\n");
			}
			if(bTestCancel)
			{
				c.as(sUser, sUserSec);
				jsonObj = c.escrowCancel(sUser, nCreateEscrowSeq).submit(SyncCond.validate_success);
				System.out.print("escrowCancel res: " + jsonObj + "\n");
			}
			if(bTime)
			{
				System.out.print("\n" + RippleDate.localFromSecondsSinceRippleEpoch(590056740) + "\n");
				System.exit(1);
			}
			System.exit(1);
		}
		else
			System.exit(1);
		//
		System.exit(1);
	}
}
