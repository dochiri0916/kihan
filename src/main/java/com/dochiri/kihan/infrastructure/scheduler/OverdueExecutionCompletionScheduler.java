package com.dochiri.kihan.infrastructure.scheduler;

import com.dochiri.kihan.application.execution.scheduler.OverdueExecutionCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueExecutionCompletionScheduler {

    private final OverdueExecutionCompletionService overdueExecutionCompletionService;

    @Scheduled(cron = "0 * * * * *")
    public void completeOverdueExecutions() {
        int completedCount = overdueExecutionCompletionService.completeOverdueOneTimeExecutions();
        if (completedCount > 0) {
            log.info("Completed overdue executions count={}", completedCount);
        }
    }
}
