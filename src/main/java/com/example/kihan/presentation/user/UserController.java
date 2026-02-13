package com.example.kihan.presentation.user;

import com.example.kihan.application.user.command.RegisterService;
import com.example.kihan.application.user.query.UserQueryService;
import com.example.kihan.infrastructure.security.jwt.JwtPrincipal;
import com.example.kihan.presentation.user.request.RegisterRequest;
import com.example.kihan.presentation.user.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterService registerService;
    private final UserQueryService userQueryService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(
                UserResponse.from(registerService.register(request))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ResponseEntity.ok(
                UserResponse.from(
                        userQueryService.getActiveUser(principal.userId())
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getActiveUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                UserResponse.from(userQueryService.getActiveUser(id)
                )
        );
    }

}