package com.dochiri.kihan.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cookie")
public record CookieProperties(
        String accessTokenName,
        String refreshTokenName,
        String domain,
        String path,
        boolean httpOnly,
        boolean secure,
        String sameSite,
        long maxAge
) {
}