package com.dochiri.kihan.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swagger.auth")
public record SwaggerAuthProperties(
        String username,
        String password,
        String role
) {
}