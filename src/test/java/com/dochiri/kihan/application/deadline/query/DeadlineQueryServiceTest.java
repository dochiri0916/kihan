package com.dochiri.kihan.application.deadline.query;

import com.dochiri.kihan.application.deadline.dto.DeadlineDetail;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeadlineQueryService 테스트")
class DeadlineQueryServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @InjectMocks
    private DeadlineQueryService deadlineQueryService;

    @Test
    @DisplayName("ID로 조회 시 DeadlineDetail로 매핑해 반환한다")
    void shouldReturnDeadlineDetailWhenGetById() {
        Deadline deadline = oneTimeDeadline(1L, "운동");
        when(deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(10L, 1L)).thenReturn(deadline);

        DeadlineDetail result = deadlineQueryService.getById(1L, 10L);

        assertEquals(1L, result.id());
        assertEquals("운동", result.title());
        verify(deadlineRepository).findByIdAndUserIdAndDeletedAtIsNull(10L, 1L);
    }

    @Test
    @DisplayName("사용자 기한 목록 조회 시 모두 DeadlineDetail로 매핑한다")
    void shouldReturnMappedDetailsWhenGetAllByUserId() {
        Deadline first = oneTimeDeadline(1L, "운동");
        Deadline second = oneTimeDeadline(2L, "독서");
        when(deadlineRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of(first, second));

        List<DeadlineDetail> result = deadlineQueryService.getAllByUserId(1L);

        assertEquals(2, result.size());
        assertEquals("운동", result.get(0).title());
        assertEquals("독서", result.get(1).title());
        verify(deadlineRepository).findByUserIdAndDeletedAtIsNull(1L);
    }

    @Test
    @DisplayName("활성 기한 조회는 저장소 결과를 그대로 반환한다")
    void shouldReturnAllActiveDeadlines() {
        Deadline first = oneTimeDeadline(1L, "운동");
        Deadline second = oneTimeDeadline(2L, "독서");
        when(deadlineRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(first, second));

        List<Deadline> result = deadlineQueryService.findAllActive();

        assertEquals(2, result.size());
        assertEquals("운동", result.get(0).getTitle());
        assertEquals("독서", result.get(1).getTitle());
        verify(deadlineRepository).findAllByDeletedAtIsNull();
    }

    private Deadline oneTimeDeadline(Long id, String title) {
        Deadline deadline = Deadline.register(
                1L,
                title,
                DeadlineType.ONE_TIME,
                LocalDate.of(2026, 2, 21),
                null
        );
        setId(deadline, id);
        return deadline;
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
