package com.example.pragmatic.application.deadline.command;

import com.example.pragmatic.domain.deadline.Deadline;
import com.example.pragmatic.domain.deadline.DeadlineType;
import com.example.pragmatic.domain.deadline.RecurrenceRule;
import com.example.pragmatic.infrastructure.persistence.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class DeadlineRegisterService {

    private final DeadlineRepository deadlineRepository;

    public Long register(String title, String description, DeadlineType type, LocalDateTime dueDate, RecurrenceRule recurrenceRule) {
        Deadline deadline = Deadline.register(title, description, type, dueDate, recurrenceRule);
        return deadlineRepository.save(deadline).getId();
    }
}
