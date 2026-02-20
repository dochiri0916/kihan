package com.dochiri.kihan.application.deadline.dto;

import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;

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