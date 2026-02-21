package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.RegisterDeadlineCommand;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterDeadlineService {

    private final DeadlineRepository deadlineRepository;

    @Transactional
    public Long execute(RegisterDeadlineCommand command) {
        Deadline deadline = Deadline.register(
                command.userId(),
                command.title(),
                command.type(),
                command.dueDate(),
                command.recurrenceRule()
        );

        return deadlineRepository.save(deadline).getId();
    }

}
