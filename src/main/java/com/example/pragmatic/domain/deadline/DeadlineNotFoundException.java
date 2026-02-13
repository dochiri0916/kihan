package com.example.pragmatic.domain.deadline;

public class DeadlineNotFoundException extends DeadlineException {
    public DeadlineNotFoundException(Long deadlineId) {
        super("마감을 찾을 수 없습니다: " + deadlineId);
    }
}