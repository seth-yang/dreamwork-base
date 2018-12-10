package org.dreamwork.secure;

import org.dreamwork.util.StringUtil;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-12-4
 * Time: 下午3:39
 */
public abstract class RSAKeyFactory {
    public static final String KEY_FACTORY_INSTANCE = "org.dreamwork.rsa.key.factory";
    protected Provider provider;

    private static DefaultRSAKeyFactory defaultFactory = null;

    protected RSAKeyFactory (Provider provider) {
        this.provider = provider;
    }

    public static RSAKeyFactory newInstance () throws Exception {
        return findImplementation (null);
    }

    /**
     * 创建新的工厂实例
     * @return 秘钥工厂实例
     * @throws Exception
     */
    public static RSAKeyFactory newInstance (Provider provider) throws Exception {
        return findImplementation (provider);
    }

    /**
     * 获取指定长度的私钥
     * @return  私钥
     * @throws Exception
     */
    public abstract PrivateKey getPrivateKey (int length) throws Exception;

    /**
     * 根据指定的私钥，获取公钥
     * @param privateKey 指定的私钥
     * @return  和私钥相匹配的公钥
     * @throws Exception
     */
    public abstract PublicKey generatePublicKeyByPrivateKey (PrivateKey privateKey) throws Exception;

    private static RSAKeyFactory findImplementation (Provider provider) throws Exception {
        String className = System.getProperty (KEY_FACTORY_INSTANCE);
        if (!StringUtil.isEmpty (className)) {
            return loadImplementation (className, provider);
        }

        Enumeration<URL> e = RSAKeyFactory.class.getClassLoader ().getResources ("META-INF/rsa.key.properties");
        if (e != null) while (e.hasMoreElements ()) {
            URL url = e.nextElement ();
            InputStream in = url.openStream ();
            try {
                Properties props = new Properties ();
                props.load (in);
                if (props.containsKey (KEY_FACTORY_INSTANCE)) {
                    className = props.getProperty (KEY_FACTORY_INSTANCE);
                    return loadImplementation (className, provider);
                }
            } finally {
                if (in != null)
                    in.close ();
            }
        }

        if (defaultFactory == null) {
            defaultFactory = new DefaultRSAKeyFactory (provider);
        }
        return defaultFactory;
    }

    private static RSAKeyFactory loadImplementation (String name, Provider provider) throws Exception {
        Class<?> type = Class.forName (name);
        if (RSAKeyFactory.class.isAssignableFrom (type)) {
            Constructor c = type.getDeclaredConstructor (Provider.class);
            return (RSAKeyFactory) c.newInstance (provider);
        }
        throw new ClassCastException ("Can't cast " + name + " to " + RSAKeyFactory.class.getCanonicalName ());
    }

    private static final class DefaultRSAKeyFactory extends RSAKeyFactory {
        private static final Map<PrivateKey, PublicKey> cache = new HashMap<PrivateKey, PublicKey> ();

        private KeyPairGenerator generator;

        static {
            File file = new File (System.getProperty ("java.io.temp"));
        }

        protected DefaultRSAKeyFactory (Provider provider) {
            super (provider);
            try {
                if (provider != null)
                    generator = KeyPairGenerator.getInstance ("RSA", provider);
                else
                    generator = KeyPairGenerator.getInstance ("RSA");
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException (ex);
            }
        }

        @Override
        public synchronized PrivateKey getPrivateKey (int length) throws Exception {
            generator.initialize (length);
            KeyPair pair = generator.generateKeyPair ();
            PrivateKey key = pair.getPrivate ();
            cache.put (key, pair.getPublic ());
            return key;
        }

        @Override
        public synchronized PublicKey generatePublicKeyByPrivateKey (PrivateKey privateKey) throws Exception {
            if (cache.containsKey (privateKey))
                return cache.get (privateKey);

            throw new Exception ("Can't generate public with privateKey");
        }
    }
}