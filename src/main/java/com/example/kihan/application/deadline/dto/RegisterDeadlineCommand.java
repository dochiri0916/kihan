package com.example.kihan.application.deadline.dto;

import com.example.kihan.domain.deadline.DeadlineType;
import com.example.kihan.domain.deadline.RecurrenceRule;

import java.time.LocalDateTime;

public record RegisterDeadlineCommand(
        Long userId,
        String title,
        String description,
        DeadlineType type,
        LocalDateTime dueDate,
        RecurrenceRule recurrenceRule
) {
}