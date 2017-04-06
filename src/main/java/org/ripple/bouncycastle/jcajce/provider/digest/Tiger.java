package org.ripple.bouncycastle.jcajce.provider.digest;

import org.ripple.bouncycastle.asn1.iana.IANAObjectIdentifiers;
import org.ripple.bouncycastle.crypto.CipherKeyGenerator;
import org.ripple.bouncycastle.crypto.digests.TigerDigest;
import org.ripple.bouncycastle.crypto.macs.HMac;
import org.ripple.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.ripple.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.ripple.bouncycastle.jcajce.provider.symmetric.util.BaseMac;
import org.ripple.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory;

public class Tiger
{
    private Tiger()
    {

    }

    static public class Digest
        extends BCMessageDigest
        implements Cloneable
    {
        public Digest()
        {
            super(new TigerDigest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            Digest d = (Digest)super.clone();
            d.digest = new TigerDigest((TigerDigest)digest);

            return d;
        }
    }

    /**
     * Tiger HMac
     */
    public static class HashMac
        extends BaseMac
    {
        public HashMac()
        {
            super(new HMac(new TigerDigest()));
        }
    }

    public static class KeyGenerator
        extends BaseKeyGenerator
    {
        public KeyGenerator()
        {
            super("HMACTIGER", 192, new CipherKeyGenerator());
        }
    }

    /**
     * Tiger HMac
     */
    public static class TigerHmac
        extends BaseMac
    {
        public TigerHmac()
        {
            super(new HMac(new TigerDigest()));
        }
    }

    /**
     * PBEWithHmacTiger
     */
    public static class PBEWithMacKeyFactory
        extends PBESecretKeyFactory
    {
        public PBEWithMacKeyFactory()
        {
            super("PBEwithHmacTiger", null, false, PKCS12, TIGER, 192, 0);
        }
    }

    /**
     * PBEWithHmacTiger
     */
    public static class PBEWithHashMac
        extends BaseMac
    {
        public PBEWithHashMac()
        {
            super(new HMac(new TigerDigest()), PKCS12, TIGER, 192);
        }
    }

    public static class Mappings
        extends DigestAlgorithmProvider
    {
        private static final String PREFIX = Tiger.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("MessageDigest.TIGER", PREFIX + "$Digest");
            provider.addAlgorithm("MessageDigest.Tiger", PREFIX + "$Digest"); // JDK 1.1.

            addHMACAlgorithm(provider, "TIGER", PREFIX + "$HashMac", PREFIX + "$KeyGenerator");
            addHMACAlias(provider, "TIGER", IANAObjectIdentifiers.hmacTIGER);

            provider.addAlgorithm("SecretKeyFactory.PBEWITHHMACTIGER", PREFIX + "$PBEWithMacKeyFactory");
        }
    }
}
