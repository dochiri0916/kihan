package com.dochiri.kihan.presentation.auth;

import com.dochiri.kihan.application.auth.command.RevokeTokenService;
import com.dochiri.kihan.application.auth.dto.LoginResult;
import com.dochiri.kihan.application.auth.facade.LoginFacade;
import com.dochiri.kihan.application.auth.facade.ReissueTokenFacade;
import com.dochiri.kihan.infrastructure.security.cookie.CookieProvider;
import com.dochiri.kihan.presentation.auth.request.LoginRequest;
import com.dochiri.kihan.presentation.auth.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginFacade loginFacade;
    private final CookieProvider cookieProvider;
    private final ReissueTokenFacade reissueTokenFacade;
    private final RevokeTokenService revokeTokenService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. Refresh Token은 HttpOnly 쿠키로 반환됩니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        LoginResult loginResult = loginFacade.login(request.toCommand());

        cookieProvider.addRefreshToken(response, loginResult.refreshToken());

        return ResponseEntity.ok(
                AuthResponse.from(loginResult.user(), loginResult.accessToken())
        );
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(
            @Parameter(description = "Refresh Token (쿠키)") @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        LoginResult loginResult = reissueTokenFacade.reissue(refreshToken);

        cookieProvider.addRefreshToken(response, loginResult.refreshToken());

        return ResponseEntity.ok(
                AuthResponse.from(loginResult.user(), loginResult.accessToken())
        );
    }

    @Operation(summary = "로그아웃", description = "Refresh Token 쿠키를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        if (StringUtils.hasText(refreshToken)) {
            revokeTokenService.revokeByToken(refreshToken);
        }

        cookieProvider.deleteRefreshToken(response);
        return ResponseEntity.noContent().build();
    }

}