package org.dreamwork.secure;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-8-19
 * Time: 下午11:30
 */
public class AlgorithmMapping {
    private static final Map<String, AlgorithmMapping> cache = new HashMap<> ();

    public static AlgorithmMapping find (String uri) {
        return cache.get (uri);
    }

    public static String translate (String uri) throws NoSuchAlgorithmException {
        if (cache.containsKey (uri))
            return cache.get (uri).jceName;

        throw new NoSuchAlgorithmException (uri);
    }

    public static String translateBlockEncryptionAlgorithm (String uri) throws NoSuchAlgorithmException {
        if (BlockEncryption.map.containsKey (uri))
            return BlockEncryption.map.get (uri).jceName;

        throw new NoSuchAlgorithmException (uri);
    }

    public static String translateKeyTransportAlgorithm (String uri) throws NoSuchAlgorithmException {
        if (KeyTransport.map.containsKey (uri))
            return KeyTransport.map.get (uri).jceName;

        throw new NoSuchAlgorithmException (uri);
    }

    public static String translateMacAlgorithm (String uri) throws NoSuchAlgorithmException {
        if (Mac.map.containsKey (uri))
            return Mac.map.get (uri).jceName;

        throw new NoSuchAlgorithmException (uri);
    }

    public static String translateMessageDigestAlgorithm (String uri) throws NoSuchAlgorithmException {
        if (MessageDigest.map.containsKey (uri))
            return MessageDigest.map.get (uri).jceName;

        throw new NoSuchAlgorithmException (uri);
    }

    public static String translateSignatureAlgorithm (String uri) throws NoSuchAlgorithmException {
        if (Signature.map.containsKey (uri))
            return Signature.map.get (uri).jceName;

        throw new NoSuchAlgorithmException (uri);
    }

    public static String translateSymmetricKeyWrapAlgorithm (String uri) throws NoSuchAlgorithmException {
        if (SymmetricKeyWrap.map.containsKey (uri))
            return SymmetricKeyWrap.map.get (uri).jceName;

        throw new NoSuchAlgorithmException (uri);
    }

    public static final class BlockEncryption {
//        private static final Map<String, AlgorithmMapping> map = new HashMap<> ();

        public static final AlgorithmMapping AES128_CBC    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#aes128-cbc", "AES/CBC/ISO10126Padding");
        public static final AlgorithmMapping AES192_CBC    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#aes192-cbc", "AES/CBC/ISO10126Padding");
        public static final AlgorithmMapping AES256_CBC    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES/CBC/ISO10126Padding");
        public static final AlgorithmMapping TRIPLEDES_CBC = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#tripledes-cbc", "DESede/CBC/ISO10126Padding");

        private static final Map<String, AlgorithmMapping> map = Map.of (
                AES128_CBC.uri, AES128_CBC,
                AES192_CBC.uri, AES192_CBC,
                AES256_CBC.uri, AES256_CBC,
                TRIPLEDES_CBC.uri, TRIPLEDES_CBC
        );

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class KeyTransport {
        public static final AlgorithmMapping RSA_1_5        = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#rsa-1_5", "RSA/ECB/PKCS1Padding");
        public static final AlgorithmMapping RSA_OAEP_MGF1P = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", "RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        private static final Map<String, AlgorithmMapping> map = Map.of (
                RSA_1_5.uri, RSA_1_5,
                RSA_OAEP_MGF1P.uri, RSA_OAEP_MGF1P
        );

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class Mac {
        public static final AlgorithmMapping HMAC_MD5       = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-md5", "HmacMD5");
        public static final AlgorithmMapping HMAC_RIPEMD160 = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160", "HMACRIPEMD160");
        public static final AlgorithmMapping HMAC_SHA1      = new AlgorithmMapping ("http://www.w3.org/2000/09/xmldsig#hmac-sha1", "HmacSHA1");
        public static final AlgorithmMapping HMAC_SHA256    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", "HmacSHA256");
        public static final AlgorithmMapping HMAC_SHA384    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-sha384", "HmacSHA384");
        public static final AlgorithmMapping HMAC_SHA512    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-sha512", "HmacSHA512");

        private static final Map<String, AlgorithmMapping> map = Map.of (
                HMAC_MD5.uri, HMAC_MD5,
                HMAC_RIPEMD160.uri, HMAC_RIPEMD160,
                HMAC_SHA1.uri, HMAC_SHA1,
                HMAC_SHA256.uri, HMAC_SHA256,
                HMAC_SHA384.uri, HMAC_SHA384,
                HMAC_SHA512.uri, HMAC_SHA512
        );

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class Signature {
        public static final AlgorithmMapping DSA_SHA1      = new AlgorithmMapping ("http://www.w3.org/2000/09/xmldsig#dsa-sha1", "SHA1withDSA");
        public static final AlgorithmMapping ECDSA_SHA1    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1", "ECDSAwithSHA1");
        public static final AlgorithmMapping RSA_MD5       = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-md5", "MD5withRSA");
        public static final AlgorithmMapping RSA_RIPEMD160 = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160", "RIPEMD160withRSA");
        public static final AlgorithmMapping RSA_SHA1      = new AlgorithmMapping ("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "SHA1withRSA");
        public static final AlgorithmMapping RSA_SHA256    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "SHA256withRSA");
        public static final AlgorithmMapping RSA_SHA384    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", "SHA384withRSA");
        public static final AlgorithmMapping RSA_SHA512    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "SHA512withRSA");

        private static final Map<String, AlgorithmMapping> map = Map.of (
                DSA_SHA1.uri, DSA_SHA1,
                ECDSA_SHA1.uri, ECDSA_SHA1,
                RSA_MD5.uri, RSA_MD5,
                RSA_RIPEMD160.uri, RSA_RIPEMD160,
                RSA_SHA1.uri, RSA_SHA1,
                RSA_SHA256.uri, RSA_SHA256,
                RSA_SHA384.uri, RSA_SHA384,
                RSA_SHA512.uri, RSA_SHA512
        );

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class MessageDigest {
        public static final AlgorithmMapping MD5       = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#md5", "MD5");
        public static final AlgorithmMapping RIPEMD160 = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#ripemd160", "RIPEMD160");
        public static final AlgorithmMapping SHA1      = new AlgorithmMapping ("http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1");
        public static final AlgorithmMapping SHA256    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256");
        public static final AlgorithmMapping SHA384    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#sha384", "SHA-384");
        public static final AlgorithmMapping SHA512    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512");

        private static final Map<String, AlgorithmMapping> map = Map.of (
                MD5.uri, MD5,
                RIPEMD160.uri, RIPEMD160,
                SHA1.uri, SHA1,
                SHA256.uri, SHA256,
                SHA384.uri, SHA384,
                SHA512.uri, SHA512
        );

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class SymmetricKeyWrap {
        public static final AlgorithmMapping KW_AES128    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#kw-aes128", "AESWrap");
        public static final AlgorithmMapping KW_AES192    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#kw-aes192", "AESWrap");
        public static final AlgorithmMapping KW_AES256    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#kw-aes256", "AESWrap");
        public static final AlgorithmMapping KW_TRIPLEDES = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#kw-tripledes", "DESedeWrap");

        private static final Map<String, AlgorithmMapping> map = Map.of (
                KW_AES128.uri, KW_AES128,
                KW_AES192.uri, KW_AES192,
                KW_AES256.uri, KW_AES256,
                KW_TRIPLEDES.uri, KW_TRIPLEDES
        );

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    static {
        cache.putAll (BlockEncryption.map);
        cache.putAll (KeyTransport.map);
        cache.putAll (Mac.map);
        cache.putAll (MessageDigest.map);
        cache.putAll (Signature.map);
        cache.putAll (SymmetricKeyWrap.map);
    }

    public final String uri;
    public final String jceName;

    private AlgorithmMapping (String uri, String jceName) {
        this.jceName = jceName;
        this.uri = uri;
    }
}