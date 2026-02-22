package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.application.realtime.event.ExecutionChangedEvent;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MarkExecutionByDeadlineAsDoneService {

    private final DeadlineRepository deadlineRepository;
    private final ExecutionRepository executionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public void execute(Long userId, Long deadlineId) {
        Deadline deadline = deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId);
        LocalDate scheduledDate = resolveScheduledDate(deadline);

        Execution execution = executionRepository
                .findByDeadlineIdAndScheduledDateAndDeletedAtIsNull(deadlineId, scheduledDate)
                .orElseGet(() -> executionRepository.save(Execution.create(deadline, scheduledDate)));

        execution.markAsDone(LocalDateTime.now(clock));
        eventPublisher.publishEvent(new ExecutionChangedEvent(
                userId,
                execution.getId(),
                deadlineId,
                execution.getStatus()
        ));
    }

    private LocalDate resolveScheduledDate(Deadline deadline) {
        if (deadline.getDueDate() != null) {
            return deadline.getDueDate();
        }
        return LocalDate.now(clock);
    }
}
