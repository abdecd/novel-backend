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
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private AllProperties allProperties;
    @Resource
    private UserBaseService userBaseService;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        UserContext.setUserId(10000);
        UserContext.setPermission((byte) 99);
        return true;
    }
//    @Override
    public boolean preHandlde(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        log.info("uri: {}, query: {}", request.getRequestURI(), request.getQueryString());

        //1、从请求头中获取令牌
        String token = request.getHeader(Constant.JWT_TOKEN_NAME);
        log.info("jwt校验:{}", token);
        if (token == null || token.isEmpty()) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            response.getWriter().println("401 Unauthorized");
            response.getWriter().close();
            return false;
        }
        //2、校验令牌
        try {
            Map<String, Object> claims = JwtUtils.getInstance().decodeJWT(token);
            Integer userId = Integer.valueOf(claims.get(Constant.JWT_ID).toString());
            Byte permission = Byte.parseByte(claims.get(Constant.JWT_PERMISSION).toString());
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
            //4、不通过，响应401状态码
            response.setStatus(401);
            response.getWriter().println("401 Unauthorized");
            response.getWriter().close();
            return false;
        }
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, Exception ex) throws Exception {
        UserContext.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
