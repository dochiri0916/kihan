package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionNotFoundException;
import com.dochiri.kihan.infrastructure.persistence.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MarkExecutionAsDoneService {

    private final ExecutionRepository executionRepository;
    private final Clock clock;

    @Transactional
    public void execute(Long userId, Long executionId) {
        Execution execution = executionRepository.findByIdAndDeletedAtIsNull(executionId)
                .orElseThrow(() -> ExecutionNotFoundException.withId(executionId));

        execution.getDeadline().verifyOwnership(userId);
        execution.markAsDone(LocalDateTime.now(clock));
    }

}
