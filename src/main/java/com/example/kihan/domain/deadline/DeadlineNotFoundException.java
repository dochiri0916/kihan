package com.example.kihan.domain.deadline;

public class DeadlineNotFoundException extends DeadlineException {
    public DeadlineNotFoundException(Long deadlineId) {
        super("기한 항목을 찾을 수 없습니다: " + deadlineId);
    }
}