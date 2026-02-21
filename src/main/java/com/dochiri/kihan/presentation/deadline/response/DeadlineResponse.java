package com.dochiri.kihan.presentation.deadline.response;

import com.dochiri.kihan.application.deadline.dto.DeadlineDetail;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "기한 응답")
public record DeadlineResponse(
        @Schema(description = "기한 ID", example = "1")
        Long id,

        @Schema(description = "기한 제목", example = "프로젝트 제출")
        String title,

        @Schema(description = "기한 타입", example = "ONE_TIME")
        DeadlineType type,

        @Schema(description = "마감일 (ONE_TIME인 경우만)", example = "2026-03-15")
        LocalDate dueDate,

        @Schema(description = "반복 규칙 (RECURRING인 경우만)")
        RecurrenceRule recurrenceRule,

        @Schema(description = "생성 일시", example = "2026-02-13T14:30:00")
        LocalDateTime createdAt
) {
    public static DeadlineResponse from(DeadlineDetail detail) {
        return new DeadlineResponse(
                detail.id(),
                detail.title(),
                detail.type(),
                detail.dueDate(),
                detail.recurrenceRule(),
                detail.createdAt()
        );
    }
}
