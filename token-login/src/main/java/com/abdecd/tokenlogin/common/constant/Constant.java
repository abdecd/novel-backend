package com.abdecd.tokenlogin.common.constant;

import java.util.regex.Pattern;

public class Constant {
    public static final String JWT_ID = "id";
    public static final String JWT_PERMISSION = "permission";
    public static final String JWT_TOKEN_NAME = "token";
    public static final String JWT_EXPIRE_TIME = "exp";

    public static final byte ENABLE = 1;
    public static final byte DISABLE = 0;
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,20}$");

    /**
     * 在该值之前的token全部无效，单位ms
     */
    public static final String LOGIN_TOKEN_BLACKLIST = "loginTokenBlacklist:";
}
