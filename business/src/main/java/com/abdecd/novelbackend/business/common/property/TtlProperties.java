package com.abdecd.novelbackend.business.common.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "novel.ttl")
@Data
public class TtlProperties {
    private Integer captchaTtlSeconds;
}
