package com.abdecd.tokenlogin.interceptor;

import com.abdecd.tokenlogin.common.constant.Constant;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.abdecd.tokenlogin.common.property.AllProperties;
import com.abdecd.tokenlogin.common.util.JwtUtils;
import com.abdecd.tokenlogin.service.UserBaseService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Component
@Order(1)
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private AllProperties allProperties;
    @Resource
    private UserBaseService userBaseService;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        var canPass = false;
        if (!(handler instanceof HandlerMethod)) {
            canPass = true;
        }
        PathPatternParser pathPatternParser = new PathPatternParser();
        for (String pattern : allProperties.getExcludePatterns()) {
            if (pathPatternParser.parse(pattern).matches(PathContainer.parsePath(request.getRequestURI()))) {
                canPass = true;
            }
        }
        log.info("uri: {}, query: {}", request.getRequestURI(), request.getQueryString());

        //1、从请求头中获取令牌
        String token = request.getHeader(Constant.JWT_TOKEN_NAME);
        if (token == null || token.isEmpty()) {
            if (!canPass) return ret401(response);
            else return true;
        }
        //2、校验令牌
        try {
            Map<String, Object> claims = JwtUtils.getInstance().decodeJWT(token);
            Integer userId = Integer.valueOf(claims.get(Constant.JWT_ID).toString());
            String permission = claims.get(Constant.JWT_PERMISSION).toString();
            log.info("userId：{}, permission: {}", userId, permission);
            UserContext.setUserId(userId);
            UserContext.setPermission(permission);
            // 即将过期时返回刷新的token
            if (claims.get(Constant.JWT_EXPIRE_TIME) != null
                    && (Long.parseLong(claims.get(Constant.JWT_EXPIRE_TIME).toString())
                    - new Date().getTime())/1000 < allProperties.getJwtRefreshTtlSeconds()) {
                response.setHeader(Constant.JWT_TOKEN_NAME, userBaseService.refreshUserToken());
            }
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            if (!canPass) return ret401(response);
            else return true;
        }
    }

    public boolean ret401(HttpServletResponse response) throws IOException {
        // 不通过，响应401状态码
        response.setStatus(401);
        response.getWriter().println("401 Unauthorized");
        response.getWriter().close();
        return false;
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, Exception ex) throws Exception {
        UserContext.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
