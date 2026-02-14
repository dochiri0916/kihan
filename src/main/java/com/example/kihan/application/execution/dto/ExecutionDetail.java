package com.example.kihan.application.execution.dto;

import com.example.kihan.domain.deadline.Execution;
import com.example.kihan.domain.deadline.ExecutionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExecutionDetail(
        Long id,
        Long deadlineId,
        LocalDate scheduledDate,
        ExecutionStatus status,
        LocalDateTime completedAt
) {
    public static ExecutionDetail from(final Execution execution) {
        return new ExecutionDetail(
                execution.getId(),
                execution.getDeadline().getId(),
                execution.getScheduledDate(),
                execution.getStatus(),
                execution.getCompletedAt()
        );
    }
}
