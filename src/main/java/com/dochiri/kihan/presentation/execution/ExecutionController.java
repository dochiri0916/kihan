package com.dochiri.kihan.presentation.execution;

import com.dochiri.kihan.application.execution.command.MarkExecutionAsPausedService;
import com.dochiri.kihan.application.execution.command.MarkExecutionAsDoneService;
import com.dochiri.kihan.application.execution.command.MarkExecutionAsInProgressService;
import com.dochiri.kihan.application.execution.command.MarkExecutionByDeadlineAsDoneService;
import com.dochiri.kihan.application.execution.query.ExecutionQueryService;
import com.dochiri.kihan.application.execution.usecase.GetExecutionsByDateRangeUseCase;
import com.dochiri.kihan.infrastructure.security.jwt.JwtPrincipal;
import com.dochiri.kihan.presentation.execution.mapper.ExecutionResponseMapper;
import com.dochiri.kihan.presentation.execution.response.ExecutionQueryResponse;
import com.dochiri.kihan.presentation.execution.response.ExecutionStatusChangedResponse;
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
    private final MarkExecutionByDeadlineAsDoneService markExecutionByDeadlineAsDoneService;
    private final MarkExecutionAsPausedService markExecutionAsPausedService;
    private final MarkExecutionAsInProgressService markExecutionAsInProgressService;
    private final ExecutionQueryService executionQueryService;
    private final GetExecutionsByDateRangeUseCase getExecutionsByDateRangeUseCase;
    private final ExecutionResponseMapper executionResponseMapper;

    @Operation(summary = "실행 조회", description = "실행 ID로 실행을 조회합니다")
    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionQueryResponse> getExecution(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        return ResponseEntity.ok(
                executionResponseMapper.toExecutionQueryResponse(
                        executionQueryService.getById(
                                principal.userId(), executionId
                        )
                )
        );
    }

    @Operation(summary = "기한별 실행 목록 조회", description = "특정 기한의 모든 실행을 조회합니다")
    @GetMapping("/deadline/{deadlineId}")
    public ResponseEntity<List<ExecutionQueryResponse>> getExecutionsByDeadline(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "기한 ID", example = "1")
            @PathVariable Long deadlineId
    ) {
        return ResponseEntity.ok(
                executionResponseMapper.toExecutionQueryResponses(
                        executionQueryService.getByDeadlineId(
                                principal.userId(), deadlineId
                        )
                )
        );
    }

    @Operation(summary = "기간별 실행 목록 조회", description = "지정된 기간의 모든 실행을 조회합니다")
    @GetMapping
    public ResponseEntity<List<ExecutionQueryResponse>> getExecutionsByDateRange(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "시작일", example = "2026-02-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "종료일", example = "2026-02-28")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                executionResponseMapper.toExecutionQueryResponses(
                        getExecutionsByDateRangeUseCase.execute(
                                principal.userId(),
                                startDate,
                                endDate
                        )
                )
        );
    }

    @Operation(summary = "실행 완료 처리", description = "실행을 완료 상태로 변경합니다")
    @PatchMapping("/{executionId}/done")
    public ResponseEntity<ExecutionStatusChangedResponse> markAsDone(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        return ResponseEntity.ok(
                executionResponseMapper.toExecutionStatusChangedResponse(
                        markExecutionAsDoneService.execute(
                                principal.userId(), executionId
                        )
                )
        );
    }

    @Operation(summary = "기한 기준 실행 완료 처리", description = "해당 기한의 실행을 완료 상태로 변경합니다. 실행이 없으면 생성 후 완료 처리합니다")
    @PatchMapping("/deadline/{deadlineId}/done")
    public ResponseEntity<ExecutionStatusChangedResponse> markByDeadlineAsDone(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "기한 ID", example = "1")
            @PathVariable Long deadlineId
    ) {
        return ResponseEntity.ok(
                executionResponseMapper.toExecutionStatusChangedResponse(
                        markExecutionByDeadlineAsDoneService.execute(
                                principal.userId(), deadlineId
                        )
                )
        );
    }

    @Operation(summary = "실행 중지 처리", description = "실행을 중지 상태로 변경합니다")
    @PatchMapping("/{executionId}/paused")
    public ResponseEntity<ExecutionStatusChangedResponse> markAsPaused(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        return ResponseEntity.ok(
                executionResponseMapper.toExecutionStatusChangedResponse(
                        markExecutionAsPausedService.execute(
                                principal.userId(), executionId
                        )
                )
        );
    }

    @Operation(summary = "실행 재개 처리", description = "중지된 실행을 진행 상태로 변경합니다")
    @PatchMapping({"/{executionId}/resume", "/{executionId}/in-progress"})
    public ResponseEntity<ExecutionStatusChangedResponse> resume(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        return ResponseEntity.ok(
                executionResponseMapper.toExecutionStatusChangedResponse(
                        markExecutionAsInProgressService.execute(
                                principal.userId(), executionId
                        )
                )
        );
    }

}
