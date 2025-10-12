package com.resetrix.genesis.shared.services.contracts;

import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IGenericService<T, K> {

    Page<K> getAll(int page, int size, String sortBy, String sortDirection);

    K getById(Long id);

    K getByUuid(UUID uuid);

    K save(T entity);

    K update(Long id, T entity);

    K updateByUuid(UUID uuid, T entity);

    void softDelete(Long id);

    void softDeleteByUuid(UUID uuid);

    void delete(Long id);

    void deleteByUuid(UUID uuid);
}
