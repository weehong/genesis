package com.resetrix.horaion.modules.constraint.controllers;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;
import com.resetrix.horaion.modules.constraint.properties.Schema;
import com.resetrix.horaion.modules.constraint.requests.ConstraintRequest;
import com.resetrix.horaion.modules.constraint.responses.ConstraintResponse;
import com.resetrix.horaion.modules.constraint.services.ConstraintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConstraintControllerUnitTest {

    @Mock
    private ConstraintService constraintService;

    @InjectMocks
    private ConstraintController constraintController;

    private ConstraintRequest request;
    private ConstraintResponse response;
    private Schema schema;
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

        // Create test request
        request = new ConstraintRequest(
            "Test Constraint",
            "Test Description",
            "Test sentence for constraint validation",
            schema
        );

        // Create test response
        response = new ConstraintResponse(
            1L,
            testUuid,
            "Test Constraint",
            "Test Description",
            "Test sentence for constraint validation",
            schema,
            false,
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now())
        );
    }

    @Test
    void findAll_shouldReturnPagedConstraints_whenDefaultParametersProvided() {
        // Arrange
        Page<ConstraintResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
        when(constraintService.getAll(0, 10, "id", "ASC")).thenReturn(page);

        // Act
        Page<ConstraintResponse> result = constraintController.findAll(0, 10, "id", "ASC");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(0);
        verify(constraintService).getAll(0, 10, "id", "ASC");
    }

    @Test
    void findAll_shouldReturnPagedConstraints_whenCustomParametersProvided() {
        // Arrange
        Page<ConstraintResponse> page = new PageImpl<>(List.of(response), PageRequest.of(1, 5), 6);
        when(constraintService.getAll(1, 5, "name", "DESC")).thenReturn(page);

        // Act
        Page<ConstraintResponse> result = constraintController.findAll(1, 5, "name", "DESC");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getNumber()).isEqualTo(1);
        verify(constraintService).getAll(1, 5, "name", "DESC");
    }

    @Test
    void findById_shouldReturnConstraint_whenValidIdProvided() {
        // Arrange
        when(constraintService.getById(1L)).thenReturn(response);

        // Act
        ConstraintResponse result = constraintController.findById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);
        verify(constraintService).getById(1L);
    }

    @Test
    void findByUuid_shouldReturnConstraint_whenValidUuidProvided() {
        // Arrange
        when(constraintService.getByUuid(testUuid)).thenReturn(response);

        // Act
        ConstraintResponse result = constraintController.findByUuid(testUuid);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);
        verify(constraintService).getByUuid(testUuid);
    }

    @Test
    void create_shouldReturnCreatedConstraint_whenValidRequestProvided() {
        // Arrange
        when(constraintService.save(any(ConstraintRequest.class))).thenReturn(response);

        // Act
        ConstraintResponse result = constraintController.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);
        verify(constraintService).save(request);
    }

    @Test
    void updateById_shouldReturnUpdatedConstraint_whenValidIdAndRequestProvided() {
        // Arrange
        when(constraintService.update(eq(1L), any(ConstraintRequest.class))).thenReturn(response);

        // Act
        ConstraintResponse result = constraintController.updateById(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);
        verify(constraintService).update(1L, request);
    }

    @Test
    void updateByUuid_shouldReturnUpdatedConstraint_whenValidUuidAndRequestProvided() {
        // Arrange
        when(constraintService.updateByUuid(eq(testUuid), any(ConstraintRequest.class))).thenReturn(response);

        // Act
        ConstraintResponse result = constraintController.updateByUuid(testUuid, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(response);
        verify(constraintService).updateByUuid(testUuid, request);
    }

    @Test
    void deleteById_shouldCallService_whenHardDeleteRequested() {
        // Arrange
        doNothing().when(constraintService).delete(1L);

        // Act
        constraintController.deleteById(1L, false);

        // Assert
        verify(constraintService).delete(1L);
    }

    @Test
    void deleteById_shouldCallSoftDeleteService_whenSoftDeleteRequested() {
        // Arrange
        doNothing().when(constraintService).softDelete(1L);

        // Act
        constraintController.deleteById(1L, true);

        // Assert
        verify(constraintService).softDelete(1L);
    }

    @Test
    void deleteByUuid_shouldCallService_whenHardDeleteRequested() {
        // Arrange
        doNothing().when(constraintService).deleteByUuid(testUuid);

        // Act
        constraintController.deleteByUuid(testUuid, false);

        // Assert
        verify(constraintService).deleteByUuid(testUuid);
    }

    @Test
    void deleteByUuid_shouldCallSoftDeleteService_whenSoftDeleteRequested() {
        // Arrange
        doNothing().when(constraintService).softDeleteByUuid(testUuid);

        // Act
        constraintController.deleteByUuid(testUuid, true);

        // Assert
        verify(constraintService).softDeleteByUuid(testUuid);
    }

    @Test
    void constructor_shouldCreateController_whenServiceProvided() {
        // Arrange & Act
        ConstraintController controller = new ConstraintController(constraintService);

        // Assert
        assertThat(controller).isNotNull();
    }
}
