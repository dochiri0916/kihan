package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionNotFoundException;
import com.dochiri.kihan.infrastructure.persistence.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkExecutionAsDelayedService {

    private final ExecutionRepository executionRepository;

    @Transactional
    public void execute(Long userId, Long executionId) {
        Execution execution = executionRepository.findByIdAndDeletedAtIsNull(executionId)
                .orElseThrow(() -> ExecutionNotFoundException.withId(executionId));

        execution.getDeadline().verifyOwnership(userId);
        execution.markAsDelayed();
    }

}
