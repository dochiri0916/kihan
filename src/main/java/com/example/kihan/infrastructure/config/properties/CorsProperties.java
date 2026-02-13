package com.example.pragmatic.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        List<String> allowedOrigins
) {
}