package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.application.realtime.event.ExecutionChangedEvent;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import com.dochiri.kihan.domain.execution.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkExecutionByDeadlineAsDoneService 테스트")
class MarkExecutionByDeadlineAsDoneServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Clock clock;

    @InjectMocks
    private MarkExecutionByDeadlineAsDoneService service;

    @Test
    @DisplayName("실행이 없으면 생성 후 완료 처리한다")
    void shouldCreateAndMarkDoneWhenExecutionDoesNotExist() {
        Deadline deadline = Deadline.register(1L, "단건", DeadlineType.ONE_TIME, LocalDate.of(2026, 2, 20), null);
        setId(deadline, 10L);
        Execution created = Execution.create(deadline, LocalDate.of(2026, 2, 20));
        setId(created, 100L);

        when(deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L)).thenReturn(deadline);
        when(executionRepository.findByDeadlineIdAndScheduledDateAndDeletedAtIsNull(10L, LocalDate.of(2026, 2, 20)))
                .thenReturn(Optional.empty());
        when(executionRepository.save(any(Execution.class))).thenReturn(created);
        when(clock.instant()).thenReturn(Instant.parse("2026-02-22T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        service.execute(1L, 10L);

        assertTrue(created.isDone());
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ExecutionChangedEvent event = (ExecutionChangedEvent) eventCaptor.getValue();
        assertEquals(ExecutionStatus.DONE, event.status());
        assertEquals(10L, event.deadlineId());
    }

    @Test
    @DisplayName("기존 실행이 있으면 생성 없이 완료 처리한다")
    void shouldMarkExistingExecutionDone() {
        Deadline deadline = Deadline.register(1L, "단건", DeadlineType.ONE_TIME, LocalDate.of(2026, 2, 20), null);
        setId(deadline, 10L);
        Execution existing = Execution.create(deadline, LocalDate.of(2026, 2, 20));
        setId(existing, 101L);

        when(deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L)).thenReturn(deadline);
        when(executionRepository.findByDeadlineIdAndScheduledDateAndDeletedAtIsNull(10L, LocalDate.of(2026, 2, 20)))
                .thenReturn(Optional.of(existing));
        when(clock.instant()).thenReturn(Instant.parse("2026-02-22T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        service.execute(1L, 10L);

        assertTrue(existing.isDone());
    }

    private void setId(Object target, Long id) {
        try {
            Field idField = target.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(target, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
