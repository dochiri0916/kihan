package com.dochiri.kihan.infrastructure.persistence.deadline;

import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineNotFoundException;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaDeadlineRepository implements DeadlineRepository {

    private final DeadlineJpaRepository deadlineJpaRepository;

    @Override
    public Deadline save(Deadline deadline) {
        return deadlineJpaRepository.save(deadline);
    }

    @Override
    public Deadline findByIdAndDeletedAtIsNull(Long id) {
        return deadlineJpaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DeadlineNotFoundException(id));
    }

    @Override
    public Deadline findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId) {
        return deadlineJpaRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new DeadlineNotFoundException(id));
    }

    @Override
    public List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId) {
        return deadlineJpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public List<Deadline> findAllByDeletedAtIsNull() {
        return deadlineJpaRepository.findAllByDeletedAtIsNull();
    }

}