package org.ripple.bouncycastle.crypto.tls;

import org.ripple.bouncycastle.crypto.CipherParameters;
import org.ripple.bouncycastle.crypto.CryptoException;
import org.ripple.bouncycastle.crypto.DSA;
import org.ripple.bouncycastle.crypto.Digest;
import org.ripple.bouncycastle.crypto.Signer;
import org.ripple.bouncycastle.crypto.digests.NullDigest;
import org.ripple.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.ripple.bouncycastle.crypto.params.ParametersWithRandom;
import org.ripple.bouncycastle.crypto.signers.DSADigestSigner;

public abstract class TlsDSASigner
    extends AbstractTlsSigner
{
    public byte[] generateRawSignature(SignatureAndHashAlgorithm algorithm,
        AsymmetricKeyParameter privateKey, byte[] hash)
        throws CryptoException
    {
        Signer signer = makeSigner(algorithm, true, true,
            new ParametersWithRandom(privateKey, this.context.getSecureRandom()));
        if (algorithm == null)
        {
            // Note: Only use the SHA1 part of the (MD5/SHA1) hash
            signer.update(hash, 16, 20);
        }
        else
        {
            signer.update(hash, 0, hash.length);
        }
        return signer.generateSignature();
    }

    public boolean verifyRawSignature(SignatureAndHashAlgorithm algorithm, byte[] sigBytes,
        AsymmetricKeyParameter publicKey, byte[] hash)
        throws CryptoException
    {
        Signer signer = makeSigner(algorithm, true, false, publicKey);
        if (algorithm == null)
        {
            // Note: Only use the SHA1 part of the (MD5/SHA1) hash
            signer.update(hash, 16, 20);
        }
        else
        {
            signer.update(hash, 0, hash.length);
        }
        return signer.verifySignature(sigBytes);
    }

    public Signer createSigner(SignatureAndHashAlgorithm algorithm, AsymmetricKeyParameter privateKey)
    {
        return makeSigner(algorithm, false, true, privateKey);
    }

    public Signer createVerifyer(SignatureAndHashAlgorithm algorithm, AsymmetricKeyParameter publicKey)
    {
        return makeSigner(algorithm, false, false, publicKey);
    }

    protected CipherParameters makeInitParameters(boolean forSigning, CipherParameters cp)
    {
        return cp;
    }

    protected Signer makeSigner(SignatureAndHashAlgorithm algorithm, boolean raw, boolean forSigning,
        CipherParameters cp)
    {
        if ((algorithm != null) != TlsUtils.isTLSv12(context))
        {
            throw new IllegalStateException();
        }

        if (algorithm != null && algorithm.getSignature() != getSignatureAlgorithm())
        {
            throw new IllegalStateException();
        }

        short hashAlgorithm = algorithm == null ? HashAlgorithm.sha1 : algorithm.getHash();
        Digest d = raw ? new NullDigest() : TlsUtils.createHash(hashAlgorithm);

        Signer s = new DSADigestSigner(createDSAImpl(hashAlgorithm), d);
        s.init(forSigning, makeInitParameters(forSigning, cp));
        return s;
    }

    protected abstract short getSignatureAlgorithm();

    protected abstract DSA createDSAImpl(short hashAlgorithm);
}
