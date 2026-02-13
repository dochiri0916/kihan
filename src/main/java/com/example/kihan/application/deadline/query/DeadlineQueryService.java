package com.example.kihan.application.deadline.query;

import com.example.kihan.application.deadline.dto.DeadlineDetail;
import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.domain.deadline.DeadlineNotFoundException;
import com.example.kihan.infrastructure.persistence.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeadlineQueryService implements DeadlineFinder, DeadlineLoader {

    private final DeadlineRepository deadlineRepository;

    @Override
    public Optional<Deadline> findByIdAndUserId(final Long deadlineId, final Long userId) {
        return deadlineRepository.findByIdAndDeletedAtIsNull(deadlineId)
                .filter(deadline -> deadline.getUserId().equals(userId));
    }

    @Override
    public Deadline loadByIdAndUserId(final Long deadlineId, final Long userId) {
        Deadline deadline = deadlineRepository.findByIdAndDeletedAtIsNull(deadlineId)
                .orElseThrow(() -> new DeadlineNotFoundException(deadlineId));
        deadline.verifyOwnership(userId);
        return deadline;
    }

    public DeadlineDetail getById(final Long userId, final Long deadlineId) {
        Deadline deadline = loadByIdAndUserId(deadlineId, userId);
        return DeadlineDetail.from(deadline);
    }

    public List<DeadlineDetail> getAllByUserId(final Long userId) {
        return deadlineRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(DeadlineDetail::from)
                .toList();
    }

}