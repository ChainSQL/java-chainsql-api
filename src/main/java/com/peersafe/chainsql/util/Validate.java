package com.peersafe.chainsql.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.resources.Constant;
import com.peersafe.base.client.Client;
import com.peersafe.base.client.requests.Request;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.STArray;
import com.peersafe.base.core.coretypes.STObject;

public class Validate {
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public Object create(String obj) {
		Object [] result ={} ;
		
		return result;
	}

	/**
	 * 
	 * @param opType
	 * @return
	 */
	public static Integer toOpType(String  opType) {
		Map<String,Integer> map = Constant.opType;
		Integer result = (Integer) map.get(opType);

		return result;
	}
	

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static STArray fromJSONArray(String str) {
		STObject obj1 = new STObject();
		obj1 = STObject.fromJSON(str);
		STArray arr = new STArray();
		arr.add(obj1);
        return arr;
    }
	
	/**
	 * 
	 * @param connection
	 * @param owner
	 * @param name
	 * @return
	 */
	public static JSONObject getUserToken(Connection connection,String owner, String name) {
		Request request = connection.client.getUserToken(owner,connection.address,name);
		return request.response.result;
	}
	
	/**
	 * 
	 * @param client
	 * @param tx_json
	 * @return
	 */
	public static JSONObject getTxJson(Client client, JSONObject tx_json) {
		Request request = client.getTxJson(tx_json);
		return request.response.result;
		
	}
	/**
	 * 
	 * @param client
	 * @param account
	 * @return
	 */
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
