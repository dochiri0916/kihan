package com.dochiri.kihan.domain.deadline;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Deadline 도메인 테스트")
class DeadlineDomainTest {

    @Test
    @DisplayName("ONE_TIME 마감은 dueDate가 있으면 정상 등록된다")
    void should_register_one_time_deadline_with_due_date() {
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
        assertNull(deadline.getRecurrenceRule());
    }

    @Test
    @DisplayName("설명이 null이어도 ONE_TIME 마감 등록이 가능하다")
    void should_allow_null_description_for_one_time_deadline() {
        Deadline deadline = Deadline.register(
                1L,
                "운동",
                null,
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 21, 9, 0),
                null
        );

        assertEquals("운동", deadline.getTitle());
        assertTrue(deadline.getDescription() == null);
    }

    @Test
    @DisplayName("RECURRING 마감은 recurrenceRule이 있으면 정상 등록된다")
    void should_register_recurring_deadline_with_rule() {
        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.WEEKLY,
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
    void should_throw_when_one_time_deadline_has_no_due_date() {
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
    void should_throw_when_one_time_deadline_has_recurrence_rule() {
        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.DAILY,
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
    void should_throw_when_recurring_deadline_has_no_recurrence_rule() {
        assertThrows(
                InvalidDeadlineRuleException.class,
                () -> Deadline.register(
                        1L,
                        "title",
                        "description",
                        DeadlineType.RECURRING,
                        LocalDateTime.of(2026, 2, 20, 10, 0),
                        null
                )
        );
    }

    @Test
    @DisplayName("RECURRING 마감은 dueDate가 있으면 예외가 발생한다")
    void should_throw_when_recurring_deadline_has_due_date() {
        RecurrenceRule rule = RecurrenceRule.create(
                RecurrencePattern.MONTHLY,
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
    @DisplayName("등록 시 userId가 null이면 예외가 발생한다")
    void should_throw_when_user_id_is_null_on_register() {
        assertThrows(
                NullPointerException.class,
                () -> Deadline.register(
                        null,
                        "title",
                        "description",
                        DeadlineType.ONE_TIME,
                        LocalDateTime.of(2026, 2, 20, 10, 0),
                        null
                )
        );
    }

    @Test
    @DisplayName("등록 시 title이 null이면 예외가 발생한다")
    void should_throw_when_title_is_null_on_register() {
        assertThrows(
                NullPointerException.class,
                () -> Deadline.register(
                        1L,
                        null,
                        "description",
                        DeadlineType.ONE_TIME,
                        LocalDateTime.of(2026, 2, 20, 10, 0),
                        null
                )
        );
    }

    @Test
    @DisplayName("등록 시 type이 null이면 예외가 발생한다")
    void should_throw_when_type_is_null_on_register() {
        assertThrows(
                NullPointerException.class,
                () -> Deadline.register(
                        1L,
                        "title",
                        "description",
                        null,
                        LocalDateTime.of(2026, 2, 20, 10, 0),
                        null
                )
        );
    }

    @Test
    @DisplayName("제목과 설명을 업데이트할 수 있다")
    void should_update_title_and_description() {
        Deadline deadline = oneTimeDeadline();

        deadline.update("새 제목", "새 설명");

        assertEquals("새 제목", deadline.getTitle());
        assertEquals("새 설명", deadline.getDescription());
    }

    @Test
    @DisplayName("제목이 null이면 제목은 유지되고 설명만 변경된다")
    void should_keep_title_when_new_title_is_null() {
        Deadline deadline = oneTimeDeadline();

        deadline.update(null, "설명만 변경");

        assertEquals("title", deadline.getTitle());
        assertEquals("설명만 변경", deadline.getDescription());
    }

    @Test
    @DisplayName("빈 제목으로 수정하면 예외가 발생한다")
    void should_throw_when_updating_with_blank_title() {
        Deadline deadline = oneTimeDeadline();

        assertThrows(InvalidDeadlineTitleException.class, () -> deadline.update("   ", null));
    }

    @Test
    @DisplayName("update에서 설명이 null이면 기존 설명을 유지한다")
    void should_keep_description_when_new_description_is_null() {
        Deadline deadline = oneTimeDeadline();

        deadline.update("새 제목", null);

        assertEquals("새 제목", deadline.getTitle());
        assertEquals("description", deadline.getDescription());
    }

    @Test
    @DisplayName("update에서 제목이 공백이면 기존 제목은 유지된다")
    void should_keep_original_title_when_blank_title_update_fails() {
        Deadline deadline = oneTimeDeadline();

        assertThrows(InvalidDeadlineTitleException.class, () -> deadline.update(" ", "새 설명"));

        assertEquals("title", deadline.getTitle());
    }

    @Test
    @DisplayName("완료 처리하면 삭제 상태가 된다")
    void should_mark_deadline_as_deleted_when_completed() {
        Deadline deadline = oneTimeDeadline();

        deadline.markAsCompleted(LocalDateTime.of(2026, 2, 20, 12, 0));

        assertTrue(deadline.isDeleted());
        assertNotNull(deadline.getDeletedAt());
    }

    @Test
    @DisplayName("완료 처리 시 now가 null이면 예외가 발생한다")
    void should_throw_when_completed_at_is_null() {
        Deadline deadline = oneTimeDeadline();

        assertThrows(NullPointerException.class, () -> deadline.markAsCompleted(null));
    }

    @Test
    @DisplayName("복구하면 삭제 상태가 해제된다")
    void should_restore_deleted_deadline() {
        Deadline deadline = oneTimeDeadline();
        deadline.markAsCompleted(LocalDateTime.of(2026, 2, 20, 12, 0));

        deadline.restore();

        assertFalse(deadline.isDeleted());
    }

    @Test
    @DisplayName("소유자가 아니면 접근 예외가 발생한다")
    void should_throw_when_ownership_verification_fails() {
        Deadline deadline = oneTimeDeadline();

        DeadlineAccessDeniedException exception = assertThrows(
                DeadlineAccessDeniedException.class,
                () -> deadline.verifyOwnership(2L)
        );

        assertTrue(exception.getMessage().contains("userId=2"));
    }

    @Test
    @DisplayName("소유자는 접근 검증을 통과한다")
    void should_pass_ownership_verification_for_owner() {
        Deadline deadline = oneTimeDeadline();

        deadline.verifyOwnership(1L);
    }

    @Test
    @DisplayName("verifyOwnership에서 요청 사용자 ID가 null이면 접근 거부 예외가 발생한다")
    void should_throw_when_ownership_verification_request_user_is_null() {
        Deadline deadline = oneTimeDeadline();

        assertThrows(DeadlineAccessDeniedException.class, () -> deadline.verifyOwnership(null));
    }

    @Test
    @DisplayName("InvalidDeadlineRuleException 팩토리 메시지를 검증한다")
    void should_validate_invalid_deadline_rule_exception_factory_messages() {
        assertTrue(InvalidDeadlineRuleException.oneTimeDueDateRequired().getMessage().contains("dueDate"));
        assertTrue(InvalidDeadlineRuleException.oneTimeNoRecurrence().getMessage().contains("recurrenceRule"));
        assertTrue(InvalidDeadlineRuleException.recurringRuleRequired().getMessage().contains("recurrenceRule"));
        assertTrue(InvalidDeadlineRuleException.recurringNoDueDate().getMessage().contains("dueDate"));
        assertTrue(InvalidDeadlineRuleException.endDateAfterStart().getMessage().contains("startDate"));
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
