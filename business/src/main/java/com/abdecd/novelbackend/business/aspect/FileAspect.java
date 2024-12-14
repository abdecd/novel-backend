package com.abdecd.novelbackend.business.aspect;

import com.abdecd.novelbackend.business.exceptionhandler.BaseException;
import com.abdecd.novelbackend.business.service.FileService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

@Aspect
@Component
public class FileAspect {
    @Autowired
    private FileService fileService;
    private static final ExpressionParser spelExpressionParser = new SpelExpressionParser();

    @Around("@annotation(UseFileService)")
    public Object useFileService(ProceedingJoinPoint joinPoint) throws Throwable {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            UseFileService useFileService = method.getAnnotation(UseFileService.class);

            // 获得方法参数
            Result result = getNeedArgs(joinPoint, methodSignature, useFileService);
            // 用于回滚
            ArrayList<String> imgList = new ArrayList<>();
            try {
                tryConvertImageFromString(useFileService.value(), useFileService, result, imgList);
                tryConvertImageFromStringArr(useFileService.valueArr(), useFileService, result, imgList);
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

    private static Result getNeedArgs(ProceedingJoinPoint joinPoint, MethodSignature methodSignature, UseFileService useFileService) {
        EvaluationContext context = new StandardEvaluationContext(joinPoint.getTarget());
        var allArgNames = methodSignature.getParameterNames();
        var allArgTypes = methodSignature.getParameterTypes();

        Object dto = null;
        for (int i = 0; i < allArgNames.length; i++) {
            context.setVariable(allArgNames[i], joinPoint.getArgs()[i]);
            // 选中第一个类型符合的dto
            if (dto == null && allArgTypes[i].equals(useFileService.param()))
                dto = joinPoint.getArgs()[i];
        }
        if (dto == null) throw new RuntimeException("未找到对应参数");

        var targetFolder = (useFileService.folder() == null || useFileService.folder().isBlank())
                ? null
                : (String) spelExpressionParser.parseExpression(useFileService.folder()).getValue(context);
        var targetName = (useFileService.name() == null || useFileService.name().isBlank())
                ? null
                : (String) spelExpressionParser.parseExpression(useFileService.name()).getValue(context);

        return new Result(dto, targetFolder, targetName);
    }

    private record Result(Object dto, String targetFolder, String targetName) {
    }

    private void tryConvertImageFromString(String[] needConvertProperties, UseFileService useFileService, Result result, ArrayList<String> imgList) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (var property : needConvertProperties) {
            // 获取图片属性
            var suffix = StringUtils.capitalize(property);
            Method getMethod = useFileService.param().getMethod("get"+suffix);
            String imgValue = (String) getMethod.invoke(result.dto());

            if (imgValue != null) {
                try {
                    imgValue = fileService.changeTmpFileToStatic(imgValue, result.targetFolder(), result.targetName());
                    if (!imgValue.isEmpty()) {
                        // 用于回滚
                        imgList.add(imgValue);
                    } else {
                        if (useFileService.strict()) throw new BaseException("文件链接异常");
                        // 不严格模式下，不换链接，保持原样
                        imgValue = null;
                    }
                } catch (IOException e) {
                    throw new BaseException("文件链接异常");
                }
                Method setMethod = useFileService.param().getMethod("set"+suffix, String.class);
                // 转正成功，换新链接
                if (imgValue != null) setMethod.invoke(result.dto(), imgValue);
            }
        }
    }

    private void tryConvertImageFromStringArr(String[] needConvertProperties, UseFileService useFileService, Result result, ArrayList<String> imgList) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (var property : needConvertProperties) {
            // 获取图片属性
            var suffix = StringUtils.capitalize(property);
            Method getMethod = useFileService.param().getMethod("get"+suffix);
            final String[] oldImgArr = (String[]) getMethod.invoke(result.dto());
            String[] imgArr = oldImgArr == null ? null : Arrays.copyOf(oldImgArr, oldImgArr.length);

            // 尝试转正
            if (imgArr != null) {
                try {
                    for (int i = 0; i < imgArr.length; i++) {
                        imgArr[i] = fileService.changeTmpFileToStatic(imgArr[i], result.targetFolder(), result.targetName());
                        // 用于回滚
                        if (!imgArr[i].isEmpty()) {
                            imgList.add(imgArr[i]);
                        } else {
                            if (useFileService.strict()) throw new BaseException("文件链接异常");
                            // 不严格模式下，保持原样
                            imgArr[i] = oldImgArr[i];
                        }
                    }
                } catch (IOException e) {
                    // 只要有错误就直接返回
                    throw new BaseException("文件链接异常");
                }
                Method setMethod = useFileService.param().getMethod("set"+suffix, String[].class);
                setMethod.invoke(result.dto(), (Object) imgArr);
            }
        }
    }
}
