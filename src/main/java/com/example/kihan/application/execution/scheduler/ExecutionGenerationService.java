package com.example.kihan.application.execution.scheduler;

import com.example.kihan.application.execution.command.CreateExecutionService;
import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.domain.deadline.DeadlineType;
import com.example.kihan.domain.deadline.RecurrencePattern;
import com.example.kihan.domain.deadline.RecurrenceRule;
import com.example.kihan.infrastructure.persistence.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionGenerationService {

    private final DeadlineRepository deadlineRepository;
    private final CreateExecutionService createExecutionService;

    @Transactional
    public void generateExecutionsForToday() {
        LocalDate today = LocalDate.now();
        List<Deadline> deadlines = deadlineRepository.findAllByDeletedAtIsNull();

        for (Deadline deadline : deadlines) {
            if (shouldCreateExecution(deadline, today)) {
                createExecutionService.execute(deadline, today);
                log.debug("Created execution for deadline {} on {}", deadline.getId(), today);
            }
        }
    }

    private boolean shouldCreateExecution(final Deadline deadline, final LocalDate date) {
        if (deadline.getType() == DeadlineType.ONE_TIME) {
            return shouldCreateOneTimeExecution(deadline, date);
        }
        return shouldCreateRecurringExecution(deadline, date);
    }

    private boolean shouldCreateOneTimeExecution(final Deadline deadline, final LocalDate date) {
        return deadline.getDueDate() != null
                && deadline.getDueDate().toLocalDate().equals(date);
    }

    private boolean shouldCreateRecurringExecution(final Deadline deadline, final LocalDate date) {
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

    private boolean shouldCreateDailyExecution(final RecurrenceRule rule, final LocalDate date) {
        long daysBetween = ChronoUnit.DAYS.between(rule.getStartDate(), date);
        return daysBetween >= 0 && daysBetween % rule.getInterval() == 0;
    }

    private boolean shouldCreateWeeklyExecution(final RecurrenceRule rule, final LocalDate date) {
        long weeksBetween = ChronoUnit.WEEKS.between(rule.getStartDate(), date);
        return weeksBetween >= 0
                && weeksBetween % rule.getInterval() == 0
                && date.getDayOfWeek() == rule.getStartDate().getDayOfWeek();
    }

    private boolean shouldCreateMonthlyExecution(final RecurrenceRule rule, final LocalDate date) {
        long monthsBetween = ChronoUnit.MONTHS.between(rule.getStartDate(), date);
        return monthsBetween >= 0
                && monthsBetween % rule.getInterval() == 0
                && date.getDayOfMonth() == rule.getStartDate().getDayOfMonth();
    }

    private boolean shouldCreateYearlyExecution(final RecurrenceRule rule, final LocalDate date) {
        long yearsBetween = ChronoUnit.YEARS.between(rule.getStartDate(), date);
        return yearsBetween >= 0
                && yearsBetween % rule.getInterval() == 0
                && date.getMonthValue() == rule.getStartDate().getMonthValue()
                && date.getDayOfMonth() == rule.getStartDate().getDayOfMonth();
    }

}
