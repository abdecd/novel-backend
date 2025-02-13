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
        response.setHeader("Cache-Control", "public, max-age=0, must-revalidate");
        if (request.getHeader("If-None-Match") != null) {
            var clientLastTime = request.getHeader("If-None-Match").substring("W/".length());
            var clientLocalDateTime = LocalDateTime.parse(clientLastTime);
            if (!clientLocalDateTime.isBefore(currentLocalDateTime)) {
                response.setStatus(304);
                return true;
            }
        }
        response.setHeader("ETag", "W/" + currentLocalDateTime);
        return false;
    }

    /**
     * 使用 http 缓存并强制验证
     */
    public static boolean tryUseCache(HttpServletRequest request, HttpServletResponse response, Object hash) {
        if (hash == null) return false;
        response.setHeader("Cache-Control", "public, max-age=0, must-revalidate");
        if (request.getHeader("If-None-Match") != null) {
            var oldHash = request.getHeader("If-None-Match").substring("W/".length());
            if (oldHash.equals(hash.toString())) {
                response.setStatus(304);
                return true;
            }
        }
        response.setHeader("ETag", "W/" + hash);
        return false;
    }
}
