package com.abdecd.novelbackend.business.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动进行图片转正和异常状态下的删除操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseFileService {

    /**
     * DTO类型
     */
    Class<?> param();

    /**
     * 上传文件的路径属性名
     */
    String[] value();

    /**
     * 目标转正文件夹
     */
    String folder() default "";

    /**
     * 转正失败直接抛出异常
     */
    boolean strict() default true;
}
