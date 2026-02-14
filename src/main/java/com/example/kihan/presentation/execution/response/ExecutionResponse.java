package com.example.kihan.presentation.execution.response;

import com.example.kihan.application.execution.dto.ExecutionDetail;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "실행 응답")
public record ExecutionResponse(
        @Schema(description = "실행 ID", example = "1")
        Long id,

        @Schema(description = "기한 ID", example = "1")
        Long deadlineId,

        @Schema(description = "예정일", example = "2026-02-14")
        LocalDate scheduledDate,

        @Schema(description = "상태", example = "PENDING")
        String status,

        @Schema(description = "완료 시각", example = "2026-02-14T10:30:00")
        LocalDateTime completedAt
) {
    public static ExecutionResponse from(final ExecutionDetail detail) {
        return new ExecutionResponse(
                detail.id(),
                detail.deadlineId(),
                detail.scheduledDate(),
                detail.status().name(),
                detail.completedAt()
        );
    }
}
