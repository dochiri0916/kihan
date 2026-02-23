package com.dochiri.kihan.domain.deadline.exception;

public class InvalidDeadlineTitleException extends DeadlineException {
    public InvalidDeadlineTitleException() {
        super("기한 항목의 제목은 필수입니다.");
    }
}