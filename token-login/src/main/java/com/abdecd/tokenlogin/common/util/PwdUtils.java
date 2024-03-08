package com.abdecd.tokenlogin.common.util;

import ch.qos.logback.core.encoder.ByteArrayUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class PwdUtils {
    // 随机生成一对密钥（包含公钥和私钥）
    private static final KeyPair keyPair = generateKeyPair();
    // 获取 公钥 和 私钥
    public static final PublicKey pubKey = keyPair.getPublic();
    private static final PrivateKey priKey = keyPair.getPrivate();

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

        // 随机生成一对密钥（包含公钥和私钥）
        return gen.generateKeyPair();
    }

    public static String getCipheredPwd(String encodedPwd) {
        Cipher cipher;
        try {
            byte[] pwdBytes = Base64.getDecoder().decode(encodedPwd);
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE,priKey);
            return new String(cipher.doFinal(pwdBytes), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String[] SALT = {"sfdsyt7aat", "dfjiowef4", "-=fjonv.sdr", "qoxirmenw", "28d3c9d3s", "jiox*j4n/3", "mewq;jiowreijx", "jwiosixuqxs", ".vldpw[dxmsfd"};

    public static String encodePwd(String username, String pwd) throws NoSuchAlgorithmException {
        var sha256 = MessageDigest.getInstance("SHA-256");
        var saltHash = (pwd + username).hashCode() + 113;
        var index = Math.abs(saltHash % 9);
        var saltedPwd = pwd.substring(0, pwd.length() - 2) + SALT[index] + pwd.substring(pwd.length() - 2);
        var hash = sha256.digest(saltedPwd.getBytes(StandardCharsets.UTF_8));
        return ByteArrayUtil.toHexString(hash);
    }
}
