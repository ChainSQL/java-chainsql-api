package java8.test;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;

public class GrantTable {
	public static final Chainsql c = Chainsql.c;
	static String[] tables = {"T_BBPS_LEDGER_ONCHAIN",
	                   "T_BBPS_LIQUIDDTL",
	                   "T_BBPS_BANKNO_CHAIN",
	                   "T_BBPS_SESSIONHIS",
	                   "T_BBPS_RUNADMIN",
	                   "T_BBPS_CASH"};
	static String[] users = {
			"rsadxc3pw976e3hfaxUijhbR3ye2orJS6x",
			"rU3S9xL7xdMcQPgB5oir2dzDp5s19eTJ7f",
			"rEE12BEg5D3fp9ZazVMGayCQWd7hCjfgh6",
			"rsrc4M7tS5ur1UJ7kEVWSvoStzCLB2HNv6"
	};
	
	public static void main(String[] args) {
		c.connect("ws://192.168.0.110:6010");
		  c.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		  c.use("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

		  String auth = "{insert:true,update:true,delete:true,select:true}";
		  for(String user:users){
			  for(String table:tables){
				  JSONObject obj = c.grant(table, user, auth).submit(SyncCond.db_success);
				  System.out.println(obj);
			  }
		  }
	}

}
