package com.dochiri.kihan.domain.execution;

import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Execution 도메인 테스트")
class ExecutionTest {

    @Test
    @DisplayName("생성 시 기본 상태는 PENDING이다")
    void shouldCreateExecutionWithPendingStatus() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        assertEquals(ExecutionStatus.PENDING, execution.getStatus());
        assertTrue(execution.isPending());
        assertTrue(!execution.isDone());
        assertTrue(!execution.isDelayed());
        assertEquals(LocalDate.of(2026, 2, 20), execution.getScheduledDate());
    }

    @Test
    @DisplayName("생성 시 deadline이 null이면 예외가 발생한다")
    void shouldThrowWhenCreatingExecutionWithNullDeadline() {
        assertThrows(
                NullPointerException.class,
                () -> Execution.create(null, LocalDate.of(2026, 2, 20))
        );
    }

    @Test
    @DisplayName("생성 시 scheduledDate가 null이면 예외가 발생한다")
    void shouldThrowWhenCreatingExecutionWithNullScheduledDate() {
        assertThrows(
                NullPointerException.class,
                () -> Execution.create(oneTimeDeadline(), null)
        );
    }

    @Test
    @DisplayName("markAsDone은 상태와 완료 시각을 설정한다")
    void shouldMarkExecutionAsDoneWithCompletedAt() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        LocalDateTime completedAt = LocalDateTime.of(2026, 2, 20, 12, 0);

        execution.markAsDone(completedAt);

        assertEquals(ExecutionStatus.DONE, execution.getStatus());
        assertEquals(completedAt, execution.getCompletedAt());
        assertTrue(execution.isDone());
    }

    @Test
    @DisplayName("markAsDone에서 completedAt이 null이면 예외가 발생한다")
    void shouldThrowWhenMarkingDoneWithNullCompletedAt() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        assertThrows(NullPointerException.class, () -> execution.markAsDone(null));
    }

    @Test
    @DisplayName("DONE 상태에서 markAsDone을 다시 호출하면 예외가 발생한다")
    void shouldThrowWhenMarkingDoneAgainAfterDone() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        execution.markAsDone(LocalDateTime.of(2026, 2, 20, 12, 0));

        ExecutionAlreadyCompletedException exception = assertThrows(
                ExecutionAlreadyCompletedException.class,
                () -> execution.markAsDone(LocalDateTime.of(2026, 2, 20, 13, 0))
        );

        assertTrue(exception.getMessage().contains("2026-02-20"));
    }

    @Test
    @DisplayName("markAsDelayed는 상태를 DELAYED로 바꾸고 완료 시각을 제거한다")
    void shouldMarkExecutionAsDelayedAndClearCompletedAt() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        execution.markAsDelayed();

        assertEquals(ExecutionStatus.DELAYED, execution.getStatus());
        assertNull(execution.getCompletedAt());
        assertTrue(execution.isDelayed());
    }

    @Test
    @DisplayName("DONE 상태에서 markAsDelayed를 호출하면 예외가 발생한다")
    void shouldThrowWhenMarkingDelayedAfterDone() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        execution.markAsDone(LocalDateTime.of(2026, 2, 20, 12, 0));

        assertThrows(ExecutionAlreadyCompletedException.class, execution::markAsDelayed);
    }

    @Test
    @DisplayName("DELAYED 상태에서 markAsDone을 호출하면 DONE으로 변경된다")
    void shouldAllowMarkingDoneAfterDelayed() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        LocalDateTime completedAt = LocalDateTime.of(2026, 2, 20, 14, 0);
        execution.markAsDelayed();

        execution.markAsDone(completedAt);

        assertTrue(execution.isDone());
        assertEquals(completedAt, execution.getCompletedAt());
    }

    @Test
    @DisplayName("ExecutionNotFoundException 메시지에 ID가 포함된다")
    void shouldContainIdInExecutionNotFoundExceptionMessage() {
        ExecutionNotFoundException exception = new ExecutionNotFoundException(101L);

        assertTrue(exception.getMessage().contains("101"));
    }

    private Deadline oneTimeDeadline() {
        return Deadline.register(
                1L,
                "title",
                "description",
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 20, 10, 0),
                null
        );
    }

}
