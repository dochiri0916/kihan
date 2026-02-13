package com.example.kihan.presentation.auth;

import com.example.kihan.application.auth.dto.LoginResult;
import com.example.kihan.application.auth.facade.LoginFacade;
import com.example.kihan.application.auth.facade.ReissueTokenFacade;
import com.example.kihan.infrastructure.security.cookie.CookieProvider;
import com.example.kihan.presentation.auth.request.LoginRequest;
import com.example.kihan.presentation.auth.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginFacade loginFacade;
    private final CookieProvider cookieProvider;
    private final ReissueTokenFacade reissueTokenFacade;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult loginResult = loginFacade.login(request);

        cookieProvider.addRefreshToken(response, loginResult.refreshToken());

        return ResponseEntity.ok(
                AuthResponse.from(loginResult.user(), loginResult.accessToken())
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(
            @CookieValue(name = "refreshToken") String refreshToken
    ) {
        return ResponseEntity.ok(
                reissueTokenFacade.reissue(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieProvider.deleteRefreshToken(response);
        return ResponseEntity.noContent().build();
    }

}