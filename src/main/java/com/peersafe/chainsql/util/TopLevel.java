package com.peersafe.chainsql.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TopLevel  {
	
	 public MapObject hashMap(Object key, Object val){
	        return new MapObject().with(key, val);
	  }
	 public List array(Object val0, Object... vals){
		 	List res = new ArrayList();
		 	if(val0.getClass().isArray()){
		 		String[] a = (String[]) val0; 
		 		for(String s:a){
		 			res.add(s);
		 		}
		 		
		 	}else{
		 		  res.add(val0);
			      res.addAll(Arrays.asList(vals));
		 	}
	        return res;
	    }
}
