package com.resetrix.horaion.shared.services;

import com.resetrix.horaion.shared.enums.AccessMode;
import com.resetrix.horaion.shared.properties.AccessControlProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Service for validating user-company relationships and access permissions.
 * 
 * This service provides multiple strategies for determining if a user has access
 * to a specific company, supporting different authentication and authorization patterns.
 * 
 * IMPLEMENTATION STRATEGIES:
 * 
 * 1. JWT Claims Strategy (Primary):
 *    - Extracts company associations from JWT token claims
 *    - Fast, stateless, scalable
 *    - Requires company claims to be added to JWT during authentication
 * 
 * 2. Role-based Strategy (Fallback):
 *    - Uses company-specific roles in JWT (e.g., COMPANY_1_USER)
 *    - Good for simple company assignments
 *    - Works with existing Cognito groups
 * 
 * 3. Database Strategy (Future):
 *    - Queries user-company relationships from database
 *    - Supports complex, dynamic relationships
 *    - Can be added when needed
 * 
 * 4. External Service Strategy (Future):
 *    - Calls external user management service
 *    - Good for microservice architectures
 *    - Can be added when needed
 */
@Service
public class UserCompanyService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCompanyService.class);
    
    private final AccessControlProperty accessControlProperty;
    
    // JWT claim names for company associations
    private static final String CLAIM_COMPANY_IDS = "company_ids";
    private static final String CLAIM_COMPANY_UUIDS = "company_uuids";
    private static final String CLAIM_USER_COMPANIES = "user_companies";
    
    public UserCompanyService(AccessControlProperty accessControlProperty) {
        this.accessControlProperty = accessControlProperty;
    }
    
    /**
     * Validates if a user has access to a specific company.
     * 
     * @param authentication The user's authentication object
     * @param companyId The company ID to check access for
     * @param accessMode The required access mode
     * @return true if the user has access, false otherwise
     */
    public boolean hasCompanyAccess(Authentication authentication, String companyId, AccessMode accessMode) {
        if (authentication == null || companyId == null || companyId.trim().isEmpty()) {
            LOGGER.warn("Invalid parameters for company access check: auth={}, companyId={}", 
                       authentication != null, companyId);
            return false;
        }
        
        String username = authentication.getName();
        LOGGER.debug("Checking {} access for user {} to company {}", accessMode, username, companyId);
        
        // Strategy 1: Try JWT Claims approach first
        if (hasCompanyAccessViaJwtClaims(authentication, companyId)) {
            LOGGER.debug("User {} has company access via JWT claims to company {}", username, companyId);
            return validateAccessMode(authentication, accessMode);
        }
        
        // Strategy 2: Try Role-based approach as fallback
        if (hasCompanyAccessViaRoles(authentication, companyId, accessMode)) {
            LOGGER.debug("User {} has company access via roles to company {}", username, companyId);
            return true;
        }
        
        // Strategy 3: Database lookup (placeholder for future implementation)
        if (hasCompanyAccessViaDatabase(authentication, companyId)) {
            LOGGER.debug("User {} has company access via database to company {}", username, companyId);
            return validateAccessMode(authentication, accessMode);
        }
        
        LOGGER.debug("User {} denied access to company {} - no valid access method found", username, companyId);
        return false;
    }
    
    /**
     * Strategy 1: Check company access via JWT claims.
     * 
     * This method looks for company associations in JWT token claims.
     * Expected claim formats:
     * - "company_ids": ["1", "2", "3"] (numeric company IDs)
     * - "company_uuids": ["uuid1", "uuid2"] (UUID company IDs)  
     * - "user_companies": [{"id": "1", "role": "admin"}, ...] (detailed company info)
     */
    private boolean hasCompanyAccessViaJwtClaims(Authentication authentication, String companyId) {
        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            LOGGER.debug("Authentication is not JWT-based, skipping JWT claims check");
            return false;
        }
        
        try {
            // Check numeric company IDs
            List<String> companyIds = jwtToken.getToken().getClaimAsStringList(CLAIM_COMPANY_IDS);
            if (companyIds != null && companyIds.contains(companyId)) {
                LOGGER.debug("Found company {} in JWT company_ids claim", companyId);
                return true;
            }
            
            // Check UUID company IDs
            List<String> companyUuids = jwtToken.getToken().getClaimAsStringList(CLAIM_COMPANY_UUIDS);
            if (companyUuids != null && companyUuids.contains(companyId)) {
                LOGGER.debug("Found company {} in JWT company_uuids claim", companyId);
                return true;
            }
            
            // Check detailed company information (future enhancement)
            // This could include role information per company
            List<Object> userCompanies = jwtToken.getToken().getClaimAsStringList(CLAIM_USER_COMPANIES);
            if (userCompanies != null) {
                // TODO: Parse detailed company objects when this format is implemented
                LOGGER.debug("Found user_companies claim but detailed parsing not yet implemented");
            }
            
        } catch (Exception e) {
            LOGGER.warn("Error extracting company claims from JWT for user {}: {}", 
                       authentication.getName(), e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Strategy 2: Check company access via role-based approach.
     * 
     * This method looks for company-specific roles in the user's authorities.
     * Expected role formats:
     * - COMPANY_{companyId}_USER (basic access)
     * - COMPANY_{companyId}_ADMIN (admin access)
     * - COMPANY_{companyId}_MANAGER (manager access)
     */
    private boolean hasCompanyAccessViaRoles(Authentication authentication, String companyId, AccessMode accessMode) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Build expected role patterns for this company
        String baseRole = "ROLE_COMPANY_" + companyId + "_";
        
        return authorities.stream()
                         .map(GrantedAuthority::getAuthority)
                         .anyMatch(role -> {
                             if (role.startsWith(baseRole)) {
                                 String roleSuffix = role.substring(baseRole.length());
                                 return isRoleSufficientForAccessMode(roleSuffix, accessMode);
                             }
                             return false;
                         });
    }
    
    /**
     * Strategy 3: Check company access via database lookup (placeholder).
     * 
     * This method would query a user-company relationship table.
     * Implementation will be added when database-based user management is needed.
     */
    private boolean hasCompanyAccessViaDatabase(Authentication authentication, String companyId) {
        // TODO: Implement database lookup when needed
        // Example implementation:
        // return userCompanyRepository.existsByUsernameAndCompanyId(authentication.getName(), companyId);
        
        LOGGER.debug("Database-based company access check not yet implemented");
        return false;
    }
    
    /**
     * Validates if the user's roles provide sufficient access for the requested access mode.
     */
    private boolean validateAccessMode(Authentication authentication, AccessMode accessMode) {
        // For JWT claims approach, we rely on the configured access patterns
        // to determine if the user's roles are sufficient for the access mode
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        return authorities.stream()
                         .map(GrantedAuthority::getAuthority)
                         .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                         .anyMatch(role -> hasRoleAccessForMode(role, accessMode));
    }
    
    /**
     * Checks if a role has access for a specific access mode using configuration.
     */
    private boolean hasRoleAccessForMode(String role, AccessMode accessMode) {
        // Use the default pattern since we don't have resource-specific context here
        AccessControlProperty.ResourceAccessPattern pattern = accessControlProperty.getResourcePattern("DEFAULT");
        
        return switch (accessMode) {
            case READ -> pattern.getReadRoles().contains(role);
            case WRITE -> pattern.getWriteRoles().contains(role);
            case DELETE -> pattern.getDeleteRoles().contains(role);
            case ADMIN -> pattern.getAdminRoles().contains(role);
        };
    }
    
    /**
     * Determines if a role suffix is sufficient for the requested access mode.
     */
    private boolean isRoleSufficientForAccessMode(String roleSuffix, AccessMode accessMode) {
        return switch (accessMode) {
            case READ -> List.of("USER", "MANAGER", "ADMIN").contains(roleSuffix);
            case WRITE -> List.of("MANAGER", "ADMIN").contains(roleSuffix);
            case DELETE, ADMIN -> List.of("ADMIN").contains(roleSuffix);
        };
    }
    
    /**
     * Gets all company IDs that a user has access to (for future use).
     */
    public List<String> getUserCompanyIds(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            return List.of();
        }
        
        try {
            List<String> companyIds = jwtToken.getToken().getClaimAsStringList(CLAIM_COMPANY_IDS);
            return Objects.requireNonNullElse(companyIds, List.of());
        } catch (Exception e) {
            LOGGER.warn("Error extracting company IDs for user {}: {}", 
                       authentication.getName(), e.getMessage());
            return List.of();
        }
    }
}
