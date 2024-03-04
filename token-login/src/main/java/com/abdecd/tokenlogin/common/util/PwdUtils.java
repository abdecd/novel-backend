package com.abdecd.tokenlogin.common.util;

import ch.qos.logback.core.encoder.ByteArrayUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PwdUtils {
    private static final String[] SALT = {"sfdsyt7aat", "dfjiowef4", "-=fjonv.sdr", "qoxirmenw", "28d3c9d3s", "jiox*j4n/3", "mewq;jiowreijx", "jwiosixuqxs", ".vldpw[dxmsfd"};

    public static String encodePwd(String username, String pwd) throws NoSuchAlgorithmException {
        var md5 = MessageDigest.getInstance("MD5");
        var saltHash = (pwd + username).hashCode() + 113;
        var index = Math.abs(saltHash % 9);
        var saltedPwd = pwd.substring(0, pwd.length() - 2) + SALT[index] + pwd.substring(pwd.length() - 2);
        var md5hash = md5.digest(saltedPwd.getBytes(StandardCharsets.UTF_8));
        return ByteArrayUtil.toHexString(md5hash);
    }
}
