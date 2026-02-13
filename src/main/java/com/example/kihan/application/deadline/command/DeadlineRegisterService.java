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
@Transactional
@RequiredArgsConstructor
public class DeadlineRegisterService {

    private final DeadlineRepository deadlineRepository;

    public Long register(Long userId, String title, String description, DeadlineType type, LocalDateTime dueDate, RecurrenceRule recurrenceRule) {
        Deadline deadline = Deadline.register(userId, title, description, type, dueDate, recurrenceRule);
        return deadlineRepository.save(deadline).getId();
    }
}