package com.abdecd.novelbackend.business.service.lib;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class CacheByFrequency<T> {
    private final RedisTemplate<String, T> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final String rootKey;
    private final int maxCount;
    private final Integer ttlSeconds;

    public CacheByFrequency(
            RedisTemplate<String, T> redisTemplate,
            @Nonnull StringRedisTemplate stringRedisTemplate,
            RedissonClient redissonClient,
            @Nonnull String rootKey,
            int maxCount,
            Integer frequencyTtlSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.rootKey = rootKey;
        this.maxCount = maxCount;
        this.ttlSeconds = frequencyTtlSeconds;
    }

    public void recordFrequency(String key) {
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(rootKey + ":zSet"))) {
            stringRedisTemplate.opsForZSet().add(rootKey + ":zSet", key, 0);
            if (ttlSeconds != null) stringRedisTemplate.expire(rootKey + ":zSet", ttlSeconds, TimeUnit.SECONDS);
        }
        stringRedisTemplate.opsForZSet().incrementScore(rootKey + ":zSet", key, 1);
    }
    @Nullable public T get(
            String key,
            @Nonnull Supplier<T> failCb,
            @Nullable Function<String, String> keyForJudgeFunc,
            @Nullable Integer keyTtlSeconds
    ) {
        if (keyForJudgeFunc == null) keyForJudgeFunc = k -> k;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rootKey + ":value:" + key))) {
            return redisTemplate.opsForValue().get(rootKey + ":value:" + key);
        } else {
            // 判断是否可缓
            var set = stringRedisTemplate.opsForZSet().reverseRange(rootKey + ":zSet", 0, maxCount);
            if (set == null || !set.contains(keyForJudgeFunc.apply(key))) {
                return failCb.get();
            } else {
                RLock lock = redissonClient.getLock(rootKey + ":lock");
                lock.lock();
                if (Boolean.TRUE.equals(redisTemplate.hasKey(rootKey + ":value:" + key)))
                    return redisTemplate.opsForValue().get(rootKey + ":value:" + key);
                redisTemplate.opsForValue().set(rootKey + ":value:" + key, failCb.get());
                if (keyTtlSeconds != null) redisTemplate.expire(rootKey + ":value:" + key, keyTtlSeconds, TimeUnit.SECONDS);
                clearUnusedOne(keyForJudgeFunc);
                lock.unlock();
                return redisTemplate.opsForValue().get(rootKey + ":value:" + key);
            }
        }
    }

    public void delete(String key) {
        redisTemplate.delete(rootKey + ":value:" + key);
    }

    /**
     * 清理缓存 随机清掉一个
     * @param keyForJudgeFunc key的判断函数，用于判断是否可缓
     */
    private void clearUnusedOne(@Nullable Function<String, String> keyForJudgeFunc) {
        if (keyForJudgeFunc == null) keyForJudgeFunc = k -> k;
        var set = stringRedisTemplate.opsForZSet().reverseRange(rootKey + ":zSet", 0, maxCount);
        if (set == null) return;
        var fullKeys = redisTemplate.keys(rootKey + ":value:*");
        if (fullKeys == null) return;
        var needDeleted = new ArrayList<String>();
        Function<String, String> finalKeyForJudgeFunc = keyForJudgeFunc;
        fullKeys.forEach(fullKey -> {
            if (!set.contains(
                    finalKeyForJudgeFunc.apply(fullKey.substring(fullKey.indexOf(":value:")+":value:".length()))
            )) needDeleted.add(fullKey);
        });
        if (needDeleted.isEmpty()) return;
        Collections.shuffle(needDeleted);
        redisTemplate.delete(needDeleted.getFirst());
    }
}
