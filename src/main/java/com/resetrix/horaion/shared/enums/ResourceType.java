package com.resetrix.horaion.shared.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Enumeration of resource types in the hierarchical access control system.
 * 
 * This enum defines the organizational hierarchy:
 * Company (Level 1) → Branch (Level 2) → Department (Level 3) → Various Modules (Level 4+)
 * 
 * Each resource type has:
 * - level: The hierarchy level (1 = top level)
 * - parent: The parent resource type in the hierarchy
 * - requiresParent: Whether this resource must have a parent for access validation
 * 
 * Usage Examples:
 * - COMPANY: Top-level resource, no parent required
 * - BRANCH: Must belong to a COMPANY
 * - DEPARTMENT: Must belong to a BRANCH (which belongs to a COMPANY)
 * - EMPLOYEE: Must belong to a DEPARTMENT (full hierarchy validation)
 */
public enum ResourceType {
    
    // ========================================
    // CORE ORGANIZATIONAL HIERARCHY
    // ========================================
    
    /**
     * Company - Top level of the organizational hierarchy.
     * No parent required, represents the root tenant in multi-tenant system.
     */
    COMPANY(1, null, false),
    
    /**
     * Branch - Second level, belongs to a company.
     * Represents physical or logical divisions within a company.
     */
    BRANCH(2, COMPANY, true),
    
    /**
     * Department - Third level, belongs to a branch.
     * Represents functional units within a branch.
     */
    DEPARTMENT(3, BRANCH, true),
    
    // ========================================
    // LEVEL 4 MODULES (Department-scoped)
    // ========================================
    
    /**
     * Employee management module.
     * Manages employee records within a department.
     */
    EMPLOYEE(4, DEPARTMENT, true),
    
    /**
     * Payroll management module.
     * Handles payroll processing within a department.
     */
    PAYROLL(4, DEPARTMENT, true),
    
    /**
     * Inventory management module.
     * Tracks inventory items within a department.
     */
    INVENTORY(4, DEPARTMENT, true),
    
    /**
     * Constraint management module.
     * Note: Currently implemented as global, but could be department-scoped.
     * TODO: Evaluate if constraints should be global or department-scoped.
     */
    CONSTRAINT(4, DEPARTMENT, false), // Set to false for now as it's currently global
    
    // ========================================
    // FUTURE MODULES (Examples for extension)
    // ========================================
    
    /**
     * Schedule management module.
     * Manages work schedules within a department.
     */
    SCHEDULE(4, DEPARTMENT, true),
    
    /**
     * Timesheet management module.
     * Tracks time entries within a department.
     */
    TIMESHEET(4, DEPARTMENT, true),
    
    /**
     * Asset management module.
     * Manages physical and digital assets within a department.
     */
    ASSET(4, DEPARTMENT, true);
    
    // ========================================
    // ENUM PROPERTIES AND METHODS
    // ========================================
    
    private final int level;
    private final ResourceType parent;
    private final boolean requiresParent;
    
    ResourceType(int level, ResourceType parent, boolean requiresParent) {
        this.level = level;
        this.parent = parent;
        this.requiresParent = requiresParent;
    }
    
    /**
     * Gets the hierarchy level of this resource type.
     * Level 1 is the top level (Company), higher numbers are deeper in the hierarchy.
     * 
     * @return the hierarchy level
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Gets the parent resource type in the hierarchy.
     * 
     * @return the parent resource type, or null if this is a top-level resource
     */
    public ResourceType getParent() {
        return parent;
    }
    
    /**
     * Indicates whether this resource type requires a parent for access validation.
     * 
     * @return true if parent validation is required, false otherwise
     */
    public boolean requiresParent() {
        return requiresParent;
    }
    
    /**
     * Checks if this resource type is a top-level resource (no parent).
     * 
     * @return true if this is a top-level resource, false otherwise
     */
    public boolean isTopLevel() {
        return parent == null;
    }
    
    /**
     * Gets the full hierarchy path from the root to this resource type.
     * 
     * @return list of resource types from root to this type
     */
    public List<ResourceType> getHierarchyPath() {
        if (isTopLevel()) {
            return List.of(this);
        }
        
        List<ResourceType> parentPath = parent.getHierarchyPath();
        return List.of(parentPath.toArray(new ResourceType[0]), this)
                   .stream()
                   .flatMap(Arrays::stream)
                   .toList();
    }
    
    /**
     * Finds all resource types at a specific hierarchy level.
     * 
     * @param level the hierarchy level to search for
     * @return list of resource types at the specified level
     */
    public static List<ResourceType> getResourceTypesAtLevel(int level) {
        return Arrays.stream(values())
                     .filter(type -> type.getLevel() == level)
                     .toList();
    }
    
    /**
     * Finds all child resource types of this resource type.
     * 
     * @return list of direct child resource types
     */
    public List<ResourceType> getChildren() {
        return Arrays.stream(values())
                     .filter(type -> this.equals(type.getParent()))
                     .toList();
    }
    
    /**
     * Checks if this resource type is an ancestor of the given resource type.
     * 
     * @param descendant the potential descendant resource type
     * @return true if this type is an ancestor of the descendant
     */
    public boolean isAncestorOf(ResourceType descendant) {
        if (descendant == null || descendant.isTopLevel()) {
            return false;
        }
        
        ResourceType current = descendant.getParent();
        while (current != null) {
            if (this.equals(current)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
    
    /**
     * Finds a resource type by name (case-insensitive).
     * 
     * @param name the name to search for
     * @return Optional containing the resource type if found
     */
    public static Optional<ResourceType> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return Arrays.stream(values())
                     .filter(type -> type.name().equalsIgnoreCase(name.trim()))
                     .findFirst();
    }
    
    @Override
    public String toString() {
        return String.format("ResourceType{name=%s, level=%d, parent=%s, requiresParent=%s}", 
                           name(), level, parent != null ? parent.name() : "null", requiresParent);
    }
}
