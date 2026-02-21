package com.dochiri.kihan.infrastructure.scheduler;

import com.dochiri.kihan.application.execution.scheduler.OverdueExecutionCompletionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OverdueExecutionCompletionScheduler 테스트")
class OverdueExecutionCompletionSchedulerTest {

    @Mock
    private OverdueExecutionCompletionService overdueExecutionCompletionService;

    @InjectMocks
    private OverdueExecutionCompletionScheduler overdueExecutionCompletionScheduler;

    @Test
    @DisplayName("스케줄 실행 시 마감 경과 실행 완료 서비스를 호출한다")
    void shouldInvokeOverdueExecutionCompletionService() {
        when(overdueExecutionCompletionService.completeOverdueOneTimeExecutions()).thenReturn(1);

        overdueExecutionCompletionScheduler.completeOverdueExecutions();

        verify(overdueExecutionCompletionService).completeOverdueOneTimeExecutions();
    }
}
