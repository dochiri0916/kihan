package com.dochiri.kihan.infrastructure.config;

import com.dochiri.kihan.infrastructure.config.properties.TimeProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeConfig {

    private final TimeProperties properties;

    public TimeConfig(TimeProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(properties.timeZone()));
    }

    @PostConstruct
    void initializeDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(properties.timeZone()));
    }

}