package com.dochiri.kihan.presentation.auth;

import com.dochiri.kihan.application.auth.command.RevokeTokenService;
import com.dochiri.kihan.application.auth.dto.LoginResult;
import com.dochiri.kihan.application.auth.facade.LoginFacade;
import com.dochiri.kihan.application.auth.facade.ReissueTokenFacade;
import com.dochiri.kihan.presentation.auth.request.LoginRequest;
import com.dochiri.kihan.presentation.auth.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final long REFRESH_TOKEN_MAX_AGE_SECONDS = 14L * 24 * 60 * 60;

    private final LoginFacade loginFacade;
    private final ReissueTokenFacade reissueTokenFacade;
    private final RevokeTokenService revokeTokenService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResult loginResult = loginFacade.login(request.toCommand());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(loginResult.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(
                AuthResponse.from(
                        loginResult.user(),
                        loginResult.accessToken()
                )
        );
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(
            @CookieValue(REFRESH_TOKEN_COOKIE) String refreshToken
    ) {
        LoginResult loginResult = reissueTokenFacade.reissue(refreshToken);
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(loginResult.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(
                AuthResponse.from(
                        loginResult.user(),
                        loginResult.accessToken()
                )
        );
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 폐기합니다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            revokeTokenService.revokeByToken(refreshToken);
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie().toString())
                .build();
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE_SECONDS)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
