package com.peersafe.base.crypto.sm;

public class SMDigest {
//	public static Hash256 getTransactionHash(HashPrefix prefix,byte[] blob){
//		try {
//			SWJAPI sdkey = SMDevice.sdkey;
//			if(sdkey == null)
//				return null;
//			int ctx = sdkey.HashInit(4);
//			int ctx1 = sdkey.HashUpdate(ctx, prefix.bytes());
//			if(ctx1 != 0)
//				return null;
//			int ctx2 = sdkey.HashUpdate(ctx, blob);
//			if(ctx2 != 0)
//				return null;
//			byte[] hashBytes =  sdkey.HashFinal(ctx);
//			return new Hash256(hashBytes);
//		} catch (SDKeyException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
}
