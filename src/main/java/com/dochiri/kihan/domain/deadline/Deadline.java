package com.dochiri.kihan.domain.deadline;

import com.dochiri.kihan.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "deadlines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deadline extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private LocalDate dueDate;

    @Embedded
    private RecurrenceRule recurrenceRule;

    public static Deadline register(Long userId, String title, LocalDate dueDate, RecurrenceRule recurrenceRule) {
        Deadline deadline = new Deadline();
        deadline.userId = requireNonNull(userId);
        deadline.title = requireNonNull(title);
        deadline.dueDate = dueDate;
        deadline.recurrenceRule = recurrenceRule;
        deadline.validate(dueDate, recurrenceRule);
        return deadline;
    }

    public static Deadline register(
            Long userId,
            String title,
            DeadlineType type,
            LocalDate dueDate,
            RecurrenceRule recurrenceRule
    ) {
        requireNonNull(type);
        if (type.isSingle() && recurrenceRule != null) {
            throw InvalidDeadlineRuleException.oneTimeNoRecurrence();
        }
        if (type.isRecurring() && recurrenceRule == null) {
            throw InvalidDeadlineRuleException.recurringRuleRequired();
        }
        return register(userId, title, dueDate, recurrenceRule);
    }

    public void update(String newTitle) {
        if (newTitle != null) {
            changeTitle(newTitle);
        }
    }

    public void updateRecurrenceRule(RecurrenceRule newRecurrenceRule) {
        if (this.recurrenceRule == null) {
            throw InvalidDeadlineRuleException.oneTimeNoRecurrence();
        }
        if (newRecurrenceRule == null) {
            throw InvalidDeadlineRuleException.recurringRuleRequired();
        }
        this.recurrenceRule = newRecurrenceRule;
    }

    private void changeTitle(String newTitle) {
        requireNonNull(newTitle);
        if (newTitle.isBlank()) {
            throw new InvalidDeadlineTitleException();
        }
        this.title = newTitle;
    }

    public void verifyOwnership(Long requestUserId) {
        if (!this.userId.equals(requestUserId)) {
            throw new DeadlineAccessDeniedException(this.getId(), requestUserId);
        }
    }

    public DeadlineType getType() {
        return this.recurrenceRule == null
                ? DeadlineType.ONE_TIME
                : DeadlineType.RECURRING;
    }

    private void validate(LocalDate dueDate, RecurrenceRule recurrenceRule) {
        if (recurrenceRule == null) {
            if (dueDate == null) {
                throw InvalidDeadlineRuleException.oneTimeDueDateRequired();
            }
            return;
        }

        if (dueDate != null) {
            throw InvalidDeadlineRuleException.recurringNoDueDate();
        }
    }

}
