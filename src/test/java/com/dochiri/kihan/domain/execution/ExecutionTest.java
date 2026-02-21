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
    @DisplayName("생성 시 기본 상태는 IN_PROGRESS이다")
    void shouldCreateExecutionWithInProgressStatus() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        assertEquals(ExecutionStatus.IN_PROGRESS, execution.getStatus());
        assertTrue(execution.isInProgress());
        assertTrue(!execution.isDone());
        assertTrue(!execution.isPaused());
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
    @DisplayName("markAsPaused는 상태를 PAUSED로 바꾸고 완료 시각을 제거한다")
    void shouldMarkExecutionAsPausedAndClearCompletedAt() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        execution.markAsPaused();

        assertEquals(ExecutionStatus.PAUSED, execution.getStatus());
        assertNull(execution.getCompletedAt());
        assertTrue(execution.isPaused());
    }

    @Test
    @DisplayName("DONE 상태에서 markAsPaused를 호출하면 예외가 발생한다")
    void shouldThrowWhenMarkingPausedAfterDone() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        execution.markAsDone(LocalDateTime.of(2026, 2, 20, 12, 0));

        assertThrows(ExecutionAlreadyCompletedException.class, execution::markAsPaused);
    }

    @Test
    @DisplayName("PAUSED 상태에서 markAsDone을 호출하면 DONE으로 변경된다")
    void shouldAllowMarkingDoneAfterPaused() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        LocalDateTime completedAt = LocalDateTime.of(2026, 2, 20, 14, 0);
        execution.markAsPaused();

        execution.markAsDone(completedAt);

        assertTrue(execution.isDone());
        assertEquals(completedAt, execution.getCompletedAt());
    }

    @Test
    @DisplayName("PAUSED 상태에서 markAsInProgress를 호출하면 IN_PROGRESS로 변경된다")
    void shouldAllowResumingFromPausedToInProgress() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        execution.markAsPaused();

        execution.markAsInProgress();

        assertTrue(execution.isInProgress());
        assertNull(execution.getCompletedAt());
    }

    @Test
    @DisplayName("DONE 상태에서 markAsInProgress를 호출하면 예외가 발생한다")
    void shouldThrowWhenResumingAfterDone() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        execution.markAsDone(LocalDateTime.of(2026, 2, 20, 12, 0));

        assertThrows(ExecutionAlreadyCompletedException.class, execution::markAsInProgress);
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
