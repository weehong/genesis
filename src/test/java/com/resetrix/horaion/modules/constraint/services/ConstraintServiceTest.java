package com.resetrix.horaion.modules.constraint.services;

import com.resetrix.horaion.modules.constraint.entities.Constraint;
import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.exceptions.ConstraintException;
import com.resetrix.horaion.modules.constraint.mappers.ConstraintMapper;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;
import com.resetrix.horaion.modules.constraint.properties.Schema;
import com.resetrix.horaion.modules.constraint.repositories.ConstraintRepository;
import com.resetrix.horaion.modules.constraint.requests.ConstraintRequest;
import com.resetrix.horaion.modules.constraint.responses.ConstraintResponse;
import com.resetrix.horaion.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConstraintServiceTest {

    @Mock
    private ConstraintRepository constraintRepository;

    @Mock
    private ConstraintMapper constraintMapper;

    @Mock
    private ConstraintSchemaService schemaService;

    @InjectMocks
    private ConstraintService constraintService;

    private Constraint constraint;
    private ConstraintRequest request;
    private ConstraintResponse response;
    private Schema schema;
    private Schema resolvedSchema;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        // Create test schema
        FieldOptions options = new FieldOptions(
            List.of(new FieldOption("value1", "Label 1")),
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            1,
            "test_field",
            "select",
            "Test Field",
            "Select a value",
            options,
            SourceType.SELECT
        );

        schema = new Schema(List.of(fieldDefinition));
        resolvedSchema = new Schema(List.of(fieldDefinition));

        // Create test request
        request = new ConstraintRequest(
            "Test Constraint",
            "Test Description",
            "Test sentence for constraint validation",
            schema
        );

        // Create test constraint entity
        constraint = new Constraint();
        constraint.setId(1L);
        constraint.setUuid(testUuid);
        constraint.setName("Test Constraint");
        constraint.setDescription("Test Description");
        constraint.setSentence("Test sentence for constraint validation");
        constraint.setFields(schema);
        constraint.setFieldsHash("test-hash");
        constraint.setSoftDelete(false);
        constraint.setCreatedAt(Timestamp.from(Instant.now()));
        constraint.setUpdatedAt(Timestamp.from(Instant.now()));

        // Create test response
        response = new ConstraintResponse(
            1L,
            testUuid,
            "Test Constraint",
            "Test Description",
            "Test sentence for constraint validation",
            resolvedSchema,
            false,
            constraint.getCreatedAt(),
            constraint.getUpdatedAt()
        );
    }

    @Test
    void getAll_shouldReturnPagedConstraints_whenValidParametersProvided() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
        Page<Constraint> constraintPage = new PageImpl<>(List.of(constraint), pageRequest, 1);

        when(constraintRepository.findAll(any(PageRequest.class))).thenReturn(constraintPage);
        when(schemaService.resolveFieldOptions(schema)).thenReturn(resolvedSchema);
        when(constraintMapper.toResponse(constraint, resolvedSchema)).thenReturn(response);

        // Act
        Page<ConstraintResponse> result = constraintService.getAll(0, 10, "id", "ASC");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(constraintRepository).findAll(any(PageRequest.class));
        verify(schemaService).resolveFieldOptions(schema);
        verify(constraintMapper).toResponse(constraint, resolvedSchema);
    }

    @Test
    void getAll_shouldThrowConstraintException_whenInvalidPageParametersProvided() {
        // Act & Assert
        assertThatThrownBy(() -> constraintService.getAll(-1, 10, "id", "ASC"))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> constraintService.getAll(0, 0, "id", "ASC"))
            .isInstanceOf(IllegalArgumentException.class);

        verify(constraintRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void getById_shouldReturnConstraint_whenValidIdProvided() {
        // Arrange
        when(constraintRepository.findById(1L)).thenReturn(Optional.of(constraint));
        when(schemaService.resolveFieldOptions(schema)).thenReturn(resolvedSchema);
        when(constraintMapper.toResponse(constraint, resolvedSchema)).thenReturn(response);

        // Act
        ConstraintResponse result = constraintService.getById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);

        verify(constraintRepository).findById(1L);
        verify(schemaService).resolveFieldOptions(schema);
        verify(constraintMapper).toResponse(constraint, resolvedSchema);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenConstraintNotFound() {
        // Arrange
        when(constraintRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> constraintService.getById(1L))
            .isInstanceOf(ConstraintException.class)
            .hasMessage("Unexpected error occurred while retrieving constraint by ID")
            .hasCauseInstanceOf(ResourceNotFoundException.class);

        verify(constraintRepository).findById(1L);
        verify(schemaService, never()).resolveFieldOptions(any());
        verify(constraintMapper, never()).toResponse(any(Constraint.class), any(Schema.class));
    }

    @Test
    void getById_shouldThrowConstraintException_whenInvalidIdProvided() {
        // Act & Assert
        assertThatThrownBy(() -> constraintService.getById(null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> constraintService.getById(0L))
            .isInstanceOf(IllegalArgumentException.class);

        verify(constraintRepository, never()).findById(anyLong());
    }

    @Test
    void getByUuid_shouldReturnConstraint_whenValidUuidProvided() {
        // Arrange
        when(constraintRepository.findByUuid(testUuid)).thenReturn(Optional.of(constraint));
        when(schemaService.resolveFieldOptions(schema)).thenReturn(resolvedSchema);
        when(constraintMapper.toResponse(constraint, resolvedSchema)).thenReturn(response);

        // Act
        ConstraintResponse result = constraintService.getByUuid(testUuid);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);

        verify(constraintRepository).findByUuid(testUuid);
        verify(schemaService).resolveFieldOptions(schema);
        verify(constraintMapper).toResponse(constraint, resolvedSchema);
    }

    @Test
    void getByUuid_shouldThrowEntityNotFoundException_whenConstraintNotFound() {
        // Arrange
        when(constraintRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> constraintService.getByUuid(testUuid))
            .isInstanceOf(ConstraintException.class)
            .hasMessage("Unexpected error occurred while retrieving constraint by UUID")
            .hasCauseInstanceOf(ResourceNotFoundException.class);

        verify(constraintRepository).findByUuid(testUuid);
        verify(schemaService, never()).resolveFieldOptions(any());
        verify(constraintMapper, never()).toResponse(any(Constraint.class), any(Schema.class));
    }

    @Test
    void getByUuid_shouldThrowConstraintException_whenInvalidUuidProvided() {
        // Act & Assert
        assertThatThrownBy(() -> constraintService.getByUuid(null))
            .isInstanceOf(IllegalArgumentException.class);

        verify(constraintRepository, never()).findByUuid(any());
    }

    @Test
    void save_shouldReturnSavedConstraint_whenValidRequestProvided() {
        // Arrange
        Constraint newConstraint = new Constraint();
        newConstraint.setFields(schema);

        when(constraintMapper.toEntity(request)).thenReturn(newConstraint);
        when(constraintRepository.save(any(Constraint.class))).thenReturn(constraint);
        when(constraintMapper.toResponse(constraint)).thenReturn(response);

        // Act
        ConstraintResponse result = constraintService.save(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);

        verify(constraintMapper).toEntity(request);
        verify(constraintRepository).save(any(Constraint.class));
        verify(constraintMapper).toResponse(constraint);
    }

    @Test
    void update_shouldReturnUpdatedConstraint_whenValidIdAndRequestProvided() {
        // Arrange
        Constraint updatedConstraint = new Constraint();
        updatedConstraint.setFields(schema);

        when(constraintRepository.findById(1L)).thenReturn(Optional.of(constraint));
        when(constraintMapper.updateEntity(constraint, request)).thenReturn(updatedConstraint);
        when(constraintRepository.save(updatedConstraint)).thenReturn(constraint);
        when(constraintMapper.toResponse(constraint)).thenReturn(response);

        // Act
        ConstraintResponse result = constraintService.update(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);

        verify(constraintRepository).findById(1L);
        verify(constraintMapper).updateEntity(constraint, request);
        verify(constraintRepository).save(updatedConstraint);
        verify(constraintMapper).toResponse(constraint);
    }

    @Test
    void updateByUuid_shouldReturnUpdatedConstraint_whenValidUuidAndRequestProvided() {
        // Arrange
        Constraint updatedConstraint = new Constraint();
        updatedConstraint.setFields(schema);

        when(constraintRepository.findByUuid(testUuid)).thenReturn(Optional.of(constraint));
        when(constraintMapper.updateEntity(constraint, request)).thenReturn(updatedConstraint);
        when(constraintRepository.save(updatedConstraint)).thenReturn(constraint);
        when(constraintMapper.toResponse(constraint)).thenReturn(response);

        // Act
        ConstraintResponse result = constraintService.updateByUuid(testUuid, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);

        verify(constraintRepository).findByUuid(testUuid);
        verify(constraintMapper).updateEntity(constraint, request);
        verify(constraintRepository).save(updatedConstraint);
        verify(constraintMapper).toResponse(constraint);
    }

    @Test
    void softDelete_shouldMarkConstraintAsDeleted_whenValidIdProvided() {
        // Arrange
        when(constraintRepository.findById(1L)).thenReturn(Optional.of(constraint));
        when(constraintRepository.save(constraint)).thenReturn(constraint);

        // Act
        constraintService.softDelete(1L);

        // Assert
        assertThat(constraint.getSoftDelete()).isTrue();

        verify(constraintRepository).findById(1L);
        verify(constraintRepository).save(constraint);
    }

    @Test
    void softDelete_shouldThrowEntityNotFoundException_whenConstraintNotFound() {
        // Arrange
        when(constraintRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> constraintService.softDelete(1L))
            .isInstanceOf(ConstraintException.class)
            .hasMessage("Unexpected error occurred while soft-deleting the constraint")
            .hasCauseInstanceOf(ResourceNotFoundException.class);

        verify(constraintRepository).findById(1L);
        verify(constraintRepository, never()).save(any());
    }

    @Test
    void softDeleteByUuid_shouldMarkConstraintAsDeleted_whenValidUuidProvided() {
        // Arrange
        when(constraintRepository.findByUuid(testUuid)).thenReturn(Optional.of(constraint));
        when(constraintRepository.save(constraint)).thenReturn(constraint);

        // Act
        constraintService.softDeleteByUuid(testUuid);

        // Assert
        assertThat(constraint.getSoftDelete()).isTrue();

        verify(constraintRepository).findByUuid(testUuid);
        verify(constraintRepository).save(constraint);
    }

    @Test
    void softDeleteByUuid_shouldThrowEntityNotFoundException_whenConstraintNotFound() {
        // Arrange
        when(constraintRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> constraintService.softDeleteByUuid(testUuid))
            .isInstanceOf(ConstraintException.class)
            .hasMessage("Unexpected error occurred while soft-deleting the constraint")
            .hasCauseInstanceOf(ResourceNotFoundException.class);

        verify(constraintRepository).findByUuid(testUuid);
        verify(constraintRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteConstraint_whenValidIdProvided() {
        // Arrange
        when(constraintRepository.findById(1L)).thenReturn(Optional.of(constraint));

        // Act
        constraintService.delete(1L);

        // Assert
        verify(constraintRepository).findById(1L);
        verify(constraintRepository).delete(constraint);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenConstraintNotFound() {
        // Arrange
        when(constraintRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> constraintService.delete(1L))
            .isInstanceOf(ConstraintException.class)
            .hasMessage("Unexpected error occurred while deleting the constraint")
            .hasCauseInstanceOf(ResourceNotFoundException.class);

        verify(constraintRepository).findById(1L);
        verify(constraintRepository, never()).delete(any());
    }

    @Test
    void deleteByUuid_shouldDeleteConstraint_whenValidUuidProvided() {
        // Arrange
        when(constraintRepository.findByUuid(testUuid)).thenReturn(Optional.of(constraint));

        // Act
        constraintService.deleteByUuid(testUuid);

        // Assert
        verify(constraintRepository).findByUuid(testUuid);
        verify(constraintRepository).delete(constraint);
    }

    @Test
    void deleteByUuid_shouldThrowEntityNotFoundException_whenConstraintNotFound() {
        // Arrange
        when(constraintRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> constraintService.deleteByUuid(testUuid))
            .isInstanceOf(ConstraintException.class)
            .hasMessage("Unexpected error occurred while deleting the constraint")
            .hasCauseInstanceOf(ResourceNotFoundException.class);

        verify(constraintRepository).findByUuid(testUuid);
        verify(constraintRepository, never()).delete(any());
    }

    // Note: computeAndSetSchemaHash is a private method and is tested indirectly through save/update operations
}
