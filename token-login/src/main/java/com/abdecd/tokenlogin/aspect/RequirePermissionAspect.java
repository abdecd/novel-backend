package com.abdecd.tokenlogin.aspect;

import com.abdecd.tokenlogin.common.context.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
public class RequirePermissionAspect {
    @Around("@annotation(RequirePermission)")
    public Object requirePermission(ProceedingJoinPoint joinPoint) throws Throwable {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);

            var permissions = requirePermission.value();
            var userPermission = (byte) UserContext.getPermission();
            for (var permission : permissions) {
                if (permission == userPermission) return joinPoint.proceed();
            }
            throw new RuntimeException("Permission denied");
        } else {
            throw new IllegalStateException("不支持非方法切入点");
        }
    }
}
