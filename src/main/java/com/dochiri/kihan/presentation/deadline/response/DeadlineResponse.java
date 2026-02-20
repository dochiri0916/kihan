package com.dochiri.kihan.presentation.deadline.response;

import com.dochiri.kihan.application.deadline.dto.DeadlineDetail;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "기한 응답")
public record DeadlineResponse(
        @Schema(description = "기한 ID", example = "1")
        Long id,

        @Schema(description = "기한 제목", example = "프로젝트 제출")
        String title,

        @Schema(description = "기한 설명", example = "최종 보고서 제출 마감")
        String description,

        @Schema(description = "기한 타입", example = "ONE_TIME")
        DeadlineType type,

        @Schema(description = "마감 일시 (ONE_TIME인 경우만)", example = "2026-03-15T23:59:59")
        LocalDateTime dueDate,

        @Schema(description = "반복 규칙 (RECURRING인 경우만)")
        RecurrenceRule recurrenceRule,

        @Schema(description = "생성 일시", example = "2026-02-13T14:30:00")
        LocalDateTime createdAt
) {
    public static DeadlineResponse from(DeadlineDetail detail) {
        return new DeadlineResponse(
                detail.id(),
                detail.title(),
                detail.description(),
                detail.type(),
                detail.dueDate(),
                detail.recurrenceRule(),
                detail.createdAt()
        );
    }
}