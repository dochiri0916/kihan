package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.UpdateDeadlineCommand;
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

import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateDeadlineService 테스트")
class UpdateDeadlineServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @InjectMocks
    private UpdateDeadlineService updateDeadlineService;

    @Test
    @DisplayName("update 호출 시 기한의 제목과 설명을 변경한다")
    void shouldUpdateTitleAndDescription() {
        UpdateDeadlineCommand command = new UpdateDeadlineCommand(1L, 10L, "새 제목", "새 설명");
        Deadline deadline = Deadline.register(
                1L,
                "기존 제목",
                "기존 설명",
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 21, 9, 0),
                null
        );
        when(deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L)).thenReturn(deadline);

        updateDeadlineService.update(command);

        assertEquals("새 제목", deadline.getTitle());
        assertEquals("새 설명", deadline.getDescription());
        verify(deadlineRepository).findByIdAndUserIdAndDeletedAtIsNull(10L, 1L);
    }

    @Test
    @DisplayName("updateRecurrence 호출 시 반복 규칙을 변경한다")
    void shouldUpdateRecurrenceRule() {
        Deadline deadline = Deadline.register(
                1L,
                "제목",
                "설명",
                DeadlineType.RECURRING,
                null,
                RecurrenceRule.create(
                        RecurrencePattern.WEEKLY,
                        LocalDate.of(2026, 2, 1),
                        null
                )
        );
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
    }
}
