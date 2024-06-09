package com.abdecd.novelbackend.business.lib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 记录并返回是否限流
     */
    public boolean isRateLimited(String key, int maxCount, int time, TimeUnit timeUnit) {
        RedisAtomicInteger atomicInteger = new RedisAtomicInteger(
                key,
                Objects.requireNonNull(stringRedisTemplate.getConnectionFactory())
        );
        if (atomicInteger.getExpire() == null || atomicInteger.getExpire() < 0)
            atomicInteger.expire(time, timeUnit);
        return atomicInteger.incrementAndGet() > maxCount;
    }
}
