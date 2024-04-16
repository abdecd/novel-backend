package com.abdecd.novelbackend.business.service.lib;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheByFrequencyFactory {
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @SuppressWarnings("unchecked")
    public <T> CacheByFrequency<T> create(String rootKey, int maxCount, Integer frequencyTtlSeconds) {
        return (CacheByFrequency<T>) new CacheByFrequency<>(redisTemplate, stringRedisTemplate, redissonClient, rootKey, maxCount, frequencyTtlSeconds);
    }
}
