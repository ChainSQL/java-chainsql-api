import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Table;
import com.ripple.client.Client;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

public class ClientTest {
	  public static final Chainsql c = Chainsql.c;
	  public Table table;
	  public static void main(String[] args) {
		
		  c.connect("ws://192.168.0.197:6007");
		  c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		  c.event.subTable("test1", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",(data)->{
	            System.out.println(data);
	        });
		
	  }

	  
		
		
		  private static void onceConnected(Client c) {
		  	JSONObject messageTx = new JSONObject();
				messageTx.put("command", "subscribe");
				messageTx.put("owner", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
				messageTx.put("tablename", "test");
				
		      c.subscriptions.addMessage(messageTx);
		      c.OnTBMessage(ClientTest::OnMessage);
		  }
		
		  private static void OnMessage(JSONObject json) {
		      //print("Ledger {0} closed @ {1} with {2} transactions"+json);
		  	System.out.println(json.toString());
		          
		  }
	/*	JSONObject messageTx = new JSONObject();
		messageTx.put("command", "subscribe");
		messageTx.put("owner", "rEtepyQeAEgBLqXCaFRwZPK1LHArQfdKYr");
		messageTx.put("tablename", "testcas");
		client.sendMessage(messageTx);*/
		

		      /*  new Client(new JavaWebSocketTransportImpl())
		            .connect("ws://192.168.0.197:6007",
		            		ClientTest::onceConnected);
		    }
	

		    private static void onceConnected(Client c) {
		    	
		    	JSONObject messageTx = new JSONObject();
				messageTx.put("command", "subscribe");
				messageTx.put("owner", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
				messageTx.put("tablename", "test");
				
		        c.subscriptions.addMessage(messageTx);
		        c.OnTBMessage(ClientTest::OnMessage);
		    }

		    private static void OnMessage(JSONObject json) {
		        //print("Ledger {0} closed @ {1} with {2} transactions"+json);
		    	System.out.println(json.toString());
		            
		    }*/

		

}
