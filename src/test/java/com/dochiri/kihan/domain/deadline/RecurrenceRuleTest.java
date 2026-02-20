package com.dochiri.kihan.domain.deadline;

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
    void 유효한_반복_규칙_생성() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.MONTHLY,
                2,
                startDate,
                endDate
        );

        assertEquals(RecurrencePattern.MONTHLY, rule.getPattern());
        assertEquals(2, rule.getInterval());
        assertEquals(startDate, rule.getStartDate());
        assertEquals(endDate, rule.getEndDate());
    }

    @Test
    @DisplayName("endDate가 없어도 반복 규칙을 생성할 수 있다")
    void end_date가_null이어도_생성된다() {
        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.DAILY,
                1,
                LocalDate.of(2026, 1, 1),
                null
        );

        assertTrue(rule.getEndDate() == null);
    }

    @Test
    @DisplayName("interval은 1 이상이어야 한다")
    void interval은_1_이상이어야_한다() {
        assertThrows(
                InvalidDeadlineRuleException.class,
                () -> RecurrenceRule.create(
                        RecurrencePattern.DAILY,
                        0,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );
    }

    @Test
    @DisplayName("endDate는 startDate 이전일 수 없다")
    void end_date는_start_date_이전일_수_없다() {
        assertThrows(
                InvalidDeadlineRuleException.class,
                () -> RecurrenceRule.create(
                        RecurrencePattern.WEEKLY,
                        1,
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 9)
                )
        );
    }

    @Test
    @DisplayName("pattern이 null이면 예외가 발생한다")
    void pattern이_null이면_예외가_발생한다() {
        assertThrows(
                NullPointerException.class,
                () -> RecurrenceRule.create(
                        null,
                        1,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );
    }

    @Test
    @DisplayName("startDate가 null이면 예외가 발생한다")
    void start_date가_null이면_예외가_발생한다() {
        assertThrows(
                NullPointerException.class,
                () -> RecurrenceRule.create(
                        RecurrencePattern.DAILY,
                        1,
                        null,
                        null
                )
        );
    }

}
