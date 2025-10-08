package com.resetrix.genesis.modules.authentication.responses;

import jakarta.validation.constraints.NotBlank;

public record CognitoSignInResponse(
    @NotBlank(message = "Access token is required")
    String accessToken,

    @NotBlank(message = "Refresh token is required")
    String refreshToken,

    @NotBlank(message = "ID token is required")
    String idToken
) {
}