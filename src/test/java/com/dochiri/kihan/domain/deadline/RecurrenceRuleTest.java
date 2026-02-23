package com.dochiri.kihan.domain.deadline;

import com.dochiri.kihan.domain.deadline.exception.InvalidDeadlineRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RecurrenceRule 도메인 테스트")
class RecurrenceRuleTest {

    @Test
    @DisplayName("유효한 값으로 반복 규칙을 생성할 수 있다")
    void shouldCreateValidRecurrenceRule() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.MONTHLY,
                startDate,
                endDate
        );

        assertEquals(RecurrencePattern.MONTHLY, rule.getPattern());
        assertEquals(startDate, rule.getStartDate());
        assertEquals(endDate, rule.getEndDate());
    }

    @Test
    @DisplayName("endDate가 없어도 반복 규칙을 생성할 수 있다")
    void shouldAllowNullEndDate() {
        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.DAILY,
                LocalDate.of(2026, 1, 1),
                null
        );

        assertTrue(rule.getEndDate() == null);
    }

    @Test
    @DisplayName("endDate는 startDate 이전일 수 없다")
    void shouldThrowWhenEndDateIsBeforeStartDate() {
        InvalidDeadlineRuleException exception = assertThrows(
                InvalidDeadlineRuleException.class,
                () -> RecurrenceRule.create(
                        RecurrencePattern.WEEKLY,
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 9)
                )
        );

        assertTrue(exception.getMessage().contains("startDate"));
    }

    @Test
    @DisplayName("endDate가 startDate와 같으면 생성 가능하다")
    void shouldAllowEndDateEqualToStartDate() {
        LocalDate date = LocalDate.of(2026, 1, 10);

        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.WEEKLY,
                date,
                date
        );

        assertEquals(date, rule.getStartDate());
        assertEquals(date, rule.getEndDate());
    }

    @Test
    @DisplayName("pattern이 null이면 예외가 발생한다")
    void shouldThrowWhenPatternIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> RecurrenceRule.create(
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );
    }

    @Test
    @DisplayName("startDate가 null이면 예외가 발생한다")
    void shouldThrowWhenStartDateIsNull() {
        assertThrows(
                InvalidDeadlineRuleException.class,
                () -> RecurrenceRule.create(
                        RecurrencePattern.DAILY,
                        null,
                        null
                )
        );
    }

}
