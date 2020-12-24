package com.peersafe.chainsql.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;


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
	
    /**
     * Check fields
     * @param strraw Raw data list.
     * @param name Table name
     * @throws Exception Throws when exception occur.
     */
	public static void checkCreate(List<JSONObject> strraw,String name) throws Exception{
		if(name.isEmpty()) {
			throw new Exception("Table name can not be empty.");
		}
		if(name.length() > 64) {
			throw new Exception("Table name can not be longer than 64");
		}
//		boolean isHavePk = false;
		Set set = new HashSet();
		for (int i = 0; i < strraw.size(); i++) {	
			JSONObject json = strraw.get(i);
			String field, type;
	    	try {
				field =json.getString("field");
				type = json.getString("type");
			} catch (Exception e) {
				throw new Exception("Raw must have  field and type");
				// TODO: handle exception
			}

	    	if( !set.add(field)){
				throw new Exception(" Duplicate column name `"+field+"`");
			}

    		if (field==null || type==null) {
    			throw new Exception("field and type cannot be empty");
    		}
    		
            if("int".equals(type)){

            }else if("float".equals(type)){

            }else if("double".equals(type)){

            }else if("decimal".equals(type)){

            }else if("varchar".equals(type) || "char".equals(type)){
            	try {
    				int length = (int) json.getInt("length");
    				if(length == 0){
    					throw new Exception(" The type varchar must have length");
    				}
    			} catch (Exception e) {
    				throw new Exception(" The type varchar must have length");
    			}
            }else if("blob".equals(type)){

            }else if("text".equals(type)){

            }else if("longtext".equals(type)){

			}
            else if("datetime".equals(type) || "date".equals(type)){

            }else{
            	throw new Exception("invalid type "+type);
            }
            
            if(json.has("AI")) {
            	throw new Exception("'AI' is deprecated, auto increment not supported now!");
            }
//            try {
//            	int PK =(int) json.getInt("PK");
//            	if(PK == 1){
//            		if (isHavePk) {
//            			throw new Exception("the table only have a PK");
//            		}
//            		isHavePk = true;
//            	}
//			} catch (Exception e) {
//				//throw new Exception("Raw must have  field and type");
//				//e.printStackTrace();
//			}
		}
	}
}
