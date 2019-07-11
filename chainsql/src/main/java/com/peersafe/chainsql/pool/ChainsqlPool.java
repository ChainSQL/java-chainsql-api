package com.peersafe.chainsql.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.peersafe.base.client.pubsub.Publisher.Callback;
import com.peersafe.chainsql.core.Chainsql;

public class ChainsqlPool {
	private int mMaxCount = 10; 
	private String mWs = "";
	private List<ChainsqlUnit> mListChainsql = new ArrayList<ChainsqlUnit>();
	
	private static ChainsqlPool singleton = new ChainsqlPool();
	public static ChainsqlPool instance() {
		return singleton;
	}
	
	public void init(String url,int count) {
		mWs = url;
		mMaxCount = count;
		for(int i=0; i<mMaxCount; i++) {
			ChainsqlUnit unit = createNewChainsqlUnit();
			mListChainsql.add(unit);
		}
	}
	
	public synchronized ChainsqlUnit getChainsqlUnit() {
		ChainsqlUnit unitTmp = getFromList();
		if(unitTmp == null) {
			unitTmp = createNewChainsqlUnit();
			unitTmp.setExtra();
		}
		unitTmp.lock();
		return unitTmp;
	}
	
	public ChainsqlUnit createNewChainsqlUnit() {
		Chainsql chainsql = new Chainsql();
		chainsql.connect(mWs);
		chainsql.connection.client.logger.setLevel(Level.SEVERE);
		ChainsqlUnit unit = new ChainsqlUnit(chainsql,false);
		return unit;
	}
	
	public synchronized ChainsqlUnit getFromList() {
		for(int i=0; i<mListChainsql.size(); i++) {
			ChainsqlUnit unit = mListChainsql.get(i);
			if(unit.available()) {
				return unit;
			}
		}
		return null;
	}
}
