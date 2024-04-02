package com.abdecd.novelbackend.business.interceptor;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        var realIp = request.getHeader("X-Real-IP");
        RedisAtomicInteger atomicInteger = new RedisAtomicInteger(RedisConstant.LIMIT_IP_RATE + realIp, Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        if (atomicInteger.getExpire() == null || atomicInteger.getExpire() < 0) atomicInteger.expire(3, TimeUnit.SECONDS);
        if (atomicInteger.incrementAndGet() > 200) throw new BaseException(MessageConstant.RATE_LIMIT);
        return true;
    }
}
