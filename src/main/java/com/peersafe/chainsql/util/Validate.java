package com.peersafe.chainsql.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.peersafe.chainsql.resources.Constant;
import com.ripple.client.Client;
import com.ripple.client.requests.Request;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.STObject;

public class Validate {
	public Object create(String obj) {
		Object [] result ={} ;
		
		return result;
	}

	public static Integer assign(List flag) {
/*		String [] flag = flags.split(","); */
		Map map = Constant.permission;
		Integer flags = 0;
		for(int i=0;i<flag.size();i++){
			String a = flag.get(i).toString();
			flags = flags|(Integer) map.get(a);
		}
		return flags;
	}
	public static Integer toOpType(String  opType) {
		Constant ps = new Constant();
		Map map = ps.opType;
		Integer result = (Integer) map.get(opType);

		return result;
	}
	
	
	public static STArray fromJSONArray(String str) {
		STObject obj1 = new STObject();
		obj1 = STObject.fromJSON(str);
		STArray arr = new STArray();
		arr.add(obj1);
        return arr;
    }
	
	public static Map rippleRes(Client client,AccountID account ,String name){
		HashMap<String,Object> map = new HashMap<String,Object>();
		Request sequence = client.accountInfo(account);
		if(sequence.response.result!=null){
			Integer Sequence = (Integer)sequence.response.result.optJSONObject("account_data").get("Sequence");
			map.put("Sequence", Sequence);
		}else{
			// System.out.println("error_message :This result is null");
		}
		Request nameindb = client.getNameInDB(name, account);
		if(nameindb.response.result!=null){
			String NameInDB =  (String)nameindb.response.result.get("nameInDB");
			map.put("NameInDB", NameInDB);
		}else{
			 //System.out.println("error_message :This result is null");
		}
		
		/*Request fee = client.getNameInDB(name, account);
		String Fee = (String) client.accountInfo(account).response.result.optJSONObject("account_data").get("Sequence");
		map.put("Fee", Fee);*/
		return map;
	}



}
