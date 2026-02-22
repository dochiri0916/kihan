package com.dochiri.kihan.presentation.deadline.request;

import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrencePattern;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.time.Clock;
import java.time.LocalDate;

@Schema(description = "기한 등록 요청 (ONE_TIME과 RECURRING은 별도 필드 사용)")
public record DeadlineRegisterRequest(
        @Schema(description = "기한 제목", example = "프로젝트 제출")
        @NotBlank String title,

        @Schema(description = "기한 타입(선택). 생략 시 pattern 유무로 자동 판별", example = "ONE_TIME", allowableValues = {"ONE_TIME", "RECURRING"}, nullable = true)
        DeadlineType type,

        @Schema(description = "마감일 (type=ONE_TIME일 때만 사용, RECURRING일 때는 null)", example = "2027-12-31", nullable = true)
        LocalDate dueDate,

        @Schema(description = "반복 패턴 (type=RECURRING일 때만 사용, ONE_TIME일 때는 null)", example = "WEEKLY", nullable = true)
        RecurrencePattern pattern,

        @Schema(description = "반복 시작일 (type=RECURRING일 때만 사용, 생략 시 생성일로 대체)", example = "2027-01-01", nullable = true)
        LocalDate startDate,

        @Schema(description = "반복 종료일 (type=RECURRING일 때만 사용, 선택)", example = "2027-12-31", nullable = true)
        LocalDate endDate
) {
    public DeadlineType resolveType() {
        if (pattern != null) {
            return DeadlineType.RECURRING;
        }
        return DeadlineType.ONE_TIME;
    }

    public RecurrenceRule toRecurrenceRule(Clock clock) {
        if (pattern == null) {
            return null;
        }
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now(clock);
        return RecurrenceRule.create(pattern, effectiveStartDate, endDate);
    }

    @AssertTrue(message = "ONE_TIME 타입은 dueDate가 필수입니다.")
    public boolean isOneTimeDueDateValid() {
        if (resolveType().isSingle()) {
            return dueDate != null;
        }
        return true;
    }

    @AssertTrue(message = "RECURRING 타입은 pattern이 필수입니다.")
    public boolean isRecurringFieldsValid() {
        if (resolveType().isRecurring()) {
            return pattern != null;
        }
        return true;
    }

    @AssertTrue(message = "RECURRING 타입은 dueDate를 사용할 수 없습니다.")
    public boolean isRecurringDueDateValid() {
        if (resolveType().isRecurring()) {
            return dueDate == null;
        }
        return true;
    }

    @AssertTrue(message = "type이 지정되면 pattern 기반 판별 결과와 일치해야 합니다.")
    public boolean isTypeConsistentWhenProvided() {
        if (type == null) {
            return true;
        }
        return type.isRecurring() == resolveType().isRecurring();
    }
}
