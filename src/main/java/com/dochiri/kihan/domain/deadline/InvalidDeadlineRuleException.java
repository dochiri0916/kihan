package com.dochiri.kihan.domain.deadline;

public class InvalidDeadlineRuleException extends DeadlineException {
    private static final String ONE_TIME_DUE_DATE_REQUIRED = "ONE_TIME 마감은 dueDate가 필수입니다.";
    private static final String DUE_DATE_MUST_BE_FUTURE = "마감일은 현재 시각 이후여야 합니다.";
    private static final String ONE_TIME_NO_RECURRENCE = "ONE_TIME 마감은 recurrenceRule을 가질 수 없습니다.";
    private static final String RECURRING_RULE_REQUIRED = "RECURRING 마감은 recurrenceRule이 필수입니다.";
    private static final String RECURRING_NO_DUE_DATE = "RECURRING 마감은 dueDate를 가질 수 없습니다.";
    private static final String END_DATE_AFTER_START = "endDate는 startDate 이후여야 합니다.";

    private InvalidDeadlineRuleException(String message) {
        super(message);
    }

    public static InvalidDeadlineRuleException oneTimeDueDateRequired() {
        return new InvalidDeadlineRuleException(ONE_TIME_DUE_DATE_REQUIRED);
    }

    public static InvalidDeadlineRuleException dueDateMustBeFuture() {
        return new InvalidDeadlineRuleException(DUE_DATE_MUST_BE_FUTURE);
    }

    public static InvalidDeadlineRuleException oneTimeNoRecurrence() {
        return new InvalidDeadlineRuleException(ONE_TIME_NO_RECURRENCE);
    }

    public static InvalidDeadlineRuleException recurringRuleRequired() {
        return new InvalidDeadlineRuleException(RECURRING_RULE_REQUIRED);
    }

    public static InvalidDeadlineRuleException recurringNoDueDate() {
        return new InvalidDeadlineRuleException(RECURRING_NO_DUE_DATE);
    }

    public static InvalidDeadlineRuleException endDateAfterStart() {
        return new InvalidDeadlineRuleException(END_DATE_AFTER_START);
    }
}
