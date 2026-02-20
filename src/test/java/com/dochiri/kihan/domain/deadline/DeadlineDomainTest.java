package com.dochiri.kihan.domain.deadline;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DeadlineDomainTest {

    @Test
    void one_time는_due_date가_없으면_예외가_발생한다() {
        assertThrows(
                InvalidDeadlineRuleException.class,
                () -> Deadline.register(
                        1L,
                        "title",
                        "description",
                        DeadlineType.ONE_TIME,
                        null,
                        null
                )
        );
    }

    @Test
    void recurring은_recurrence_rule이_없으면_예외가_발생한다() {
        assertThrows(
                InvalidDeadlineRuleException.class,
                () -> Deadline.register(
                        1L,
                        "title",
                        "description",
                        DeadlineType.RECURRING,
                        LocalDateTime.now(),
                        null
                )
        );
    }

}
