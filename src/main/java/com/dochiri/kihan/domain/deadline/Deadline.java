package com.dochiri.kihan.domain.deadline;

import com.dochiri.kihan.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

import static java.util.Objects.*;

@Entity
@Table(name = "deadlines")
@Audited
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deadline extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeadlineType type;

    private LocalDateTime dueDate;

    @Embedded
    private RecurrenceRule recurrenceRule;

    public static Deadline register(Long userId, String title, String description, DeadlineType type, LocalDateTime dueDate, RecurrenceRule recurrenceRule) {
        Deadline deadline = new Deadline();
        deadline.userId = requireNonNull(userId);
        deadline.title = requireNonNull(title);
        deadline.description = description;
        deadline.type = requireNonNull(type);
        deadline.dueDate = dueDate;
        deadline.recurrenceRule = recurrenceRule;
        deadline.validate(type, dueDate, recurrenceRule);
        return deadline;
    }

    public void update(String newTitle, String newDescription) {
        if (newTitle != null) {
            changeTitle(newTitle);
        }

        if (newDescription != null) {
            changeDescription(newDescription);
        }
    }

    private void changeTitle(String newTitle) {
        requireNonNull(newTitle);
        if (newTitle.isBlank()) {
            throw new InvalidDeadlineTitleException();
        }
        this.title = newTitle;
    }

    private void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void markAsCompleted(LocalDateTime now) {
        this.delete(requireNonNull(now));
    }

    public void verifyOwnership(Long requestUserId) {
        if (!this.userId.equals(requestUserId)) {
            throw new DeadlineAccessDeniedException(this.getId(), requestUserId);
        }
    }

    private void validate(DeadlineType type, LocalDateTime dueDate, RecurrenceRule recurrenceRule) {
        if (type == DeadlineType.ONE_TIME) {
            if (dueDate == null) {
                throw InvalidDeadlineRuleException.oneTimeDueDateRequired();
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
