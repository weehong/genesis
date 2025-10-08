package com.resetrix.genesis.shared.constants;

import java.util.regex.Pattern;

public final class SecurityConstants {
    public static final Pattern XSS_PATTERN = Pattern.compile(
        ".*(<script|<iframe|javascript:|onload=|onerror=|onclick=).*",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/authentication/sign-in",
        "/",
        "/actuator/health",
        "/actuator/info"
    };

    private SecurityConstants() {
        throw new AssertionError("No instances.");
    }
}
