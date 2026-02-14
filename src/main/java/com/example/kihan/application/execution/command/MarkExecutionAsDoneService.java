package com.example.kihan.application.execution.command;

import com.example.kihan.domain.deadline.Execution;
import com.example.kihan.domain.deadline.ExecutionNotFoundException;
import com.example.kihan.infrastructure.persistence.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkExecutionAsDoneService {

    private final ExecutionRepository executionRepository;

    @Transactional
    public void execute(final Long executionId) {
        Execution execution = executionRepository.findByIdAndDeletedAtIsNull(executionId)
                .orElseThrow(() -> ExecutionNotFoundException.withId(executionId));

        execution.markAsDone();
    }

}
