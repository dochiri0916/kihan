package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkExecutionAsDoneService 테스트")
class MarkExecutionAsDoneServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private MarkExecutionAsDoneService markExecutionAsDoneService;

    @Test
    @DisplayName("소유자 요청이면 실행을 DONE으로 변경한다")
    void shouldMarkExecutionAsDoneWhenRequestedByOwner() {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                DeadlineType.ONE_TIME,
                LocalDate.of(2026, 2, 21),
                null
        );
        Execution execution = Execution.create(deadline, LocalDate.of(2026, 2, 21));
        when(executionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(execution);

        Instant fixed = Instant.parse("2026-02-21T10:00:00Z");
        when(clock.instant()).thenReturn(fixed);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        markExecutionAsDoneService.execute(1L, 100L);

        assertTrue(execution.isDone());
        assertEquals(LocalDateTime.of(2026, 2, 21, 10, 0), execution.getCompletedAt());
        verify(executionRepository).findByIdAndDeletedAtIsNull(100L);
    }
}
