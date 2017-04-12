import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Table;

public class ClientTest {
	  public static final Chainsql c = Chainsql.c;
	  public Table table;
	  public static void main(String[] args) {
		
		  c.connect("ws://192.168.0.197:6007");
		  c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		  c.event.subTable("test1a", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",(data)->{
	            System.out.println("subdata--"+data);
	        });
	  	}

}
