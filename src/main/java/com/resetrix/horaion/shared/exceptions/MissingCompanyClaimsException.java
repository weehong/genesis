package com.resetrix.horaion.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user's JWT token is missing required company claims
 * (company_id or company_uuid) needed for company-scoped access control.
 *
 * This exception is typically thrown when:
 * - A non-system-administrator user attempts to access company resources
 * - The user's JWT token lacks both company_id and company_uuid claims
 * - The authorization service cannot determine which company the user belongs to
 */
public class MissingCompanyClaimsException extends BaseException {

    private final String username;
    private final String requestedCompanyId;

    /**
     * Creates a new MissingCompanyClaimsException.
     *
     * @param username The username of the user whose JWT token is missing company claims
     * @param requestedCompanyId The company ID that was being accessed when the error occurred
     */
    public MissingCompanyClaimsException(String username, String requestedCompanyId) {
        super(
            String.format(
                "User '%s' JWT token is missing required company claims (company_id or company_uuid) "
                + "for accessing company '%s' resources",
                username != null ? username : "<unknown>",
                requestedCompanyId != null ? requestedCompanyId : "<unknown>"
            ),
            HttpStatus.FORBIDDEN,
            "MISSING_COMPANY_CLAIMS"
        );

        this.username = username;
        this.requestedCompanyId = requestedCompanyId;
    }

    /**
     * Gets the username of the user whose JWT token is missing company claims.
     *
     * @return The username, or null if not available
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the company ID that was being accessed when the error occurred.
     *
     * @return The requested company ID, or null if not available
     */
    public String getRequestedCompanyId() {
        return requestedCompanyId;
    }
}
