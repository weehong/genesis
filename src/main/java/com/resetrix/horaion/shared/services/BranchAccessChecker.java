package com.resetrix.horaion.shared.services;

import java.util.Collection;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.resetrix.horaion.modules.branch.requests.BranchRequest;
import com.resetrix.horaion.modules.branch.responses.BranchResponse;
import com.resetrix.horaion.modules.branch.services.IBranchService;
import static com.resetrix.horaion.shared.constants.SecurityConstants.ROLE_SYSTEM_ADMINISTRATOR;

/**
 * Service for checking branch-level access authorization.
 * This service provides branch-scoped access control that can be used
 * in @PreAuthorize expressions across all controllers.
 * 
 * CRITICAL SECURITY STATUS:
 * ========================
 * 
 * ⚠️  WARNING: Branch access control is PARTIALLY IMPLEMENTED
 * 
 * CURRENT BEHAVIOR:
 * - System administrators: ✅ Can access all branches  
 * - Non-admin users: ❌ DENIED ACCESS (secure default)
 * 
 * PRODUCTION DEPLOYMENT BLOCKER:
 * This service will deny all non-admin access to branch-scoped endpoints until
 * proper company/branch membership validation is implemented.
 * 
 * IMPLEMENTATION REQUIRED:
 * Choose and implement one of these approaches in validateUserCompanyAccess():
 * 
 * 1. JWT Claims (RECOMMENDED for stateless auth)
 * 2. Database lookup (for complex user-company relationships)  
 * 3. Role-based (for simple company assignments)
 * 4. External service (for microservice architectures)
 * 
 * See detailed implementation options in validateUserCompanyAccess() method.
 */
@Service("branchAccessChecker")
public class BranchAccessChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchAccessChecker.class);
    
    private final IBranchService<BranchRequest, BranchResponse> branchService;
    
    // TODO: Replace this with proper configuration or remove once real implementation is done
    // Setting this to false provides secure defaults but will break functionality
    private static final boolean ALLOW_ACCESS_PENDING_IMPLEMENTATION = false;

    public BranchAccessChecker(IBranchService<BranchRequest, BranchResponse> branchService) {
        this.branchService = branchService;
    }

    /**
     * Checks if the current user has access to the specified branch.
     * 
     * System administrators have access to all branches.
     * Other users need proper company/branch membership validation.
     * 
     * @param authentication The current authentication object
     * @param branchId The branch ID to check access for (can be numeric or UUID string)
     * @return true if the user has access to the branch, false otherwise
     */
    public boolean hasAccess(Authentication authentication, String branchId) {
        if (authentication == null) {
            LOGGER.warn("Authentication is null, denying branch access");
            return false;
        }

        if (branchId == null || branchId.trim().isEmpty()) {
            LOGGER.warn("Branch ID is null or empty, denying access");
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

        // For non-admin users, we need to validate branch access
        return validateBranchAccessForUser(authentication, branchId);
    }

    /**
     * Validates branch access for non-admin users.
     * 
     * @param authentication The current authentication object
     * @param branchId The branch ID to validate access for
     * @return true if the user has access to the branch, false otherwise
     */
    private boolean validateBranchAccessForUser(Authentication authentication, String branchId) {
        try {
            // Try to get branch information to validate it exists
            BranchResponse branch;
            if (isNumericId(branchId)) {
                branch = branchService.getById(Long.valueOf(branchId));
            } else {
                branch = branchService.getByUuid(UUID.fromString(branchId));
            }

            // Branch exists, now validate user's access to it
            return validateUserCompanyAccess(authentication, branch);

        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid branch ID format: {}", branchId);
            return false;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid UUID format for branch ID: {}", branchId);
            return false;
        } catch (Exception e) {
            LOGGER.warn("Error retrieving branch {}: {}", branchId, e.getMessage());
            return false;
        }
    }

    /**
     * Validates if the user has access to the branch based on company membership.
     * 
     * @param authentication The current authentication object
     * @param branch The branch to validate access for
     * @return true if the user has access, false otherwise
     */
    private boolean validateUserCompanyAccess(Authentication authentication, BranchResponse branch) {
        String username = authentication.getName();
        Long companyId = branch.companyId();
        
        if (ALLOW_ACCESS_PENDING_IMPLEMENTATION) {
            LOGGER.warn("DEVELOPMENT MODE: Allowing branch access for user {} to branch {} (company {}) - THIS IS NOT SECURE FOR PRODUCTION!",
                       username, branch.branchName(), companyId);
            return true;
        }
        
        // CRITICAL SECURITY IMPLEMENTATION NEEDED:
        // 
        // The previous JWT-based company claims (company_id, company_uuid) were removed
        // during the recent security refactoring. This leaves us without a way to determine
        // which company/branches a user belongs to.
        //
        // IMPLEMENTATION OPTIONS:
        // 
        // 1. JWT Claims Approach:
        //    - Re-add company_id/company_uuid claims to JWT tokens
        //    - Validate user's company against branch's company
        //    - Example: if (userCompanyId.equals(branch.companyId())) return true;
        //
        // 2. Database Lookup Approach:
        //    - Create user-company-branch mapping table
        //    - Query user's company associations
        //    - Example: userCompanyService.hasAccessToCompany(username, companyId)
        //
        // 3. Role-based Approach:
        //    - Add company-specific roles to JWT (e.g., COMPANY_1_USER)
        //    - Check if user has role for this company
        //    - Example: hasRole("COMPANY_" + companyId + "_USER")
        //
        // 4. External Service Approach:
        //    - Call external user management service
        //    - Validate user's company membership via API
        //    - Example: userManagementClient.validateCompanyAccess(username, companyId)
        
        LOGGER.error("SECURITY ALERT: Branch access validation not implemented! User {} denied access to branch {} (company {}).",
                    username, branch.branchName(), companyId);
        
        // Secure default: deny access until proper validation is implemented
        return false;
    }

    /**
     * Checks if a string represents a numeric ID.
     * 
     * @param id The string to check
     * @return true if the string is a valid Long, false otherwise
     */
    private boolean isNumericId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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