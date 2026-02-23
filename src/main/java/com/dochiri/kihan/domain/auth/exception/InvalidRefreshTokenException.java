package com.dochiri.kihan.domain.auth.exception;

public class InvalidRefreshTokenException extends RefreshTokenException {
    private static final String INVALID_TOKEN_TYPE = "리프레시 토큰이 아닙니다.";
    private static final String OWNER_MISMATCH = "토큰 소유자가 일치하지 않습니다.";

    private InvalidRefreshTokenException(String message) {
        super(message);
    }

    public static InvalidRefreshTokenException invalidTokenType() {
        return new InvalidRefreshTokenException(INVALID_TOKEN_TYPE);
    }

    public static InvalidRefreshTokenException ownerMismatch() {
        return new InvalidRefreshTokenException(OWNER_MISMATCH);
    }
}