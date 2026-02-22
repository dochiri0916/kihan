package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.dochiri.kihan.application.realtime.event.DeadlineChangedEvent;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import com.dochiri.kihan.domain.deadline.RecurrencePattern;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import com.dochiri.kihan.domain.deadline.DeadlineType;
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
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateDeadlineService 테스트")
class UpdateDeadlineServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateDeadlineService updateDeadlineService;

    @Test
    @DisplayName("update 호출 시 기한의 제목을 변경한다")
    void shouldUpdateTitle() {
        UpdateDeadlineCommand command = new UpdateDeadlineCommand(1L, 10L, "새 제목");
        Deadline deadline = Deadline.register(
                1L,
                "기존 제목",
                LocalDate.of(2026, 2, 21),
                null
        );
        setId(deadline, 10L);
        when(deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L)).thenReturn(deadline);

        updateDeadlineService.update(command);

        assertEquals("새 제목", deadline.getTitle());
        verify(deadlineRepository).findByIdAndUserIdAndDeletedAtIsNull(10L, 1L);
        verify(eventPublisher).publishEvent(eq(new DeadlineChangedEvent(1L, "deadline.updated", 10L)));
    }

    @Test
    @DisplayName("updateRecurrence 호출 시 반복 규칙을 변경한다")
    void shouldUpdateRecurrenceRule() {
        Deadline deadline = Deadline.register(
                1L,
                "제목",
                null,
                RecurrenceRule.create(
                        RecurrencePattern.WEEKLY,
                        LocalDate.of(2026, 2, 1),
                        null
                )
        );
        setId(deadline, 10L);
        when(deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L)).thenReturn(deadline);

        RecurrenceRule newRule = RecurrenceRule.create(
                RecurrencePattern.MONTHLY,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 12, 31)
        );
        updateDeadlineService.updateRecurrence(1L, 10L, newRule);

        assertEquals(RecurrencePattern.MONTHLY, deadline.getRecurrenceRule().getPattern());
        assertEquals(LocalDate.of(2026, 3, 1), deadline.getRecurrenceRule().getStartDate());
        assertEquals(LocalDate.of(2026, 12, 31), deadline.getRecurrenceRule().getEndDate());
        verify(deadlineRepository).findByIdAndUserIdAndDeletedAtIsNull(10L, 1L);
        verify(eventPublisher).publishEvent(eq(new DeadlineChangedEvent(1L, "deadline.updated", 10L)));
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
