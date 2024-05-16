package com.abdecd.tokenlogin.common.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "token-login")
@Data
public class AllProperties {
    private Integer jwtTtlSeconds;
    private Integer jwtRefreshTtlSeconds;
    private String[] excludePatterns;
    /**
     * 数据脱敏; AES 加密密钥 详见 common/dataencrypt/EncryptStrHandler
     */
    private String encryptStrAesKey = "1234561234561234";
    /**
     * 测试模式; true 时启用虚拟用户允许所有请求 详见 interceptor/LoginInterceptor.inTest()
     */
    private Boolean test = false;
}
