package com.example.kihan.presentation.audit;

import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.infrastructure.persistence.DeadlineRevisionRepository;
import com.example.kihan.presentation.audit.response.RevisionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Audit", description = "감사 로그 API")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final DeadlineRevisionRepository deadlineRevisionRepository;

    @Operation(summary = "기한 변경 이력 조회", description = "특정 기한의 모든 변경 이력을 조회합니다.")
    @GetMapping("/deadlines/{id}/revisions")
    public ResponseEntity<List<RevisionResponse<Deadline>>> getDeadlineRevisions(
            @Parameter(description = "기한 ID") @PathVariable Long id
    ) {
        List<RevisionResponse<Deadline>> revisions = deadlineRevisionRepository.findRevisions(id)
                .getContent().stream()
                .map(RevisionResponse::from)
                .toList();
        return ResponseEntity.ok(revisions);
    }

    @Operation(summary = "기한 특정 리비전 조회", description = "특정 시점의 기한 상태를 조회합니다.")
    @GetMapping("/deadlines/{id}/revisions/{revisionNumber}")
    public ResponseEntity<RevisionResponse<Deadline>> getDeadlineRevision(
            @Parameter(description = "기한 ID") @PathVariable Long id,
            @Parameter(description = "리비전 번호") @PathVariable Long revisionNumber
    ) {
        return deadlineRevisionRepository.findRevision(id, revisionNumber)
                .map(RevisionResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
