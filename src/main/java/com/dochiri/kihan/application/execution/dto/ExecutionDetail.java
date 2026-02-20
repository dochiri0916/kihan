package com.dochiri.kihan.application.execution.dto;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExecutionDetail(
        Long id,
        Long deadlineId,
        LocalDate scheduledDate,
        ExecutionStatus status,
        LocalDateTime completedAt
) {
    public static ExecutionDetail from(Execution execution) {
        return new ExecutionDetail(
                execution.getId(),
                execution.getDeadline().getId(),
                execution.getScheduledDate(),
                execution.getStatus(),
                execution.getCompletedAt()
        );
    }
}
