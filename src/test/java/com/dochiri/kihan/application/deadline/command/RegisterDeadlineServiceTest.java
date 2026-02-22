package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.RegisterDeadlineCommand;
import com.dochiri.kihan.application.realtime.event.DeadlineChangedEvent;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import com.dochiri.kihan.domain.deadline.InvalidDeadlineRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterDeadlineService 테스트")
class RegisterDeadlineServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RegisterDeadlineService registerDeadlineService;

    @Test
    @DisplayName("유효한 명령이면 기한을 저장하고 ID를 반환한다")
    void shouldSaveAndReturnIdWhenCommandIsValid() {
        RegisterDeadlineCommand command = new RegisterDeadlineCommand(
                1L,
                "운동",
                LocalDate.of(2026, 2, 21),
                null
        );
        when(deadlineRepository.save(any(Deadline.class))).thenAnswer(invocation -> {
            Deadline deadline = invocation.getArgument(0);
            setId(deadline, 100L);
            return deadline;
        });

        Long id = registerDeadlineService.execute(command);

        assertEquals(100L, id);
        verify(deadlineRepository).save(any(Deadline.class));
        verify(eventPublisher).publishEvent(eq(new DeadlineChangedEvent(1L, "deadline.created", 100L)));
    }

    @Test
    @DisplayName("유효하지 않은 규칙이면 예외를 던지고 저장하지 않는다")
    void shouldThrowAndNotSaveWhenRuleIsInvalid() {
        RegisterDeadlineCommand command = new RegisterDeadlineCommand(
                1L,
                "운동",
                null,
                null
        );

        assertThrows(InvalidDeadlineRuleException.class, () -> registerDeadlineService.execute(command));

        verify(deadlineRepository, never()).save(any(Deadline.class));
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
