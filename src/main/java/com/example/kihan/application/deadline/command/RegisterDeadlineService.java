package com.example.kihan.application.deadline.command;

import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.domain.deadline.DeadlineType;
import com.example.kihan.domain.deadline.RecurrenceRule;
import com.example.kihan.infrastructure.persistence.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterDeadlineService {

    private final DeadlineRepository deadlineRepository;

    @Transactional
    public Long register(final Long userId, final String title, final String description, final DeadlineType type, final LocalDateTime dueDate, final RecurrenceRule recurrenceRule) {
        Deadline deadline = Deadline.register(userId, title, description, type, dueDate, recurrenceRule);
        return deadlineRepository.save(deadline).getId();
    }

}