package com.dochiri.kihan.presentation.deadline;

import com.dochiri.kihan.application.deadline.command.DeleteDeadlineService;
import com.dochiri.kihan.application.deadline.dto.RegisterDeadlineCommand;
import com.dochiri.kihan.application.deadline.command.RegisterDeadlineService;
import com.dochiri.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.dochiri.kihan.application.deadline.command.UpdateDeadlineService;
import com.dochiri.kihan.application.deadline.query.DeadlineSortBy;
import com.dochiri.kihan.application.deadline.query.DeadlineQueryService;
import com.dochiri.kihan.infrastructure.realtime.DeadlineStreamBroker;
import com.dochiri.kihan.infrastructure.security.jwt.JwtPrincipal;
import com.dochiri.kihan.presentation.deadline.request.DeadlineRecurrenceUpdateRequest;
import com.dochiri.kihan.presentation.deadline.request.DeadlineRegisterRequest;
import com.dochiri.kihan.presentation.deadline.response.DeadlinePageInfoResponse;
import com.dochiri.kihan.presentation.deadline.response.DeadlinePageResponse;
import com.dochiri.kihan.presentation.deadline.request.DeadlineUpdateRequest;
import com.dochiri.kihan.presentation.deadline.response.DeadlineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "Deadline", description = "기한 관리 API")
@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final RegisterDeadlineService registerDeadlineService;
    private final UpdateDeadlineService updateDeadlineService;
    private final DeleteDeadlineService deleteDeadlineService;
    private final DeadlineQueryService deadlineQueryService;
    private final DeadlineStreamBroker deadlineStreamBroker;
    private final Clock clock;

    @Operation(
            summary = "기한 등록",
            description = "새로운 기한을 등록합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "단건 기한",
                                            value = """
                                                    {
                                                      "title": "프로젝트 제출",
                                                      "dueDate": "2027-12-31",
                                                      "pattern": null,
                                                      "startDate": null,
                                                      "endDate": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "RECURRING 기한",
                                            value = """
                                                    {
                                                      "title": "주간 회의",
                                                      "dueDate": null,
                                                      "pattern": "WEEKLY",
                                                      "startDate": "2027-01-01",
                                                      "endDate": "2027-12-31"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @PostMapping
    public ResponseEntity<Void> register(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody DeadlineRegisterRequest request
    ) {
        RegisterDeadlineCommand command = new RegisterDeadlineCommand(
                principal.userId(),
                request.title(),
                request.dueDate(),
                request.toRecurrenceRule(clock)
        );
        Long deadlineId = registerDeadlineService.execute(command);
        return ResponseEntity.created(URI.create("/api/deadlines/" + deadlineId)).build();
    }

    @Operation(summary = "기한 단건 조회", description = "ID로 기한을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{id}")
    public ResponseEntity<DeadlineResponse> findById(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "기한 ID") @PathVariable Long id
    ) {
        return ResponseEntity.ok(DeadlineResponse.from(deadlineQueryService.getById(principal.userId(), id)));
    }

    @Operation(summary = "기한 목록 조회", description = "사용자의 기한을 페이지 단위로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "304", description = "변경 없음")
    @GetMapping
    public ResponseEntity<DeadlinePageResponse> findAll(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "CREATED_AT") DeadlineSortBy sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        LocalDateTime lastModifiedAt = deadlineQueryService.getLastModifiedAt(principal.userId());
        long lastModifiedMillis = toEpochMillis(lastModifiedAt);
        if (hasNotBeenModified(ifModifiedSince, lastModifiedMillis)) {
            return ResponseEntity.status(304)
                    .lastModified(lastModifiedMillis)
                    .build();
        }

        Page<DeadlineResponse> responsePage = deadlineQueryService
                .getPageByUserId(principal.userId(), page, size, sortBy, direction)
                .map(DeadlineResponse::from);

        DeadlinePageResponse response = new DeadlinePageResponse(
                responsePage.getContent(),
                new DeadlinePageInfoResponse(
                        responsePage.getNumber(),
                        responsePage.getSize(),
                        responsePage.getTotalElements(),
                        responsePage.getTotalPages(),
                        responsePage.hasNext(),
                        responsePage.hasPrevious()
                )
        );

        return ResponseEntity.ok()
                .lastModified(lastModifiedMillis)
                .body(response);
    }

    @Operation(summary = "기한/실행 변경 이벤트 구독", description = "SSE 스트림으로 기한/실행 변경 이벤트를 구독합니다.")
    @ApiResponse(responseCode = "200", description = "구독 성공")
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
    ) {
        return deadlineStreamBroker.subscribe(principal.userId(), lastEventId);
    }

    @Operation(summary = "기한 수정", description = "기한의 제목을 수정합니다.")
    @ApiResponse(responseCode = "204", description = "수정 성공")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "기한 ID") @PathVariable Long id,
            @RequestBody DeadlineUpdateRequest request
    ) {
        UpdateDeadlineCommand command = new UpdateDeadlineCommand(
                principal.userId(),
                id,
                request.title()
        );
        updateDeadlineService.update(command);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "반복 규칙 수정", description = "반복 기한의 반복 규칙을 수정합니다.")
    @ApiResponse(responseCode = "204", description = "수정 성공")
    @PatchMapping("/{id}/recurrence")
    public ResponseEntity<Void> updateRecurrence(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "기한 ID") @PathVariable Long id,
            @Valid @RequestBody DeadlineRecurrenceUpdateRequest request
    ) {
        updateDeadlineService.updateRecurrence(principal.userId(), id, request.toRecurrenceRule());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "기한 삭제", description = "기한을 삭제합니다(soft delete).")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
            @Parameter(description = "기한 ID") @PathVariable Long id
    ) {
        deleteDeadlineService.execute(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    private boolean hasNotBeenModified(String ifModifiedSince, long lastModifiedMillis) {
        if (ifModifiedSince == null || ifModifiedSince.isBlank()) {
            return false;
        }
        try {
            long ifModifiedSinceMillis = DateTimeFormatter.RFC_1123_DATE_TIME
                    .parse(ifModifiedSince, java.time.Instant::from)
                    .toEpochMilli();
            return ifModifiedSinceMillis >= lastModifiedMillis;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private long toEpochMillis(LocalDateTime lastModifiedAt) {
        if (lastModifiedAt == null) {
            return 0L;
        }
        return lastModifiedAt.atZone(clock.getZone())
                .toInstant()
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                .toEpochMilli();
    }
}
