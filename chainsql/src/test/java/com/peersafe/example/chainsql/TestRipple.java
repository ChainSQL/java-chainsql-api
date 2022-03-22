package com.peersafe.example.chainsql;


import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.RippleDate;
import com.peersafe.base.core.coretypes.STArray;
import com.peersafe.base.core.coretypes.STObject;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.util.Util;

import java.nio.channels.ScatteringByteChannel;

public class TestRipple {
	
	public static final Chainsql c = new Chainsql();
	//
	//account,secret
	private static String[] sAddr = {
			"zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh", // root
			"zLi3jhvjkJp32cKyNScQx6GXZaLoXuEJ2u", // user1
			"zPcimjPjkhQk7a7uFHLKEv6fyGHwFGQjHa", // user
			"zKXfeKXkTtLSTkEzaJyu2cRmRBFRvTW2zc", // issuer
            "zN7TwUjJ899xcvNXZkNJ8eFFv2VLKdESsj" // gmRoot

	};
	private static String[] sSec = {
			"xnoPBzXtMeMyMHUVTgbuqAfg1SUTb", // root sec
			"xx3fkguaAC3dom5pVmx93Fxn8BdYN", // user1 sec
			"xxCosoAJMADiy6kQFVgq1Nz8QewkU", // user sec
			"xhtBo8BLBZtTgc3LHnRspaFro5P4H",  // issuer sec
            "p97evg5Rht7ZB7DbEpVqmV3yiSBMxR3pRBKJyLcRWt7SL5gEeBb" // gmRoot sec

	};
	public static String rootAddress = sAddr[0];
	public static String rootSecret = sSec[0];

    public static String gmRootAddress = sAddr[4];
	public static String gmRootSecret = sSec[4];

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
		c.connect("ws://localhost:5510");
		//
		String sCurrency = "abc";
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
			boolean bPay = false;
			if(bActive)
			{
				//c.connection.client.subscribeAccount(AccountID.fromString(rootAddress));
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
				try {
					jsonObj = c.accountSet("1.002", "0.2", "0.3").submit(SyncCond.validate_success);
					System.out.print("set gateWay:" + jsonObj + "\ntrust gateWay ...\n");
				}
				catch (Exception e)
				{
					System.out.print(e);
				}
				JSONObject user = new JSONObject();
				user.put("User", "zLi3jhvjkJp32cKyNScQx6GXZaLoXuEJ2u");
				JSONObject whitelist = new JSONObject();
				whitelist.put("WhiteList", user);
				STObject object = STObject.fromJSONObject(whitelist);
				STArray arry = new STArray();
				arry.add(object);
				
				String tablestr = "{\"WhiteList\":{\"User\":\"" + sUser1+ "\"}}";
				JSONArray array =  Util.strToJSONArray(tablestr);
				jsonObj = c.whitelistSet(array, 10).submit(SyncCond.validate_success);
				System.out.print("set gateWay:" + jsonObj + "\ntrust gateWay ...\n");
				
				//jsonObj = c.whitelistSet(array, 11).submit(SyncCond.validate_success);
				//System.out.print("set gateWay:" + jsonObj + "\ntrust gateWay ...\n");
				
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
				jsonObj  = c.pay(sUser1, "10000000", sCurrency, sGateWay).submit(SyncCond.validate_success);
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
			int nCreateEscrowSeq = 2;
			if(bTestCreate)
			{
				c.as(sGateWay, sGateWaySec);
//				jsonObj = c.escrowCreate(sUser1, "10", "", "2020-01-03 18:10:40").submit(SyncCond.validate_success);
				jsonObj = c.escrowCreate(sUser1, "10", "2020-01-03 17:27:06", "2020-01-03 18:10:40").submit(SyncCond.validate_success);
//				jsonObj = c.escrowCreate(sUser1, "10", sCurrency, sGateWay, "", "2020-01-03 16:17:56").submit(SyncCond.validate_success);
//				jsonObj = c.escrowCreate(sUser1, "10", sCurrency, sGateWay, "2018-09-12 16:42:00", "2018-09-12 16:43:00").submit(SyncCond.validate_success);
				System.out.print("escrowCreate res: " + jsonObj + "\n");
				jsonObj = c.getTransaction(jsonObj.getString("tx_hash"));
				nCreateEscrowSeq = jsonObj.getInt("Sequence");
				System.out.print("nCreateEscrowSeq: " + nCreateEscrowSeq + "\n");
				System.exit(1);
			}
			if(bTestFin)
			{
				c.as(sUser1, sUserSec1);
				jsonObj = c.escrowExecute(sGateWay, nCreateEscrowSeq).submit(SyncCond.validate_success);
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
				System.out.print("\n" + RippleDate.localFromSecondsSinceRippleEpoch(631346404) + "\n");
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
