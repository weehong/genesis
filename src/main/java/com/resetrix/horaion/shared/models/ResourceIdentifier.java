package com.resetrix.horaion.shared.models;

import com.resetrix.horaion.shared.enums.ResourceType;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a unique identifier for a resource in the hierarchical access control system.
 * 
 * This class encapsulates both the resource type and its identifier,
 * supporting both numeric IDs and UUIDs as used throughout the application.
 * 
 * Usage Examples:
 * - ResourceIdentifier.of(ResourceType.COMPANY, "1")
 * - ResourceIdentifier.of(ResourceType.BRANCH, "550e8400-e29b-41d4-a716-446655440000")
 * - ResourceIdentifier.of(ResourceType.DEPARTMENT, 123L)
 */
public class ResourceIdentifier {
    
    private final ResourceType type;
    private final String id;
    
    private ResourceIdentifier(ResourceType type, String id) {
        this.type = Objects.requireNonNull(type, "Resource type cannot be null");
        this.id = Objects.requireNonNull(id, "Resource ID cannot be null");
        
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource ID cannot be empty");
        }
    }
    
    /**
     * Creates a ResourceIdentifier with a string ID.
     * 
     * @param type the resource type
     * @param id the resource ID as a string
     * @return a new ResourceIdentifier
     */
    public static ResourceIdentifier of(ResourceType type, String id) {
        return new ResourceIdentifier(type, id);
    }
    
    /**
     * Creates a ResourceIdentifier with a Long ID.
     * 
     * @param type the resource type
     * @param id the resource ID as a Long
     * @return a new ResourceIdentifier
     */
    public static ResourceIdentifier of(ResourceType type, Long id) {
        Objects.requireNonNull(id, "Resource ID cannot be null");
        return new ResourceIdentifier(type, id.toString());
    }
    
    /**
     * Creates a ResourceIdentifier with a UUID.
     * 
     * @param type the resource type
     * @param id the resource ID as a UUID
     * @return a new ResourceIdentifier
     */
    public static ResourceIdentifier of(ResourceType type, UUID id) {
        Objects.requireNonNull(id, "Resource ID cannot be null");
        return new ResourceIdentifier(type, id.toString());
    }
    
    /**
     * Gets the resource type.
     * 
     * @return the resource type
     */
    public ResourceType getType() {
        return type;
    }
    
    /**
     * Gets the resource ID as a string.
     * 
     * @return the resource ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the resource ID as a Long if it represents a numeric ID.
     * 
     * @return the resource ID as a Long
     * @throws NumberFormatException if the ID is not a valid Long
     */
    public Long getIdAsLong() {
        return Long.valueOf(id);
    }
    
    /**
     * Gets the resource ID as a UUID if it represents a UUID.
     * 
     * @return the resource ID as a UUID
     * @throws IllegalArgumentException if the ID is not a valid UUID
     */
    public UUID getIdAsUUID() {
        return UUID.fromString(id);
    }
    
    /**
     * Checks if the resource ID is numeric (can be parsed as a Long).
     * 
     * @return true if the ID is numeric, false otherwise
     */
    public boolean isNumericId() {
        try {
            Long.parseLong(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Checks if the resource ID is a UUID.
     * 
     * @return true if the ID is a valid UUID, false otherwise
     */
    public boolean isUUID() {
        try {
            UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Creates a string representation suitable for logging.
     * 
     * @return a string representation of this resource identifier
     */
    public String toLogString() {
        return String.format("%s:%s", type.name(), id);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceIdentifier that = (ResourceIdentifier) o;
        return type == that.type && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
    
    @Override
    public String toString() {
        return String.format("ResourceIdentifier{type=%s, id='%s'}", type, id);
    }
}
