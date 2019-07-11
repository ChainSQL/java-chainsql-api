package com.peersafe.chainsql.pool;

import com.peersafe.chainsql.core.Chainsql;

public class ChainsqlUnit {
	private Chainsql c;
	private boolean using;
	private boolean extra = false;
	
	ChainsqlUnit(Chainsql c,boolean using){
		this.c = c;
		this.using = using;
	}
	
	public synchronized Chainsql lock() {
		this.using = true;
		return c;
	}
	
	public synchronized void unlock() {
		this.using = false;
		if(extra) {
			this.c.disconnect();
		}
	}
	
	public void setExtra() {
		extra = true;
	}
	
	public boolean available() {
		return this.using == false;
	}
	
	public Chainsql getChainsql() {
		return c;
	}
}
