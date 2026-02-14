package com.example.kihan.infrastructure.scheduler;

import com.example.kihan.application.execution.scheduler.ExecutionGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyExecutionScheduler {

    private final ExecutionGenerationService executionGenerationService;

    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyExecutions() {
        log.info("Starting daily execution generation");
        executionGenerationService.generateExecutionsForToday();
        log.info("Completed daily execution generation");
    }

}
