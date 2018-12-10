package org.dreamwork.misc;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * <p>Title: 提供Base64编码及Zip压缩支持</p>
 * <p>Description: tools</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: topfounder</p>
 *
 * @author seth yang
 * @version 1.0
 */
@Deprecated
public class BASE64Zipper {
    private static final String algorithm = "DES";

    private static final byte[] key = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf};

    private static final char[] letters = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    /**
     * 文本编码方式
     */
    private String enc = System.getProperty ("file.encoding");

    /**
     * 设置文本编码方式
     *
     * @param encoding 编码方式
     */
    public void setEncoding (String encoding) {
        this.enc = encoding;
    }

    /**
     * 获取文本编码方式
     *
     * @return String 文本编方式
     */
    public String getEncoding () {
        return this.enc;
    }

    /**
     * 用Zip算法压缩字节数组
     *
     * @param src 源字节数组
     * @return 压缩后的字节数据
     * @throws java.io.IOException io exception
     */
    public byte[] zip (byte[] src) throws IOException {
        ZipEntry entry = new ZipEntry ("test");
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream (baos);
            zos.putNextEntry (entry);
            zos.write (src);
        } finally {
            if (zos != null) zos.close ();
        }
        return baos.toByteArray ();
    }

    /**
     * 解压缩字节数据
     *
     * @param zipped 压缩过的数据
     * @return 解压后的数据
     * @throws IOException io exception
     */
    public byte[] unzip (byte[] zipped) throws IOException {
        ZipInputStream zis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        try {
            zis = new ZipInputStream (new ByteArrayInputStream (zipped));
            ZipEntry entry = zis.getNextEntry ();
            byte[] b = new byte[1024*100];

            int len;
            while ((len = zis.read (b)) > 0) {
                baos.write (b, 0, len);
            }
            return baos.toByteArray ();
        } finally {
            if (zis != null) zis.close ();
        }
    }

    /**
     * 对字节数据进行Base64编码
     *
     * @param src 指定的字节流
     * @return 经Base64算法编码后的结果字符串
     * @throws IOException io exception
     */
    public String Base64Encode (byte[] src) throws IOException {
/*
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder ();
        return encoder.encode (src);
*/
        return new String(Base64.encode (src), this.enc);
    }

    /**
     * 将经Base64编码过的字符串还原为字节流
     *
     * @param s 经Base64编码的字符串
     * @return 还原后的字节流
     * @throws IOException io exception
     */
    public byte[] Base64Decode (String s) throws IOException {
        return Base64.decode (s);
/*
        sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder ();
        return decoder.decodeBuffer (s);
*/
    }

    /**
     * 先用zip压缩给定的字符串，然后对结果字节数据进行Base64编码
     *
     * @param src 指定的字节流
     * @return 经Zip压缩再经Base64编码后的结果字符串
     * @throws IOException io exception
     */
    public String Base64ZipEncode (byte[] src) throws IOException {
/*
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder ();
        return encoder.encode (zip (src));
*/
        return new String(Base64.encode (zip (src)), enc);
    }

    /**
     * 还原先用zip压缩给定的字符串，然后对结果字节数据进行Base64编码的字符串
     *
     * @param str 指定的字符串
     * @return 还原后的字节流
     * @throws IOException io exception
     */
    public byte[] Base64ZipDecode (String str) throws IOException {
/*
        sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder ();
        return unzip (decoder.decodeBuffer (str));
*/
        return unzip (Base64.decode (str));
    }

    public byte[] DESEncode (String str) throws Exception {
        //产生一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        //从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec (key);
        //创建一个密钥工厂，然后用它把DESKeySpec转换成Secret Key对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
        SecretKey keySpec = keyFactory.generateSecret(dks);
        //Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        //用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, sr);
        //执行加密操作
//        return cipher.doFinal(plaintext);
        byte[] plaintext = str.getBytes (enc);
        int length = plaintext.length;
        while (length % 8 != 0) length ++;
        byte[] temp = new byte [length];
        System.arraycopy (plaintext, 0, temp, 0, plaintext.length);
        for (int i = plaintext.length; i < temp.length; i ++) temp [i] = 0;
//        return cipher.doFinal(plaintext);
        return cipher.doFinal (temp);
    }

    public String DESEncodeString (String str) throws Exception {
        return hex2str (DESEncode (str));
    }

    public String DESDecode (byte[] cryptotext) throws Exception {
        //产生一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        //从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
        //创建一个密钥工厂，然后用它把DESKeySpec转换成Secret Key对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
        SecretKey keySpec = keyFactory.generateSecret(dks);
        //Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        //用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, keySpec, sr);
        //执行解密操作
//        return cipher.doFinal(cryptotext);
        byte[] temp = cipher.doFinal (cryptotext);
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        for (byte aTemp : temp) {
            if (aTemp != 0) baos.write (aTemp);
        }

        return new String (baos.toByteArray (), enc);        
    }

    public String DESDEcode (String cryptotext) throws Exception {
        return DESDecode (str2hex(cryptotext));
    }

    public static String hex2str (byte[] hex) {
        StringBuffer builder = new StringBuffer ();
        for (byte b : hex) {
            builder.append (letters[(b >> 4) & 0xf]).append (letters[b & 0xf]);
        }
        return builder.toString ();
    }

    public static byte[] str2hex (String str) {
        char[] a = str.toLowerCase ().toCharArray ();
        byte[] hex = new byte [a.length / 2];
        for (int i = 0; i < a.length; i +=2) {
            char c1 = a [i], c2 = a [i + 1];
            hex [i / 2] = (byte) ((((c1 >= '0' && c1 <= '9') ? c1 - '0' : c1 - 'a' + 10) << 4) |
                                  ((c2 >= '0' && c2 <= '9') ? c2 - '0' : c2 - 'a' + 10));
        }
        return hex;
    }

    private static MessageDigest md5;
    static {
        try {
            md5 = MessageDigest.getInstance ("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
    }
    
    public static String md5 (String text) {
        return hex2str (md5.digest (text.getBytes ()));
    }
}