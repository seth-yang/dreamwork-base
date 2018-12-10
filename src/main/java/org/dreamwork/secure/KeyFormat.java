package org.dreamwork.secure;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-11-28
 * Time: 下午11:54
 */
public class KeyFormat {
    public static enum PrivateKeyFormat {
        PKCS1, PKCS8, PKCS16
    }
    public static enum PublicKeyFormat {
        X509
    }
    public static enum KeyEncoding {
        Binary, Base64
    }
}