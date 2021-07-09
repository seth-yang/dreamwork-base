package org.dreamwork.secure;

import org.dreamwork.util.StringUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-11-28
 * Time: 下午4:33
 */
public class SecureUtil {
    public static final String KEY_SECURE_PROVIDER = "org.dreamwork.secure.provider";
    private static Provider provider;

    private KeyGenerator g;
    private SecureContext context;

    static {
        String u = System.getProperty (KEY_SECURE_PROVIDER);
        if (!StringUtil.isEmpty (u)) try {
            provider = (Provider) Class.forName (u).newInstance ();
        } catch (Exception ex) {
            //
        }

        if (provider == null) {
            InputStream in = null;
            try {
                Enumeration<URL> urls = SecureUtil.class.getClassLoader ().getResources ("META-INF/provider.properties");
                while (urls.hasMoreElements ()) {
                    URL url = urls.nextElement ();
                    in = url.openStream ();
                    Properties props = new Properties ();
                    props.load (in);
                    String className = props.getProperty (KEY_SECURE_PROVIDER);
                    if (className != null) {
                        provider = (Provider) Class.forName (className).newInstance ();
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException (ex);
            } finally {
                if (in != null) try {
                    in.close ();
                } catch (IOException ex) {
                    //
                }
            }
        }

        if (provider != null) {
            Security.addProvider (provider);
        }
    }

    public SecureUtil (SecureContext context) {
        this.context = context;
        try {
            if (provider != null)
                g = KeyGenerator.getInstance (context.getBlockEncryptionAlgorithmName (), provider);
            else {
                g = KeyGenerator.getInstance (context.getBlockEncryptionAlgorithmName ());
            }
            g.init (128);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException (ex);
        }
    }

    public static Provider getProvider () {
        return provider;
    }

    /**
     * 使用 客户端 公钥来加密请求数据
     * @param buff mina 远程请求
     * @param kek 用于加密 AES 秘钥的公钥或私钥
     * @return 加密后的结果
     * @throws Exception 任何异常
     */
    public byte[] encrypt (byte[] buff, Key kek) throws Exception {
        Key key = g.generateKey ();
        Cipher c = getCipher (context.getKeyTransport ().jceName);
        c.init (Cipher.WRAP_MODE, kek);
        byte[] encryptedKey = c.wrap (key);

        c = getCipher (context.getBlockEncryption ().jceName);
        Random random = new SecureRandom ();
        byte[] iv = new byte [c.getBlockSize ()];
        random.nextBytes (iv);
        IvParameterSpec ivp = new IvParameterSpec (iv);
        c.init (Cipher.ENCRYPT_MODE, key, ivp);
        byte[] body = c.doFinal (buff);

        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        DataOutputStream dos = new DataOutputStream (baos);
        dos.write (iv.length);
        dos.write (iv);
        dos.writeChar (encryptedKey.length);
        dos.write (encryptedKey);
        dos.writeInt (body.length);
        dos.write (body);
        dos.flush ();

        return baos.toByteArray ();
    }

    /**
     * 使用 私钥 来解密客户端请求
     * @param buffer 远程请求
     * @param kek 用于解密 AES 秘钥的公钥或私钥
     * @return 解密后的结果
     * @throws Exception 任何异常
     */
    public byte[] decrypt (byte[] buffer, Key kek) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream (buffer);
        DataInputStream dis = new DataInputStream (bais);

        int length = dis.read (), read;
        byte[] iv = new byte[length];
        if ((read = dis.read (iv)) != length)
            throw new IOException ("expect " + length + " bytes, but read " + read + " bytes.");

        length = dis.readChar ();
        byte[] encryptedKey = new byte[length];
        if ((read = dis.read (encryptedKey)) != length)
            throw new IOException ("expect " + length + " bytes, but read " + read + " bytes.");

        length = dis.readInt ();
        byte[] body = new byte[length];
        if ((read = dis.read (body)) != length)
            throw new IOException ("expect " + length + " bytes, but read " + read + " bytes.");

        IvParameterSpec ivp = new IvParameterSpec (iv);

        Cipher c = getCipher (context.getKeyTransport ().jceName);
        c.init (Cipher.UNWRAP_MODE, kek);
        Key key = c.unwrap (encryptedKey, context.getBlockEncryptionAlgorithmName (), Cipher.SECRET_KEY);

        c = getCipher (context.getBlockEncryption ().jceName);
        c.init (Cipher.DECRYPT_MODE, key, ivp);
        body = c.doFinal (body);
        return body;
    }

    public byte[] sign (PrivateKey key, byte[] data) throws Exception {
        Signature signer =
                provider == null ?
                Signature.getInstance (context.getSignature ().jceName) :
                Signature.getInstance (context.getSignature ().jceName, provider);
        signer.initSign (key);
        signer.update (data);
        return signer.sign ();
    }

    public boolean verify (PublicKey key, byte[] raw, byte[] signedData) throws Exception {
        Signature signature =
            provider == null ?
                Signature.getInstance (context.getSignature ().jceName) :
                Signature.getInstance (context.getSignature ().jceName, provider);
        signature.initVerify (key);
        signature.update (raw);
        return signature.verify (signedData);
    }

    private Cipher getCipher (String name) throws NoSuchPaddingException, NoSuchAlgorithmException {
        return provider == null ?
                Cipher.getInstance (name) :
                Cipher.getInstance (name, provider);
    }
}