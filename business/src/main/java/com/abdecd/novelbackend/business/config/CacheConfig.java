package com.abdecd.novelbackend.business.config;

import com.abdecd.novelbackend.business.common.util.ProtostuffRedisSerializer;
import jakarta.annotation.Nonnull;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

        // 设置 Value 的序列化方式
        return redisCacheConfiguration
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new ProtostuffRedisSerializer()));
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisCacheConfiguration redisCacheConfiguration, RedisConnectionFactory redisConnectionFactory) {
        return new CustomizedRedisCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory), redisCacheConfiguration);
    }
}

class CustomizedRedisCacheManager extends RedisCacheManager {

    public CustomizedRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Nonnull
    @Override
    protected RedisCache createRedisCache(@Nonnull String name, RedisCacheConfiguration cacheConfig) {
        String[] array = name.split("#",2);
        name = array[0];
        if (array.length > 1) {
            if (array[1].startsWith("S")) {
                long ttl = Long.parseLong(array[1].substring(1));
                cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(ttl)); // 缓存时间按秒设置
            } else {
                long ttl = Long.parseLong(array[1]);
                cacheConfig = cacheConfig.entryTtl(Duration.ofDays(ttl)); // 缓存时间按日设置
            }
        }
        return super.createRedisCache(name, cacheConfig);
    }
}
