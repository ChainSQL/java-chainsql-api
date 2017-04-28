package com.peersafe.chainsql.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
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

	public static Integer toOpType(String  opType) {
		Map<String,Integer> map = Constant.opType;
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
	
	public static JSONObject getUserToken(Connection connection,String owner, String name) {
		Request request = connection.client.getUserToken(owner,connection.address,name);
		return request.response.result;
	}
	
	public static JSONObject getTxJson(Client client, JSONObject tx_json) {
		Request request = client.getTxJson(tx_json);
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
