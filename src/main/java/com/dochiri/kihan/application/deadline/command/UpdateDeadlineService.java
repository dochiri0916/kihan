package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.dochiri.kihan.application.realtime.event.DeadlineChangedEvent;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateDeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void update(UpdateDeadlineCommand command) {
        Deadline deadline = deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(command.deadlineId(), command.userId());
        deadline.update(command.title());
        eventPublisher.publishEvent(new DeadlineChangedEvent(command.userId(), "deadline.updated", deadline.getId()));
    }

    @Transactional
    public void updateRecurrence(Long userId, Long deadlineId, RecurrenceRule recurrenceRule) {
        Deadline deadline = deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId);
        deadline.updateRecurrenceRule(recurrenceRule);
        eventPublisher.publishEvent(new DeadlineChangedEvent(userId, "deadline.updated", deadline.getId()));
    }

}
