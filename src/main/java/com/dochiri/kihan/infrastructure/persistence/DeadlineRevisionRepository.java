package com.dochiri.kihan.infrastructure.persistence;

import com.dochiri.kihan.domain.deadline.Deadline;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadlineRevisionRepository extends RevisionRepository<Deadline, Long, Long> {
}
