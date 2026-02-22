package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.RegisterDeadlineCommand;
import com.dochiri.kihan.application.realtime.event.DeadlineChangedEvent;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterDeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long execute(RegisterDeadlineCommand command) {
        Deadline deadline = Deadline.register(
                command.userId(),
                command.title(),
                command.dueDate(),
                command.recurrenceRule()
        );

        Deadline savedDeadline = deadlineRepository.save(deadline);
        eventPublisher.publishEvent(new DeadlineChangedEvent(command.userId(), "deadline.created", savedDeadline.getId()));
        return savedDeadline.getId();
    }

}
