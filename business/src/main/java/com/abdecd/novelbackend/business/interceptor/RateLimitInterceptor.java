package com.abdecd.novelbackend.business.interceptor;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.lib.RateLimiter;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private RateLimiter rateLimiter;
    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        var realIp = request.getHeader("X-Real-IP");
        if (rateLimiter.isRateLimited(
                RedisConstant.LIMIT_IP_RATE + realIp,
                RedisConstant.LIMIT_IP_RATE_CNT,
                RedisConstant.LIMIT_IP_RATE_RESET_TIME,
                TimeUnit.SECONDS
        )) throw new BaseException(MessageConstant.RATE_LIMIT);
        return true;
    }
}
