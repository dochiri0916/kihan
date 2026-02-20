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
    void create시_기본_상태는_pending이다() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        assertEquals(ExecutionStatus.PENDING, execution.getStatus());
        assertTrue(execution.isPending());
        assertEquals(LocalDate.of(2026, 2, 20), execution.getScheduledDate());
    }

    @Test
    @DisplayName("생성 시 deadline이 null이면 예외가 발생한다")
    void create에서_deadline이_null이면_예외() {
        assertThrows(
                NullPointerException.class,
                () -> Execution.create(null, LocalDate.of(2026, 2, 20))
        );
    }

    @Test
    @DisplayName("생성 시 scheduledDate가 null이면 예외가 발생한다")
    void create에서_scheduled_date가_null이면_예외() {
        assertThrows(
                NullPointerException.class,
                () -> Execution.create(oneTimeDeadline(), null)
        );
    }

    @Test
    @DisplayName("markAsDone은 상태와 완료 시각을 설정한다")
    void mark_as_done은_완료_상태와_완료시각을_설정한다() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        LocalDateTime completedAt = LocalDateTime.of(2026, 2, 20, 12, 0);

        execution.markAsDone(completedAt);

        assertEquals(ExecutionStatus.DONE, execution.getStatus());
        assertEquals(completedAt, execution.getCompletedAt());
        assertTrue(execution.isDone());
    }

    @Test
    @DisplayName("markAsDone에서 completedAt이 null이면 예외가 발생한다")
    void mark_as_done에서_completed_at_null이면_예외() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        assertThrows(NullPointerException.class, () -> execution.markAsDone(null));
    }

    @Test
    @DisplayName("DONE 상태에서 markAsDone을 다시 호출하면 예외가 발생한다")
    void 이미_done이면_mark_as_done에서_예외가_발생한다() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        execution.markAsDone(LocalDateTime.of(2026, 2, 20, 12, 0));

        assertThrows(
                ExecutionAlreadyCompletedException.class,
                () -> execution.markAsDone(LocalDateTime.of(2026, 2, 20, 13, 0))
        );
    }

    @Test
    @DisplayName("markAsDelayed는 상태를 DELAYED로 바꾸고 완료 시각을 제거한다")
    void mark_as_delayed는_상태와_완료시각을_갱신한다() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));

        execution.markAsDelayed();

        assertEquals(ExecutionStatus.DELAYED, execution.getStatus());
        assertNull(execution.getCompletedAt());
        assertTrue(execution.isDelayed());
    }

    @Test
    @DisplayName("DONE 상태에서 markAsDelayed를 호출하면 예외가 발생한다")
    void done_상태에서_mark_as_delayed면_예외() {
        Execution execution = Execution.create(oneTimeDeadline(), LocalDate.of(2026, 2, 20));
        execution.markAsDone(LocalDateTime.of(2026, 2, 20, 12, 0));

        assertThrows(ExecutionAlreadyCompletedException.class, execution::markAsDelayed);
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
