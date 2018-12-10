package org.dreamwork.secure;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2014/12/8
 * Time: 15:26
 */
public class SimpleKeyFetcherFactory extends KeyFetcherFactory {
    private IKeyFetcher fetcher;
    @Override
    public IKeyFetcher getKeyFetcher () {
        if (fetcher == null) {
            fetcher = new SimpleKeyFetcher ();
        }
        return fetcher;
    }

    public static class SimpleKeyFetcher implements IKeyFetcher {
        @Override
        public PrivateKey getPrivateKey (String issuer) {
            return null;
        }

        @Override
        public PublicKey getPublicKey (String issuer) {
            return null;
        }

        @Override
        public X509Certificate getCertificate (String issuer) {
            return null;
        }
    }
}