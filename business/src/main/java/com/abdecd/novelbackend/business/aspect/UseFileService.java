package com.abdecd.novelbackend.business.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动进行文件转正和异常状态下的删除操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseFileService {

    /**
     * DTO类型 默认选中第一个类型符合的参数
     */
    Class<?> param();

    /**
     * 上传文件的路径属性名
     */
    String[] value();

    /**
     * 目标转正文件夹
     * SpEL root为被注解方法所在的对象
     * 文件夹以 "/" 开头
     */
    String folder() default "";

    /**
     * 目标转正后文件名
     * SpEL root为被注解方法所在的对象
     */
    String name() default "";

    /**
     * 除非字段为null，否则转正失败直接抛出异常
     */
    boolean strict() default true;
}
