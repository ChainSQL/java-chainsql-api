package org.ripple.bouncycastle.crypto.params;

import java.math.BigInteger;

import org.ripple.bouncycastle.math.ec.ECConstants;
import org.ripple.bouncycastle.math.ec.ECCurve;
import org.ripple.bouncycastle.math.ec.ECPoint;
import org.ripple.bouncycastle.util.Arrays;

public class ECDomainParameters
    implements ECConstants
{
    private ECCurve     curve;
    private byte[]      seed;
    private ECPoint     G;
    private BigInteger  n;
    private BigInteger  h;

    public ECDomainParameters(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n)
    {
        this(curve, G, n, ONE, null);
    }

    public ECDomainParameters(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n,
        BigInteger  h)
    {
        this(curve, G, n, h, null);
    }

    public ECDomainParameters(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n,
        BigInteger  h,
        byte[]      seed)
    {
        this.curve = curve;
        this.G = G.normalize();
        this.n = n;
        this.h = h;
        this.seed = seed;
    }

    public ECCurve getCurve()
    {
        return curve;
    }

    public ECPoint getG()
    {
        return G;
    }

    public BigInteger getN()
    {
        return n;
    }

    public BigInteger getH()
    {
        return h;
    }

    public byte[] getSeed()
    {
        return Arrays.clone(seed);
    }
}
