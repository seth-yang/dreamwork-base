package org.dreamwork.secure;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-12-5
 * Time: 下午3:36
 */
public class OpenSSLRSAKeyFactory extends RSAKeyFactory {
    private static final String COMMAND_PATTERN = "{0} genrsa -out {1} {2}";

    protected OpenSSLRSAKeyFactory (Provider provider) {
        super (provider);
    }

    @Override
    public PrivateKey getPrivateKey (int length) throws Exception {
        return null;
    }

    @Override
    public PublicKey generatePublicKeyByPrivateKey (PrivateKey privateKey) throws Exception {
        return null;
    }

    private void generatePrivateKey (int length) {

    }
}
