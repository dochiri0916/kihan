package com.dochiri.kihan.application.execution.scheduler;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OverdueExecutionCompletionService 테스트")
class OverdueExecutionCompletionServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private OverdueExecutionCompletionService overdueExecutionCompletionService;

    @Test
    @DisplayName("마감 시각이 지난 ONE_TIME 실행을 완료 처리한다")
    void shouldCompleteOverdueOneTimeExecutions() {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                LocalDate.of(2026, 2, 21),
                null
        );
        Execution execution = Execution.create(deadline, LocalDate.of(2026, 2, 21));
        LocalDate today = LocalDate.of(2026, 2, 21);
        LocalDateTime now = LocalDateTime.of(2026, 2, 21, 10, 0);

        when(clock.instant()).thenReturn(Instant.parse("2026-02-21T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(executionRepository.findOverdueOneTimeAndNotDone(today)).thenReturn(List.of(execution));

        int completedCount = overdueExecutionCompletionService.completeOverdueOneTimeExecutions();

        assertEquals(1, completedCount);
        assertTrue(execution.isDone());
        assertEquals(now, execution.getCompletedAt());
    }
}
