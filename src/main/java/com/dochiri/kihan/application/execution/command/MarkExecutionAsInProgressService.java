package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkExecutionAsInProgressService {

    private final ExecutionRepository executionRepository;

    @Transactional
    public void execute(Long userId, Long id) {
        Execution execution = executionRepository.findByIdAndDeletedAtIsNull(id);

        execution.getDeadline().verifyOwnership(userId);
        execution.markAsInProgress();
    }

}
