package com.resetrix.genesis.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface UuidRepository<T> extends JpaRepository<T, Long> {
    Optional<T> findByUuid(UUID uuid);
}