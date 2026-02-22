package com.dochiri.kihan.presentation.deadline.request;

import com.dochiri.kihan.domain.deadline.RecurrencePattern;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.time.Clock;
import java.time.LocalDate;

@Schema(description = "기한 등록 요청 (pattern 유무로 단건/반복 자동 판별)")
public record DeadlineRegisterRequest(
        @Schema(description = "기한 제목", example = "프로젝트 제출")
        @NotBlank String title,

        @Schema(description = "마감일 (pattern이 없을 때 사용)", example = "2027-12-31", nullable = true)
        LocalDate dueDate,

        @Schema(description = "반복 패턴 (값이 있으면 반복으로 처리)", example = "WEEKLY", nullable = true)
        RecurrencePattern pattern,

        @Schema(description = "반복 시작일 (pattern이 있을 때만 사용, 생략 시 생성일로 대체)", example = "2027-01-01", nullable = true)
        LocalDate startDate,

        @Schema(description = "반복 종료일 (pattern이 있을 때만 사용, 선택)", example = "2027-12-31", nullable = true)
        LocalDate endDate
) {
    public RecurrenceRule toRecurrenceRule(Clock clock) {
        if (pattern == null) {
            return null;
        }
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now(clock);
        return RecurrenceRule.create(pattern, effectiveStartDate, endDate);
    }

    @AssertTrue(message = "단건 등록은 dueDate가 필수입니다.")
    public boolean isSingleDueDateValid() {
        if (pattern == null) {
            return dueDate != null;
        }
        return true;
    }

    @AssertTrue(message = "dueDate 또는 pattern 중 하나는 필수입니다.")
    public boolean isShapeResolvable() {
        return pattern != null || dueDate != null;
    }

    @AssertTrue(message = "반복 등록은 dueDate를 사용할 수 없습니다.")
    public boolean isRecurringDueDateValid() {
        if (pattern != null) {
            return dueDate == null;
        }
        return true;
    }
}
