package com.dochiri.kihan.presentation.deadline.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기한 수정 요청")
public record DeadlineUpdateRequest(
        @Schema(description = "수정할 제목 (null이면 수정 안 함)", example = "프로젝트 최종 제출")
        String title,

        @Schema(description = "수정할 설명 (null이면 수정 안 함)", example = "보고서와 코드 제출")
        String description
) {
}