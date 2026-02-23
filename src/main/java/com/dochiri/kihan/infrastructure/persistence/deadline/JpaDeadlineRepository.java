package com.dochiri.kihan.infrastructure.persistence.deadline;

import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.exception.DeadlineNotFoundException;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaDeadlineRepository implements DeadlineRepository {

    private final DeadlineJpaRepository jpaRepository;

    @Override
    public Deadline save(Deadline deadline) {
        return jpaRepository.save(deadline);
    }

    @Override
    public Deadline findByIdAndDeletedAtIsNull(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DeadlineNotFoundException(id));
    }

    @Override
    public Deadline findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId) {
        return jpaRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new DeadlineNotFoundException(id));
    }

    @Override
    public List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId) {
        return jpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId, Sort sort) {
        return jpaRepository.findByUserIdAndDeletedAtIsNull(userId, sort);
    }

    @Override
    public Page<Deadline> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
    }

    @Override
    public LocalDateTime findLastModifiedAtByUserId(Long userId) {
        return jpaRepository.findLastModifiedAtByUserId(userId);
    }

    @Override
    public List<Deadline> findAllByDeletedAtIsNull() {
        return jpaRepository.findAllByDeletedAtIsNull();
    }

}
