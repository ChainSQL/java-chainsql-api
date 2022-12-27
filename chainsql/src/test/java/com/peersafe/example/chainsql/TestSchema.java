package com.peersafe.example.chainsql;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SchemaOpType;
import com.peersafe.chainsql.core.Submit.SyncCond;

public class TestSchema {
	public static final Chainsql c = new Chainsql();
	public static String sTableName,sTableName2,sReName;
	public static String sNewAccountId,sNewSecret;

	public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
	public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
	
	public static String userSecret = "xnnUqirFepEKzVdsoBKkMf577upwT";
	public static String userAddress = "zpMZ2H58HFPB5QTycMGWSXUeF47eA8jyd4";
	
	
	public static void main(String[] args) {

		try{
			c.connect("ws://127.0.0.1:6006");

			testSchema();

		}catch (Exception e){

			e.printStackTrace();
		}
	}
	
	private static void testSchema() {
		TestSchema test = new TestSchema();
		c.as(rootAddress, rootSecret);
		
//		test.testSchemaList();
		
//		test.testSchemaCreate();
		
//		sTableName = "hello_123";
//		c.setSchema("57256592FD987D4256DDCE4812484BDE9B9193A70DDEC983541A182275045FE0");
//		test.testCreateTable();
		
		test.testSchemaModify();
	}
	
	public void testSchemaList() {
		JSONObject option = new JSONObject();
		JSONObject list = c.getSchemaList(option);
		System.out.println(list);
	}
	
	public void testSchemaCreate() {
		JSONObject schemaInfo = new JSONObject();
		schemaInfo.put("SchemaName","hello1");
		schemaInfo.put("WithState",false);

		schemaInfo.put("SchemaAdmin",rootAddress);

		List<String> validators = new ArrayList<String>();
		validators.add("03C53D4B7E4D558DBD8EA67E68AD97844FCCF48DD4C7A5C10E05B293A11DC9BB40");
		validators.add("021D3E9C571DF23054DBB2005E76EA5BE5227D381FB9B4A52467B5E6412ABAFBA0");
		validators.add("0317B5CAEBE6C778D133B1CA670D00E994D3AFAC2C0E6AA8F11B0DA277309F193E");
		JSONArray validatorsJsonArray = new JSONArray(validators);
		schemaInfo.put("Validators",validatorsJsonArray);

		List<String> peerList = new ArrayList<String>();
		peerList.add("127.0.0.1:5125");
		peerList.add("127.0.0.1:5126");
		peerList.add("127.0.0.1:5127");
		JSONArray peerListJsonArray = new JSONArray(peerList);

		schemaInfo.put("PeerList",peerListJsonArray);
		schemaInfo.put("SchemaAdmin",rootAddress);
		
		try {
			JSONObject ret = c.createSchema(schemaInfo).submit(SyncCond.validate_success);
			System.out.println(ret);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testSchemaModify() {
		JSONObject schemaInfo = new JSONObject();
		schemaInfo.put("SchemaID", "312242E6D2DB07BF1F856A2525A2DBA4199F9B60CF38584C5815CD79808572FD");
		
		List<String> validators = new ArrayList<String>();
		validators.add("02DB6543999A94815F7FC5F61D3AB1BA60938F15BC004E70613F7824DA70D5CCAB");
		JSONArray validatorsJsonArray = new JSONArray(validators);
		schemaInfo.put("Validators",validatorsJsonArray);

		List<String> peerList = new ArrayList<String>();
		peerList.add("127.0.0.1:5128");
		JSONArray peerListJsonArray = new JSONArray(peerList);

		schemaInfo.put("PeerList",peerListJsonArray);
		
		try {
			JSONObject obj = c.modifySchema(SchemaOpType.schema_add, schemaInfo).submit(SyncCond.validate_success);
			System.out.println(obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
