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
	
	public synchronized void lock() {
		this.using = true;
	}
	
	public synchronized void unlock() {
		this.unlock(false);
	}

	public synchronized void unlock(boolean disconnect) {
		this.using = false;
		if (extra || disconnect) {
			this.c.disconnect();
		}
	}
	
	public void setExtra() {
		extra = true;
	}
	
	public synchronized boolean available() {
		return !this.using;
	}
	
	public Chainsql getChainsql() {
		return c;
	}
}
