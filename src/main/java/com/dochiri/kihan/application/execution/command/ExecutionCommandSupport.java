package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExecutionCommandSupport {

    private final ExecutionRepository executionRepository;

    public Execution loadOwnedActiveExecution(Long userId, Long executionId) {
        Execution execution = executionRepository.findByIdAndDeadlineActiveAndDeletedAtIsNull(executionId);
        execution.getDeadline().verifyOwnership(userId);
        return execution;
    }
}
