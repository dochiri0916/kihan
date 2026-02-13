package com.example.pragmatic.domain.deadline;

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
    private RecurrencePattern pattern;

    private int interval;

    private LocalDate startDate;

    private LocalDate endDate;

    private RecurrenceRule(final RecurrencePattern pattern, final int interval, final LocalDate startDate, final LocalDate endDate) {
        this.pattern = requireNonNull(pattern);
        this.interval = interval;
        this.startDate = requireNonNull(startDate);
        this.endDate = endDate;
        validate(interval, startDate, endDate);
    }

    public static RecurrenceRule of(final RecurrencePattern pattern, final int interval, final LocalDate startDate, final LocalDate endDate) {
        return new RecurrenceRule(pattern, interval, startDate, endDate);
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
