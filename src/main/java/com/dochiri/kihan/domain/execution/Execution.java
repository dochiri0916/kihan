package com.dochiri.kihan.domain.execution;

import com.dochiri.kihan.domain.BaseEntity;
import com.dochiri.kihan.domain.deadline.Deadline;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "executions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Execution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deadline_id", nullable = false)
    private Deadline deadline;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    private LocalDateTime completedAt;

    public static Execution create(Deadline deadline, LocalDate scheduledDate) {
        Execution execution = new Execution();
        execution.deadline = requireNonNull(deadline);
        execution.scheduledDate = requireNonNull(scheduledDate);
        execution.status = ExecutionStatus.IN_PROGRESS;
        return execution;
    }

    public void markAsDone(LocalDateTime completedAt) {
        if (this.status == ExecutionStatus.DONE) {
            throw ExecutionAlreadyCompletedException.withDate(this.scheduledDate);
        }
        this.status = ExecutionStatus.DONE;
        this.completedAt = requireNonNull(completedAt);
    }

    public void markAsPaused() {
        if (this.status == ExecutionStatus.DONE) {
            throw ExecutionAlreadyCompletedException.withDate(this.scheduledDate);
        }
        this.status = ExecutionStatus.PAUSED;
        this.completedAt = null;
    }

    public void markAsInProgress() {
        if (this.status == ExecutionStatus.DONE) {
            throw ExecutionAlreadyCompletedException.withDate(this.scheduledDate);
        }
        this.status = ExecutionStatus.IN_PROGRESS;
        this.completedAt = null;
    }

    public boolean isInProgress() {
        return this.status == ExecutionStatus.IN_PROGRESS;
    }

    public boolean isDone() {
        return this.status == ExecutionStatus.DONE;
    }

    public boolean isPaused() {
        return this.status == ExecutionStatus.PAUSED;
    }

}
