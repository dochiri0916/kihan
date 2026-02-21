package com.dochiri.kihan.application.deadline.dto;

import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;

import java.time.LocalDate;

public record RegisterDeadlineCommand(
        Long userId,
        String title,
        DeadlineType type,
        LocalDate dueDate,
        RecurrenceRule recurrenceRule
) {
}
