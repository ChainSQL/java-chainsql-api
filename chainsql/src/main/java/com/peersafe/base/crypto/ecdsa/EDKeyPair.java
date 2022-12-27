package com.peersafe.base.crypto.ecdsa;

import com.peersafe.base.encodings.common.B16;
import com.peersafe.base.utils.HashUtils;
import com.peersafe.base.utils.Utils;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PrivateKey;

public class EDKeyPair implements IKeyPair {

    public final EdDSAPrivateKeySpec keySpec;
    public static final EdDSANamedCurveSpec ed25519 = EdDSANamedCurveTable.getByName("ed25519-sha-512");
    /**
     * 
     * @param keySpec keySpec
     */
    public EDKeyPair(EdDSAPrivateKeySpec keySpec) {
        this.keySpec = keySpec;
    }


    public String type(){
        return "ed25519";
    }


    /**
     * 
     * @param seedBytes seed bytes.
     * @return EDKeyPair Keypair.
     */
    public static EDKeyPair from256Seed(byte[] seedBytes) {
        EdDSAPrivateKeySpec keySpec = new EdDSAPrivateKeySpec(seedBytes,
                                                              ed25519);
        return new EDKeyPair(keySpec);
    }
    /**
     * 
     * @param seedBytes SeedBytes.
     * @return EDKeyPair Keypair.
     */
    public static EDKeyPair from128Seed(byte[] seedBytes) {
        assert seedBytes.length == 16;
        return from256Seed(HashUtils.halfSha512(seedBytes));
    }

    @Override
    public String canonicalPubHex() {
        return B16.toString(canonicalPubBytes());
    }

    @Override
    public BigInteger pub() {
        return Utils.uBigInt(pubBytes_());

    }
    /**
     * 
     * @return byte[] value
     */
    private byte[] pubBytes_() {
        return keySpec.getA().toByteArray();
    }

    @Override
    public String privHex() {
        return B16.toString(keySpec.geta());
    }

    @Override
    public BigInteger priv() {
        return Utils.uBigInt(keySpec.geta());
    }

    @Override
    public boolean verifySignature(byte[] message, byte[] sigBytes) {
        try {
            EdDSAEngine sgr = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
            sgr.initVerify(new EdDSAPublicKey(new EdDSAPublicKeySpec(keySpec.getA(), ed25519)));
            sgr.update(message);
            return sgr.verify(sigBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] signMessage(byte[] message) {
        try {
            EdDSAEngine sgr = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
            PrivateKey sKey = new EdDSAPrivateKey(keySpec);
            sgr.initSign(sKey);
            sgr.update(message);
            return sgr.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] canonicalPubBytes() {
        byte[] pub = new byte[33];
        pub[0] = (byte) 0xed;
        System.arraycopy(this.pubBytes_(), 0, pub, 1, 32);
        return pub;
    }

    @Override
    public byte[] pub160Hash() {
        return HashUtils.SHA256_RIPEMD160(canonicalPubBytes());
    }

}
