package contract;

import java.util.concurrent.CompletableFuture;

import org.web3j.utils.Numeric;

import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.TransactionReceipt;
import com.peersafe.chainsql.core.Chainsql;

public class ContractTester {
	public static final Chainsql c = Chainsql.c;
	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	
	public static void main(String[] args) throws Exception{
		
		CompletableFuture<Greeter> completable = Greeter.deploy(c,
				 Contract.GAS_LIMIT,Contract.INITIAL_DROPS,
		 "Hello blockchain world!").sendAsync();
		completable.whenComplete((v,e)->{
			if(v != null) {
				try {
					System.out.println("Value stored in remote smart contract: " +
							 v.greet().send());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			if(e != null) {
				System.out.println(e);
			}
		});
		
		// // Now lets deploy a smart contract
		System.out.println("Deploying smart contract");
		c.connect("ws://192.168.0.89:6007");
		c.as(rootAddress, rootSecret);

		
//		 Greeter contract = Greeter.deploy(c,
//				 Contract.GAS_LIMIT,Contract.INITIAL_DROPS,
//		 "Hello blockchain world!").send();
		
//		
//		 String contractAddress = contract.getContractAddress();
//		 System.out.println("Smart contract deployed to address " + contractAddress);
//		
//		 System.out.println("Value stored in remote smart contract: " +
//		 contract.greet().send());

		Greeter contract = Greeter.load(c,"zKotgrRHyoc7dywd7vf6LgFBXnv3K66rEg", 
				 Contract.GAS_LIMIT);

		 System.out.println("Value stored in remote smart contract: " +
		 contract.greet().send());
		 
		// Lets modify the value in our smart contract
		TransactionReceipt transactionReceipt = contract.newGreeting("Well hello again").send();

		 System.out.println("Value stored in remote smart contract: " +
		 contract.greet().send());
		 
		 System.out.println(transactionReceipt.getTransactionHash());
//		// Events enable us to log specific events happening during the execution of our
//		// smart
//		// contract to the blockchain. Index events cannot be logged in their entirety.
//		// For Strings and arrays, the hash of values is provided, not the original
//		// value.
//		// For further information, refer to
//		// https://docs.web3j.io/filters.html#filters-and-events
//		for (Greeter.ModifiedEventResponse event : contract.getModifiedEvents(transactionReceipt)) {
//			log.info("Modify event fired, previous value: " + event.oldGreeting + ", new value: " + event.newGreeting);
//			log.info("Indexed event previous value: " + Numeric.toHexString(event.oldGreetingIdx) + ", new value: "
//					+ Numeric.toHexString(event.newGreetingIdx));
//		}
	}
}
