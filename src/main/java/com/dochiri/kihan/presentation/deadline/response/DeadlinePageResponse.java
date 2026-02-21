package com.dochiri.kihan.presentation.deadline.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "기한 목록 페이지 응답")
public record DeadlinePageResponse(
        @Schema(description = "기한 목록")
        List<DeadlineResponse> items,
        @Schema(description = "페이지 정보")
        DeadlinePageInfoResponse pageInfo
) {
}
