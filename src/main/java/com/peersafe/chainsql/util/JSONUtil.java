package com.peersafe.chainsql.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class JSONUtil {
	 
    /**
     * ��ʵ��Listת��ΪJSONString
     * @param obj
     * @return
     * @throws JSONException
     * @throws IOException
     */
	  public static String StrToJsonStr(String args) {
			JSONObject a = new JSONObject(args);
			a.toString();
			//checkinsert(a);
			
      return a.toString();
  }
  
    public static JSONObject StrToJson(String args) {
			JSONObject a = new JSONObject(args);	
			//checkinsert(a);
			
        return a;
    }
    
	public static void checkinsert(List<JSONObject> strraw) throws Exception{
			boolean isHavePk = false;
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

    		if (field==null || type==null) {
    			throw new Exception("field and type cannot be empty");
    		}
            if("int".equals(type)){

            }else if("float".equals(type)){

            }else if("double".equals(type)){

            }else if("decimal".equals(type)){

            }else if("varchar".equals(type)){
            	try {
    				int length = (int) json.get("length");
    			} catch (Exception e) {
    				throw new Exception(" The type varchar must have length");
    			}
            }else if("blob".equals(type)){

            }else if("text".equals(type)){

            }else if("datatime".equals(type)){

            }else{
            	throw new Exception("invalid type "+type);
            }
            try {
            	int PK =(int) json.get("PK");
            	if (isHavePk) {
    				throw new Exception("the table only have a PK");
    			}
            	isHavePk = true;
			} catch (Exception e) {
				//throw new Exception("Raw must have  field and type");
				//e.printStackTrace();
			}
    		
		}
	}
    /**
     * ��ʵ���ַ���ת��Ϊ16����
     * @param obj
     * @return
     * @throws JSONException
     * @throws IOException
     */
	public static String toHexString(String s){
		String strr = "";
		try {
			strr = new String(s.getBytes("gb2312"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println(strr);
		String str="";
		for (int i=0;i<s.length();i++)
		{
		int ch = (int)s.charAt(i);
		String s4 = Integer.toHexString(ch);
		str = str + s4;
		}
		return str;
	} 
}
