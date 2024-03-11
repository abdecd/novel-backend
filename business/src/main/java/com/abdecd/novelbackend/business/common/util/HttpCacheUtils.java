package com.abdecd.novelbackend.business.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;

public class HttpCacheUtils {
    /**
     * 使用 http 缓存并强制验证
     */
    public static boolean tryUseCache(HttpServletRequest request, HttpServletResponse response, LocalDateTime currentLocalDateTime) {
        if (currentLocalDateTime == null) return false;
        if (request.getHeader("If-None-Match") != null) {
            var clientLastTime = request.getHeader("If-None-Match");
            var clientLocalDateTime = LocalDateTime.parse(clientLastTime);
            if (!clientLocalDateTime.isBefore(currentLocalDateTime)) {
                response.setStatus(304);
                return true;
            }
        }
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("ETag", currentLocalDateTime.toString());
        return false;
    }
}
