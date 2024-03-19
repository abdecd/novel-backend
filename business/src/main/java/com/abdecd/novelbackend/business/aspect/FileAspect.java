package com.abdecd.novelbackend.business.aspect;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.service.FileService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

@Aspect
@Component
public class FileAspect {
    @Autowired
    private FileService fileService;

    @Around("@annotation(UseFileService)")
    public Object useFileService(ProceedingJoinPoint joinPoint) throws Throwable {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            UseFileService useFileService = method.getAnnotation(UseFileService.class);

            // 获得方法参数
            var arg = joinPoint.getArgs()[0];
            ArrayList<String> imgList = new ArrayList<>();

            var properties = useFileService.value();
            for (var property : properties) {
                // 获取图片属性
                var suffix = StringUtils.capitalize(property);
                Method getMethod = useFileService.param().getMethod("get"+suffix);
                String img = (String) getMethod.invoke(arg);

                if (img != null) {
                    try {
                        img = fileService.changeTmpFileToStatic(img, useFileService.folder(), useFileService.name());
                    } catch (IOException e) {
                        throw new BaseException("文件链接异常");
                    }
                    // 转正成功，换新链接
                    Method setMethod = useFileService.param().getMethod("set"+suffix, String.class);
                    if (!img.isEmpty()) setMethod.invoke(arg, img);
                    else {
                        if (useFileService.strict()) throw new BaseException("文件链接异常");
                        img = null;
                    }
                }
                if (img != null) imgList.add(img);
            }
            try {
                return joinPoint.proceed();
            } catch (Exception e) {
                // 回滚
                if (!imgList.isEmpty()) for (var img : imgList) fileService.deleteFile(img);
                throw e;
            }
        } else {
            throw new IllegalStateException("不支持非方法切入点");
        }
    }
}
