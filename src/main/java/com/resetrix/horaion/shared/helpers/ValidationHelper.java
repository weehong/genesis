package com.resetrix.horaion.shared.helpers;

import org.springframework.data.domain.Sort;

import java.util.UUID;

/**
 * Utility class that provides common validation methods used across all service layers.
 * <p>
 * This class contains static methods for validating common parameters such as pagination,
 * sort direction, IDs, and UUIDs to ensure consistency and reduce code duplication.
 * </p>
 */
public final class ValidationHelper {

    private ValidationHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates pagination parameters.
     *
     * @param page the page number (must be >= 0)
     * @param size the page size (must be > 0 and <= 1000)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Size must be > 0 and <= 1000");
        }
    }

    /**
     * Parses and validates sort direction string.
     *
     * @param sortDirection the sort direction string (can be null, empty, "ASC", or "DESC")
     * @return Sort.Direction enum value (defaults to ASC if null or empty)
     * @throws IllegalArgumentException if sortDirection is invalid
     */
    public static Sort.Direction parseSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return Sort.Direction.ASC;
        }

        String normalizedDirection = sortDirection.trim().toUpperCase();
        if ("ASC".equals(normalizedDirection)) {
            return Sort.Direction.ASC;
        } else if ("DESC".equals(normalizedDirection)) {
            return Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException("Invalid sortDirection: must be 'ASC' or 'DESC'");
        }
    }

    /**
     * Validates that an ID is not null and is a positive number.
     *
     * @param id the ID to validate
     * @throws IllegalArgumentException if ID is null or not positive
     */
    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number");
        }
    }

    /**
     * Validates that a UUID is not null.
     *
     * @param uuid the UUID to validate
     * @throws IllegalArgumentException if UUID is null
     */
    public static void validateUuid(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
    }

    /**
     * Validates pagination parameters with custom size limit.
     *
     * @param page     the page number (must be >= 0)
     * @param size     the page size (must be > 0 and <= maxSize)
     * @param maxSize  the maximum allowed page size
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static void validatePaginationParameters(int page, int size, int maxSize) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > maxSize) {
            throw new IllegalArgumentException(String.format("Size must be > 0 and <= %d", maxSize));
        }
    }

    /**
     * Validates that an ID is not null and is a positive number with custom entity name.
     *
     * @param id         the ID to validate
     * @param entityName the name of the entity for error messages
     * @throws IllegalArgumentException if ID is null or not positive
     */
    public static void validateId(Long id, String entityName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(String.format("%s ID must be a positive number", entityName));
        }
    }

    /**
     * Validates that a UUID is not null with custom entity name.
     *
     * @param uuid       the UUID to validate
     * @param entityName the name of the entity for error messages
     * @throws IllegalArgumentException if UUID is null
     */
    public static void validateUuid(UUID uuid, String entityName) {
        if (uuid == null) {
            throw new IllegalArgumentException(String.format("%s UUID cannot be null", entityName));
        }
    }
}
