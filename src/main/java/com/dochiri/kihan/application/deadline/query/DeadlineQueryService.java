package com.dochiri.kihan.application.deadline.query;

import com.dochiri.kihan.application.deadline.dto.DeadlineDetail;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeadlineQueryService {

    private final DeadlineRepository deadlineRepository;

    public DeadlineDetail getById(Long userId, Long deadlineId) {
        Deadline deadline = deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId);
        return DeadlineDetail.from(deadline);
    }

    public List<DeadlineDetail> getAllByUserId(Long userId) {
        return deadlineRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(DeadlineDetail::from)
                .toList();
    }

    public List<DeadlineDetail> getAllByUserId(Long userId, DeadlineSortBy sortBy, Sort.Direction direction) {
        return deadlineRepository.findByUserIdAndDeletedAtIsNull(userId, buildSort(sortBy, direction)).stream()
                .map(DeadlineDetail::from)
                .toList();
    }

    public Page<DeadlineDetail> getPageByUserId(
            Long userId,
            int page,
            int size,
            DeadlineSortBy sortBy,
            Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, direction));
        return deadlineRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(DeadlineDetail::from);
    }

    public LocalDateTime getLastModifiedAt(Long userId) {
        return deadlineRepository.findLastModifiedAtByUserId(userId);
    }

    public List<Deadline> findAllActive() {
        return deadlineRepository.findAllByDeletedAtIsNull();
    }

    private Sort buildSort(DeadlineSortBy sortBy, Sort.Direction direction) {
        return Sort.by(direction, toProperty(sortBy))
                .and(Sort.by(direction, "id"));
    }

    private String toProperty(DeadlineSortBy sortBy) {
        return switch (sortBy) {
            case CREATED_AT -> "createdAt";
            case DUE_DATE -> "dueDate";
            case TITLE -> "title";
        };
    }

}
