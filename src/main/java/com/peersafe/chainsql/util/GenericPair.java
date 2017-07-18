package com.peersafe.chainsql.util;

/** 
 *  
 * @author wy_okmeiyu from CSDN 
 * @QQ     You can't see 
 * @Version 1.0 
 * @TODO: 创造一个类似C++中的Pair类 
 * @UpdateDate： 2015-11-19 
 * @param <E> 
 */  
public class GenericPair<E extends Object, F extends Object> {  
    private E first;  
    private F second;  
      
    public GenericPair(){  
    }  
    public GenericPair(E first,F second){
    	this.first = first;
    	this.second = second;
    }
      
    public E getFirst() {  
        return first;  
    }  
    public void setFirst(E first) {  
        this.first = first;  
    }  
    public F getSecond() {  
        return second;  
    }  
    public void setSecond(F second) {  
        this.second = second;  
    }  
      
    public boolean equals(Object obj){
    	if(obj instanceof GenericPair){
    		GenericPair<?, ?> pair = (GenericPair<?, ?>)obj;
    		return pair.getFirst().equals(this.first) && pair.getSecond().equals(this.second);
    	}
    	return false;
    }
}  
