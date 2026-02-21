package com.dochiri.kihan.presentation.execution;

import com.dochiri.kihan.application.execution.command.MarkExecutionAsPausedService;
import com.dochiri.kihan.application.execution.command.MarkExecutionAsDoneService;
import com.dochiri.kihan.application.execution.command.MarkExecutionAsInProgressService;
import com.dochiri.kihan.application.execution.dto.ExecutionDetail;
import com.dochiri.kihan.application.execution.query.DateRangeQuery;
import com.dochiri.kihan.application.execution.query.ExecutionQueryService;
import com.dochiri.kihan.infrastructure.security.jwt.JwtPrincipal;
import com.dochiri.kihan.presentation.execution.response.ExecutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "실행", description = "실행 관리 API")
@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final MarkExecutionAsDoneService markExecutionAsDoneService;
    private final MarkExecutionAsPausedService markExecutionAsPausedService;
    private final MarkExecutionAsInProgressService markExecutionAsInProgressService;
    private final ExecutionQueryService executionQueryService;

    @Operation(summary = "실행 조회", description = "실행 ID로 실행을 조회합니다")
    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionResponse> getExecution(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        ExecutionDetail detail = executionQueryService.findById(principal.userId(), executionId);
        return ResponseEntity.ok(ExecutionResponse.from(detail));
    }

    @Operation(summary = "기한별 실행 목록 조회", description = "특정 기한의 모든 실행을 조회합니다")
    @GetMapping("/deadline/{deadlineId}")
    public ResponseEntity<List<ExecutionResponse>> getExecutionsByDeadline(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "기한 ID", example = "1")
            @PathVariable Long deadlineId
    ) {
        List<ExecutionDetail> details = executionQueryService.findByDeadlineId(principal.userId(), deadlineId);
        return ResponseEntity.ok(details.stream()
                .map(ExecutionResponse::from)
                .toList());
    }

    @Operation(summary = "기간별 실행 목록 조회", description = "지정된 기간의 모든 실행을 조회합니다")
    @GetMapping
    public ResponseEntity<List<ExecutionResponse>> getExecutionsByDateRange(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "시작일", example = "2026-02-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "종료일", example = "2026-02-28")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        DateRangeQuery query = new DateRangeQuery(principal.userId(), startDate, endDate);
        List<ExecutionDetail> details = executionQueryService.findByDateRange(query);
        return ResponseEntity.ok(details.stream()
                .map(ExecutionResponse::from)
                .toList());
    }

    @Operation(summary = "실행 완료 처리", description = "실행을 완료 상태로 변경합니다")
    @PatchMapping("/{executionId}/done")
    public ResponseEntity<Void> markAsDone(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        markExecutionAsDoneService.execute(principal.userId(), executionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "실행 중지 처리", description = "실행을 중지 상태로 변경합니다")
    @PatchMapping("/{executionId}/paused")
    public ResponseEntity<Void> markAsPaused(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        markExecutionAsPausedService.execute(principal.userId(), executionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "실행 재개 처리", description = "중지된 실행을 진행 상태로 변경합니다")
    @PatchMapping({"/{executionId}/resume", "/{executionId}/in-progress"})
    public ResponseEntity<Void> resume(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        markExecutionAsInProgressService.execute(principal.userId(), executionId);
        return ResponseEntity.noContent().build();
    }

}
