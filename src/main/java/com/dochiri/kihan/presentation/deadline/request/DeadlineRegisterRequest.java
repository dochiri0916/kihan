package com.dochiri.kihan.presentation.deadline.request;

import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrencePattern;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "기한 등록 요청 (ONE_TIME과 RECURRING은 별도 필드 사용)")
public record DeadlineRegisterRequest(
        @Schema(description = "기한 제목", example = "프로젝트 제출")
        @NotBlank String title,

        @Schema(description = "기한 설명", example = "최종 보고서 제출 마감")
        String description,

        @Schema(description = "기한 타입", example = "ONE_TIME", allowableValues = {"ONE_TIME", "RECURRING"})
        @NotNull DeadlineType type,

        @Schema(description = "마감 일시 (type=ONE_TIME일 때만 사용, RECURRING일 때는 null)", example = "2027-12-31T23:59:59", nullable = true)
        LocalDateTime dueDate,

        @Schema(description = "반복 패턴 (type=RECURRING일 때만 사용, ONE_TIME일 때는 null)", example = "WEEKLY", nullable = true)
        RecurrencePattern pattern,

        @Schema(description = "반복 간격 (type=RECURRING일 때만 사용)", example = "1", nullable = true)
        Integer interval,

        @Schema(description = "반복 시작일 (type=RECURRING일 때만 사용)", example = "2027-01-01", nullable = true)
        LocalDate startDate,

        @Schema(description = "반복 종료일 (type=RECURRING일 때만 사용, 선택)", example = "2027-12-31", nullable = true)
        LocalDate endDate
) {
    public RecurrenceRule toRecurrenceRule() {
        if (pattern == null) {
            return null;
        }
        int recurrenceInterval = interval == null ? 1 : interval;
        return RecurrenceRule.create(pattern, recurrenceInterval, startDate, endDate);
    }
}
