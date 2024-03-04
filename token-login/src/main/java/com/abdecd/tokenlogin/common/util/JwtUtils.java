package com.abdecd.tokenlogin.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Map;

public class JwtUtils {
    private static final JwtUtils instance = new JwtUtils();
    private JwtUtils() {}
    public static JwtUtils getInstance() {
        return instance;
    }

    KeyPair keyPair;
    Algorithm RSA256;
    {
        try {
            keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            RSA256 = Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
//    Algorithm HMAC256 = Algorithm.HMAC256(" ");

    public String encodeJWT(int ttlSeconds, Map<String, Object> claims) {
        var expiredDate = Calendar.getInstance();
        expiredDate.add(Calendar.SECOND,ttlSeconds);
        return JWT.create()
                .withClaim("claims",claims)
                .withExpiresAt(expiredDate.getTime())
                .sign(RSA256);
    }

    public Map<String, Object> decodeJWT(String token) {
        return JWT
                .require(RSA256)
                .build()
                .verify(token)
                .getClaims()
                .get("claims")
                .asMap();
    }
}
