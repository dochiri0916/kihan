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

    @Scheduled(cron = "${scheduler.overdue-completion.cron:0 * * * * *}")
    public void completeOverdueExecutions() {
        long startedAt = System.nanoTime();
        int completedCount = overdueExecutionCompletionService.completeOverdueOneTimeExecutions();
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        log.debug("Overdue completion tick completedCount={}, elapsedMs={}", completedCount, elapsedMillis);
    }
}
