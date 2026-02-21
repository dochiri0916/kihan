package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateDeadlineService 테스트")
class UpdateDeadlineServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @Mock
    private Clock clock;

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
    @DisplayName("markAsCompleted 호출 시 Clock 기준 시각으로 완료 처리한다")
    void shouldMarkAsCompletedWithClockTime() {
        Deadline deadline = Deadline.register(
                1L,
                "제목",
                "설명",
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 21, 9, 0),
                null
        );
        when(deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L)).thenReturn(deadline);

        Instant fixed = Instant.parse("2026-02-21T10:00:00Z");
        when(clock.instant()).thenReturn(fixed);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        updateDeadlineService.markAsCompleted(1L, 10L);

        assertTrue(deadline.isDeleted());
        assertEquals(LocalDateTime.of(2026, 2, 21, 10, 0), deadline.getDeletedAt());
        verify(deadlineRepository).findByIdAndUserIdAndDeletedAtIsNull(10L, 1L);
    }
}
