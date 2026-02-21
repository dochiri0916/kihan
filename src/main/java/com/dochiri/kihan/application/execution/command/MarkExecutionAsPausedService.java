package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.application.realtime.event.ExecutionChangedEvent;
import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionNotFoundException;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkExecutionAsPausedService {

    private final ExecutionRepository executionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(Long userId, Long id) {
        Execution execution = executionRepository.findByIdAndDeletedAtIsNull(id);

        if (execution.getDeadline().isDeleted()) {
            throw new ExecutionNotFoundException(id);
        }
        execution.getDeadline().verifyOwnership(userId);
        execution.markAsPaused();
        eventPublisher.publishEvent(new ExecutionChangedEvent(
                userId,
                execution.getId(),
                execution.getDeadline().getId(),
                execution.getStatus()
        ));
    }

}
