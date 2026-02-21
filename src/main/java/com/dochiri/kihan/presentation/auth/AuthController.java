package com.dochiri.kihan.presentation.auth;

import com.dochiri.kihan.application.auth.command.RevokeTokenService;
import com.dochiri.kihan.application.auth.dto.LoginResult;
import com.dochiri.kihan.application.auth.facade.LoginFacade;
import com.dochiri.kihan.application.auth.facade.ReissueTokenFacade;
import com.dochiri.kihan.presentation.auth.request.LoginRequest;
import com.dochiri.kihan.presentation.auth.request.LogoutRequest;
import com.dochiri.kihan.presentation.auth.request.ReissueRequest;
import com.dochiri.kihan.presentation.auth.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

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

        return ResponseEntity.ok(
                AuthResponse.from(
                        loginResult.user(),
                        loginResult.accessToken(),
                        loginResult.refreshToken()
                )
        );
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        LoginResult loginResult = reissueTokenFacade.reissue(request.refreshToken());

        return ResponseEntity.ok(
                AuthResponse.from(
                        loginResult.user(),
                        loginResult.accessToken(),
                        loginResult.refreshToken()
                )
        );
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 폐기합니다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest request
    ) {
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            revokeTokenService.revokeByToken(request.refreshToken());
        }

        return ResponseEntity.noContent().build();
    }

}
