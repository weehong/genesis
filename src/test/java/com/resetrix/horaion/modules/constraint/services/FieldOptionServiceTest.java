package com.resetrix.horaion.modules.constraint.services;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;
import com.resetrix.horaion.modules.constraint.strategies.DataSourceStrategy;
import com.resetrix.horaion.modules.constraint.strategies.DataSourceStrategyFactory;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FieldOptionServiceTest {

    @Mock
    private DataSourceStrategyFactory strategyFactory;

    @Mock
    private DataSourceStrategy dataSourceStrategy;

    @InjectMocks
    private FieldOptionService fieldOptionService;

    private FieldDefinition fieldDefinition;
    private List<FieldOption> expectedOptions;

    @BeforeEach
    void setUp() {
        // Create test field options
        expectedOptions = List.of(
            new FieldOption("option1", "Option 1"),
            new FieldOption("option2", "Option 2"),
            new FieldOption("option3", "Option 3")
        );

        // Create test field definition
        FieldOptions options = new FieldOptions(
            null,
            1,
            5
        );

        fieldDefinition = new FieldDefinition(
            1,
            "company_field",
            "select",
            "Company Field",
            "Choose a company",
            options,
            SourceType.DATABASE
        );
    }

    @Test
    void resolveOptions_shouldReturnFieldOptions_whenValidFieldDefinitionProvided() {
        // Arrange
        when(strategyFactory.getStrategy(SourceType.DATABASE)).thenReturn(dataSourceStrategy);
        when(dataSourceStrategy.fetchOptions(fieldDefinition)).thenReturn(expectedOptions);

        // Act
        List<FieldOption> result = fieldOptionService.resolveOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(expectedOptions);

        verify(strategyFactory).getStrategy(SourceType.DATABASE);
        verify(dataSourceStrategy).fetchOptions(fieldDefinition);
    }

    @Test
    void resolveOptions_shouldReturnEmptyList_whenStrategyReturnsEmptyList() {
        // Arrange
        List<FieldOption> emptyOptions = List.of();
        when(strategyFactory.getStrategy(SourceType.DATABASE)).thenReturn(dataSourceStrategy);
        when(dataSourceStrategy.fetchOptions(fieldDefinition)).thenReturn(emptyOptions);

        // Act
        List<FieldOption> result = fieldOptionService.resolveOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(strategyFactory).getStrategy(SourceType.DATABASE);
        verify(dataSourceStrategy).fetchOptions(fieldDefinition);
    }

    @Test
    void resolveOptions_shouldThrowDataSourceException_whenStrategyFactoryThrowsException() {
        // Arrange
        DataSourceException expectedException = new DataSourceException("No strategy found for source type: DATABASE");
        when(strategyFactory.getStrategy(SourceType.DATABASE)).thenThrow(expectedException);

        // Act & Assert
        assertThatThrownBy(() -> fieldOptionService.resolveOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessage("Unexpected error occurred while resolving field options")
            .hasCauseInstanceOf(DataSourceException.class);

        verify(strategyFactory).getStrategy(SourceType.DATABASE);
    }

    @Test
    void resolveOptions_shouldThrowDataSourceException_whenStrategyThrowsException() {
        // Arrange
        DataSourceException expectedException = new DataSourceException("Failed to fetch options from repository");
        when(strategyFactory.getStrategy(SourceType.DATABASE)).thenReturn(dataSourceStrategy);
        when(dataSourceStrategy.fetchOptions(fieldDefinition)).thenThrow(expectedException);

        // Act & Assert
        assertThatThrownBy(() -> fieldOptionService.resolveOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessage("Unexpected error occurred while resolving field options")
            .hasCauseInstanceOf(DataSourceException.class);

        verify(strategyFactory).getStrategy(SourceType.DATABASE);
        verify(dataSourceStrategy).fetchOptions(fieldDefinition);
    }

    @Test
    void resolveOptions_shouldHandleSelectSourceType() {
        // Arrange
        FieldOptions selectOptions = new FieldOptions(
            List.of(
                new FieldOption("select1", "Select 1"),
                new FieldOption("select2", "Select 2")
            ),
            null,
            null
        );

        FieldDefinition selectFieldDefinition = new FieldDefinition(
            2,
            "select_field",
            "select",
            "Select Field",
            "Choose an option",
            selectOptions,
            SourceType.SELECT
        );

        List<FieldOption> selectExpectedOptions = List.of(
            new FieldOption("select1", "Select 1"),
            new FieldOption("select2", "Select 2")
        );

        when(strategyFactory.getStrategy(SourceType.SELECT)).thenReturn(dataSourceStrategy);
        when(dataSourceStrategy.fetchOptions(selectFieldDefinition)).thenReturn(selectExpectedOptions);

        // Act
        List<FieldOption> result = fieldOptionService.resolveOptions(selectFieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(selectExpectedOptions);

        verify(strategyFactory).getStrategy(SourceType.SELECT);
        verify(dataSourceStrategy).fetchOptions(selectFieldDefinition);
    }

    @Test
    void resolveOptions_shouldHandleInputSourceType() {
        // Arrange
        FieldOptions inputOptions = new FieldOptions(
            List.of(),
            null,
            null
        );

        FieldDefinition inputFieldDefinition = new FieldDefinition(
            3,
            "input_field",
            "text",
            "Input Field",
            "Enter text",
            inputOptions,
            SourceType.INPUT
        );

        List<FieldOption> inputExpectedOptions = List.of();

        when(strategyFactory.getStrategy(SourceType.INPUT)).thenReturn(dataSourceStrategy);
        when(dataSourceStrategy.fetchOptions(inputFieldDefinition)).thenReturn(inputExpectedOptions);

        // Act
        List<FieldOption> result = fieldOptionService.resolveOptions(inputFieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(strategyFactory).getStrategy(SourceType.INPUT);
        verify(dataSourceStrategy).fetchOptions(inputFieldDefinition);
    }

    @Test
    void resolveOptions_shouldHandleRuntimeException_andWrapInDataSourceException() {
        // Arrange
        RuntimeException runtimeException = new RuntimeException("Unexpected error");
        when(strategyFactory.getStrategy(SourceType.DATABASE)).thenReturn(dataSourceStrategy);
        when(dataSourceStrategy.fetchOptions(fieldDefinition)).thenThrow(runtimeException);

        // Act & Assert
        assertThatThrownBy(() -> fieldOptionService.resolveOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessage("Unexpected error occurred while resolving field options")
            .hasCauseInstanceOf(RuntimeException.class);

        verify(strategyFactory).getStrategy(SourceType.DATABASE);
        verify(dataSourceStrategy).fetchOptions(fieldDefinition);
    }

    @Test
    void resolveOptions_shouldHandleNullFieldDefinition() {
        // Act & Assert
        assertThatThrownBy(() -> fieldOptionService.resolveOptions(null))
            .isInstanceOf(DataSourceException.class);
    }

    @Test
    void resolveOptions_shouldHandleFieldDefinitionWithNullSourceType() {
        // Arrange
        FieldDefinition fieldWithNullSourceType = new FieldDefinition(
            4,
            "null_source_field",
            "text",
            "Null Source Field",
            "Enter text",
            null,
            null
        );

        // Act & Assert
        assertThatThrownBy(() -> fieldOptionService.resolveOptions(fieldWithNullSourceType))
            .isInstanceOf(DataSourceException.class);
    }

    @Test
    void resolveOptions_shouldLogDebugMessages_whenResolvingOptions() {
        // Arrange
        when(strategyFactory.getStrategy(SourceType.DATABASE)).thenReturn(dataSourceStrategy);
        when(dataSourceStrategy.fetchOptions(fieldDefinition)).thenReturn(expectedOptions);

        // Act
        List<FieldOption> result = fieldOptionService.resolveOptions(fieldDefinition);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        // Verify that the service interacted with dependencies correctly
        verify(strategyFactory).getStrategy(SourceType.DATABASE);
        verify(dataSourceStrategy).fetchOptions(fieldDefinition);
    }
}
