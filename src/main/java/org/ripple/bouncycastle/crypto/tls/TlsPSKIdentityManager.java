package org.ripple.bouncycastle.crypto.tls;

public interface TlsPSKIdentityManager
{
    byte[] getHint();

    byte[] getPSK(byte[] identity);
}
