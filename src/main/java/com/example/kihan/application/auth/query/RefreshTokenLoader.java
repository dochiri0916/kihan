package com.example.kihan.application.auth.query;

import com.example.kihan.domain.auth.RefreshToken;

import java.time.LocalDateTime;

public interface RefreshTokenLoader {

    RefreshToken loadValidToken(String token, LocalDateTime now);

}
