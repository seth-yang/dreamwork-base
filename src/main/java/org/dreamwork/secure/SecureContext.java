package org.dreamwork.secure;

import com.google.gson.annotations.Expose;

import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2014/12/8
 * Time: 14:26
 */
public class SecureContext {
    @Expose
    private AlgorithmMapping blockEncryption, keyTransport, mac, signature, messageDigest, symmetricKeyWrap;
    private IKeyFetcher fetcher;

    public AlgorithmMapping getBlockEncryption () {
        return blockEncryption;
    }

    public String getBlockEncryptionAlgorithmName () throws NoSuchAlgorithmException {
        String name = blockEncryption.jceName.toLowerCase ();
        if (name.contains ("aes")) return "AES";
        if (name.contains ("des")) return "DES";

        throw new NoSuchAlgorithmException (blockEncryption.jceName);
    }

    public void setBlockEncryption (AlgorithmMapping blockEncryption) {
        this.blockEncryption = blockEncryption;
    }

    public AlgorithmMapping getKeyTransport () {
        return keyTransport;
    }

    public void setKeyTransport (AlgorithmMapping keyTransport) {
        this.keyTransport = keyTransport;
    }

    public AlgorithmMapping getMac () {
        return mac;
    }

    public void setMac (AlgorithmMapping mac) {
        this.mac = mac;
    }

    public AlgorithmMapping getSignature () {
        return signature;
    }

    public void setSignature (AlgorithmMapping signature) {
        this.signature = signature;
    }

    public AlgorithmMapping getMessageDigest () {
        return messageDigest;
    }

    public void setMessageDigest (AlgorithmMapping messageDigest) {
        this.messageDigest = messageDigest;
    }

    public AlgorithmMapping getSymmetricKeyWrap () {
        return symmetricKeyWrap;
    }

    public void setSymmetricKeyWrap (AlgorithmMapping symmetricKeyWrap) {
        this.symmetricKeyWrap = symmetricKeyWrap;
    }

    public IKeyFetcher getFetcher () {
        return fetcher;
    }

    public void setFetcher (IKeyFetcher fetcher) {
        this.fetcher = fetcher;
    }
}