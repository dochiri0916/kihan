package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.application.realtime.event.ExecutionChangedEvent;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.InvalidExecutionStatusTransitionException;
import com.dochiri.kihan.domain.execution.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkExecutionAsPausedService 테스트")
class MarkExecutionAsPausedServiceTest {

    @Mock
    private ExecutionCommandSupport executionCommandSupport;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MarkExecutionAsPausedService markExecutionAsPausedService;

    @Test
    @DisplayName("소유자 요청이면 실행을 PAUSED로 변경한다")
    void shouldMarkExecutionAsPausedWhenRequestedByOwner() {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                LocalDate.of(2026, 2, 21),
                null
        );
        Execution execution = Execution.create(deadline, LocalDate.of(2026, 2, 21));
        when(executionCommandSupport.loadOwnedActiveExecution(1L, 101L)).thenReturn(execution);

        markExecutionAsPausedService.execute(1L, 101L);

        assertTrue(execution.isPaused());
        assertNull(execution.getCompletedAt());
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ExecutionChangedEvent event = assertInstanceOf(ExecutionChangedEvent.class, eventCaptor.getValue());
        assertEquals(1L, event.userId());
        assertTrue(Objects.equals(event.deadlineId(), deadline.getId()));
        assertEquals(ExecutionStatus.PAUSED, event.status());
    }

    @Test
    @DisplayName("이미 중지된 실행은 다시 중지할 수 없다")
    void shouldFailWhenPausingAlreadyPausedExecution() {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                LocalDate.of(2026, 2, 21),
                null
        );
        Execution execution = Execution.create(deadline, LocalDate.of(2026, 2, 21));
        execution.markAsPaused();
        when(executionCommandSupport.loadOwnedActiveExecution(1L, 101L)).thenReturn(execution);

        assertThrows(InvalidExecutionStatusTransitionException.class,
                () -> markExecutionAsPausedService.execute(1L, 101L));
    }
}
