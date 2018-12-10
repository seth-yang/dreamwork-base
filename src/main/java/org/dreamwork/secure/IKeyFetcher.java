package org.dreamwork.secure;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2014/12/8
 * Time: 15:21
 */
public interface IKeyFetcher {
    PrivateKey getPrivateKey (String issuer) throws NoSuchAlgorithmException, InvalidKeySpecException;
    PublicKey getPublicKey (String issuer) throws NoSuchAlgorithmException, InvalidKeySpecException;
    X509Certificate getCertificate (String issuer) throws CertificateException;
}