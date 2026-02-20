package com.dochiri.kihan.application.execution.scheduler;

import com.dochiri.kihan.application.execution.command.CreateExecutionService;
import com.dochiri.kihan.application.deadline.query.DeadlineQueryService;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionGenerationService {

    private final DeadlineQueryService deadlineQueryService;
    private final CreateExecutionService createExecutionService;

    @Transactional
    public void generateExecutionsForToday() {
        LocalDate today = LocalDate.now();
        List<Deadline> deadlines = deadlineQueryService.findAllActive();

        for (Deadline deadline : deadlines) {
            if (shouldCreateExecution(deadline, today)) {
                createExecutionService.execute(deadline, today)
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
        }
    }

    private boolean shouldCreateExecution(Deadline deadline, LocalDate date) {
        if (deadline.getType() == DeadlineType.ONE_TIME) {
            return shouldCreateOneTimeExecution(deadline, date);
        }
        return shouldCreateRecurringExecution(deadline, date);
    }

    private boolean shouldCreateOneTimeExecution(Deadline deadline, LocalDate date) {
        return deadline.getDueDate() != null
                && deadline.getDueDate().toLocalDate().equals(date);
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

        return switch (rule.getPattern()) {
            case DAILY -> shouldCreateDailyExecution(rule, date);
            case WEEKLY -> shouldCreateWeeklyExecution(rule, date);
            case MONTHLY -> shouldCreateMonthlyExecution(rule, date);
            case YEARLY -> shouldCreateYearlyExecution(rule, date);
        };
    }

    private boolean shouldCreateDailyExecution(RecurrenceRule rule, LocalDate date) {
        long daysBetween = ChronoUnit.DAYS.between(rule.getStartDate(), date);
        return daysBetween >= 0 && daysBetween % rule.getInterval() == 0;
    }

    private boolean shouldCreateWeeklyExecution(RecurrenceRule rule, LocalDate date) {
        long weeksBetween = ChronoUnit.WEEKS.between(rule.getStartDate(), date);
        return weeksBetween >= 0
                && weeksBetween % rule.getInterval() == 0
                && date.getDayOfWeek() == rule.getStartDate().getDayOfWeek();
    }

    private boolean shouldCreateMonthlyExecution(RecurrenceRule rule, LocalDate date) {
        long monthsBetween = ChronoUnit.MONTHS.between(
                YearMonth.from(rule.getStartDate()),
                YearMonth.from(date)
        );
        int targetDay = Math.min(
                rule.getStartDate().getDayOfMonth(),
                date.lengthOfMonth()
        );

        return monthsBetween >= 0
                && monthsBetween % rule.getInterval() == 0
                && date.getDayOfMonth() == targetDay;
    }

    private boolean shouldCreateYearlyExecution(RecurrenceRule rule, LocalDate date) {
        long yearsBetween = ChronoUnit.YEARS.between(rule.getStartDate(), date);
        return yearsBetween >= 0
                && yearsBetween % rule.getInterval() == 0
                && date.getMonthValue() == rule.getStartDate().getMonthValue()
                && date.getDayOfMonth() == rule.getStartDate().getDayOfMonth();
    }

}