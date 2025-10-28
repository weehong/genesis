package com.resetrix.horaion.shared.services;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import static com.resetrix.horaion.shared.constants.SecurityConstants.CLAIM_COMPANY_ID;
import static com.resetrix.horaion.shared.constants.SecurityConstants.CLAIM_COMPANY_UUID;
import static com.resetrix.horaion.shared.constants.SecurityConstants.ROLE_PRIVILEGED_SYSTEM_USER;
import static com.resetrix.horaion.shared.constants.SecurityConstants.ROLE_SYSTEM_ADMINISTRATOR;
import static com.resetrix.horaion.shared.constants.SecurityConstants.ROLE_SYSTEM_OWNER;
import static com.resetrix.horaion.shared.constants.SecurityConstants.ROLE_USER;
import com.resetrix.horaion.shared.exceptions.MissingCompanyClaimsException;

/**
 * Generic authorization service for handling role-based and company-scoped access control.
 * This service provides reusable authorization logic that can be used across all modules
 * in the application (Branch, Company, User, etc.).
 */
@Service
public class AuthorizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationService.class);

    /**
     * Checks if the current user has system administrator privileges.
     * System administrators can access all resources across all companies.
     *
     * @param authentication The current authentication object
     * @return true if the user is a system administrator, false otherwise
     */
    public boolean hasSystemAdminAccess(Authentication authentication) {
        if (authentication == null) {
            LOGGER.debug("Authentication is null, denying system admin access");
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean hasSystemAdminRole = authorities.stream()
            .anyMatch(authority -> ROLE_SYSTEM_ADMINISTRATOR.equals(authority.getAuthority()));

        LOGGER.debug("Authentication principal has system admin access: {}", hasSystemAdminRole);

        return hasSystemAdminRole;
    }

    /**
     * Checks if the current user has elevated privileges (all roles except basic user).
     * Users with elevated privileges can create, update, and delete resources.
     * This includes: SYSTEM_ADMINISTRATOR, SYSTEM_OWNER, and PRIVILEGED_SYSTEM_USER.
     *
     * @param authentication The current authentication object
     * @return true if the user has elevated privileges, false otherwise
     */
    public boolean hasElevatedAccess(Authentication authentication) {
        if (authentication == null) {
            LOGGER.debug("Authentication is null, denying elevated access");
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean hasElevatedRole = authorities.stream()
            .anyMatch(authority ->
                          ROLE_SYSTEM_ADMINISTRATOR.equals(authority.getAuthority())
                              || ROLE_SYSTEM_OWNER.equals(authority.getAuthority())
                              || ROLE_PRIVILEGED_SYSTEM_USER.equals(authority.getAuthority()));

        LOGGER.debug("Authentication principal has elevated access: {}", hasElevatedRole);

        return hasElevatedRole;
    }

    /**
     * Checks if the current user has company-scoped access privileges.
     * This includes all roles that can access company resources: SYSTEM_ADMINISTRATOR,
     * SYSTEM_OWNER, PRIVILEGED_SYSTEM_USER, and USER.
     *
     * @param authentication The current authentication object
     * @return true if the user has company-scoped access, false otherwise
     */
    public boolean hasCompanyScopedAccess(Authentication authentication) {
        if (authentication == null) {
            LOGGER.debug("Authentication is null, denying company-scoped access");
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean hasCompanyScopedRole = authorities.stream()
            .anyMatch(authority ->
                          ROLE_SYSTEM_ADMINISTRATOR.equals(authority.getAuthority())
                              || ROLE_SYSTEM_OWNER.equals(authority.getAuthority())
                              || ROLE_PRIVILEGED_SYSTEM_USER.equals(authority.getAuthority())
                              || ROLE_USER.equals(authority.getAuthority()));

        LOGGER.debug("Authentication principal has company-scoped access: {}", hasCompanyScopedRole);

        return hasCompanyScopedRole;
    }

    /**
     * Checks if the current user can access company resources based on their role and company association.
     * - SYSTEM_ADMINISTRATOR: Can access any company's resources
     * - SYSTEM_OWNER/PRIVILEGED_SYSTEM_USER/USER: Can only access their own company's resources
     * <p>
     * This method supports both numeric company IDs and UUID-based company identifiers.
     * It checks both company_id and company_uuid JWT claims for company membership.
     *
     * @param authentication The current authentication object
     * @param companyId      The ID of the company whose resources are being accessed (can be null for system admins)
     * @return true if the user can access the company's resources, false otherwise
     * @throws IllegalArgumentException      if companyId is null for non-system-administrator users
     * @throws MissingCompanyClaimsException if JWT token is missing required company claims for
     *                                        non-system-administrator users
     */
    public boolean canAccessCompanyResources(Authentication authentication, String companyId) {
        if (authentication == null) {
            LOGGER.debug("Authentication is null, denying access");
            return false;
        }

        // Check if user has required roles for company-scoped access
        if (!hasCompanyScopedAccess(authentication)) {
            LOGGER.debug("Authentication principal does not have required roles for company-scoped access");
            return false;
        }

        // System administrators can access any company's resources
        if (hasSystemAdminAccess(authentication)) {
            LOGGER.debug("Authentication principal has system admin access, allowing access to any company resources");
            return true;
        }

        // For non-system-admins, company ID is required
        if (companyId == null) {
            LOGGER.error("Company ID cannot be null for company-scoped access");
            throw new IllegalArgumentException("Company ID cannot be null for company-scoped access");
        }

        return checkJwtCompanyAccess(authentication, companyId);
    }

    /**
     * Helper method to check JWT-based company access.
     * Validates that the user's JWT token contains company claims and matches the requested company.
     *
     * @param authentication The current authentication object
     * @param companyId      The company ID to check against
     * @return true if the user has access to the company, false otherwise
     * @throws MissingCompanyClaimsException if JWT token is missing required company claims
     */
    private boolean checkJwtCompanyAccess(Authentication authentication, String companyId) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            LOGGER.debug("Authentication is not a JWT token, denying access");
            return false;
        }

        Jwt jwt = jwtAuth.getToken();
        String userCompanyId = jwt.getClaimAsString(CLAIM_COMPANY_ID);
        String userCompanyUuid = jwt.getClaimAsString(CLAIM_COMPANY_UUID);

        // Check if user has any company claims at all
        if (userCompanyId == null && userCompanyUuid == null) {
            LOGGER.error(
                "JWT token is missing required company claims (company_id or company_uuid) "
                    + "for accessing company resources");
            throw new MissingCompanyClaimsException(authentication.getName(), companyId);
        }

        return validateCompanyMatch(companyId, userCompanyId, userCompanyUuid);
    }

    /**
     * Helper method to validate if the requested company ID matches user's company claims.
     *
     * @param companyId       The requested company ID
     * @param userCompanyId   The user's company ID claim
     * @param userCompanyUuid The user's company UUID claim
     * @return true if there's a match, false otherwise
     */
    private boolean validateCompanyMatch(String companyId, String userCompanyId, String userCompanyUuid) {
        boolean hasAccess = false;
        String matchedClaim = null;

        // Check if the requested company ID matches either claim
        if (userCompanyId != null && companyId.equals(userCompanyId)) {
            hasAccess = true;
            matchedClaim = "company_id";
        } else if (userCompanyUuid != null && companyId.equals(userCompanyUuid)) {
            hasAccess = true;
            matchedClaim = "company_uuid";
        }

        LOGGER.debug(
            "Company access check - matched via: {}, access granted: {}",
            matchedClaim,
            hasAccess);

        return hasAccess;
    }

    /**
     * Overloaded method for backward compatibility with Long company IDs.
     * Delegates to the String-based method.
     *
     * @param authentication The current authentication object
     * @param companyId      The ID of the company whose resources are being accessed
     * @return true if the user can access the company's resources, false otherwise
     * @throws IllegalArgumentException      if companyId is null for non-system-administrator users
     * @throws MissingCompanyClaimsException if JWT token is missing required company claims for
     *                                        non-system-administrator users
     */
    public boolean canAccessCompanyResources(Authentication authentication, Long companyId) {
        return canAccessCompanyResources(authentication, companyId != null ? companyId.toString() : null);
    }

    /**
     * Checks if the current user can modify (create, update, delete) resources for the specified company.
     * This method can be extended to include role-based permissions for different operations.
     * Currently, it uses the same logic as read access but requires elevated privileges.
     *
     * @param authentication The current authentication object
     * @param companyId      The ID of the company whose resources are being modified
     * @return true if the user can modify the company's resources, false otherwise
     * @throws IllegalArgumentException      if companyId is null for non-system-administrator users
     * @throws MissingCompanyClaimsException if JWT token is missing required company claims for
     *                                        non-system-administrator users
     */
    public boolean canModifyCompanyResources(Authentication authentication, String companyId) {
        LOGGER.debug("Checking resource modification access for company: {}", companyId);

        // Must have elevated access to modify resources
        if (!hasElevatedAccess(authentication)) {
            LOGGER.debug("Authentication principal does not have elevated access, denying modification rights");
            return false;
        }

        // Must also have access to the company's resources
        return canAccessCompanyResources(authentication, companyId);
    }

    /**
     * Overloaded method for backward compatibility with Long company IDs.
     *
     * @param authentication The current authentication object
     * @param companyId      The ID of the company whose resources are being modified
     * @return true if the user can modify the company's resources, false otherwise
     * @throws IllegalArgumentException      if companyId is null for non-system-administrator users
     * @throws MissingCompanyClaimsException if JWT token is missing required company claims for
     *                                        non-system-administrator users
     */
    public boolean canModifyCompanyResources(Authentication authentication, Long companyId) {
        return canModifyCompanyResources(authentication, companyId != null ? companyId.toString() : null);
    }

    /**
     * Gets the company ID from the user's JWT token.
     * This method checks both company_id and company_uuid claims.
     * It returns the first non-null value found, prioritizing company_id.
     *
     * @param authentication The current authentication object
     * @return The company ID from the JWT token, or null if not found
     */
    public String getUserCompanyId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            // Try company_id first, then company_uuid
            String companyId = jwt.getClaimAsString(CLAIM_COMPANY_ID);
            if (companyId != null) {
                return companyId;
            }

            return jwt.getClaimAsString(CLAIM_COMPANY_UUID);
        }
        return null;
    }

    /**
     * Gets the company UUID from the user's JWT token.
     * This method specifically checks the custom:company_uuid claim.
     *
     * @param authentication The current authentication object
     * @return The company UUID from the JWT token, or null if not found
     */
    public String getUserCompanyUuid(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString(CLAIM_COMPANY_UUID);
        }
        return null;
    }

    /**
     * Checks if the current user belongs to the specified company.
     * This method checks both company_id and company_uuid claims.
     *
     * @param authentication The current authentication object
     * @param companyId      The company ID to check against (can be numeric ID or UUID)
     * @return true if the user belongs to the specified company, false otherwise
     */
    public boolean belongsToCompany(Authentication authentication, String companyId) {
        if (companyId == null) {
            return false;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            String userCompanyId = jwt.getClaimAsString(CLAIM_COMPANY_ID);
            String userCompanyUuid = jwt.getClaimAsString(CLAIM_COMPANY_UUID);

            return companyId.equals(userCompanyId) || companyId.equals(userCompanyUuid);
        }

        return false;
    }

    /**
     * Overloaded method for backward compatibility with Long company IDs.
     *
     * @param authentication The current authentication object
     * @param companyId      The company ID to check against
     * @return true if the user belongs to the specified company, false otherwise
     */
    public boolean belongsToCompany(Authentication authentication, Long companyId) {
        return belongsToCompany(authentication, companyId != null ? companyId.toString() : null);
    }
}
