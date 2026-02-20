package com.example.kihan.domain.deadline;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurrenceRule {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrencePattern pattern;

    @Column(nullable = false, name = "recurrence_interval")
    private int interval;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    public static RecurrenceRule create(final RecurrencePattern pattern, final int interval, final LocalDate startDate, final LocalDate endDate) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.pattern = requireNonNull(pattern);
        rule.interval = interval;
        rule.startDate = requireNonNull(startDate);
        rule.endDate = endDate;
        rule.validate(interval, startDate, endDate);
        return rule;
    }

    private void validate(final int interval, final LocalDate startDate, final LocalDate endDate) {
        if (interval < 1) {
            throw InvalidDeadlineRuleException.intervalMustBePositive();
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw InvalidDeadlineRuleException.endDateAfterStart();
        }
    }

}