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
}
