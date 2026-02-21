package com.dochiri.kihan.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JpaConfig 테스트")
class JpaConfigTest {

    @Test
    @DisplayName("auditingDateTimeProvider는 주입된 Clock 기준 시각을 반환한다")
    void shouldReturnDateTimeFromInjectedClock() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-02-21T10:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        JpaConfig jpaConfig = new JpaConfig();

        DateTimeProvider provider = jpaConfig.auditingDateTimeProvider(clock);

        LocalDateTime dateTime = (LocalDateTime) provider.getNow().orElseThrow();
        assertEquals(LocalDateTime.of(2026, 2, 21, 19, 0), dateTime);
    }
}
