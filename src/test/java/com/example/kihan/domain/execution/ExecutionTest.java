package com.example.kihan.domain.execution;

import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.domain.deadline.DeadlineType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionTest {

    @Test
    void mark_as_done은_완료_상태와_완료시각을_설정한다() {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                "description",
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 20, 10, 0),
                null
        );
        Execution execution = Execution.create(deadline, LocalDate.of(2026, 2, 20));
        LocalDateTime completedAt = LocalDateTime.of(2026, 2, 20, 12, 0);

        execution.markAsDone(completedAt);

        assertEquals(ExecutionStatus.DONE, execution.getStatus());
        assertEquals(completedAt, execution.getCompletedAt());
    }

    @Test
    void 이미_done이면_mark_as_done에서_예외가_발생한다() {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                "description",
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 20, 10, 0),
                null
        );
        Execution execution = Execution.create(deadline, LocalDate.of(2026, 2, 20));

        execution.markAsDone(LocalDateTime.of(2026, 2, 20, 12, 0));

        assertThrows(
                ExecutionAlreadyCompletedException.class,
                () -> execution.markAsDone(LocalDateTime.of(2026, 2, 20, 13, 0))
        );
    }

}
