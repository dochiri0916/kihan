package com.example.kihan.presentation.deadline;

import com.example.kihan.application.deadline.command.DeadlineRegisterService;
import com.example.kihan.application.deadline.command.DeadlineUpdateService;
import com.example.kihan.application.deadline.query.DeadlineQueryService;
import com.example.kihan.presentation.deadline.request.DeadlineRegisterRequest;
import com.example.kihan.presentation.deadline.request.DeadlineUpdateRequest;
import com.example.kihan.presentation.deadline.response.DeadlineResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineRegisterService deadlineRegisterService;
    private final DeadlineUpdateService deadlineUpdateService;
    private final DeadlineQueryService deadlineQueryService;

    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody DeadlineRegisterRequest request) {
        Long deadlineId = deadlineRegisterService.register(
                request.title(),
                request.description(),
                request.type(),
                request.dueDate(),
                request.toRecurrenceRule()
        );
        return ResponseEntity.created(URI.create("/api/deadlines/" + deadlineId)).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeadlineResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(DeadlineResponse.from(deadlineQueryService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<DeadlineResponse>> findAll() {
        List<DeadlineResponse> responses = deadlineQueryService.findAll().stream()
                .map(DeadlineResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody DeadlineUpdateRequest request) {
        if (request.title() != null) {
            deadlineUpdateService.changeTitle(id, request.title());
        }
        if (request.description() != null) {
            deadlineUpdateService.changeDescription(id, request.description());
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deadlineUpdateService.markAsCompleted(id);
        return ResponseEntity.noContent().build();
    }
}
