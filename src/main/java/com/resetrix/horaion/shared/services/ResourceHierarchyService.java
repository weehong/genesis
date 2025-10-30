package com.resetrix.horaion.shared.services;

import com.resetrix.horaion.modules.branch.services.IBranchService;
import com.resetrix.horaion.modules.branch.requests.BranchRequest;
import com.resetrix.horaion.modules.branch.responses.BranchResponse;
import com.resetrix.horaion.modules.company.services.ICompanyService;
import com.resetrix.horaion.modules.company.requests.CompanyRequest;
import com.resetrix.horaion.modules.company.responses.CompanyResponse;
import com.resetrix.horaion.modules.department.services.IDepartmentService;
import com.resetrix.horaion.modules.department.requests.DepartmentRequest;
import com.resetrix.horaion.modules.department.responses.DepartmentResponse;
import com.resetrix.horaion.shared.enums.ResourceType;
import com.resetrix.horaion.shared.models.ResourceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for resolving resource hierarchy relationships and validating resource existence.
 * 
 * This service provides methods to:
 * - Resolve the full hierarchy path for any resource
 * - Validate that resources exist and are properly linked
 * - Extract parent resource information from child resources
 * 
 * The service acts as a bridge between the generic access control system
 * and the specific service implementations for each resource type.
 */
@Service
public class ResourceHierarchyService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHierarchyService.class);
    
    private final ICompanyService<CompanyRequest, CompanyResponse> companyService;
    private final IBranchService<BranchRequest, BranchResponse> branchService;
    private final IDepartmentService<DepartmentRequest, DepartmentResponse> departmentService;
    
    public ResourceHierarchyService(
            ICompanyService<CompanyRequest, CompanyResponse> companyService,
            IBranchService<BranchRequest, BranchResponse> branchService,
            IDepartmentService<DepartmentRequest, DepartmentResponse> departmentService) {
        this.companyService = companyService;
        this.branchService = branchService;
        this.departmentService = departmentService;
    }
    
    /**
     * Gets the complete hierarchy path for a resource, from the root company down to the specified resource.
     * 
     * @param resourceType the type of the resource
     * @param resourceId the ID of the resource
     * @return list of ResourceIdentifiers representing the full hierarchy path
     * @throws IllegalArgumentException if the resource doesn't exist or hierarchy is invalid
     */
    public List<ResourceIdentifier> getHierarchyPath(ResourceType resourceType, String resourceId) {
        LOGGER.debug("Resolving hierarchy path for {} with ID: {}", resourceType, resourceId);
        
        List<ResourceIdentifier> path = new ArrayList<>();
        
        try {
            switch (resourceType) {
                case COMPANY -> {
                    // Company is the root, so path is just the company itself
                    validateCompanyExists(resourceId);
                    path.add(ResourceIdentifier.of(ResourceType.COMPANY, resourceId));
                }
                case BRANCH -> {
                    // Path: Company -> Branch
                    BranchResponse branch = getBranch(resourceId);
                    path.add(ResourceIdentifier.of(ResourceType.COMPANY, branch.companyId().toString()));
                    path.add(ResourceIdentifier.of(ResourceType.BRANCH, resourceId));
                }
                case DEPARTMENT -> {
                    // Path: Company -> Branch -> Department
                    DepartmentResponse department = getDepartment(resourceId);
                    BranchResponse branch = getBranch(department.branchId().toString());
                    path.add(ResourceIdentifier.of(ResourceType.COMPANY, branch.companyId().toString()));
                    path.add(ResourceIdentifier.of(ResourceType.BRANCH, department.branchId().toString()));
                    path.add(ResourceIdentifier.of(ResourceType.DEPARTMENT, resourceId));
                }
                default -> {
                    // For future modules at level 4+, assume they belong to a department
                    // This is a placeholder - specific implementations should be added as modules are created
                    if (resourceType.getLevel() >= 4 && resourceType.getParent() == ResourceType.DEPARTMENT) {
                        // For now, we'll need the department ID to be provided or resolved
                        // This is a limitation that will be addressed when specific modules are implemented
                        throw new UnsupportedOperationException(
                            String.format("Hierarchy resolution for %s is not yet implemented. " +
                                        "This will be added when the specific module is created.", resourceType));
                    } else {
                        throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
                    }
                }
            }
            
            LOGGER.debug("Resolved hierarchy path for {}: {}", 
                        ResourceIdentifier.of(resourceType, resourceId).toLogString(),
                        path.stream().map(ResourceIdentifier::toLogString).toList());
            
            return path;
            
        } catch (Exception e) {
            LOGGER.error("Failed to resolve hierarchy path for {} with ID {}: {}", 
                        resourceType, resourceId, e.getMessage());
            throw new IllegalArgumentException(
                String.format("Unable to resolve hierarchy for %s with ID %s: %s", 
                            resourceType, resourceId, e.getMessage()), e);
        }
    }
    
    /**
     * Gets the parent resource identifier for a given resource.
     * 
     * @param resourceType the type of the resource
     * @param resourceId the ID of the resource
     * @return Optional containing the parent resource identifier, or empty if no parent
     */
    public Optional<ResourceIdentifier> getParentResource(ResourceType resourceType, String resourceId) {
        if (resourceType.isTopLevel()) {
            return Optional.empty();
        }
        
        try {
            List<ResourceIdentifier> path = getHierarchyPath(resourceType, resourceId);
            // Parent is the second-to-last element in the path
            if (path.size() >= 2) {
                return Optional.of(path.get(path.size() - 2));
            }
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.warn("Failed to get parent resource for {} with ID {}: {}", 
                       resourceType, resourceId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Validates that a resource exists and returns basic information about it.
     * 
     * @param resourceType the type of the resource
     * @param resourceId the ID of the resource
     * @return true if the resource exists, false otherwise
     */
    public boolean resourceExists(ResourceType resourceType, String resourceId) {
        try {
            switch (resourceType) {
                case COMPANY -> {
                    validateCompanyExists(resourceId);
                    return true;
                }
                case BRANCH -> {
                    getBranch(resourceId);
                    return true;
                }
                case DEPARTMENT -> {
                    getDepartment(resourceId);
                    return true;
                }
                default -> {
                    // For future modules, return false until implemented
                    LOGGER.debug("Resource existence check for {} not implemented yet", resourceType);
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Resource {} with ID {} does not exist: {}", resourceType, resourceId, e.getMessage());
            return false;
        }
    }
    
    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================
    
    private void validateCompanyExists(String companyId) {
        if (isNumericId(companyId)) {
            companyService.getById(Long.valueOf(companyId));
        } else {
            companyService.getByUuid(UUID.fromString(companyId));
        }
    }
    
    private BranchResponse getBranch(String branchId) {
        if (isNumericId(branchId)) {
            return branchService.getById(Long.valueOf(branchId));
        } else {
            return branchService.getByUuid(UUID.fromString(branchId));
        }
    }
    
    private DepartmentResponse getDepartment(String departmentId) {
        if (isNumericId(departmentId)) {
            return departmentService.getById(Long.valueOf(departmentId));
        } else {
            return departmentService.getByUuid(UUID.fromString(departmentId));
        }
    }
    
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
}
