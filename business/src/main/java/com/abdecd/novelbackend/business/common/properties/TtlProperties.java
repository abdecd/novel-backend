package com.abdecd.novelbackend.business.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "novel.ttl")
@Data
public class TtlProperties {
    private Integer jwtTtlSeconds;
    private Integer captchaTtlSeconds;
}
