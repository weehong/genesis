package com.resetrix.genesis.modules.authentication.requests;

import com.resetrix.genesis.modules.authentication.validators.ValidE164Phone;
import jakarta.validation.constraints.NotBlank;

public record CognitoSignUpRequest(
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "Password is required")
    String password,

    @NotBlank(message = "Phone Number is required")
    @ValidE164Phone
    String phoneNumber
) {
}
