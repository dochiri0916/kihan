package com.dochiri.kihan.application.execution.dto;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExecutionStatusChangedResult(
        Long executionId,
        Long deadlineId,
        ExecutionStatus status,
        LocalDate scheduledDate,
        LocalDateTime completedAt
) {
    public static ExecutionStatusChangedResult from(Execution execution) {
        return new ExecutionStatusChangedResult(
                execution.getId(),
                execution.getDeadline().getId(),
                execution.getStatus(),
                execution.getScheduledDate(),
                execution.getCompletedAt()
        );
    }
}