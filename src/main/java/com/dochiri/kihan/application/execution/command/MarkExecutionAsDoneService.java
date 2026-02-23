package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.application.execution.dto.ExecutionStatusChangedResult;
import com.dochiri.kihan.application.realtime.event.ExecutionChangedEvent;
import com.dochiri.kihan.domain.execution.Execution;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MarkExecutionAsDoneService {

    private final ExecutionCommandSupport executionCommandSupport;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ExecutionStatusChangedResult execute(Long userId, Long id) {
        Execution execution = executionCommandSupport.loadOwnedActiveExecution(userId, id);
        execution.markAsDone(LocalDateTime.now(clock));
        eventPublisher.publishEvent(new ExecutionChangedEvent(
                userId,
                execution.getId(),
                execution.getDeadline().getId(),
                execution.getStatus()
        ));

        return ExecutionStatusChangedResult.from(execution);
    }

}