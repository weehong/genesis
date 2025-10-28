package com.resetrix.horaion.shared.constants;

import java.util.regex.Pattern;

public final class SecurityConstants {
    /**
     * DEPRECATED: This regex-based XSS pattern is insufficient for proper XSS prevention.
     *
     * Proper XSS prevention requires context-appropriate output encoding/escaping:
     * - Use Spring's HtmlUtils.htmlEscape() for HTML context (already applied in ApiResponseWrapperAdvice)
     * - Use OWASP Java Encoder for JavaScript, CSS, or URL contexts if needed
     * - Implement Content Security Policy (CSP) headers as defense-in-depth
     *
     * This pattern is kept for backward compatibility with existing lightweight validation
     * but should NOT be relied upon as the primary XSS defense mechanism.
     *
     * @deprecated Use proper output encoding/escaping instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
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

    // Role constants for AWS Cognito groups and custom claims
    public static final String ROLE_SYSTEM_ADMINISTRATOR = "ROLE_SYSTEM_ADMINISTRATOR";
    public static final String ROLE_SYSTEM_OWNER = "ROLE_SYSTEM_OWNER";
    public static final String ROLE_PRIVILEGED_SYSTEM_USER = "ROLE_PRIVILEGED_SYSTEM_USER";
    public static final String ROLE_USER = "ROLE_USER";

    // Cognito group names (these should match your AWS Cognito User Pool groups)
    public static final String COGNITO_GROUP_SYSTEM_ADMINISTRATOR = "system-administrator";
    public static final String COGNITO_GROUP_SYSTEM_OWNER = "system-owner";
    public static final String COGNITO_GROUP_PRIVILEGED_SYSTEM_USER = "privileged-system-user";
    public static final String COGNITO_GROUP_USER = "user";

    // JWT claim names
    public static final String CLAIM_COGNITO_GROUPS = "cognito:groups";
    public static final String CLAIM_CUSTOM_ROLES = "custom:roles";
    public static final String CLAIM_COMPANY_ID = "company_id";
    public static final String CLAIM_COMPANY_UUID = "company_uuid";

    private SecurityConstants() {
        throw new AssertionError("No instances.");
    }
}
