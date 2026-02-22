package com.dochiri.kihan.infrastructure.scheduler;

import com.dochiri.kihan.application.execution.scheduler.ExecutionGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyExecutionScheduler {

    private final ExecutionGenerationService executionGenerationService;

    @Scheduled(cron = "0 * * * * *")
    public void generateDailyExecutions() {
        log.debug("Starting execution generation tick");
        executionGenerationService.generateExecutionsForToday();
        log.debug("Completed execution generation tick");
    }

}
