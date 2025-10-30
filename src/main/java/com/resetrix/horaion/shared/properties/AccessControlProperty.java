package com.resetrix.horaion.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the hierarchical access control system.
 * 
 * This class allows configuration of access control behavior through
 * application properties, making it easy to adjust security settings
 * without code changes.
 * 
 * Example application.yaml configuration:
 * 
 * ```yaml
 * app:
 *   access-control:
 *     development-mode: false
 *     cache-enabled: true
 *     cache-ttl-seconds: 300
 *     default-access-mode: READ
 *     resource-patterns:
 *       COMPANY:
 *         admin-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER]
 *         write-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER, PRIVILEGED_SYSTEM_USER]
 *         read-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER, PRIVILEGED_SYSTEM_USER, USER]
 *       BRANCH:
 *         admin-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER]
 *         write-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER, PRIVILEGED_SYSTEM_USER]
 *         read-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER, PRIVILEGED_SYSTEM_USER, USER]
 *       DEPARTMENT:
 *         admin-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER]
 *         write-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER, PRIVILEGED_SYSTEM_USER]
 *         read-roles: [SYSTEM_ADMINISTRATOR, SYSTEM_OWNER, PRIVILEGED_SYSTEM_USER, USER]
 *       EMPLOYEE:
 *         admin-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN]
 *         write-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN, HR_USER]
 *         read-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN, HR_USER, MANAGER]
 *       PAYROLL:
 *         admin-roles: [SYSTEM_ADMINISTRATOR, PAYROLL_ADMIN]
 *         write-roles: [SYSTEM_ADMINISTRATOR, PAYROLL_ADMIN]
 *         read-roles: [SYSTEM_ADMINISTRATOR, PAYROLL_ADMIN, PAYROLL_USER]
 * ```
 */
@Component
@ConfigurationProperties(prefix = "app.access-control")
public class AccessControlProperty {
    
    /**
     * Enable development mode with relaxed security (NOT for production).
     * When true, allows access pending implementation.
     */
    private boolean developmentMode = false;
    
    /**
     * Enable caching of access control decisions.
     * Improves performance but may delay permission changes.
     */
    private boolean cacheEnabled = true;
    
    /**
     * Cache TTL in seconds for access control decisions.
     */
    private int cacheTtlSeconds = 300; // 5 minutes
    
    /**
     * Default access mode when none is specified.
     */
    private String defaultAccessMode = "READ";
    
    /**
     * Resource-specific access patterns.
     * Maps resource type names to their access configuration.
     */
    private Map<String, ResourceAccessPattern> resourcePatterns = new HashMap<>();
    
    // ========================================
    // GETTERS AND SETTERS
    // ========================================
    
    public boolean isDevelopmentMode() {
        return developmentMode;
    }
    
    public void setDevelopmentMode(boolean developmentMode) {
        this.developmentMode = developmentMode;
    }
    
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
    
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }
    
    public int getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }
    
    public void setCacheTtlSeconds(int cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }
    
    public String getDefaultAccessMode() {
        return defaultAccessMode;
    }
    
    public void setDefaultAccessMode(String defaultAccessMode) {
        this.defaultAccessMode = defaultAccessMode;
    }
    
    public Map<String, ResourceAccessPattern> getResourcePatterns() {
        return resourcePatterns;
    }
    
    public void setResourcePatterns(Map<String, ResourceAccessPattern> resourcePatterns) {
        this.resourcePatterns = resourcePatterns;
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    /**
     * Gets the access pattern for a specific resource type.
     * 
     * @param resourceType the resource type name
     * @return the access pattern, or a default pattern if not configured
     */
    public ResourceAccessPattern getResourcePattern(String resourceType) {
        return resourcePatterns.getOrDefault(resourceType, createDefaultPattern());
    }
    
    /**
     * Checks if a role has the required access level for a resource type.
     * 
     * @param resourceType the resource type name
     * @param role the user's role
     * @param accessMode the required access mode (READ, WRITE, DELETE, ADMIN)
     * @return true if the role has the required access
     */
    public boolean hasRoleAccess(String resourceType, String role, String accessMode) {
        ResourceAccessPattern pattern = getResourcePattern(resourceType);
        
        return switch (accessMode.toUpperCase()) {
            case "READ" -> pattern.getReadRoles().contains(role);
            case "WRITE" -> pattern.getWriteRoles().contains(role);
            case "DELETE" -> pattern.getDeleteRoles().contains(role);
            case "ADMIN" -> pattern.getAdminRoles().contains(role);
            default -> false;
        };
    }
    
    private ResourceAccessPattern createDefaultPattern() {
        ResourceAccessPattern defaultPattern = new ResourceAccessPattern();
        defaultPattern.setAdminRoles(List.of("SYSTEM_ADMINISTRATOR"));
        defaultPattern.setWriteRoles(List.of("SYSTEM_ADMINISTRATOR", "SYSTEM_OWNER", "PRIVILEGED_SYSTEM_USER"));
        defaultPattern.setReadRoles(List.of("SYSTEM_ADMINISTRATOR", "SYSTEM_OWNER", "PRIVILEGED_SYSTEM_USER", "USER"));
        defaultPattern.setDeleteRoles(List.of("SYSTEM_ADMINISTRATOR", "SYSTEM_OWNER"));
        return defaultPattern;
    }
    
    // ========================================
    // NESTED CLASS FOR RESOURCE ACCESS PATTERNS
    // ========================================
    
    /**
     * Configuration for access patterns of a specific resource type.
     */
    public static class ResourceAccessPattern {
        
        private List<String> adminRoles = List.of("SYSTEM_ADMINISTRATOR");
        private List<String> writeRoles = List.of("SYSTEM_ADMINISTRATOR", "SYSTEM_OWNER", "PRIVILEGED_SYSTEM_USER");
        private List<String> readRoles = List.of("SYSTEM_ADMINISTRATOR", "SYSTEM_OWNER", "PRIVILEGED_SYSTEM_USER", "USER");
        private List<String> deleteRoles = List.of("SYSTEM_ADMINISTRATOR", "SYSTEM_OWNER");
        
        public List<String> getAdminRoles() {
            return adminRoles;
        }
        
        public void setAdminRoles(List<String> adminRoles) {
            this.adminRoles = adminRoles;
        }
        
        public List<String> getWriteRoles() {
            return writeRoles;
        }
        
        public void setWriteRoles(List<String> writeRoles) {
            this.writeRoles = writeRoles;
        }
        
        public List<String> getReadRoles() {
            return readRoles;
        }
        
        public void setReadRoles(List<String> readRoles) {
            this.readRoles = readRoles;
        }
        
        public List<String> getDeleteRoles() {
            return deleteRoles;
        }
        
        public void setDeleteRoles(List<String> deleteRoles) {
            this.deleteRoles = deleteRoles;
        }
    }
}
