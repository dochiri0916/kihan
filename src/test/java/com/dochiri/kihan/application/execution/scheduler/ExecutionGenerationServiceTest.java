package com.dochiri.kihan.application.execution.scheduler;

import com.dochiri.kihan.application.deadline.query.DeadlineQueryService;
import com.dochiri.kihan.application.execution.command.CreateExecutionService;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrencePattern;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutionGenerationService 테스트")
class ExecutionGenerationServiceTest {

    @Mock
    private DeadlineQueryService deadlineQueryService;

    @Mock
    private CreateExecutionService createExecutionService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ExecutionGenerationService executionGenerationService;

    @Test
    @DisplayName("단건 마감일이 오늘보다 이전이어도 실행을 보정 생성한다")
    void shouldCreateSingleExecutionWhenDueDateAlreadyPassed() {
        Deadline deadline = Deadline.register(
                1L,
                "지연 단건",
                DeadlineType.ONE_TIME,
                LocalDate.of(2026, 2, 20),
                null
        );
        setId(deadline, 10L);
        when(deadlineQueryService.findAllActive()).thenReturn(List.of(deadline));
        when(clock.instant()).thenReturn(Instant.parse("2026-02-22T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(createExecutionService.execute(deadline, LocalDate.of(2026, 2, 20))).thenReturn(Optional.of(100L));

        executionGenerationService.generateExecutionsForToday();

        verify(createExecutionService).execute(deadline, LocalDate.of(2026, 2, 20));
    }

    @Test
    @DisplayName("반복 마감은 오늘 날짜 기준으로 실행을 생성한다")
    void shouldCreateRecurringExecutionForToday() {
        Deadline deadline = Deadline.register(
                1L,
                "주간 반복",
                DeadlineType.RECURRING,
                null,
                RecurrenceRule.create(RecurrencePattern.DAILY, LocalDate.of(2026, 2, 1), null)
        );
        setId(deadline, 11L);
        when(deadlineQueryService.findAllActive()).thenReturn(List.of(deadline));
        when(clock.instant()).thenReturn(Instant.parse("2026-02-22T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(createExecutionService.execute(deadline, LocalDate.of(2026, 2, 22))).thenReturn(Optional.of(101L));

        executionGenerationService.generateExecutionsForToday();

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(createExecutionService).execute(org.mockito.ArgumentMatchers.eq(deadline), dateCaptor.capture());
        assertEquals(LocalDate.of(2026, 2, 22), dateCaptor.getValue());
    }

    private void setId(Deadline deadline, Long id) {
        try {
            Field idField = deadline.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(deadline, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
