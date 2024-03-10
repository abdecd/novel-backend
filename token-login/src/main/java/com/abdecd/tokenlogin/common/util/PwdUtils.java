package com.abdecd.tokenlogin.common.util;

import ch.qos.logback.core.encoder.ByteArrayUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

public class PwdUtils {
    private static final KeyPair keyPair = generateKeyPair();
    public static final PublicKey publicKey = keyPair.getPublic();
    private static final PrivateKey privateKey = keyPair.getPrivate();

    private static KeyPair generateKeyPair() {
        // 获取指定算法的密钥对生成器
        KeyPairGenerator gen;
        try {
            gen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // 初始化密钥对生成器（密钥长度要适中, 太短不安全, 太长加密/解密速度慢）
        gen.initialize(2048);

        return gen.generateKeyPair();
    }

    public static String getEncryptedPwd(String encryptedPwd) {
        Cipher cipher;
        try {
            byte[] pwdBytes = Base64.getDecoder().decode(encryptedPwd);
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); // 使用RSA-OAEP算法
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
            return new String(cipher.doFinal(pwdBytes), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("decrypt error");
        }
    }

    private static final String SALT = "eFdsPt7aatdfjiowef4-qoXiCmenw28d3c9d3sjiox*j4n/3mewq;jiowreiSFLddLDCjxjSixuqxs.Vldpw[dxmsfd";

    private static String getSaltedPwd(String username, String pwd) {
        var saltIndex1 = 10 + Math.abs((username.hashCode() + 113) % 37);
        var saltIndex2 = SALT.length() - Math.abs((pwd.hashCode() + 113) % 37);
        String salt;
        if (saltIndex2 - saltIndex1 > 43) {
            salt = SALT.substring(saltIndex1, saltIndex2);
        } else {
            salt = SALT.substring(saltIndex2) + SALT.substring(0, saltIndex1);
        }
        if (saltIndex2 > 72) salt = salt.toUpperCase();
        return pwd.substring(0, pwd.length() - 2) + salt + pwd.substring(pwd.length() - 2);
    }

//    public static String encodePwd(String username, String pwd) throws RuntimeException {
//        MessageDigest sha256;
//        try {
//            sha256 = MessageDigest.getInstance("SHA-256");
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("unknown error");
//        }
//        var saltedPwd = getSaltedPwd(username, pwd);
//        var hash = sha256.digest(saltedPwd.getBytes(StandardCharsets.UTF_8));
//        return ByteArrayUtil.toHexString(hash);
//    }

    public static String encodePwd(String username, String encryptedPwd) throws RuntimeException {
        var pwd = getEncryptedPwd(encryptedPwd);
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unknown error");
        }
        var saltedPwd = getSaltedPwd(username, pwd);
        var hash = sha256.digest(saltedPwd.getBytes(StandardCharsets.UTF_8));
        return ByteArrayUtil.toHexString(hash);
    }
}
