package com.abdecd.tokenlogin.service;

import com.abdecd.tokenlogin.common.constant.Constant;
import com.abdecd.tokenlogin.common.property.AllProperties;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class LoginBlackListManager {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AllProperties allProperties;

    public void forceLogout(Integer userId) {
        stringRedisTemplate.opsForValue().set(
                Constant.LOGIN_TOKEN_BLACKLIST + userId,
                new Date().getTime() + allProperties.getJwtTtlSeconds() * 1000L + "",
                allProperties.getJwtTtlSeconds(),
                TimeUnit.SECONDS
        );
    }

    public boolean checkInBlackList(int userId, long tokenTtlms) {
        var timestamp = stringRedisTemplate.opsForValue().get(Constant.LOGIN_TOKEN_BLACKLIST + userId);
        if (timestamp == null) return false;
        return Long.parseLong(timestamp) > tokenTtlms;
    }
}
