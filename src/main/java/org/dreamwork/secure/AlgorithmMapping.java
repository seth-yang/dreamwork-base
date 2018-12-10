package org.dreamwork.secure;

import com.google.gson.annotations.Expose;

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
    private static final Map<String, AlgorithmMapping> cache = new HashMap<String, AlgorithmMapping> ();

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
        private static final Map<String, AlgorithmMapping> map = new HashMap<String, AlgorithmMapping> ();

        public static final AlgorithmMapping AES128_CBC    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#aes128-cbc", "AES/CBC/ISO10126Padding");
        public static final AlgorithmMapping AES192_CBC    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#aes192-cbc", "AES/CBC/ISO10126Padding");
        public static final AlgorithmMapping AES256_CBC    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES/CBC/ISO10126Padding");
        public static final AlgorithmMapping TRIPLEDES_CBC = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#tripledes-cbc", "DESede/CBC/ISO10126Padding");

        static {
            map.put (AES128_CBC.uri, AES128_CBC);
            map.put (AES192_CBC.uri, AES192_CBC);
            map.put (AES256_CBC.uri, AES256_CBC);
            map.put (TRIPLEDES_CBC.uri, TRIPLEDES_CBC);
        }

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class KeyTransport {
        private static final Map<String, AlgorithmMapping> map = new HashMap<String, AlgorithmMapping> ();

        public static final AlgorithmMapping RSA_1_5        = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#rsa-1_5", "RSA/ECB/PKCS1Padding");
        public static final AlgorithmMapping RSA_OAEP_MGF1P = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", "RSA/ECB/OAEPWithSHA1AndMGF1Padding");

        static {
            map.put (RSA_1_5.uri, RSA_1_5);
            map.put (RSA_OAEP_MGF1P.uri, RSA_OAEP_MGF1P);
        }

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class Mac {
        private static final Map<String, AlgorithmMapping> map = new HashMap<String, AlgorithmMapping> ();

        public static final AlgorithmMapping HMAC_MD5       = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-md5", "HmacMD5");
        public static final AlgorithmMapping HMAC_RIPEMD160 = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160", "HMACRIPEMD160");
        public static final AlgorithmMapping HMAC_SHA1      = new AlgorithmMapping ("http://www.w3.org/2000/09/xmldsig#hmac-sha1", "HmacSHA1");
        public static final AlgorithmMapping HMAC_SHA256    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", "HmacSHA256");
        public static final AlgorithmMapping HMAC_SHA384    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-sha384", "HmacSHA384");
        public static final AlgorithmMapping HMAC_SHA512    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#hmac-sha512", "HmacSHA512");

        static {
            map.put (HMAC_MD5.uri, HMAC_MD5);
            map.put (HMAC_RIPEMD160.uri, HMAC_RIPEMD160);
            map.put (HMAC_SHA1.uri, HMAC_SHA1);
            map.put (HMAC_SHA256.uri, HMAC_SHA256);
            map.put (HMAC_SHA384.uri, HMAC_SHA384);
            map.put (HMAC_SHA512.uri, HMAC_SHA512);
        }

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class Signature {
        private static final Map<String, AlgorithmMapping> map = new HashMap<String, AlgorithmMapping> ();

        public static final AlgorithmMapping DSA_SHA1      = new AlgorithmMapping ("http://www.w3.org/2000/09/xmldsig#dsa-sha1", "SHA1withDSA");
        public static final AlgorithmMapping ECDSA_SHA1    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1", "ECDSAwithSHA1");
        public static final AlgorithmMapping RSA_MD5       = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-md5", "MD5withRSA");
        public static final AlgorithmMapping RSA_RIPEMD160 = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160", "RIPEMD160withRSA");
        public static final AlgorithmMapping RSA_SHA1      = new AlgorithmMapping ("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "SHA1withRSA");
        public static final AlgorithmMapping RSA_SHA256    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "SHA256withRSA");
        public static final AlgorithmMapping RSA_SHA384    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", "SHA384withRSA");
        public static final AlgorithmMapping RSA_SHA512    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "SHA512withRSA");

        static {
            map.put (DSA_SHA1.uri, DSA_SHA1);
            map.put (ECDSA_SHA1.uri, ECDSA_SHA1);
            map.put (RSA_MD5.uri, RSA_MD5);
            map.put (RSA_RIPEMD160.uri, RSA_RIPEMD160);
            map.put (RSA_SHA1.uri, RSA_SHA1);
            map.put (RSA_SHA256.uri, RSA_SHA256);
            map.put (RSA_SHA384.uri, RSA_SHA384);
            map.put (RSA_SHA512.uri, RSA_SHA512);
        }

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class MessageDigest {
        private static final Map<String, AlgorithmMapping> map = new HashMap<String, AlgorithmMapping> ();

        public static final AlgorithmMapping MD5       = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#md5", "MD5");
        public static final AlgorithmMapping RIPEMD160 = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#ripemd160", "RIPEMD160");
        public static final AlgorithmMapping SHA1      = new AlgorithmMapping ("http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1");
        public static final AlgorithmMapping SHA256    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256");
        public static final AlgorithmMapping SHA384    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmldsig-more#sha384", "SHA-384");
        public static final AlgorithmMapping SHA512    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512");

        static {
            map.put (MD5.uri, MD5);
            map.put (RIPEMD160.uri, RIPEMD160);
            map.put (SHA1.uri, SHA1);
            map.put (SHA256.uri, SHA256);
            map.put (SHA384.uri, SHA384);
            map.put (SHA512.uri, SHA512);
        }

        public static AlgorithmMapping find (String uri) {
            return map.get (uri);
        }
    }

    public static final class SymmetricKeyWrap {
        private static final Map<String, AlgorithmMapping> map = new HashMap<String, AlgorithmMapping> ();

        public static final AlgorithmMapping KW_AES128    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#kw-aes128", "AESWrap");
        public static final AlgorithmMapping KW_AES192    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#kw-aes192", "AESWrap");
        public static final AlgorithmMapping KW_AES256    = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#kw-aes256", "AESWrap");
        public static final AlgorithmMapping KW_TRIPLEDES = new AlgorithmMapping ("http://www.w3.org/2001/04/xmlenc#kw-tripledes", "DESedeWrap");

        static {
            map.put (KW_AES128.uri, KW_AES128);
            map.put (KW_AES192.uri, KW_AES192);
            map.put (KW_AES256.uri, KW_AES256);
            map.put (KW_TRIPLEDES.uri, KW_TRIPLEDES);
        }

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

    @Expose
    public final String uri;
    @Expose
    public final String jceName;

    private AlgorithmMapping (String uri, String jceName) {
        this.jceName = jceName;
        this.uri = uri;
    }



/*
    public static void main (String[] args) throws Exception {
        URL url = AlgorithmMapping.class.getResource ("/org/apache/xml/security/resource/config.xml");
        Document doc = XMLUtil.parse (url);
        NodeList list = doc.getElementsByTagName ("JCEAlgorithmMappings");
        Element e = (Element) list.item (0);
        list = e.getElementsByTagName ("Algorithms");
        Node node = list.item (0);
        list = node.getChildNodes ();

        Map<String, Map<String, Holder>> groups = new TreeMap<String, Map<String, Holder>> ();

//        System.out.println ("static {");
        for (int i = 0; i < list.getLength (); i ++) {
            node = list.item (i);
            if (node.getNodeType () != Node.ELEMENT_NODE) continue;

            e = (Element) node;

            String uri = e.getAttribute ("URI");
            String className = e.getAttribute ("AlgorithmClass");
            String jceName = e.getAttribute ("JCEName");

            Map<String, Holder> map = groups.get (className);
            if (map == null) {
                map = new TreeMap<String, Holder> ();
                groups.put (className, map);
            }

            int pos = uri.lastIndexOf ('/');
            String varName = uri.substring (pos + 1);
            varName = varName.toUpperCase ().replace ('-', '_').replace ('#', '_');
            varName = varName.replaceAll ("XMLDSIG_(MORE_)?|XMLENC_", "");

            Holder holder = new Holder (varName, uri, jceName);
            map.put (varName, holder);
        }

*/
/*
        for (String key : groups.keySet ()) {
            System.out.printf ("\tpublic static final class %s {%n", key);
            System.out.printf ("\t\tprivate static final Map<String, AlgorithmMapping> map = new HashMap<String, AlgorithmMapping> ();%n%n");
            Map<String, Holder> map = groups.get (key);

            int length = getLength (map.keySet ());

            for (Holder holder : map.values ()) {
                String expression = String.format (
                        "public static final AlgorithmMapping %s = new AlgorithmMapping (\"%s\", \"%s\");",
                        getFixString (holder.varName, length),
                        holder.uri,
                        holder.jceName
                );
                System.out.println ("\t\t" + expression);
            }

            System.out.printf ("%n\t\tstatic {%n");
            for (String name : map.keySet ()) {
                System.out.printf ("\t\t\tmap.put (%s.uri, %s);%n", name, name);
            }
            System.out.println ("\t\t}");
            System.out.println ();
            System.out.println ("\t\tpublic static AlgorithmMapping find (String uri) {");
            System.out.println ("\t\t\treturn map.get (uri);");
            System.out.println ("\t\t}");
            System.out.println ("\t}");
            System.out.println ();
        }

        System.out.println ("\tstatic {");
        for (String key : groups.keySet ()) {
            Map<String, Holder> map = groups.get (key);
            for (String name : map.keySet ()) {
                String varName = key + '.' + name;
                String expression = String.format ("cache.put (%s.uri, %s);", varName, varName);
                System.out.println ("\t\t" + expression);
            }
        }
        System.out.println ("\t}");
*//*

        for (String name : groups.keySet ()) {
            System.out.printf ("\tpublic static String translate%sAlgorithm (String uri) throws NoSuchAlgorithmException {%n", name);
            System.out.printf ("\t\tif (%s.map.containsKey (uri))%n", name);
            System.out.printf ("\t\t\treturn %s.map.get (uri).jceName;%n", name);
            System.out.println ();
            System.out.println ("\t\tthrow new NoSuchAlgorithmException (uri);");
            System.out.println ("\t}");
        }
    }

    private static int getLength (Collection<String> c) {
        int max = 0;
        for (String s : c) {
            if (s.length () > max) max = s.length ();
        }
        return max;
    }

    private static String getFixString (String text, int length) {
        StringBuilder builder = new StringBuilder (text);
        for (int i = text.length (); i < length; i ++) {
            builder.append (' ');
        }
        return builder.toString ();
    }

    private static class Holder {
        String varName, uri, jceName;

        private Holder (String varName, String uri, String jceName) {
            this.varName = varName;
            this.uri = uri;
            this.jceName = jceName;
        }

        public String toString () {
            return String.format (
                    "public static final AlgorithmMapping %s = new AlgorithmMapping (\"%s\", \"%s\");",
                    varName,
                    uri,
                    jceName
            );
        }
    }
*/
}