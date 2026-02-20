package com.example.kihan.domain.deadline;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RecurrenceRuleTest {

    @Test
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

}
