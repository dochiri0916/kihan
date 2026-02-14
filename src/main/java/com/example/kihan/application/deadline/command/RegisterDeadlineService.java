package com.example.kihan.application.deadline.command;

import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.infrastructure.persistence.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterDeadlineService {

    private final DeadlineRepository deadlineRepository;

    @Transactional
    public Long register(final RegisterDeadlineCommand command) {
        Deadline deadline = Deadline.register(
                command.userId(),
                command.title(),
                command.description(),
                command.type(),
                command.dueDate(),
                command.recurrenceRule()
        );
        return deadlineRepository.save(deadline).getId();
    }

}