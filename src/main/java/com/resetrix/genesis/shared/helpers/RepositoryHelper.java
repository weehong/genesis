package com.resetrix.genesis.shared.helpers;

import com.resetrix.genesis.shared.exceptions.ResourceNotFoundException;
import com.resetrix.genesis.shared.repositories.UuidRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public final class RepositoryHelper {

    private RepositoryHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static <T, ID> T findByIdOrThrow(
        JpaRepository<T, ID> repository,
        ID id,
        Class<T> entityClass) {

        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                entityClass.getSimpleName() + " not found with ID: " + id));
    }

    public static <T> T findByUuidOrThrow(
        UuidRepository<T> repository,
        UUID uuid,
        Class<T> entityClass) {

        return repository.findByUuid(uuid)
            .orElseThrow(() -> new ResourceNotFoundException(
                entityClass.getSimpleName() + " not found with UUID: " + uuid));
    }
}