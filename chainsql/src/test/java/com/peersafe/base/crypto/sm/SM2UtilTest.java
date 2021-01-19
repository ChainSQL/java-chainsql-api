package com.peersafe.base.crypto.sm;

import junit.framework.TestCase;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.Assert;

import java.math.BigInteger;

public class SM2UtilTest extends TestCase {

    public String priHex_  = "722010b6ed5070dc8fed33da3d3299752f727e367a42b8207801984ef5a3a8db";
    public String pubXHex_ = "E3F3306E72959B0E73A33F2FA323CF3D68D78D3EA523B9ACB8333E108AA27941";
    public String pubYHex_ = "5231CF268132A5566BBD42C8993750257FDE73CD1ABC50F97CE651499B087A13";

    public String msgHex_  = "27AAD2A5A9852934438C52BE5304B1E31A5C89790BE106B92A340EC49349BEA2";

    public void testGenerateKeyPairParameter() {

        AsymmetricCipherKeyPair keyPair     = SM2Util.generateKeyPairParameter();
        ECPrivateKeyParameters priKeyParams = (ECPrivateKeyParameters) keyPair.getPrivate();
        ECPublicKeyParameters pubKeyParams  = (ECPublicKeyParameters)  keyPair.getPublic();

        String privateHex = priKeyParams.getD().toString(16);
        String pubX       = ByteUtils.toHexString(pubKeyParams.getQ().getAffineXCoord().getEncoded()).toUpperCase();
        String pubY       = ByteUtils.toHexString(pubKeyParams.getQ().getAffineYCoord().getEncoded()).toUpperCase();

        Assert.assertEquals("私钥的长度为32字节",64,privateHex.length());
        Assert.assertEquals("公钥的X坐标为32字节",64,pubX.length());
        Assert.assertEquals("公钥的Y坐标为32字节",64,pubY.length());
    }


    public void testSign() {

        try{

            ECPrivateKeyParameters priKey = new ECPrivateKeyParameters(
                    new BigInteger(ByteUtils.fromHexString(priHex_)), SM2Util.DOMAIN_PARAMS);

            // der
            byte[] derSign = SM2Util.sign(priKey,ByteUtils.fromHexString(msgHex_));

            ECPublicKeyParameters pubKey = BCECUtil.createECPublicKeyParameters(pubXHex_, pubYHex_, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);
            boolean flag  = SM2Util.verify(pubKey,ByteUtils.fromHexString(msgHex_),derSign);

            // r+s
            byte[] rawSign = SM2Util.decodeDERSM2Sign(derSign);

            Assert.assertEquals("原始签名数据R+S 为64 字节",64,ByteUtils.toHexString(rawSign).toUpperCase().length() / 2);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testVerify() {

        try{

            ECPublicKeyParameters pubKey = BCECUtil.createECPublicKeyParameters(pubXHex_, pubYHex_, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);
            byte[] signed = ByteUtils.fromHexString("0DBD7ED2A2CB528C2FC4E41D7552F9A3EA40FA9EC83EEF3B9E76D12CDB64255D9C40C0881CAF2A9574FD79C4BEE7340E976CE8BE627B68DD52A28F62021E97B1");

            byte[] derSigned = SM2Util.encodeSM2SignToDER(signed);
            boolean flag  = SM2Util.verify(pubKey,ByteUtils.fromHexString(msgHex_),derSigned);
            if (!flag) {
                Assert.fail("verify failed");
            }

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testSignAndVerify() {

        try{

            AsymmetricCipherKeyPair keyPair     = SM2Util.generateKeyPairParameter();
            ECPrivateKeyParameters priKeyParams = (ECPrivateKeyParameters) keyPair.getPrivate();
            ECPublicKeyParameters pubKeyParams  = (ECPublicKeyParameters)  keyPair.getPublic();

            byte[] signedData = SM2Util.sign(priKeyParams,ByteUtils.fromHexString(msgHex_));
            boolean bOK  = SM2Util.verify(pubKeyParams,ByteUtils.fromHexString(msgHex_),signedData);

            if (!bOK) {
                Assert.fail("verify failed");
            }

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }


    public void testEncryptAndDecrypt() {

        try{

            byte[] plainBytes = ByteUtils.fromHexString("F5B6FEF1C44143FB37475F95FE914328");
            byte[] pubBytes   = ByteUtils.fromHexString("47F4A5E131B246F3D884A64EE0FF105A73D240E2BDD5F2133AAAFFF5F346CFAEAA27C3AD6D91B8610C65152EFA1986C90FF455A56202CA5448661D7DA821A671FA");

            byte[] ret  =   SM2Util.encrypt(pubBytes, plainBytes);
            byte[] priv = ByteUtils.fromHexString("32bbdc4cf266bf6d408c2a24354c7283c2b778cef491c60dfc27dd2ae2145681");
            byte[] decryptedPlain =  SM2Util.decrypt(priv, ret);

            Assert.assertEquals("sm2 非对称加解密错误！", ByteUtils.toHexString(plainBytes).toUpperCase(),
                    ByteUtils.toHexString(decryptedPlain).toUpperCase());

        }catch (Exception e){

            e.printStackTrace();
            Assert.fail();
        }

    }


}