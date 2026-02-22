package com.dochiri.kihan.domain.deadline;

import java.time.LocalDate;
import java.time.YearMonth;

public enum RecurrencePattern {
    DAILY {
        @Override
        boolean matchesWithinCycle(LocalDate startDate, LocalDate date) {
            return true;
        }
    },
    WEEKLY {
        @Override
        boolean matchesWithinCycle(LocalDate startDate, LocalDate date) {
            return date.getDayOfWeek() == startDate.getDayOfWeek();
        }
    },
    MONTHLY {
        @Override
        boolean matchesWithinCycle(LocalDate startDate, LocalDate date) {
            int targetDay = Math.min(startDate.getDayOfMonth(), date.lengthOfMonth());
            return date.getDayOfMonth() == targetDay;
        }
    },
    YEARLY {
        @Override
        boolean matchesWithinCycle(LocalDate startDate, LocalDate date) {
            int targetDay = Math.min(
                    startDate.getDayOfMonth(),
                    YearMonth.of(date.getYear(), startDate.getMonth()).lengthOfMonth()
            );
            return date.getMonthValue() == startDate.getMonthValue()
                    && date.getDayOfMonth() == targetDay;
        }
    };

    public final boolean matches(LocalDate startDate, LocalDate date) {
        if (date.isBefore(startDate)) {
            return false;
        }
        return matchesWithinCycle(startDate, date);
    }

    abstract boolean matchesWithinCycle(LocalDate startDate, LocalDate date);
}
