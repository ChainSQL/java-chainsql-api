package com.peersafe.chainsql.util;


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
      
    @Override
    public int hashCode(){
    	return this.first.hashCode() + this.second.hashCode();
    }
    @Override
    public boolean equals(Object obj){
    	if(obj instanceof GenericPair){
    		GenericPair<?, ?> pair = (GenericPair<?, ?>)obj;
    		return pair.getFirst().equals(this.first) && pair.getSecond().equals(this.second);
    	}
    	return false;
    }
}  
