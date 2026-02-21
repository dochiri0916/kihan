package com.dochiri.kihan.presentation.auth.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank
        String refreshToken
) {
}
