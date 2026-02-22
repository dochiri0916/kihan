package com.dochiri.kihan.application.execution.scheduler;

import com.dochiri.kihan.application.execution.command.CreateExecutionService;
import com.dochiri.kihan.application.deadline.query.DeadlineQueryService;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionGenerationService {

    private final DeadlineQueryService deadlineQueryService;
    private final CreateExecutionService createExecutionService;
    private final Clock clock;

    @Transactional
    public void generateExecutionsForToday() {
        LocalDate today = LocalDate.now(clock);
        List<Deadline> deadlines = deadlineQueryService.findAllActive();

        for (Deadline deadline : deadlines) {
            try {
                if (shouldCreateExecution(deadline, today)) {
                    LocalDate scheduledDate = resolveScheduledDate(deadline, today);
                    createExecutionService.execute(deadline, scheduledDate)
                            .ifPresentOrElse(
                                    executionId -> log.debug(
                                            "Created execution {} for deadline {} on {}",
                                            executionId,
                                            deadline.getId(),
                                            today
                                    ),
                                    () -> log.debug(
                                            "Skip execution creation for deadline {} on {} (already exists)",
                                            deadline.getId(),
                                            today
                                    )
                            );
                }
            } catch (Exception exception) {
                log.error(
                        "Failed to create execution for deadline {} on {}: {}",
                        deadline.getId(),
                        today,
                        exception.getMessage(),
                        exception
                );
            }
        }
    }

    private boolean shouldCreateExecution(Deadline deadline, LocalDate date) {
        if (deadline.getDueDate() != null) {
            return shouldCreateOneTimeExecution(deadline, date);
        }
        return shouldCreateRecurringExecution(deadline, date);
    }

    private boolean shouldCreateOneTimeExecution(Deadline deadline, LocalDate date) {
        return deadline.getDueDate() != null
                && !deadline.getDueDate().isAfter(date);
    }

    private boolean shouldCreateRecurringExecution(Deadline deadline, LocalDate date) {
        RecurrenceRule rule = deadline.getRecurrenceRule();
        if (rule == null) {
            return false;
        }

        if (date.isBefore(rule.getStartDate())) {
            return false;
        }

        if (rule.getEndDate() != null && date.isAfter(rule.getEndDate())) {
            return false;
        }

        return rule.getPattern().matches(rule.getStartDate(), date);
    }

    private LocalDate resolveScheduledDate(Deadline deadline, LocalDate today) {
        if (deadline.getDueDate() != null) {
            return deadline.getDueDate();
        }
        return today;
    }

}
