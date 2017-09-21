package com.peersafe.chainsql.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.peersafe.base.client.Client;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.STArray;
import com.peersafe.base.core.coretypes.STObject;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.resources.Constant;

public class Validate {
	public Object create(String obj) {
		Object [] result ={} ;
		
		return result;
	}

	public static Integer toOpType(String  opType) {
		Map<String,Integer> map = Constant.opType;
		Integer result = (Integer) map.get(opType);

		return result;
	}
	
	public static STArray fromJSONArray(JSONArray arr) {
		STArray stArr = new STArray();
		if(arr.length() > 0){
			STObject obj1 = new STObject();
			for(int i=0; i<arr.length(); i++){
				obj1 = STObject.fromJSON(arr.get(i).toString());
				stArr.add(obj1);
			}
		}
		
        return stArr;
    }
	
	public static JSONObject getUserToken(Connection connection,String owner, String name) {
		Request request = connection.client.getUserToken(owner,connection.address,name);
		return request.response.result;
	}
	
	public static JSONObject tablePrepare(Client client, JSONObject tx_json) {
		Request request = client.tablePrepare(tx_json);
		return request.response.result;
		
	}

	public static Map<String,Object> rippleRes(Client client,AccountID account){
		HashMap<String,Object> map = new HashMap<String,Object>();
		Request request = client.accountInfo(account);
		if(request.response.result!=null){
			Integer sequence = (Integer)request.response.result.optJSONObject("account_data").get("Sequence");
			map.put("Sequence", sequence);
		}else if(request.response.message.has("error")){
			map.put("error_message", request.response.message.getString("error_message"));
		}
		return map;
	}
}
