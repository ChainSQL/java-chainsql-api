package com.peersafe.chainsql.resources;

import java.util.HashMap;

public class Constant {
	 	
		public final static HashMap<String,Integer> permission = new HashMap<String,Integer>() {{    
		    put("lsfSelect", 65536);
		 	put("lsfInsert", 131072);
			put("lsfUpdate", 262144);
			put("lsfDelete",524288);
			put("lsfExecute", 1048576);
		}}; 
		public final static HashMap<String,Integer> opType = new HashMap<String,Integer>() {{    
			put("t_create", 1);
			put("t_drop", 2);
			put("t_rename", 3);
			put("t_assign", 4);
			put("t_assignCancle", 5);
			put("r_insert", 6);
			put("r_get", 7);
			put("r_update", 8);
			put("r_delete", 9);
			put("t_assert",10);
		}}; 
		
		
}
