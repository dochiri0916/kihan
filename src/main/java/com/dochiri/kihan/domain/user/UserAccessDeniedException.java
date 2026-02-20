package com.dochiri.kihan.domain.user;

public class UserAccessDeniedException extends UserException {

    private UserAccessDeniedException(final String message) {
        super(message);
    }

    public static UserAccessDeniedException forUser(final Long userId) {
        return new UserAccessDeniedException("사용자 정보 조회 권한이 없습니다. userId=" + userId);
    }

}
