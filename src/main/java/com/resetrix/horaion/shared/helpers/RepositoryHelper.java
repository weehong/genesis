package com.resetrix.horaion.shared.helpers;

import com.resetrix.horaion.shared.exceptions.ResourceNotFoundException;
import com.resetrix.horaion.shared.repositories.UuidRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Utility class providing common repository operations with standardized error handling.
 *
 * <p>This helper class encapsulates common patterns for entity retrieval from repositories,
 * ensuring consistent exception handling when entities are not found. It provides methods
 * for both ID-based and UUID-based entity lookups with automatic exception throwing.</p>
 *
 * <p>All methods in this class throw {@link ResourceNotFoundException} when the requested
 * entity cannot be found, providing a consistent error handling approach across the application.</p>
 *
 * @author Genesis Team
 * @since 1.0.0
 */
public final class RepositoryHelper {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException always, as this is a utility class
     */
    private RepositoryHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Finds an entity by its ID or throws a ResourceNotFoundException if not found.
     *
     * <p>This method provides a standardized way to retrieve entities by their primary key
     * with automatic exception handling. It eliminates the need for repetitive null checks
     * and exception handling in service layers.</p>
     *
     * @param <T> the entity type
     * @param <ID> the ID type (typically Long, Integer, or String)
     * @param repository the JPA repository to search in
     * @param id the ID of the entity to find
     * @param entityClass the class of the entity (used for error message generation)
     * @return the found entity
     * @throws ResourceNotFoundException if no entity with the given ID exists
     * @throws IllegalArgumentException if any parameter is null
     *
     * @example
     * <pre>
     * Company company = RepositoryHelper.findByIdOrThrow(
     *     companyRepository,
     *     companyId,
     *     Company.class
     * );
     * </pre>
     */
    public static <T, ID> T findByIdOrThrow(
        JpaRepository<T, ID> repository,
        ID id,
        Class<T> entityClass) {

        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                entityClass.getSimpleName() + " not found with ID: " + id));
    }

    /**
     * Finds an entity by its UUID or throws a ResourceNotFoundException if not found.
     *
     * <p>This method provides a standardized way to retrieve entities by their UUID
     * with automatic exception handling. It is specifically designed for repositories
     * that implement the {@link UuidRepository} interface and support UUID-based lookups.</p>
     *
     * <p>UUIDs are commonly used as alternative identifiers that are globally unique
     * and can be safely exposed in public APIs without revealing internal ID sequences.</p>
     *
     * @param <T> the entity type
     * @param repository the UUID repository to search in
     * @param uuid the UUID of the entity to find
     * @param entityClass the class of the entity (used for error message generation)
     * @return the found entity
     * @throws ResourceNotFoundException if no entity with the given UUID exists
     * @throws IllegalArgumentException if any parameter is null
     *
     * @example
     * <pre>
     * UUID companyUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
     * Company company = RepositoryHelper.findByUuidOrThrow(
     *     companyRepository,
     *     companyUuid,
     *     Company.class
     * );
     * </pre>
     */
    public static <T> T findByUuidOrThrow(
        UuidRepository<T> repository,
        UUID uuid,
        Class<T> entityClass) {

        return repository.findByUuid(uuid)
            .orElseThrow(() -> new ResourceNotFoundException(
                entityClass.getSimpleName() + " not found with UUID: " + uuid));
    }
}