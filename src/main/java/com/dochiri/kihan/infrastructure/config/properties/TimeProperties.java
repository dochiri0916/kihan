package com.dochiri.kihan.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record TimeProperties(
        String timeZone
) {
}