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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateExecutionService 테스트")
class CreateExecutionServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @InjectMocks
    private CreateExecutionService createExecutionService;

    @Test
    @DisplayName("같은 날짜 실행이 없으면 새 실행을 저장하고 ID를 반환한다")
    void shouldCreateAndReturnIdWhenNoExecutionExists() {
        Deadline deadline = deadlineWithId(10L);
        LocalDate date = LocalDate.of(2026, 2, 21);
        when(executionRepository.existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(10L, date)).thenReturn(false);
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> {
            Execution execution = invocation.getArgument(0);
            setId(execution, 99L);
            return execution;
        });

        Optional<Long> result = createExecutionService.execute(deadline, date);

        assertTrue(result.isPresent());
        assertEquals(99L, result.get());
    }

    @Test
    @DisplayName("같은 날짜 실행이 이미 있으면 저장하지 않고 empty를 반환한다")
    void shouldReturnEmptyWhenExecutionAlreadyExists() {
        Deadline deadline = deadlineWithId(10L);
        LocalDate date = LocalDate.of(2026, 2, 21);
        when(executionRepository.existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(10L, date)).thenReturn(true);

        Optional<Long> result = createExecutionService.execute(deadline, date);

        assertTrue(result.isEmpty());
        verify(executionRepository, never()).save(any(Execution.class));
    }

    private Deadline deadlineWithId(Long id) {
        Deadline deadline = Deadline.register(
                1L,
                "title",
                DeadlineType.ONE_TIME,
                LocalDate.of(2026, 2, 21),
                null
        );
        setId(deadline, id);
        return deadline;
    }

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
