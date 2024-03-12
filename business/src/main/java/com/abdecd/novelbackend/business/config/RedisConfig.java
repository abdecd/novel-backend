package com.abdecd.novelbackend.business.config;

import com.abdecd.novelbackend.business.common.util.ProtostuffRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {
    @Bean
    public <T1, T2> RedisTemplate<T1, T2> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<T1, T2> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(new ProtostuffRedisSerializer());
        return redisTemplate;
    }
}
