package com.dochiri.kihan.infrastructure.scheduler;

import com.dochiri.kihan.application.execution.scheduler.ExecutionGenerationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyExecutionScheduler 테스트")
class DailyExecutionSchedulerTest {

    @Mock
    private ExecutionGenerationService executionGenerationService;

    @InjectMocks
    private DailyExecutionScheduler dailyExecutionScheduler;

    @Test
    @DisplayName("스케줄 실행 시 일일 실행 생성 서비스를 호출한다")
    void shouldInvokeExecutionGenerationService() {
        dailyExecutionScheduler.generateDailyExecutions();

        verify(executionGenerationService).generateExecutionsForToday();
    }
}
