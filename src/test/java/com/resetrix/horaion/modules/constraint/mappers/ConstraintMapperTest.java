package com.resetrix.horaion.modules.constraint.mappers;

import com.resetrix.horaion.modules.constraint.entities.Constraint;
import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;
import com.resetrix.horaion.modules.constraint.properties.Schema;
import com.resetrix.horaion.modules.constraint.requests.ConstraintRequest;
import com.resetrix.horaion.modules.constraint.responses.ConstraintResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintMapperTest {

    private ConstraintMapper constraintMapper;
    private Constraint constraint;
    private ConstraintRequest request;
    private Schema schema;
    private Schema resolvedSchema;
    private UUID testUuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @BeforeEach
    void setUp() {
        constraintMapper = new ConstraintMapper();
        testUuid = UUID.randomUUID();
        createdAt = Timestamp.from(Instant.now().minusSeconds(3600));
        updatedAt = Timestamp.from(Instant.now());

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

        // Create resolved schema (different from original)
        FieldOptions resolvedOptions = new FieldOptions(
            List.of(
                new FieldOption("resolved1", "Resolved Label 1"),
                new FieldOption("resolved2", "Resolved Label 2")
            ),
            1,
            3
        );

        FieldDefinition resolvedFieldDefinition = new FieldDefinition(
            1,
            "test_field",
            "select",
            "Test Field",
            "Select a value",
            resolvedOptions,
            SourceType.DATABASE
        );

        resolvedSchema = new Schema(List.of(resolvedFieldDefinition));

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
        constraint.setCreatedAt(createdAt);
        constraint.setUpdatedAt(updatedAt);
    }

    @Test
    void toResponse_shouldMapConstraintToResponse_whenConstraintProvided() {
        // Act
        ConstraintResponse result = constraintMapper.toResponse(constraint);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(constraint.getId());
        assertThat(result.uuid()).isEqualTo(constraint.getUuid());
        assertThat(result.name()).isEqualTo(constraint.getName());
        assertThat(result.description()).isEqualTo(constraint.getDescription());
        assertThat(result.sentence()).isEqualTo(constraint.getSentence());
        assertThat(result.schemas()).isEqualTo(constraint.getFields());
        assertThat(result.softDelete()).isEqualTo(constraint.getSoftDelete());
        assertThat(result.createdAt()).isEqualTo(constraint.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(constraint.getUpdatedAt());
    }

    @Test
    void toResponse_shouldMapConstraintToResponseWithResolvedSchema_whenConstraintAndSchemaProvided() {
        // Act
        ConstraintResponse result = constraintMapper.toResponse(constraint, resolvedSchema);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(constraint.getId());
        assertThat(result.uuid()).isEqualTo(constraint.getUuid());
        assertThat(result.name()).isEqualTo(constraint.getName());
        assertThat(result.description()).isEqualTo(constraint.getDescription());
        assertThat(result.sentence()).isEqualTo(constraint.getSentence());
        assertThat(result.schemas()).isEqualTo(resolvedSchema);
        assertThat(result.softDelete()).isEqualTo(constraint.getSoftDelete());
        assertThat(result.createdAt()).isEqualTo(constraint.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(constraint.getUpdatedAt());
    }

    @Test
    void toResponse_shouldHandleNullValues_whenConstraintHasNullFields() {
        // Arrange
        Constraint constraintWithNulls = new Constraint();
        constraintWithNulls.setId(2L);
        constraintWithNulls.setUuid(testUuid);
        constraintWithNulls.setName("Test");
        constraintWithNulls.setDescription(null);
        constraintWithNulls.setSentence("Test sentence");
        constraintWithNulls.setFields(null);
        constraintWithNulls.setSoftDelete(true);
        constraintWithNulls.setCreatedAt(null);
        constraintWithNulls.setUpdatedAt(null);

        // Act
        ConstraintResponse result = constraintMapper.toResponse(constraintWithNulls);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.uuid()).isEqualTo(testUuid);
        assertThat(result.name()).isEqualTo("Test");
        assertThat(result.description()).isNull();
        assertThat(result.sentence()).isEqualTo("Test sentence");
        assertThat(result.schemas()).isNull();
        assertThat(result.softDelete()).isTrue();
        assertThat(result.createdAt()).isNull();
        assertThat(result.updatedAt()).isNull();
    }

    @Test
    void toEntity_shouldCreateNewConstraintFromRequest_whenRequestProvided() {
        // Act
        Constraint result = constraintMapper.toEntity(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // New entity should not have ID
        assertThat(result.getUuid()).isNull(); // UUID will be set by @PrePersist
        assertThat(result.getName()).isEqualTo(request.name());
        assertThat(result.getDescription()).isEqualTo(request.description());
        assertThat(result.getSentence()).isEqualTo(request.sentence());
        assertThat(result.getFields()).isEqualTo(request.schema());
        assertThat(result.getFieldsHash()).isNull(); // Hash will be set by service
        assertThat(result.getSoftDelete()).isFalse(); // Will be set by @PrePersist
        assertThat(result.getCreatedAt()).isNull(); // Will be set by @CreationTimestamp
        assertThat(result.getUpdatedAt()).isNull(); // Will be set by @UpdateTimestamp
    }

    @Test
    void toEntity_shouldHandleNullDescription_whenRequestHasNullDescription() {
        // Arrange
        ConstraintRequest requestWithNullDescription = new ConstraintRequest(
            "Test Constraint",
            null, // null description
            "Test sentence",
            schema
        );

        // Act
        Constraint result = constraintMapper.toEntity(requestWithNullDescription);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Constraint");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getSentence()).isEqualTo("Test sentence");
        assertThat(result.getFields()).isEqualTo(schema);
    }

    @Test
    void updateEntity_shouldUpdateExistingConstraintFromRequest_whenConstraintAndRequestProvided() {
        // Arrange
        ConstraintRequest updateRequest = new ConstraintRequest(
            "Updated Constraint Name",
            "Updated Description",
            "Updated sentence for constraint validation",
            resolvedSchema
        );

        // Store original values
        Long originalId = constraint.getId();
        UUID originalUuid = constraint.getUuid();
        String originalHash = constraint.getFieldsHash();
        Boolean originalSoftDelete = constraint.getSoftDelete();
        Timestamp originalCreatedAt = constraint.getCreatedAt();
        Timestamp originalUpdatedAt = constraint.getUpdatedAt();

        // Act
        Constraint result = constraintMapper.updateEntity(constraint, updateRequest);

        // Assert
        assertThat(result).isSameAs(constraint); // Should return the same instance
        assertThat(result.getId()).isEqualTo(originalId); // ID should not change
        assertThat(result.getUuid()).isEqualTo(originalUuid); // UUID should not change
        assertThat(result.getName()).isEqualTo(updateRequest.name());
        assertThat(result.getDescription()).isEqualTo(updateRequest.description());
        assertThat(result.getSentence()).isEqualTo(updateRequest.sentence());
        assertThat(result.getFields()).isEqualTo(updateRequest.schema());
        assertThat(result.getFieldsHash()).isEqualTo(originalHash); // Hash should not change in mapper
        assertThat(result.getSoftDelete()).isEqualTo(originalSoftDelete); // SoftDelete should not change
        assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt); // CreatedAt should not change
        assertThat(result.getUpdatedAt()).isEqualTo(originalUpdatedAt); // UpdatedAt should not change in mapper
    }

    @Test
    void updateEntity_shouldHandleNullValuesInRequest_whenRequestHasNullFields() {
        // Arrange
        ConstraintRequest updateRequestWithNulls = new ConstraintRequest(
            "Updated Name",
            null, // null description
            "Updated sentence",
            schema
        );

        // Act
        Constraint result = constraintMapper.updateEntity(constraint, updateRequestWithNulls);

        // Assert
        assertThat(result).isSameAs(constraint);
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getSentence()).isEqualTo("Updated sentence");
        assertThat(result.getFields()).isEqualTo(schema);
    }

    @Test
    void updateEntity_shouldCompletelyReplaceFields_whenNewSchemaProvided() {
        // Arrange
        FieldOptions newOptions = new FieldOptions(
            List.of(
                new FieldOption("new1", "New Option 1"),
                new FieldOption("new2", "New Option 2"),
                new FieldOption("new3", "New Option 3")
            ),
            2,
            5
        );

        FieldDefinition newFieldDefinition = new FieldDefinition(
            2,
            "new_field",
            "multiselect",
            "New Field",
            "Select multiple values",
            newOptions,
            SourceType.DATABASE
        );

        Schema newSchema = new Schema(List.of(newFieldDefinition));

        ConstraintRequest updateRequest = new ConstraintRequest(
            "Updated Constraint",
            "Updated Description",
            "Updated sentence",
            newSchema
        );

        // Act
        Constraint result = constraintMapper.updateEntity(constraint, updateRequest);

        // Assert
        assertThat(result.getFields()).isEqualTo(newSchema);
        assertThat(result.getFields()).isNotEqualTo(schema); // Should be completely replaced
        assertThat(result.getFields().fields()).hasSize(1);
        assertThat(result.getFields().fields().get(0).name()).isEqualTo("new_field");
        assertThat(result.getFields().fields().get(0).type()).isEqualTo("multiselect");
        assertThat(result.getFields().fields().get(0).sourceType()).isEqualTo(SourceType.DATABASE);
    }

    @Test
    void mapRequestToEntity_shouldMapAllFieldsCorrectly_whenCalledDirectly() {
        // This tests the private method indirectly through public methods
        // Arrange
        Constraint emptyConstraint = new Constraint();

        // Act
        Constraint result = constraintMapper.updateEntity(emptyConstraint, request);

        // Assert
        assertThat(result.getName()).isEqualTo(request.name());
        assertThat(result.getDescription()).isEqualTo(request.description());
        assertThat(result.getSentence()).isEqualTo(request.sentence());
        assertThat(result.getFields()).isEqualTo(request.schema());
    }

    @Test
    void toResponse_shouldHandleComplexSchema_whenConstraintHasComplexFields() {
        // Arrange
        FieldOptions complexOptions = new FieldOptions(
            List.of(
                new FieldOption("complex1", "Complex Option 1"),
                new FieldOption("complex2", "Complex Option 2")
            ),
            1,
            2
        );

        FieldDefinition complexField = new FieldDefinition(
            3,
            "complex_field",
            "checkbox",
            "Complex Field",
            "Select multiple complex options",
            complexOptions,
            SourceType.DATABASE
        );

        Schema complexSchema = new Schema(List.of(complexField));
        constraint.setFields(complexSchema);

        // Act
        ConstraintResponse result = constraintMapper.toResponse(constraint);

        // Assert
        assertThat(result.schemas()).isEqualTo(complexSchema);
        assertThat(result.schemas().fields()).hasSize(1);
        assertThat(result.schemas().fields().get(0).options().option()).hasSize(2);
        assertThat(result.schemas().fields().get(0).options().min()).isEqualTo(1);
        assertThat(result.schemas().fields().get(0).options().max()).isEqualTo(2);
    }
}
