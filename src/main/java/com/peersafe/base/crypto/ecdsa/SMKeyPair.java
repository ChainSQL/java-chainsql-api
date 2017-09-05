package com.peersafe.base.crypto.ecdsa;

import java.math.BigInteger;

public class SMKeyPair implements IKeyPair {
    BigInteger priv, pub;
    byte[] pubBytes;
	@Override
	public String canonicalPubHex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] canonicalPubBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger pub() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger priv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String privHex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifySignature(byte[] message, byte[] sigBytes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] signMessage(byte[] message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] pub160Hash() {
		// TODO Auto-generated method stub
		return null;
	}

//	public byte[] static 
}
