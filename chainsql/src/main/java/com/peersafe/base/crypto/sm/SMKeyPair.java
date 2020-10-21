package com.peersafe.base.crypto.sm;

import java.math.BigInteger;

import com.peersafe.base.crypto.ecdsa.EDKeyPair;
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

			//System.out.println("私钥 : " + priKeyParams.getD().toString(16).toUpperCase());
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

	/**
	 *
	 * @param seedBytes SeedBytes.
	 * @return EDKeyPair Keypair.
	 */
	public static SMKeyPair from128Seed(byte[] seedBytes) {
		assert seedBytes.length == 16;
		return from256Seed(HashUtils.halfSha512(seedBytes));
	}

	public static SMKeyPair from256Seed(byte[] seedBytes) {

		assert seedBytes.length == 32;

		BigInteger privateBig =  new BigInteger(Util.bytesToHex(seedBytes),16);
		ECPrivateKeyParameters priKey = new  ECPrivateKeyParameters(privateBig,SM2Util.DOMAIN_PARAMS);

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
		return this.priv_.toString(16);
	}


	public BigInteger priv() {
		return  this.priv_;
	}


	public boolean verifySignature(byte[] message, byte[] sigBytes) {

		try {

			byte[] pub = new byte[33];
			System.arraycopy(this.pubBytes_, 1, pub, 0, 33);

			ECPoint pt =   SM2Util.CURVE.decodePoint(pub);
			ECPublicKeyParameters sm2PublicKey = new  ECPublicKeyParameters(pt,SM2Util.DOMAIN_PARAMS);
			boolean flag = SM2Util.verify(sm2PublicKey,message , sigBytes);
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
