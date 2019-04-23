package com.peersafe.example.contract;

import org.json.JSONObject;
import org.web3j.utils.Numeric;

import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.example.contract.Greeter.ModifiedEventResponse;

public class ContractTester {
	public static final Chainsql c = new Chainsql();
	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";

	public static void main(String[] args) throws Exception {
		c.connect("ws://127.0.0.1:6007");
		c.as(rootAddress, rootSecret);

//		// deploy asynchronously
//		CompletableFuture<Greeter> completable = Greeter
//				.deploy(c, Contract.GAS_LIMIT, Contract.INITIAL_DROPS, "Hello blockchain world!").sendAsync();
//		completable.whenComplete((v, e) -> {
//			if (v != null) {
//				try {
//					System.out.println("Value stored in remote smart contract: " + v.greet().send());
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//			}
//			if (e != null) {
//				System.out.println(e);
//			}
//		});
//
//		// // Now lets deploy a smart contract
//		System.out.println("Deploying smart contract");

		 Greeter contract = Greeter.deploy(c,
			 Contract.GAS_LIMIT,Contract.INITIAL_DROPS,
			 "Hello blockchain world!");

		
		 String contractAddress = contract.getContractAddress();
		 System.out.println("Smart contract deployed to address " + contractAddress);
		
		 System.out.println("Value stored in remote smart contract: " +
		 contract.greet());

//		
//		// load contract from address, set gas limit
//		Greeter contract = Greeter.load(c, "zPxcuzA2SCzx7i5TK2tnbCYN1fhBjrY1Sp", Contract.GAS_LIMIT);
//		 
//		System.out.println("Value stored in remote smart contract: " + contract.greet());
		
		// subscribe modified event
		contract.onModifiedEvents(new Callback<Greeter.ModifiedEventResponse>() {

			@Override
			public void called(ModifiedEventResponse event) {
				System.out.println("Modify event fired, previous value: " + event.oldGreeting + ", new value: "
						+ event.newGreeting);
				System.out.println("Indexed event previous value: " + Numeric.toHexString(event.oldGreetingIdx)
						+ ", new value: " + Numeric.toHexString(event.newGreetingIdx));
			}

		});

		// Lets modify the value in our smart contract
		JSONObject ret = contract.newGreeting("Well hello again3").submit(SyncCond.validate_success);

		System.out.println("Value stored in remote smart contract: " + contract.greet());

		System.out.println(ret);

	}
}
