package com.dochiri.kihan.application.realtime.event;

import com.dochiri.kihan.domain.execution.ExecutionStatus;

public record ExecutionChangedEvent(
        Long userId,
        Long executionId,
        Long deadlineId,
        ExecutionStatus status
) {
}
