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
    public Optional<Deadline> findByIdAndUserId(Long deadlineId, Long userId) {
        return deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId);
    }

    @Override
    public Deadline loadByIdAndUserId(Long deadlineId, Long userId) {
        return findByIdAndUserId(deadlineId, userId)
                .orElseThrow(() -> new DeadlineNotFoundException(deadlineId));
    }

    public DeadlineDetail findById(Long userId, Long deadlineId) {
        Deadline deadline = loadByIdAndUserId(deadlineId, userId);
        return DeadlineDetail.from(deadline);
    }

    public List<DeadlineDetail> findAllByUserId(Long userId) {
        return deadlineRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(DeadlineDetail::from)
                .toList();
    }
}
