package com.dochiri.kihan.infrastructure.realtime;

import com.dochiri.kihan.application.realtime.event.DeadlineChangedEvent;
import com.dochiri.kihan.application.realtime.event.ExecutionChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DeadlineStreamEventListener {

    private final DeadlineStreamBroker deadlineStreamBroker;
    private final Clock clock;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeadlineChanged(DeadlineChangedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("deadlineId", event.deadlineId());
        payload.put("updatedAt", LocalDateTime.now(clock));
        payload.put("version", 1);

        deadlineStreamBroker.publish(event.userId(), event.eventType(), payload);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleExecutionChanged(ExecutionChangedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("executionId", event.executionId());
        payload.put("deadlineId", event.deadlineId());
        payload.put("status", event.status());
        payload.put("updatedAt", LocalDateTime.now(clock));
        payload.put("version", 1);

        deadlineStreamBroker.publish(event.userId(), "execution.updated", payload);
    }

}