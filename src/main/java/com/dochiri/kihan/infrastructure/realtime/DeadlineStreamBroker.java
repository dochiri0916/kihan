package com.dochiri.kihan.infrastructure.realtime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DeadlineStreamBroker {

    private static final long SSE_TIMEOUT_MILLIS = 30L * 60L * 1000L;
    private static final int MAX_EVENT_BACKLOG = 200;

    private final AtomicLong eventSequence = new AtomicLong(0);
    private final Map<Long, SseEmitter> emittersByUserId = new ConcurrentHashMap<>();
    private final Map<Long, ConcurrentNavigableMap<Long, StreamEvent>> backlogsByUserId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        SseEmitter previous = emittersByUserId.put(userId, emitter);
        if (previous != null) {
            previous.complete();
        }

        emitter.onCompletion(() -> emittersByUserId.remove(userId, emitter));
        emitter.onTimeout(() -> emittersByUserId.remove(userId, emitter));
        emitter.onError(error -> emittersByUserId.remove(userId, emitter));

        sendEventToEmitter(userId, emitter, nextEventId(), "connected", Map.of("message", "connected"));
        replayOrResync(userId, emitter, lastEventId);
        return emitter;
    }

    public void publish(Long userId, String eventName, Object data) {
        long eventId = nextEventId();
        store(userId, eventId, eventName, data);

        SseEmitter emitter = emittersByUserId.get(userId);
        if (emitter != null) {
            sendEventToEmitter(userId, emitter, eventId, eventName, data);
        }
    }

    @Scheduled(fixedDelay = 25_000L)
    public void sendHeartbeat() {
        List<Map.Entry<Long, SseEmitter>> snapshot = new ArrayList<>(emittersByUserId.entrySet());
        for (Map.Entry<Long, SseEmitter> entry : snapshot) {
            sendEventToEmitter(entry.getKey(), entry.getValue(), nextEventId(), "keepalive", Map.of("ping", true));
        }
    }

    private void replayOrResync(Long userId, SseEmitter emitter, String lastEventId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return;
        }

        long parsedLastEventId;
        try {
            parsedLastEventId = Long.parseLong(lastEventId);
        } catch (NumberFormatException exception) {
            sendEventToEmitter(userId, emitter, nextEventId(), "resync_required", Map.of("reason", "invalid_last_event_id"));
            return;
        }

        NavigableMap<Long, StreamEvent> backlog = backlogsByUserId.get(userId);
        if (backlog == null || backlog.isEmpty()) {
            return;
        }

        Long firstEventId = backlog.firstKey();
        if (parsedLastEventId < firstEventId) {
            sendEventToEmitter(userId, emitter, nextEventId(), "resync_required", Map.of("reason", "backlog_expired"));
            return;
        }

        backlog.tailMap(parsedLastEventId, false).forEach((eventId, event) ->
                sendEventToEmitter(userId, emitter, eventId, event.eventName(), event.data())
        );
    }

    private void store(Long userId, long eventId, String eventName, Object data) {
        ConcurrentNavigableMap<Long, StreamEvent> backlog = backlogsByUserId.computeIfAbsent(
                userId,
                ignored -> new ConcurrentSkipListMap<>()
        );
        backlog.put(eventId, new StreamEvent(eventName, data));

        while (backlog.size() > MAX_EVENT_BACKLOG) {
            backlog.pollFirstEntry();
        }
    }

    private long nextEventId() {
        return eventSequence.incrementAndGet();
    }

    private void sendEventToEmitter(Long userId, SseEmitter emitter, long eventId, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(Long.toString(eventId))
                    .name(eventName)
                    .data(data));
        } catch (IOException exception) {
            emittersByUserId.remove(userId, emitter);
            emitter.completeWithError(exception);
        }
    }

    private record StreamEvent(String eventName, Object data) {
    }
}
