package com.resetrix.horaion.modules.constraint.services;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;
import com.resetrix.horaion.modules.constraint.properties.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConstraintSchemaServiceTest {

    @Mock
    private FieldOptionService fieldOptionService;

    @InjectMocks
    private ConstraintSchemaService constraintSchemaService;

    private FieldDefinition selectFieldDefinition;
    private FieldDefinition databaseFieldDefinition;
    private FieldOptions staticOptions;
    private FieldOptions databaseOptions;
    private List<FieldOption> resolvedOptions;

    @BeforeEach
    void setUp() {
        // Create static field options
        staticOptions = new FieldOptions(
            List.of(
                new FieldOption("option1", "Option 1"),
                new FieldOption("option2", "Option 2")
            ),
            null,
            null
        );

        // Create database field options
        databaseOptions = new FieldOptions(
            null,
            1,
            5
        );

        // Create resolved options
        resolvedOptions = List.of(
            new FieldOption("company1", "Company 1"),
            new FieldOption("company2", "Company 2"),
            new FieldOption("company3", "Company 3")
        );

        // Create SELECT field definition (should not be resolved)
        selectFieldDefinition = new FieldDefinition(
            1,
            "select_field",
            "select",
            "Select Field",
            "Choose an option",
            staticOptions,
            SourceType.SELECT
        );

        // Create DATABASE field definition (should be resolved)
        databaseFieldDefinition = new FieldDefinition(
            2,
            "database_field",
            "select",
            "Database Field",
            "Choose from database",
            databaseOptions,
            SourceType.DATABASE
        );
    }

    @Test
    void resolveFieldOptions_shouldReturnSameSchema_whenSchemaIsNull() {
        // Act
        Schema result = constraintSchemaService.resolveFieldOptions(null);

        // Assert
        assertThat(result).isNull();
        verify(fieldOptionService, never()).resolveOptions(any());
    }

    @Test
    void resolveFieldOptions_shouldReturnSameSchema_whenSchemaFieldsIsNull() {
        // Arrange
        Schema schema = new Schema(null);

        // Act
        Schema result = constraintSchemaService.resolveFieldOptions(schema);

        // Assert
        assertThat(result).isEqualTo(schema);
        verify(fieldOptionService, never()).resolveOptions(any());
    }

    @Test
    void resolveFieldOptions_shouldReturnSchemaWithResolvedFields_whenDatabaseFieldsPresent() {
        // Arrange
        Schema schema = new Schema(List.of(selectFieldDefinition, databaseFieldDefinition));
        when(fieldOptionService.resolveOptions(databaseFieldDefinition)).thenReturn(resolvedOptions);

        // Act
        Schema result = constraintSchemaService.resolveFieldOptions(schema);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.fields()).hasSize(2);

        // First field (SELECT) should remain unchanged
        FieldDefinition firstField = result.fields().get(0);
        assertThat(firstField).isEqualTo(selectFieldDefinition);

        // Second field (DATABASE) should have resolved options
        FieldDefinition secondField = result.fields().get(1);
        assertThat(secondField.id()).isEqualTo(databaseFieldDefinition.id());
        assertThat(secondField.name()).isEqualTo(databaseFieldDefinition.name());
        assertThat(secondField.type()).isEqualTo(databaseFieldDefinition.type());
        assertThat(secondField.label()).isEqualTo(databaseFieldDefinition.label());
        assertThat(secondField.placeholder()).isEqualTo(databaseFieldDefinition.placeholder());
        assertThat(secondField.sourceType()).isEqualTo(databaseFieldDefinition.sourceType());
        assertThat(secondField.options().option()).isEqualTo(resolvedOptions);
        assertThat(secondField.options().min()).isEqualTo(databaseOptions.min());
        assertThat(secondField.options().max()).isEqualTo(databaseOptions.max());

        verify(fieldOptionService).resolveOptions(databaseFieldDefinition);
        verify(fieldOptionService, never()).resolveOptions(selectFieldDefinition);
    }

    @Test
    void resolveFieldOptions_shouldReturnSchemaWithUnchangedFields_whenOnlySelectFieldsPresent() {
        // Arrange
        Schema schema = new Schema(List.of(selectFieldDefinition));

        // Act
        Schema result = constraintSchemaService.resolveFieldOptions(schema);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.fields()).hasSize(1);
        assertThat(result.fields().get(0)).isEqualTo(selectFieldDefinition);

        verify(fieldOptionService, never()).resolveOptions(any());
    }

    @Test
    void resolveFieldDefinitionOptions_shouldReturnSameField_whenSourceTypeIsNotDatabase() {
        // Act
        FieldDefinition result = constraintSchemaService.resolveFieldDefinitionOptions(selectFieldDefinition);

        // Assert
        assertThat(result).isEqualTo(selectFieldDefinition);
        verify(fieldOptionService, never()).resolveOptions(any());
    }

    @Test
    void resolveFieldDefinitionOptions_shouldReturnFieldWithResolvedOptions_whenSourceTypeIsDatabase() {
        // Arrange
        when(fieldOptionService.resolveOptions(databaseFieldDefinition)).thenReturn(resolvedOptions);

        // Act
        FieldDefinition result = constraintSchemaService.resolveFieldDefinitionOptions(databaseFieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(databaseFieldDefinition.id());
        assertThat(result.name()).isEqualTo(databaseFieldDefinition.name());
        assertThat(result.type()).isEqualTo(databaseFieldDefinition.type());
        assertThat(result.label()).isEqualTo(databaseFieldDefinition.label());
        assertThat(result.placeholder()).isEqualTo(databaseFieldDefinition.placeholder());
        assertThat(result.sourceType()).isEqualTo(databaseFieldDefinition.sourceType());
        assertThat(result.options().option()).isEqualTo(resolvedOptions);
        assertThat(result.options().min()).isEqualTo(databaseOptions.min());
        assertThat(result.options().max()).isEqualTo(databaseOptions.max());

        verify(fieldOptionService).resolveOptions(databaseFieldDefinition);
    }

    @Test
    void resolveFieldDefinitionOptions_shouldThrowException_whenFieldOptionServiceFails() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Failed to resolve options");
        when(fieldOptionService.resolveOptions(databaseFieldDefinition)).thenThrow(expectedException);

        // Act & Assert
        assertThatThrownBy(() -> constraintSchemaService.resolveFieldDefinitionOptions(databaseFieldDefinition))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to resolve options");

        verify(fieldOptionService).resolveOptions(databaseFieldDefinition);
    }

    @Test
    void resolveFieldDefinitionOptions_shouldPreserveMinMaxValues_whenDatabaseFieldHasMinMax() {
        // Arrange
        FieldOptions databaseOptionsWithMinMax = new FieldOptions(
            null,
            2,
            10
        );

        FieldDefinition databaseFieldWithMinMax = new FieldDefinition(
            3,
            "database_field_with_min_max",
            "multiselect",
            "Database Field with Min/Max",
            "Choose multiple from database",
            databaseOptionsWithMinMax,
            SourceType.DATABASE
        );

        when(fieldOptionService.resolveOptions(databaseFieldWithMinMax)).thenReturn(resolvedOptions);

        // Act
        FieldDefinition result = constraintSchemaService.resolveFieldDefinitionOptions(databaseFieldWithMinMax);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.options().option()).isEqualTo(resolvedOptions);
        assertThat(result.options().min()).isEqualTo(databaseOptionsWithMinMax.min());
        assertThat(result.options().max()).isEqualTo(databaseOptionsWithMinMax.max());

        verify(fieldOptionService).resolveOptions(databaseFieldWithMinMax);
    }

    @Test
    void resolveFieldOptions_shouldHandleEmptyFieldsList() {
        // Arrange
        Schema schema = new Schema(List.of());

        // Act
        Schema result = constraintSchemaService.resolveFieldOptions(schema);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.fields()).isEmpty();

        verify(fieldOptionService, never()).resolveOptions(any());
    }

    @Test
    void resolveFieldOptions_shouldHandleMixedFieldTypes() {
        // Arrange
        FieldDefinition textField = new FieldDefinition(
            3,
            "text_field",
            "text",
            "Text Field",
            "Enter text",
            null,
            SourceType.SELECT
        );

        Schema schema = new Schema(List.of(selectFieldDefinition, databaseFieldDefinition, textField));
        when(fieldOptionService.resolveOptions(databaseFieldDefinition)).thenReturn(resolvedOptions);

        // Act
        Schema result = constraintSchemaService.resolveFieldOptions(schema);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.fields()).hasSize(3);

        // Only the database field should be resolved
        assertThat(result.fields().get(0)).isEqualTo(selectFieldDefinition);
        assertThat(result.fields().get(1).options().option()).isEqualTo(resolvedOptions);
        assertThat(result.fields().get(2)).isEqualTo(textField);

        verify(fieldOptionService).resolveOptions(databaseFieldDefinition);
        verify(fieldOptionService, never()).resolveOptions(selectFieldDefinition);
        verify(fieldOptionService, never()).resolveOptions(textField);
    }

    @Test
    void resolveFieldDefinitionOptions_shouldHandleNullOptions_whenDatabaseFieldHasNullOptions() {
        // Arrange
        FieldDefinition databaseFieldWithNullOptions = new FieldDefinition(
            4,
            "database_field_null_options",
            "select",
            "Database Field with Null Options",
            "Choose from database",
            null, // This is the key - null options
            SourceType.DATABASE
        );

        when(fieldOptionService.resolveOptions(databaseFieldWithNullOptions)).thenReturn(resolvedOptions);

        // Act
        FieldDefinition result = constraintSchemaService.resolveFieldDefinitionOptions(databaseFieldWithNullOptions);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(databaseFieldWithNullOptions.id());
        assertThat(result.name()).isEqualTo(databaseFieldWithNullOptions.name());
        assertThat(result.type()).isEqualTo(databaseFieldWithNullOptions.type());
        assertThat(result.label()).isEqualTo(databaseFieldWithNullOptions.label());
        assertThat(result.placeholder()).isEqualTo(databaseFieldWithNullOptions.placeholder());
        assertThat(result.sourceType()).isEqualTo(databaseFieldWithNullOptions.sourceType());
        assertThat(result.options().option()).isEqualTo(resolvedOptions);
        // When original options are null, min and max should also be null
        assertThat(result.options().min()).isNull();
        assertThat(result.options().max()).isNull();

        verify(fieldOptionService).resolveOptions(databaseFieldWithNullOptions);
    }
}
