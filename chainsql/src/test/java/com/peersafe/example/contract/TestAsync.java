package com.peersafe.example.contract;

import java.math.BigInteger;

import org.web3j.tuples.generated.Tuple2;

import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.example.contract.Listen.ListenSetA1EventResponse;
import com.peersafe.example.contract.Listen.ListenSetA2EventResponse;
import com.peersafe.example.contract.Listen.ListenSetBEventResponse;

public class TestAsync {
	public static final Chainsql c = Chainsql.c;
	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	public static void main(String[] args) throws Exception {
		c.connect("ws://10.100.0.90:6005");
		c.as(rootAddress, rootSecret);
		TestTransfer transfer = TestTransfer.load(c, "zMpSo7EoPEwjQ3e7djBR6xeHfm47eHsHUt", Contract.GAS_LIMIT);
		transfer.getOwner(new Callback<Tuple2<String, BigInteger>>(){

			@Override
			public void called(Tuple2<String, BigInteger> owner) {
				System.out.println("owner.first="+owner.getValue1()+",owner.second=" + owner.getValue2());
			}
			
		});
		
		transfer.owner(new Callback<String>() {

			@Override
			public void called(String args) {
				System.out.println("owner address:" + args);
			}
			
		});
		
		transfer.getUserBalance("zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh", new Callback<BigInteger>() {

			@Override
			public void called(BigInteger args) {
				System.out.println("user balance=" + args.intValue());
			}
			
		});
	}

}
