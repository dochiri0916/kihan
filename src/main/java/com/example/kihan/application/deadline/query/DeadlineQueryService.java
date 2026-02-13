package com.example.kihan.application.deadline.query;

import com.example.kihan.application.deadline.dto.DeadlineDetail;
import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.domain.deadline.DeadlineNotFoundException;
import com.example.kihan.infrastructure.persistence.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeadlineQueryService {

    private final DeadlineRepository deadlineRepository;

    public DeadlineDetail findById(Long deadlineId) {
        Deadline deadline = deadlineRepository.findByIdAndDeletedAtIsNull(deadlineId)
                .orElseThrow(() -> new DeadlineNotFoundException(deadlineId));
        return DeadlineDetail.from(deadline);
    }

    public List<DeadlineDetail> findAll() {
        return deadlineRepository.findByDeletedAtIsNull().stream()
                .map(DeadlineDetail::from)
                .toList();
    }
}
