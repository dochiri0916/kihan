package com.dochiri.kihan.presentation.deadline.request;

import com.dochiri.kihan.domain.deadline.RecurrencePattern;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "반복 규칙 수정 요청")
public record DeadlineRecurrenceUpdateRequest(
        @Schema(description = "반복 패턴", example = "WEEKLY")
        @NotNull
        RecurrencePattern pattern,

        @Schema(description = "반복 시작일", example = "2027-01-01")
        @NotNull
        LocalDate startDate,

        @Schema(description = "반복 종료일 (선택)", example = "2027-12-31", nullable = true)
        LocalDate endDate
) {
    public RecurrenceRule toRecurrenceRule() {
        return RecurrenceRule.create(pattern, startDate, endDate);
    }
}
