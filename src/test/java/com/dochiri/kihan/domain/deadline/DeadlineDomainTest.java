package com.dochiri.kihan.domain.deadline;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Deadline 도메인 테스트")
class DeadlineDomainTest {

    @Test
    @DisplayName("ONE_TIME 마감은 dueDate가 있으면 정상 등록된다")
    void one_time_정상_등록() {
        LocalDateTime dueDate = LocalDateTime.of(2026, 2, 21, 9, 0);

        Deadline deadline = Deadline.register(
                1L,
                "운동",
                "아침 러닝",
                DeadlineType.ONE_TIME,
                dueDate,
                null
        );

        assertEquals(1L, deadline.getUserId());
        assertEquals("운동", deadline.getTitle());
        assertEquals("아침 러닝", deadline.getDescription());
        assertEquals(DeadlineType.ONE_TIME, deadline.getType());
        assertEquals(dueDate, deadline.getDueDate());
        assertTrue(deadline.getRecurrenceRule() == null);
    }

    @Test
    @DisplayName("RECURRING 마감은 recurrenceRule이 있으면 정상 등록된다")
    void recurring_정상_등록() {
        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.WEEKLY,
                1,
                LocalDate.of(2026, 2, 1),
                null
        );

        Deadline deadline = Deadline.register(
                1L,
                "정기 점검",
                "주간 점검",
                DeadlineType.RECURRING,
                null,
                rule
        );

        assertEquals(DeadlineType.RECURRING, deadline.getType());
        assertTrue(deadline.getDueDate() == null);
        assertEquals(rule, deadline.getRecurrenceRule());
    }

    @Test
    @DisplayName("ONE_TIME 마감은 dueDate가 없으면 예외가 발생한다")
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
    @DisplayName("ONE_TIME 마감은 recurrenceRule이 있으면 예외가 발생한다")
    void one_time_마감은_recurrence_rule이_있으면_예외가_발생한다() {
        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.DAILY,
                1,
                LocalDate.of(2026, 2, 1),
                null
        );

        assertThrows(
                InvalidDeadlineRuleException.class,
                () -> Deadline.register(
                        1L,
                        "title",
                        "description",
                        DeadlineType.ONE_TIME,
                        LocalDateTime.of(2026, 2, 20, 10, 0),
                        rule
                )
        );
    }

    @Test
    @DisplayName("RECURRING 마감은 recurrenceRule이 없으면 예외가 발생한다")
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

    @Test
    @DisplayName("RECURRING 마감은 dueDate가 있으면 예외가 발생한다")
    void recurring은_due_date가_있으면_예외가_발생한다() {
        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.MONTHLY,
                1,
                LocalDate.of(2026, 2, 1),
                null
        );

        assertThrows(
                InvalidDeadlineRuleException.class,
                () -> Deadline.register(
                        1L,
                        "title",
                        "description",
                        DeadlineType.RECURRING,
                        LocalDateTime.of(2026, 2, 20, 10, 0),
                        rule
                )
        );
    }

    @Test
    @DisplayName("제목과 설명을 업데이트할 수 있다")
    void update는_제목과_설명을_변경한다() {
        Deadline deadline = oneTimeDeadline();

        deadline.update("새 제목", "새 설명");

        assertEquals("새 제목", deadline.getTitle());
        assertEquals("새 설명", deadline.getDescription());
    }

    @Test
    @DisplayName("제목이 null이면 제목은 유지되고 설명만 변경된다")
    void update에서_제목이_null이면_기존값을_유지한다() {
        Deadline deadline = oneTimeDeadline();

        deadline.update(null, "설명만 변경");

        assertEquals("title", deadline.getTitle());
        assertEquals("설명만 변경", deadline.getDescription());
    }

    @Test
    @DisplayName("빈 제목으로 수정하면 예외가 발생한다")
    void update에서_빈_제목이면_예외가_발생한다() {
        Deadline deadline = oneTimeDeadline();

        assertThrows(InvalidDeadlineTitleException.class, () -> deadline.update("   ", null));
    }

    @Test
    @DisplayName("완료 처리하면 삭제 상태가 된다")
    void mark_as_completed는_삭제_상태로_변경한다() {
        Deadline deadline = oneTimeDeadline();

        deadline.markAsCompleted(LocalDateTime.of(2026, 2, 20, 12, 0));

        assertTrue(deadline.isDeleted());
        assertNotNull(deadline.getDeletedAt());
    }

    @Test
    @DisplayName("복구하면 삭제 상태가 해제된다")
    void restore는_삭제_상태를_해제한다() {
        Deadline deadline = oneTimeDeadline();
        deadline.markAsCompleted(LocalDateTime.of(2026, 2, 20, 12, 0));

        deadline.restore();

        assertFalse(deadline.isDeleted());
    }

    @Test
    @DisplayName("소유자가 아니면 접근 예외가 발생한다")
    void verify_ownership는_타인_요청에_예외를_던진다() {
        Deadline deadline = oneTimeDeadline();

        assertThrows(DeadlineAccessDeniedException.class, () -> deadline.verifyOwnership(2L));
    }

    @Test
    @DisplayName("소유자는 접근 검증을 통과한다")
    void verify_ownership는_소유자면_통과한다() {
        Deadline deadline = oneTimeDeadline();

        deadline.verifyOwnership(1L);
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
