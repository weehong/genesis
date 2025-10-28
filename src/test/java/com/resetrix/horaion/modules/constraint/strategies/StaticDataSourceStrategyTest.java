package com.resetrix.horaion.modules.constraint.strategies;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StaticDataSourceStrategyTest {

    private StaticDataSourceStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StaticDataSourceStrategy();
    }

    @Test
    void supports_shouldReturnTrue_whenSourceTypeIsSelect() {
        // Act & Assert
        assertThat(strategy.supports(SourceType.SELECT.name())).isTrue();
        assertThat(strategy.supports("SELECT")).isTrue();
        assertThat(strategy.supports("select")).isTrue();
    }

    @Test
    void supports_shouldReturnTrue_whenSourceTypeIsInput() {
        // Act & Assert
        assertThat(strategy.supports(SourceType.INPUT.name())).isTrue();
        assertThat(strategy.supports("INPUT")).isTrue();
        assertThat(strategy.supports("input")).isTrue();
    }

    @Test
    void supports_shouldReturnFalse_whenSourceTypeIsDatabase() {
        // Act & Assert
        assertThat(strategy.supports(SourceType.DATABASE.name())).isFalse();
        assertThat(strategy.supports("DATABASE")).isFalse();
        assertThat(strategy.supports("database")).isFalse();
    }

    @Test
    void supports_shouldReturnFalse_whenSourceTypeIsUnknown() {
        // Act & Assert
        assertThat(strategy.supports("UNKNOWN")).isFalse();
        assertThat(strategy.supports("INVALID")).isFalse();
        assertThat(strategy.supports("")).isFalse();
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    void fetchOptions_shouldReturnStaticOptions_whenSelectFieldDefinitionWithOptionsProvided() {
        // Arrange
        List<FieldOption> staticOptions = List.of(
            new FieldOption("option1", "Option 1"),
            new FieldOption("option2", "Option 2"),
            new FieldOption("option3", "Option 3")
        );

        FieldOptions fieldOptions = new FieldOptions(
            staticOptions,
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            1,
            "select_field",
            "select",
            "Select Field",
            "Choose an option",
            fieldOptions,
            SourceType.SELECT
        );

        // Act
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(staticOptions);
    }

    @Test
    void fetchOptions_shouldReturnStaticOptions_whenInputFieldDefinitionWithOptionsProvided() {
        // Arrange
        List<FieldOption> staticOptions = List.of(
            new FieldOption("default1", "Default 1"),
            new FieldOption("default2", "Default 2")
        );

        FieldOptions fieldOptions = new FieldOptions(
            staticOptions,
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            2,
            "input_field",
            "text",
            "Input Field",
            "Enter text",
            fieldOptions,
            SourceType.INPUT
        );

        // Act
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(staticOptions);
    }

    @Test
    void fetchOptions_shouldReturnEmptyList_whenFieldDefinitionHasNullOptions() {
        // Arrange
        FieldDefinition fieldDefinition = new FieldDefinition(
            3,
            "field_without_options",
            "select",
            "Field Without Options",
            "Choose an option",
            null,
            SourceType.SELECT
        );

        // Act
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void fetchOptions_shouldReturnEmptyList_whenFieldOptionsHasNullOptionsList() {
        // Arrange
        FieldOptions fieldOptions = new FieldOptions(
            null,
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            4,
            "field_with_null_options",
            "select",
            "Field With Null Options",
            "Choose an option",
            fieldOptions,
            SourceType.SELECT
        );

        // Act
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void fetchOptions_shouldReturnEmptyList_whenFieldOptionsHasEmptyOptionsList() {
        // Arrange
        FieldOptions fieldOptions = new FieldOptions(
            Collections.emptyList(),
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            5,
            "field_with_empty_options",
            "select",
            "Field With Empty Options",
            "Choose an option",
            fieldOptions,
            SourceType.SELECT
        );

        // Act
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void fetchOptions_shouldThrowDataSourceException_whenUnsupportedSourceTypeProvided() {
        // Arrange
        FieldOptions fieldOptions = new FieldOptions(
            List.of(new FieldOption("option1", "Option 1")),
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            6,
            "database_field",
            "select",
            "Database Field",
            "Choose from database",
            fieldOptions,
            SourceType.DATABASE
        );

        // Act & Assert
        assertThatThrownBy(() -> strategy.fetchOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessage("StaticDataSourceStrategy does not support source type: DATABASE");
    }

    @Test
    void fetchOptions_shouldHandleSingleOption() {
        // Arrange
        List<FieldOption> singleOption = List.of(
            new FieldOption("single", "Single Option")
        );

        FieldOptions fieldOptions = new FieldOptions(
            singleOption,
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            7,
            "single_option_field",
            "select",
            "Single Option Field",
            "Only one choice",
            fieldOptions,
            SourceType.SELECT
        );

        // Act
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualTo("single");
        assertThat(result.get(0).label()).isEqualTo("Single Option");
    }

    @Test
    void fetchOptions_shouldPreserveOptionOrder() {
        // Arrange
        List<FieldOption> orderedOptions = List.of(
            new FieldOption("first", "First Option"),
            new FieldOption("second", "Second Option"),
            new FieldOption("third", "Third Option")
        );

        FieldOptions fieldOptions = new FieldOptions(
            orderedOptions,
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            8,
            "ordered_field",
            "select",
            "Ordered Field",
            "Options in order",
            fieldOptions,
            SourceType.SELECT
        );

        // Act
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).value()).isEqualTo("first");
        assertThat(result.get(1).value()).isEqualTo("second");
        assertThat(result.get(2).value()).isEqualTo("third");
    }

    @Test
    void fetchOptions_shouldHandleOptionsWithSpecialCharacters() {
        // Arrange
        List<FieldOption> specialOptions = List.of(
            new FieldOption("option-with-dash", "Option with Dash"),
            new FieldOption("option_with_underscore", "Option with Underscore"),
            new FieldOption("option with spaces", "Option with Spaces"),
            new FieldOption("option@with#symbols", "Option with Symbols")
        );

        FieldOptions fieldOptions = new FieldOptions(
            specialOptions,
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            9,
            "special_chars_field",
            "select",
            "Special Characters Field",
            "Options with special characters",
            fieldOptions,
            SourceType.SELECT
        );

        // Act
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result).isEqualTo(specialOptions);
    }
}
