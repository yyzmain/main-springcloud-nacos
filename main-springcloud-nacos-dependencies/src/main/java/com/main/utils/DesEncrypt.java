package com.main.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 3DES加密解密
 */
public class DesEncrypt {

    /**
     * 密钥
     */
    private static String key = "liuyunqiang@lx100$#365#$";
    /**
     * 向量
     */
    private static String iv = "01234567";
    /**
     * 加解密统一使用的编码方式
     */
    private static String encoding = "utf-8";

    public DesEncrypt() {
    }

    public DesEncrypt(String key) {
        setKey(key);
    }

    private static void setKey(String key) {
        DesEncrypt.key = key;
    }

    /**
     * 3DES加密
     *
     * @param plainText 普通文本
     */
    public static String encrypt(String plainText) {
        try {
            DESedeKeySpec spec = new DESedeKeySpec(key.getBytes(UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("desede");
            Key desKey = keyFactory.generateSecret(spec);
            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes(UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, desKey, ips);
            byte[] encryptData = cipher.doFinal(plainText.getBytes(encoding));
            return Base64.encode(encryptData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 3DES解密
     *
     * @param encryptText 加密文本
     */
    public static String decrypt(String encryptText) {
        try {
            DESedeKeySpec spec = new DESedeKeySpec(key.getBytes(UTF_8));
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
            Key deskey = keyfactory.generateSecret(spec);
            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes(UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
            byte[] decryptData = cipher.doFinal(Base64.decode(encryptText));
            return new String(decryptData, encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        String name = "nl-rabc";
        String encryptName = encrypt(name);
        System.out.println("name:" + encryptName);
        String decryptName = decrypt(encryptName);
        System.out.println(encryptName + ":" + decryptName);
    }


}
