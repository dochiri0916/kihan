package com.dochiri.kihan.application.deadline.dto;

import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;

import java.time.LocalDateTime;

public record DeadlineDetail(
        Long id,
        String title,
        String description,
        DeadlineType type,
        LocalDateTime dueDate,
        RecurrenceRule recurrenceRule,
        LocalDateTime createdAt
) {
    public static DeadlineDetail from(Deadline deadline) {
        return new DeadlineDetail(
                deadline.getId(),
                deadline.getTitle(),
                deadline.getDescription(),
                deadline.getType(),
                deadline.getDueDate(),
                deadline.getRecurrenceRule(),
                deadline.getCreatedAt()
        );
    }
}
