package com.resetrix.horaion.shared.services;

import com.resetrix.horaion.shared.enums.AccessMode;
import com.resetrix.horaion.shared.enums.ResourceType;
import com.resetrix.horaion.shared.models.ResourceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.resetrix.horaion.shared.constants.SecurityConstants.ROLE_SYSTEM_ADMINISTRATOR;

/**
 * Generic resource access checker for hierarchical access control.
 * 
 * This service provides flexible, hierarchical access control that can be used
 * across all modules in the application. It supports:
 * 
 * - Hierarchical validation: Access to child resources requires access to parent resources
 * - Multiple access modes: READ, WRITE, DELETE, ADMIN
 * - Extensible design: Easy to add new resource types and access patterns
 * - Consistent security: Same access control logic across all modules
 * 
 * CURRENT IMPLEMENTATION STATUS:
 * =============================
 * 
 * ⚠️  WARNING: Company/branch membership validation is PARTIALLY IMPLEMENTED
 * 
 * CURRENT BEHAVIOR:
 * - System administrators: ✅ Can access all resources
 * - Non-admin users: ❌ DENIED ACCESS (secure default)
 * 
 * IMPLEMENTATION REQUIRED:
 * The validateUserCompanyAccess() method needs to be implemented with one of these approaches:
 * 1. JWT Claims (recommended)
 * 2. Database lookup
 * 3. Role-based access
 * 4. External service
 * 
 * See the method documentation for detailed implementation options.
 */
@Service("resourceAccessChecker")
public class ResourceAccessChecker {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceAccessChecker.class);
    
    private final ResourceHierarchyService hierarchyService;
    private final UserCompanyService userCompanyService;

    public ResourceAccessChecker(ResourceHierarchyService hierarchyService,
                               UserCompanyService userCompanyService) {
        this.hierarchyService = hierarchyService;
        this.userCompanyService = userCompanyService;
    }
    
    // ========================================
    // PUBLIC ACCESS CONTROL METHODS
    // ========================================
    
    /**
     * Checks if the current user has READ access to the specified resource.
     * 
     * @param authentication The current authentication object
     * @param resourceType The type of resource to check access for
     * @param resourceId The resource ID to check access for (can be numeric or UUID string)
     * @return true if the user has access to the resource, false otherwise
     */
    public boolean hasAccess(Authentication authentication, ResourceType resourceType, String resourceId) {
        return hasAccess(authentication, resourceType, resourceId, AccessMode.READ);
    }
    
    /**
     * Checks if the current user has the specified access mode to the resource.
     * 
     * @param authentication The current authentication object
     * @param resourceType The type of resource to check access for
     * @param resourceId The resource ID to check access for
     * @param accessMode The required access mode
     * @return true if the user has the required access, false otherwise
     */
    public boolean hasAccess(Authentication authentication, ResourceType resourceType, String resourceId, AccessMode accessMode) {
        if (authentication == null) {
            LOGGER.warn("Authentication is null, denying access to {} with ID: {}", resourceType, resourceId);
            return false;
        }
        
        if (resourceType == null) {
            LOGGER.warn("Resource type is null, denying access");
            return false;
        }
        
        if (resourceId == null || resourceId.trim().isEmpty()) {
            LOGGER.warn("Resource ID is null or empty for {}, denying access", resourceType);
            return false;
        }
        
        String username = authentication.getName();
        LOGGER.debug("Checking {} access for user {} to {} with ID: {}", 
                    accessMode, username, resourceType, resourceId);
        
        // System administrators have access to all resources
        if (isSystemAdministrator(authentication)) {
            LOGGER.debug("User {} has system admin access, allowing {} access to {} with ID: {}", 
                        username, accessMode, resourceType, resourceId);
            return true;
        }
        
        // Validate that the resource exists and get its hierarchy
        try {
            List<ResourceIdentifier> hierarchyPath = hierarchyService.getHierarchyPath(resourceType, resourceId);
            return validateHierarchicalAccess(authentication, hierarchyPath, accessMode);
        } catch (Exception e) {
            LOGGER.warn("Failed to validate access for user {} to {} with ID {}: {}", 
                       username, resourceType, resourceId, e.getMessage());
            return false;
        }
    }
    
    // ========================================
    // CONVENIENCE METHODS FOR COMMON PATTERNS
    // ========================================
    
    /**
     * Checks if the user has access to a company.
     */
    public boolean hasCompanyAccess(Authentication authentication, String companyId) {
        return hasAccess(authentication, ResourceType.COMPANY, companyId);
    }
    
    /**
     * Checks if the user has access to a branch.
     */
    public boolean hasBranchAccess(Authentication authentication, String branchId) {
        return hasAccess(authentication, ResourceType.BRANCH, branchId);
    }
    
    /**
     * Checks if the user has access to a department.
     */
    public boolean hasDepartmentAccess(Authentication authentication, String departmentId) {
        return hasAccess(authentication, ResourceType.DEPARTMENT, departmentId);
    }
    
    // Overloaded methods for Long IDs
    public boolean hasAccess(Authentication authentication, ResourceType resourceType, Long resourceId) {
        return hasAccess(authentication, resourceType, resourceId != null ? resourceId.toString() : null);
    }
    
    public boolean hasCompanyAccess(Authentication authentication, Long companyId) {
        return hasCompanyAccess(authentication, companyId != null ? companyId.toString() : null);
    }
    
    public boolean hasBranchAccess(Authentication authentication, Long branchId) {
        return hasBranchAccess(authentication, branchId != null ? branchId.toString() : null);
    }
    
    public boolean hasDepartmentAccess(Authentication authentication, Long departmentId) {
        return hasDepartmentAccess(authentication, departmentId != null ? departmentId.toString() : null);
    }
    
    // Overloaded methods for UUID IDs
    public boolean hasAccess(Authentication authentication, ResourceType resourceType, UUID resourceId) {
        return hasAccess(authentication, resourceType, resourceId != null ? resourceId.toString() : null);
    }
    
    public boolean hasCompanyAccess(Authentication authentication, UUID companyId) {
        return hasCompanyAccess(authentication, companyId != null ? companyId.toString() : null);
    }
    
    public boolean hasBranchAccess(Authentication authentication, UUID branchId) {
        return hasBranchAccess(authentication, branchId != null ? branchId.toString() : null);
    }
    
    public boolean hasDepartmentAccess(Authentication authentication, UUID departmentId) {
        return hasDepartmentAccess(authentication, departmentId != null ? departmentId.toString() : null);
    }
    
    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================
    
    private boolean isSystemAdministrator(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                         .anyMatch(authority -> ROLE_SYSTEM_ADMINISTRATOR.equals(authority.getAuthority()));
    }
    
    private boolean validateHierarchicalAccess(Authentication authentication, List<ResourceIdentifier> hierarchyPath, AccessMode accessMode) {
        String username = authentication.getName();
        
        // Find the company in the hierarchy path (should be the first element)
        ResourceIdentifier companyResource = hierarchyPath.stream()
                                                          .filter(resource -> resource.getType() == ResourceType.COMPANY)
                                                          .findFirst()
                                                          .orElse(null);
        
        if (companyResource == null) {
            LOGGER.error("No company found in hierarchy path for user {}: {}", 
                        username, hierarchyPath.stream().map(ResourceIdentifier::toLogString).toList());
            return false;
        }
        
        // Validate user's access to the company (this is where the main business logic goes)
        return validateUserCompanyAccess(authentication, companyResource.getId(), accessMode);
    }

    /**
     * Validates if the user has access to the specified company.
     *
     * CRITICAL SECURITY IMPLEMENTATION NEEDED:
     * ========================================
     *
     * This method currently uses a secure default (deny all non-admin access) until
     * proper company membership validation is implemented.
     *
     * IMPLEMENTATION OPTIONS:
     *
     * 1. JWT Claims Approach (RECOMMENDED):
     *    - Re-add company_id/company_uuid claims to JWT tokens during authentication
     *    - Extract user's company associations from JWT claims
     *    - Validate user's company against the requested company
     *    - Example:
     *      ```java
     *      JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
     *      List<String> userCompanies = jwtToken.getToken().getClaimAsStringList("company_ids");
     *      return userCompanies.contains(companyId);
     *      ```
     *
     * 2. Database Lookup Approach:
     *    - Create user-company-branch mapping table
     *    - Query user's company associations from database
     *    - Example:
     *      ```java
     *      return userCompanyService.hasAccessToCompany(username, Long.valueOf(companyId));
     *      ```
     *
     * 3. Role-based Approach:
     *    - Add company-specific roles to JWT (e.g., COMPANY_1_USER, COMPANY_1_ADMIN)
     *    - Check if user has appropriate role for the company
     *    - Example:
     *      ```java
     *      String requiredRole = "COMPANY_" + companyId + "_" + accessMode.name();
     *      return hasRole(authentication, requiredRole);
     *      ```
     *
     * 4. External Service Approach:
     *    - Call external user management service to validate company membership
     *    - Example:
     *      ```java
     *      return userManagementClient.validateCompanyAccess(username, companyId, accessMode);
     *      ```
     *
     * @param authentication The current authentication object
     * @param companyId The company ID to validate access for
     * @param accessMode The required access mode
     * @return true if the user has access, false otherwise
     */
    private boolean validateUserCompanyAccess(Authentication authentication, String companyId, AccessMode accessMode) {
        String username = authentication.getName();

        LOGGER.debug("Validating {} access for user {} to company {}", accessMode, username, companyId);

        try {
            // Use the UserCompanyService to validate access
            boolean hasAccess = userCompanyService.hasCompanyAccess(authentication, companyId, accessMode);

            if (hasAccess) {
                LOGGER.debug("User {} granted {} access to company {}", username, accessMode, companyId);
            } else {
                LOGGER.debug("User {} denied {} access to company {} - insufficient permissions",
                           username, accessMode, companyId);
            }

            return hasAccess;

        } catch (Exception e) {
            LOGGER.error("Error validating company access for user {} to company {}: {}",
                        username, companyId, e.getMessage(), e);

            // Secure default: deny access on any error
            return false;
        }
    }
}
