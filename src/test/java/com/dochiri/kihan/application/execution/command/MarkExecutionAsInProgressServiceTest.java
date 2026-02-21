package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.InvalidExecutionStatusTransitionException;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkExecutionAsInProgressService 테스트")
class MarkExecutionAsInProgressServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @InjectMocks
    private MarkExecutionAsInProgressService markExecutionAsInProgressService;

    @Test
    @DisplayName("소유자 요청이면 실행을 IN_PROGRESS로 변경한다")
    void shouldMarkExecutionAsInProgressWhenRequestedByOwner() {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                "description",
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 21, 9, 0),
                null
        );
        Execution execution = Execution.create(deadline, LocalDate.of(2026, 2, 21));
        execution.markAsPaused();
        when(executionRepository.findByIdAndDeletedAtIsNull(101L)).thenReturn(execution);

        markExecutionAsInProgressService.execute(1L, 101L);

        assertTrue(execution.isInProgress());
    }

    @Test
    @DisplayName("중지 상태가 아니면 재개할 수 없다")
    void shouldFailWhenResumingWithoutPausedStatus() {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                "description",
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 21, 9, 0),
                null
        );
        Execution execution = Execution.create(deadline, LocalDate.of(2026, 2, 21));
        when(executionRepository.findByIdAndDeletedAtIsNull(101L)).thenReturn(execution);

        assertThrows(InvalidExecutionStatusTransitionException.class,
                () -> markExecutionAsInProgressService.execute(1L, 101L));
    }
}
