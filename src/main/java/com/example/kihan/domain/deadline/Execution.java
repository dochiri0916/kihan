package com.example.kihan.domain.deadline;

import com.example.kihan.domain.BaseEntity;
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

    public static Execution create(final Deadline deadline, final LocalDate scheduledDate) {
        Execution execution = new Execution();
        execution.deadline = requireNonNull(deadline);
        execution.scheduledDate = requireNonNull(scheduledDate);
        execution.status = ExecutionStatus.PENDING;
        return execution;
    }

    public void markAsDone() {
        if (this.status == ExecutionStatus.DONE) {
            throw ExecutionAlreadyCompletedException.withDate(this.scheduledDate);
        }
        this.status = ExecutionStatus.DONE;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsDelayed() {
        if (this.status == ExecutionStatus.DONE) {
            throw ExecutionAlreadyCompletedException.withDate(this.scheduledDate);
        }
        this.status = ExecutionStatus.DELAYED;
        this.completedAt = null;
    }

    public boolean isPending() {
        return this.status == ExecutionStatus.PENDING;
    }

    public boolean isDone() {
        return this.status == ExecutionStatus.DONE;
    }

    public boolean isDelayed() {
        return this.status == ExecutionStatus.DELAYED;
    }

}
