package com.dochiri.kihan.application.deadline.command;

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
@DisplayName("DeleteDeadlineService 테스트")
class DeleteDeadlineServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private DeleteDeadlineService deleteDeadlineService;

    @Test
    @DisplayName("요청자의 기한이면 Clock 기준 시각으로 삭제 처리한다")
    void shouldDeleteDeadlineWithClockTime() {
        Deadline deadline = Deadline.register(
                1L,
                "제목",
                "설명",
                DeadlineType.ONE_TIME,
                LocalDateTime.of(2026, 2, 21, 9, 0),
                null
        );
        when(deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L)).thenReturn(deadline);

        Instant fixed = Instant.parse("2026-02-21T11:00:00Z");
        when(clock.instant()).thenReturn(fixed);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        deleteDeadlineService.execute(1L, 10L);

        assertTrue(deadline.isDeleted());
        assertEquals(LocalDateTime.of(2026, 2, 21, 11, 0), deadline.getDeletedAt());
        verify(deadlineRepository).findByIdAndUserIdAndDeletedAtIsNull(10L, 1L);
    }

}