package com.resetrix.horaion.shared.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

import static com.resetrix.horaion.shared.constants.SecurityConstants.ROLE_SYSTEM_ADMINISTRATOR;

/**
 * Service for checking branch-level access authorization.
 * This service provides branch-scoped access control that can be used
 * in @PreAuthorize expressions across all controllers.
 */
@Service("branchAccessChecker")
public class BranchAccessChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchAccessChecker.class);

    /**
     * Checks if the current user has access to the specified branch.
     * 
     * System administrators have access to all branches.
     * Other users would need company/branch-specific validation logic.
     * 
     * @param authentication The current authentication object
     * @param branchId The branch ID to check access for (can be numeric or UUID string)
     * @return true if the user has access to the branch, false otherwise
     */
    public boolean hasAccess(Authentication authentication, String branchId) {
        if (authentication == null) {
            LOGGER.debug("Authentication is null, denying branch access");
            return false;
        }

        // System administrators can access any branch
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isSystemAdmin = authorities.stream()
            .anyMatch(authority -> ROLE_SYSTEM_ADMINISTRATOR.equals(authority.getAuthority()));

        if (isSystemAdmin) {
            LOGGER.debug("User {} has system admin access, allowing access to branch {}", 
                authentication.getName(), branchId);
            return true;
        }

        // For now, allow all authenticated users with valid roles
        // TODO: Implement actual branch-level access control based on user's company/branch associations
        // This could involve checking JWT claims or database relationships
        LOGGER.debug("User {} requesting access to branch {} - allowing based on role-based access", 
            authentication.getName(), branchId);
        
        return true;
    }

    /**
     * Overloaded method for Long branch IDs.
     * 
     * @param authentication The current authentication object
     * @param branchId The branch ID to check access for
     * @return true if the user has access to the branch, false otherwise
     */
    public boolean hasAccess(Authentication authentication, Long branchId) {
        return hasAccess(authentication, branchId != null ? branchId.toString() : null);
    }

    /**
     * Overloaded method for UUID branch IDs.
     * 
     * @param authentication The current authentication object
     * @param branchId The branch UUID to check access for
     * @return true if the user has access to the branch, false otherwise
     */
    public boolean hasAccess(Authentication authentication, UUID branchId) {
        return hasAccess(authentication, branchId != null ? branchId.toString() : null);
    }
}