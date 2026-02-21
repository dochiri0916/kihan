package com.dochiri.kihan.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app")
public record TimeProperties(
        @DefaultValue("Asia/Seoul")
        String timeZone
) {
}