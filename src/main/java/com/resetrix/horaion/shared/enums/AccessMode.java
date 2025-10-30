package com.resetrix.horaion.shared.enums;

/**
 * Enumeration of access modes for resource access control.
 * 
 * This enum defines different types of access that can be requested
 * for resources in the hierarchical access control system.
 * 
 * Access modes are hierarchical:
 * - READ: Basic read access
 * - WRITE: Includes READ + modification capabilities
 * - DELETE: Includes WRITE + deletion capabilities
 * - ADMIN: Includes DELETE + administrative capabilities
 */
public enum AccessMode {
    
    /**
     * Read access - allows viewing/retrieving resource data.
     * This is the most basic level of access.
     */
    READ(1),
    
    /**
     * Write access - allows modifying resource data.
     * Includes READ access capabilities.
     */
    WRITE(2),
    
    /**
     * Delete access - allows removing resources.
     * Includes WRITE and READ access capabilities.
     */
    DELETE(3),
    
    /**
     * Administrative access - full control over resources.
     * Includes DELETE, WRITE, and READ access capabilities.
     */
    ADMIN(4);
    
    private final int level;
    
    AccessMode(int level) {
        this.level = level;
    }
    
    /**
     * Gets the access level of this mode.
     * Higher numbers indicate more privileged access.
     * 
     * @return the access level
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Checks if this access mode includes the capabilities of another access mode.
     * 
     * @param other the access mode to check against
     * @return true if this mode includes the other mode's capabilities
     */
    public boolean includes(AccessMode other) {
        return this.level >= other.level;
    }
    
    /**
     * Checks if this access mode allows read operations.
     * 
     * @return true if read access is allowed
     */
    public boolean allowsRead() {
        return includes(READ);
    }
    
    /**
     * Checks if this access mode allows write operations.
     * 
     * @return true if write access is allowed
     */
    public boolean allowsWrite() {
        return includes(WRITE);
    }
    
    /**
     * Checks if this access mode allows delete operations.
     * 
     * @return true if delete access is allowed
     */
    public boolean allowsDelete() {
        return includes(DELETE);
    }
    
    /**
     * Checks if this access mode allows administrative operations.
     * 
     * @return true if admin access is allowed
     */
    public boolean allowsAdmin() {
        return includes(ADMIN);
    }
}
