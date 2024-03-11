package com.abdecd.tokenlogin.config;

import com.abdecd.tokenlogin.common.property.AllProperties;
import com.abdecd.tokenlogin.interceptor.LoginInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@MapperScan("com.abdecd.tokenlogin.mapper")
@ComponentScan("com.abdecd.tokenlogin")
@EnableConfigurationProperties(AllProperties.class)
public class LoginConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**");
    }
}

//"/error",
//        "/doc.html/**",
//        "/swagger-ui.html/**",
//        "/v3/api-docs/**",
//        "/user/login",
//        "/user/signup",
//        "/common/captcha"