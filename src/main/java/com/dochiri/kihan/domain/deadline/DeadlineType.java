package com.dochiri.kihan.domain.deadline;

public enum DeadlineType {
    ONE_TIME,
    RECURRING;

    public boolean isRecurring() {
        return this == RECURRING;
    }

    public boolean isSingle() {
        return this != RECURRING;
    }
}