package com.example.kihan.presentation.execution;

import com.example.kihan.application.execution.command.MarkExecutionAsDelayedService;
import com.example.kihan.application.execution.command.MarkExecutionAsDoneService;
import com.example.kihan.application.execution.dto.ExecutionDetail;
import com.example.kihan.application.execution.query.ExecutionQueryService;
import com.example.kihan.presentation.execution.response.ExecutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "실행", description = "실행 관리 API")
@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final MarkExecutionAsDoneService markExecutionAsDoneService;
    private final MarkExecutionAsDelayedService markExecutionAsDelayedService;
    private final ExecutionQueryService executionQueryService;

    @Operation(summary = "실행 조회", description = "실행 ID로 실행을 조회합니다")
    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionResponse> getExecution(
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable final Long executionId
    ) {
        ExecutionDetail detail = executionQueryService.findById(executionId);
        return ResponseEntity.ok(ExecutionResponse.from(detail));
    }

    @Operation(summary = "기한별 실행 목록 조회", description = "특정 기한의 모든 실행을 조회합니다")
    @GetMapping("/deadline/{deadlineId}")
    public ResponseEntity<List<ExecutionResponse>> getExecutionsByDeadline(
            @Parameter(description = "기한 ID", example = "1")
            @PathVariable final Long deadlineId
    ) {
        List<ExecutionDetail> details = executionQueryService.findByDeadlineId(deadlineId);
        return ResponseEntity.ok(details.stream()
                .map(ExecutionResponse::from)
                .toList());
    }

    @Operation(summary = "기간별 실행 목록 조회", description = "지정된 기간의 모든 실행을 조회합니다")
    @GetMapping
    public ResponseEntity<List<ExecutionResponse>> getExecutionsByDateRange(
            @Parameter(description = "시작일", example = "2026-02-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,

            @Parameter(description = "종료일", example = "2026-02-28")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate
    ) {
        List<ExecutionDetail> details = executionQueryService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(details.stream()
                .map(ExecutionResponse::from)
                .toList());
    }

    @Operation(summary = "실행 완료 처리", description = "실행을 완료 상태로 변경합니다")
    @PatchMapping("/{executionId}/done")
    public ResponseEntity<Void> markAsDone(
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable final Long executionId
    ) {
        markExecutionAsDoneService.execute(executionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "실행 지연 처리", description = "실행을 지연 상태로 변경합니다")
    @PatchMapping("/{executionId}/delayed")
    public ResponseEntity<Void> markAsDelayed(
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable final Long executionId
    ) {
        markExecutionAsDelayedService.execute(executionId);
        return ResponseEntity.noContent().build();
    }

}
