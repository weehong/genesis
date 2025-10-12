package com.resetrix.genesis.shared.helpers;

import com.resetrix.genesis.shared.exceptions.ResourceNotFoundException;
import com.resetrix.genesis.shared.repositories.UuidRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryHelperTest {

    @Mock
    private JpaRepository<TestEntity, Long> jpaRepository;

    @Mock
    private UuidRepository<TestEntity> uuidRepository;

    @Test
    void constructor_shouldThrowIllegalStateException_whenInstantiated() throws Exception {
        // Arrange
        Constructor<RepositoryHelper> constructor = RepositoryHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert
        InvocationTargetException exception = assertThrows(
            InvocationTargetException.class,
            constructor::newInstance
        );

        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertEquals(IllegalStateException.class, cause.getClass());
        assertEquals("Utility class", cause.getMessage());
    }

    @Test
    void findByIdOrThrow_shouldReturnEntity_whenEntityExists() {
        // Arrange
        Long id = 1L;
        TestEntity expectedEntity = new TestEntity(id, "Test Entity");
        
        when(jpaRepository.findById(id)).thenReturn(Optional.of(expectedEntity));

        // Act
        TestEntity result = RepositoryHelper.findByIdOrThrow(jpaRepository, id, TestEntity.class);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEntity, result);
        assertEquals(id, result.getId());
        assertEquals("Test Entity", result.getName());
    }

    @Test
    void findByIdOrThrow_shouldThrowResourceNotFoundException_whenEntityNotFound() {
        // Arrange
        Long id = 999L;
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> RepositoryHelper.findByIdOrThrow(jpaRepository, id, TestEntity.class)
        );

        assertEquals("TestEntity not found with ID: 999", exception.getMessage());
    }

    @Test
    void findByUuidOrThrow_shouldReturnEntity_whenEntityExists() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        TestEntity expectedEntity = new TestEntity(1L, "Test Entity");
        expectedEntity.setUuid(uuid);
        
        when(uuidRepository.findByUuid(uuid)).thenReturn(Optional.of(expectedEntity));

        // Act
        TestEntity result = RepositoryHelper.findByUuidOrThrow(uuidRepository, uuid, TestEntity.class);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEntity, result);
        assertEquals(uuid, result.getUuid());
        assertEquals("Test Entity", result.getName());
    }

    @Test
    void findByUuidOrThrow_shouldThrowResourceNotFoundException_whenEntityNotFound() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        when(uuidRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> RepositoryHelper.findByUuidOrThrow(uuidRepository, uuid, TestEntity.class)
        );

        assertEquals("TestEntity not found with UUID: " + uuid, exception.getMessage());
    }

    @Test
    void findByIdOrThrow_shouldHandleNullId() {
        // Arrange
        Long nullId = null;
        when(jpaRepository.findById(nullId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> RepositoryHelper.findByIdOrThrow(jpaRepository, nullId, TestEntity.class)
        );

        assertEquals("TestEntity not found with ID: null", exception.getMessage());
    }

    @Test
    void findByUuidOrThrow_shouldHandleNullUuid() {
        // Arrange
        UUID nullUuid = null;
        when(uuidRepository.findByUuid(nullUuid)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> RepositoryHelper.findByUuidOrThrow(uuidRepository, nullUuid, TestEntity.class)
        );

        assertEquals("TestEntity not found with UUID: null", exception.getMessage());
    }

    // Test entity class for testing purposes
    private static class TestEntity {
        private Long id;
        private String name;
        private UUID uuid;

        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }
}
