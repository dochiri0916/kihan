package com.dochiri.kihan.presentation.deadline.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기한 페이지 정보")
public record DeadlinePageInfoResponse(
        @Schema(description = "현재 페이지(0-base)", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "20")
        int size,
        @Schema(description = "전체 데이터 개수", example = "183")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "10")
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,
        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious
) {
}
