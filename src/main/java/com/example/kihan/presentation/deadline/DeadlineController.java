package com.example.kihan.presentation.deadline;

import com.example.kihan.application.deadline.command.DeleteDeadlineService;
import com.example.kihan.application.deadline.dto.RegisterDeadlineCommand;
import com.example.kihan.application.deadline.command.RegisterDeadlineService;
import com.example.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.example.kihan.application.deadline.command.UpdateDeadlineService;
import com.example.kihan.application.deadline.query.DeadlineQueryService;
import com.example.kihan.infrastructure.security.jwt.JwtPrincipal;
import com.example.kihan.presentation.deadline.request.DeadlineRegisterRequest;
import com.example.kihan.presentation.deadline.request.DeadlineUpdateRequest;
import com.example.kihan.presentation.deadline.response.DeadlineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Deadline", description = "기한 관리 API")
@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final RegisterDeadlineService registerDeadlineService;
    private final UpdateDeadlineService updateDeadlineService;
    private final DeleteDeadlineService deleteDeadlineService;
    private final DeadlineQueryService deadlineQueryService;

    @Operation(
            summary = "기한 등록",
            description = "새로운 기한을 등록합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "ONE_TIME 기한",
                                            value = """
                                                    {
                                                      "title": "프로젝트 제출",
                                                      "description": "최종 보고서 제출",
                                                      "type": "ONE_TIME",
                                                      "dueDate": "2027-12-31T23:59:59",
                                                      "pattern": null,
                                                      "interval": null,
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
                                                      "description": "팀 주간 회의",
                                                      "type": "RECURRING",
                                                      "dueDate": null,
                                                      "pattern": "WEEKLY",
                                                      "interval": 1,
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
                request.description(),
                request.type(),
                request.dueDate(),
                request.toRecurrenceRule()
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

    @Operation(summary = "기한 목록 조회", description = "사용자의 모든 기한을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<DeadlineResponse>> findAll(@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(deadlineQueryService.getAllByUserId(principal.userId()).stream()
                .map(DeadlineResponse::from)
                .toList());
    }

    @Operation(summary = "기한 수정", description = "기한의 제목과 설명을 수정합니다.")
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
                request.title(),
                request.description()
        );
        updateDeadlineService.update(command);
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
}