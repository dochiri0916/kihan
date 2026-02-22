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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeadlineType type;

    private LocalDate dueDate;

    @Embedded
    private RecurrenceRule recurrenceRule;

    public static Deadline register(Long userId, String title, DeadlineType type, LocalDate dueDate, RecurrenceRule recurrenceRule) {
        Deadline deadline = new Deadline();
        deadline.userId = requireNonNull(userId);
        deadline.title = requireNonNull(title);
        deadline.type = requireNonNull(type);
        deadline.dueDate = dueDate;
        deadline.recurrenceRule = recurrenceRule;
        deadline.validate(type, dueDate, recurrenceRule);
        return deadline;
    }

    public void update(String newTitle) {
        if (newTitle != null) {
            changeTitle(newTitle);
        }
    }

    public void updateRecurrenceRule(RecurrenceRule newRecurrenceRule) {
        if (!this.type.isRecurring()) {
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

    private void validate(DeadlineType type, LocalDate dueDate, RecurrenceRule recurrenceRule) {
        if (type.isSingle()) {
            if (dueDate == null) {
                throw InvalidDeadlineRuleException.oneTimeDueDateRequired();
            }
            if (recurrenceRule != null) {
                throw InvalidDeadlineRuleException.oneTimeNoRecurrence();
            }
        }

        if (type.isRecurring()) {
            if (recurrenceRule == null) {
                throw InvalidDeadlineRuleException.recurringRuleRequired();
            }
            if (dueDate != null) {
                throw InvalidDeadlineRuleException.recurringNoDueDate();
            }
        }
    }

}
