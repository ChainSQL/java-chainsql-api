package com.peersafe.base.crypto.sm;

import java.math.BigInteger;
import com.peersafe.base.crypto.ecdsa.IKeyPair;

import com.peersafe.base.utils.HashUtils;
import com.peersafe.chainsql.util.Util;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;


public class SMKeyPair implements IKeyPair {

	BigInteger  priv_;
	BigInteger  pub_;
	byte[]      pubBytes_;
	byte[]      seedBytes_;


	public SMKeyPair(byte[] seedBytes,BigInteger priv, BigInteger pub) {

		this.priv_      = priv;
		this.pub_       = pub;
		this.pubBytes_  = pub.toByteArray();
		this.seedBytes_ = seedBytes;
	}

	public String type(){
		return "softGMAlg";
	}

	public static SMKeyPair generateKeyPair(){

		try{

			AsymmetricCipherKeyPair keyPair     = SM2Util.generateKeyPairParameter();
			ECPrivateKeyParameters priKeyParams = (ECPrivateKeyParameters) keyPair.getPrivate();
			ECPublicKeyParameters pubKeyParams  = (ECPublicKeyParameters)  keyPair.getPublic();

			String pubX = ByteUtils.toHexString(pubKeyParams.getQ().getAffineXCoord().getEncoded()).toUpperCase();
			String pubY = ByteUtils.toHexString(pubKeyParams.getQ().getAffineYCoord().getEncoded()).toUpperCase();

			String publicKeyHex = "47" + pubX + pubY;
			assert  publicKeyHex.length() == 130;

			return new SMKeyPair(null,priKeyParams.getD(),new BigInteger(publicKeyHex,16));

		}catch (Exception e){

			e.printStackTrace();
			return null;
		}

	}

	public static SMKeyPair from256Seed(byte[] seedBytes) {

		assert seedBytes.length == 32;

		BigInteger         privateBig = new BigInteger(Util.bytesToHex(seedBytes),16);

		ECPrivateKeyParameters priKey = new ECPrivateKeyParameters(privateBig,SM2Util.DOMAIN_PARAMS);
		ECPublicKeyParameters  pubKey = BCECUtil.buildECPublicKeyByPrivateKey(priKey);

		String pubX = ByteUtils.toHexString(pubKey.getQ().getAffineXCoord().getEncoded()).toUpperCase();
		String pubY = ByteUtils.toHexString(pubKey.getQ().getAffineYCoord().getEncoded()).toUpperCase();

		String publicKeyHex = "47" + pubX + pubY;
		assert  publicKeyHex.length() == 130;

		return new SMKeyPair(seedBytes,priKey.getD(),new BigInteger(publicKeyHex,16));
	}


	public String canonicalPubHex() {
		return Util.bytesToHex(canonicalPubBytes());
	}


	public BigInteger pub() {
		return  this.pub_;

	}

	private byte[] pubBytes_() {
		return this.pubBytes_;
	}


	public String privHex() {

		String privHex =   this.priv_.toString(16).toUpperCase();
		assert  privHex.length() <= 64;

		// left padding "0"
		return String.format("%64s", privHex).replace(" ","0");
	}


	public BigInteger priv() {
		return  this.priv_;
	}


	public boolean verifySignature(byte[] message, byte[] signedBytes) {

		try {

			assert (this.pubBytes_.length == 65 && this.pubBytes_[0] == 0x47);
			byte[] pubX = new byte[32];
			System.arraycopy(this.pubBytes_, 1, pubX, 0, 32);

			byte[] pubY = new byte[32];
			System.arraycopy(this.pubBytes_, 33, pubY, 0, 32);

			ECPublicKeyParameters pubKey = BCECUtil.createECPublicKeyParameters(pubX, pubY, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);
			byte[] derSigned = SM2Util.encodeSM2SignToDER(signedBytes);
			boolean flag = SM2Util.verify(pubKey,message , derSigned);
			return flag;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public byte[] signMessage(byte[] message)
	{
		try {

			ECPrivateKeyParameters smPrivateKey = new  ECPrivateKeyParameters(this.priv_,SM2Util.DOMAIN_PARAMS);

			// der
			byte[] sign = SM2Util.sign(smPrivateKey,message);
			// r+s
			byte[] rawSign = SM2Util.decodeDERSM2Sign(sign);

			return rawSign;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public byte[] canonicalPubBytes() {

		return  this.pubBytes_;
	}

	public byte[] pub160Hash() {
		return HashUtils.SHA256_RIPEMD160(canonicalPubBytes());
	}

}
