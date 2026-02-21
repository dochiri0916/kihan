package com.dochiri.kihan.application.realtime.event;

public record DeadlineChangedEvent(
        Long userId,
        String eventType,
        Long deadlineId
) {
}
