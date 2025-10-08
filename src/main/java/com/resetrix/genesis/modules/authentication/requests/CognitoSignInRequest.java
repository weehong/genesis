package com.resetrix.genesis.modules.authentication.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CognitoSignInRequest(
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    String email,

    @NotBlank(message = "Password is required.")
    @Size(
        min = 8,
        max = 128,
        message = "Password must be between 8 and 128 characters."
    )
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)"
            + "(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/])"
            + "[A-Za-z\\d!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]{8,128}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, "
            + "one digit, and one special character."
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password
) {
}
