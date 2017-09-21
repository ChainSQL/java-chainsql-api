package com.peersafe.base.crypto.sm;

import java.math.BigInteger;
import java.util.Vector;

import cn.com.sansec.key.CertInfo;
import cn.com.sansec.key.SWJAPI;
import cn.com.sansec.key.exception.SDKeyException;

import com.peersafe.base.config.Config;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.hash.prefixes.HashPrefix;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.utils.HashUtils;

public class SMKeyPair implements IKeyPair {
    BigInteger priv, pub;
    byte[] pubBytes = new byte[65];
    
    private SWJAPI sdkey;

    public SMKeyPair() throws Exception{
    	this.sdkey = SMDevice.sdkey;
    	if(Config.isNewKeyPair()){
    		boolean bRet = genEccKeyPair();
    		if(!bRet){
    			throw new Exception("GenEccKeyPair failed");
    		}
    		Config.setNewKeyPair(false);
    	}else if(!isContainerExist()){
    		boolean bRet = genEccKeyPair();
    		if(!bRet){
    			throw new Exception("GenEccKeyPair failed");
    		}
    	}
		byte[] publicBytes = sdkey.GetEccPublicKey(SMDevice.getContainerName(), 1);
		//国密首字节是0X47
		pubBytes[0] = 0x47;
		System.arraycopy(publicBytes, 0, pubBytes, 1, publicBytes.length);
    }
    
    public boolean  genEccKeyPair() {
        if (sdkey == null) {
            return false;
        }
        try {
           int  reValue = sdkey.GenEccKeyPair(SMDevice.getContainerName(), 1);
           return reValue == 0;
        } catch (SDKeyException e) {
            return false;
        }
    }
    public boolean isContainerExist(){
    	try {
    		Vector list = sdkey.getKeysList();//获取密钥对的列表，如果需要获取证书列表，请调用getCertsList(参考certRWTest)
			if (list == null) 
			{
				return false;
			}
			else
			{
				int num = list.size();
				CertInfo keyInfo = null;
				for (int i=0; i<num; i++) {
					keyInfo = (CertInfo) list.get(i);
					if(keyInfo.getAlias().equals(SMDevice.getContainerName()))
						return true;
				}
			}
		} catch (SDKeyException e) {
			e.printStackTrace();;
		}
    	return false;
    }
    
	@Override
	public String canonicalPubHex() {
		return null;
	}

	@Override
	public byte[] canonicalPubBytes() {
		return pubBytes;
	}

	@Override
	public BigInteger pub() {
		return null;
	}

	@Override
	public BigInteger priv() {
		return null;
	}

	@Override
	public String privHex() {
		return null;
	}

	@Override
	public boolean verifySignature(byte[] message, byte[] sigBytes) {
		return false;
	}

    public byte[] signHash(byte[] hash) throws SDKeyException {
        return sdkey.EccSign(SMDevice.getContainerName(), 1, hash);
    }
    
	@Override
	public byte[] signMessage(byte[] message) {
		try {
			int ctx = sdkey.HashInit(4);
			sdkey.HashUpdate(ctx, message);
			byte[] hash = sdkey.HashFinal(ctx);
			return signHash(hash);
		} catch (SDKeyException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] pub160Hash() {
		return HashUtils.SHA256_RIPEMD160(pubBytes);
	}
}
