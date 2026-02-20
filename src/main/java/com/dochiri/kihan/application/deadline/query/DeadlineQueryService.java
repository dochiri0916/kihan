package com.dochiri.kihan.application.deadline.query;

import com.dochiri.kihan.application.deadline.dto.DeadlineDetail;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineNotFoundException;
import com.dochiri.kihan.infrastructure.persistence.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeadlineQueryService implements DeadlineFinder, DeadlineLoader {

    private  DeadlineRepository deadlineRepository;

    @Override
    public Optional<Deadline> findByIdAndUserId(Long deadlineId, Long userId) {
        return deadlineRepository.findByIdAndDeletedAtIsNull(deadlineId)
                .filter(deadline -> deadline.getUserId().equals(userId));
    }

    @Override
    public Deadline loadByIdAndUserId(Long deadlineId, Long userId) {
        Deadline deadline = deadlineRepository.findByIdAndDeletedAtIsNull(deadlineId)
                .orElseThrow(() -> new DeadlineNotFoundException(deadlineId));
        deadline.verifyOwnership(userId);
        return deadline;
    }

    public DeadlineDetail getById(Long userId, Long deadlineId) {
        Deadline deadline = loadByIdAndUserId(deadlineId, userId);
        return DeadlineDetail.from(deadline);
    }

    public List<DeadlineDetail> getAllByUserId(Long userId) {
        return deadlineRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(DeadlineDetail::from)
                .toList();
    }

    public List<Deadline> findAllActive() {
        return deadlineRepository.findAllByDeletedAtIsNull();
    }

}
