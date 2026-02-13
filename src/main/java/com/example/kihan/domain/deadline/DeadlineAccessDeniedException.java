package com.example.kihan.domain.deadline;

public class DeadlineAccessDeniedException extends DeadlineException {
    public DeadlineAccessDeniedException(Long deadlineId, Long userId) {
        super("기한 항목에 접근할 권한이 없습니다: deadlineId=" + deadlineId + ", userId=" + userId);
    }
}
