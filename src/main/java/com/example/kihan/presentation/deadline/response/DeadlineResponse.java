package com.example.kihan.presentation.deadline.response;

import com.example.kihan.application.deadline.dto.DeadlineDetail;
import com.example.kihan.domain.deadline.DeadlineType;
import com.example.kihan.domain.deadline.RecurrenceRule;

import java.time.LocalDateTime;

public record DeadlineResponse(
        Long id,
        String title,
        String description,
        DeadlineType type,
        LocalDateTime dueDate,
        RecurrenceRule recurrenceRule,
        LocalDateTime createdAt
) {
    public static DeadlineResponse from(DeadlineDetail detail) {
        return new DeadlineResponse(
                detail.id(),
                detail.title(),
                detail.description(),
                detail.type(),
                detail.dueDate(),
                detail.recurrenceRule(),
                detail.createdAt()
        );
    }
}
