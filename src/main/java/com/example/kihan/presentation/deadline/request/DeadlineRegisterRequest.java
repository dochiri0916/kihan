package com.example.kihan.presentation.deadline.request;

import com.example.kihan.domain.deadline.DeadlineType;
import com.example.kihan.domain.deadline.RecurrencePattern;
import com.example.kihan.domain.deadline.RecurrenceRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DeadlineRegisterRequest(
        @NotBlank String title,
        String description,
        @NotNull DeadlineType type,
        LocalDateTime dueDate,
        RecurrencePattern pattern,
        Integer interval,
        LocalDate startDate,
        LocalDate endDate
) {
    public RecurrenceRule toRecurrenceRule() {
        if (pattern == null) {
            return null;
        }
        return RecurrenceRule.of(pattern, interval, startDate, endDate);
    }
}
