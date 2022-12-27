package com.peersafe.chainsql.pool;

import java.util.ArrayList;
import java.util.List;

import com.peersafe.chainsql.core.Chainsql;

public class ChainsqlPool {
	private String mWs = "";
	private final List<ChainsqlUnit> mListChainsql = new ArrayList<ChainsqlUnit>();

	private static final ChainsqlPool singleton = new ChainsqlPool();
	public static ChainsqlPool instance() {
		return singleton;
	}

	public void init(String url, int count) {
		mWs = url;
		for (int i = 0; i< count; i++) {
			ChainsqlUnit unit = createNewChainsqlUnit();
			mListChainsql.add(unit);
		}
	}

	public synchronized ChainsqlUnit getChainsqlUnit() {
		ChainsqlUnit unitTmp = getFromList();
		if (unitTmp == null) {
			unitTmp = createNewChainsqlUnit();
			unitTmp.setExtra();
		}
		unitTmp.lock();
		return unitTmp;
	}

	public void shutdown() {
		for (ChainsqlUnit chainsqlUnit : mListChainsql) {
			chainsqlUnit.unlock(true);
		}
	}

	private ChainsqlUnit createNewChainsqlUnit() {
		Chainsql chainsql = new Chainsql();
		chainsql.connect(mWs);
		return new ChainsqlUnit(chainsql,false);
	}

	private synchronized ChainsqlUnit getFromList() {
		for (ChainsqlUnit unit : mListChainsql) {
			if (unit.available()) {
				return unit;
			}
		}
		return null;
	}
}
