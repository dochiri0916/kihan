package com.example.pragmatic.domain.deadline;

import com.example.pragmatic.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.util.Objects.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deadline extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private DeadlineType type;

    private LocalDateTime dueDate;

    @Embedded
    private RecurrenceRule recurrenceRule;

    public static Deadline register(final String title, final String description, final DeadlineType type, final LocalDateTime dueDate, final RecurrenceRule recurrenceRule) {
        Deadline deadline = new Deadline();
        deadline.title = requireNonNull(title);
        deadline.description = description;
        deadline.type = requireNonNull(type);
        deadline.dueDate = dueDate;
        deadline.recurrenceRule = recurrenceRule;
        deadline.validate(type, dueDate, recurrenceRule);
        return deadline;
    }

    public void changeTitle(final String newTitle) {
        this.title = requireNonNull(newTitle);
    }

    public void changeDescription(final String newDescription) {
        this.description = newDescription;
    }

    public void markAsCompleted() {
        this.delete();
    }

    private void validate(final DeadlineType type, final LocalDateTime dueDate, final RecurrenceRule recurrenceRule) {
        if (type == DeadlineType.ONE_TIME) {
            if (dueDate == null) {
                throw InvalidDeadlineRuleException.oneTimeDueDateRequired();
            }
            if (dueDate.isBefore(LocalDateTime.now())) {
                throw InvalidDeadlineRuleException.dueDateMustBeFuture();
            }
            if (recurrenceRule != null) {
                throw InvalidDeadlineRuleException.oneTimeNoRecurrence();
            }
        }

        if (type == DeadlineType.RECURRING) {
            if (recurrenceRule == null) {
                throw InvalidDeadlineRuleException.recurringRuleRequired();
            }
            if (dueDate != null) {
                throw InvalidDeadlineRuleException.recurringNoDueDate();
            }
        }
    }
}