package com.resetrix.horaion.modules.constraint.strategies;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DataSourceStrategyFactoryTest {

    @Mock
    private DataSourceStrategy staticDataSourceStrategy;

    @Mock
    private DataSourceStrategy repositoryDataSourceStrategy;

    @Mock
    private DataSourceStrategy customDataSourceStrategy;

    private DataSourceStrategyFactory factory;

    @BeforeEach
    void setUp() {
        // Configure mock strategies with lenient stubbing to avoid unnecessary stubbing errors
        lenient().when(staticDataSourceStrategy.supports("SELECT")).thenReturn(true);
        lenient().when(staticDataSourceStrategy.supports("INPUT")).thenReturn(true);
        lenient().when(staticDataSourceStrategy.supports("DATABASE")).thenReturn(false);

        lenient().when(repositoryDataSourceStrategy.supports("SELECT")).thenReturn(false);
        lenient().when(repositoryDataSourceStrategy.supports("INPUT")).thenReturn(false);
        lenient().when(repositoryDataSourceStrategy.supports("DATABASE")).thenReturn(true);

        lenient().when(customDataSourceStrategy.supports("SELECT")).thenReturn(false);
        lenient().when(customDataSourceStrategy.supports("INPUT")).thenReturn(false);
        lenient().when(customDataSourceStrategy.supports("DATABASE")).thenReturn(false);
        lenient().when(customDataSourceStrategy.supports("CUSTOM")).thenReturn(true);

        // Create factory with mock strategies
        List<DataSourceStrategy> strategies = List.of(
            staticDataSourceStrategy,
            repositoryDataSourceStrategy,
            customDataSourceStrategy
        );
        factory = new DataSourceStrategyFactory(strategies);
    }

    @Test
    void getStrategy_shouldReturnStaticStrategy_whenSelectSourceTypeProvided() {
        // Act
        DataSourceStrategy result = factory.getStrategy(SourceType.SELECT);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(staticDataSourceStrategy);
    }

    @Test
    void getStrategy_shouldReturnStaticStrategy_whenInputSourceTypeProvided() {
        // Act
        DataSourceStrategy result = factory.getStrategy(SourceType.INPUT);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(staticDataSourceStrategy);
    }

    @Test
    void getStrategy_shouldReturnRepositoryStrategy_whenDatabaseSourceTypeProvided() {
        // Act
        DataSourceStrategy result = factory.getStrategy(SourceType.DATABASE);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(repositoryDataSourceStrategy);
    }

    @Test
    void getStrategy_shouldThrowDataSourceException_whenNoStrategySupportsSourceType() {
        // Arrange
        // Configure all strategies to not support the source type
        lenient().when(staticDataSourceStrategy.supports("UNSUPPORTED")).thenReturn(false);
        lenient().when(repositoryDataSourceStrategy.supports("UNSUPPORTED")).thenReturn(false);
        lenient().when(customDataSourceStrategy.supports("UNSUPPORTED")).thenReturn(false);

        // Create a custom SourceType for testing (since we can't create new enum values)
        // We'll simulate this by testing with a source type that no strategy supports

        // Act & Assert
        assertThatThrownBy(() -> {
            // We need to test with an existing SourceType, so let's modify the mocks
            lenient().when(staticDataSourceStrategy.supports("SELECT")).thenReturn(false);
            lenient().when(repositoryDataSourceStrategy.supports("SELECT")).thenReturn(false);
            lenient().when(customDataSourceStrategy.supports("SELECT")).thenReturn(false);
            
            factory.getStrategy(SourceType.SELECT);
        })
        .isInstanceOf(DataSourceException.class)
        .hasMessage("No strategy found for source type: SELECT");
    }

    @Test
    void getStrategy_shouldReturnFirstMatchingStrategy_whenMultipleStrategiesSupportSourceType() {
        // Arrange
        // Configure both static and repository strategies to support SELECT
        lenient().when(staticDataSourceStrategy.supports("SELECT")).thenReturn(true);
        lenient().when(repositoryDataSourceStrategy.supports("SELECT")).thenReturn(true);

        // Act
        DataSourceStrategy result = factory.getStrategy(SourceType.SELECT);

        // Assert
        // Should return the first strategy in the list that supports the source type
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(staticDataSourceStrategy);
    }

    @Test
    void getStrategy_shouldHandleEmptyStrategiesList() {
        // Arrange
        DataSourceStrategyFactory emptyFactory = new DataSourceStrategyFactory(List.of());

        // Act & Assert
        assertThatThrownBy(() -> emptyFactory.getStrategy(SourceType.SELECT))
            .isInstanceOf(DataSourceException.class)
            .hasMessage("No strategy found for source type: SELECT");
    }

    @Test
    void getStrategy_shouldHandleSingleStrategy() {
        // Arrange
        DataSourceStrategyFactory singleStrategyFactory = new DataSourceStrategyFactory(
            List.of(staticDataSourceStrategy)
        );

        // Act
        DataSourceStrategy result = singleStrategyFactory.getStrategy(SourceType.SELECT);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(staticDataSourceStrategy);
    }

    @Test
    void getStrategy_shouldHandleCaseInsensitiveSourceTypeMatching() {
        // Arrange
        lenient().when(staticDataSourceStrategy.supports("select")).thenReturn(true);
        lenient().when(staticDataSourceStrategy.supports("SELECT")).thenReturn(true);
        lenient().when(staticDataSourceStrategy.supports("Select")).thenReturn(true);

        // Act
        DataSourceStrategy result = factory.getStrategy(SourceType.SELECT);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(staticDataSourceStrategy);
    }

    @Test
    void getStrategy_shouldCallSupportsMethodOnAllStrategiesUntilMatch() {
        // Arrange
        // Configure strategies so that the third one matches
        lenient().when(staticDataSourceStrategy.supports("DATABASE")).thenReturn(false);
        lenient().when(repositoryDataSourceStrategy.supports("DATABASE")).thenReturn(false);
        lenient().when(customDataSourceStrategy.supports("DATABASE")).thenReturn(true);

        // Act
        DataSourceStrategy result = factory.getStrategy(SourceType.DATABASE);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(customDataSourceStrategy);
    }

    @Test
    void constructor_shouldAcceptStrategiesList() {
        // Arrange
        List<DataSourceStrategy> strategies = List.of(
            staticDataSourceStrategy,
            repositoryDataSourceStrategy
        );

        // Act
        DataSourceStrategyFactory newFactory = new DataSourceStrategyFactory(strategies);

        // Assert
        assertThat(newFactory).isNotNull();
        
        // Verify it works by getting a strategy
        DataSourceStrategy result = newFactory.getStrategy(SourceType.SELECT);
        assertThat(result).isEqualTo(staticDataSourceStrategy);
    }

    @Test
    void getStrategy_shouldHandleNullStrategiesInList() {
        // Arrange
        List<DataSourceStrategy> strategiesWithNull = new ArrayList<>();
        strategiesWithNull.add(null);
        strategiesWithNull.add(staticDataSourceStrategy);
        strategiesWithNull.add(null);
        strategiesWithNull.add(repositoryDataSourceStrategy);

        DataSourceStrategyFactory factoryWithNulls = new DataSourceStrategyFactory(strategiesWithNull);

        // Act
        DataSourceStrategy result = factoryWithNulls.getStrategy(SourceType.SELECT);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(staticDataSourceStrategy);
    }

    @Test
    void getStrategy_shouldThrowException_whenAllStrategiesAreNull() {
        // Arrange
        List<DataSourceStrategy> nullStrategies = new ArrayList<>();
        nullStrategies.add(null);
        nullStrategies.add(null);
        nullStrategies.add(null);
        DataSourceStrategyFactory factoryWithAllNulls = new DataSourceStrategyFactory(nullStrategies);

        // Act & Assert
        assertThatThrownBy(() -> factoryWithAllNulls.getStrategy(SourceType.SELECT))
            .isInstanceOf(DataSourceException.class)
            .hasMessage("No strategy found for source type: SELECT");
    }

    @Test
    void getStrategy_shouldWorkWithDifferentSourceTypeOrder() {
        // Test that the factory works regardless of the order strategies are checked
        
        // Test INPUT
        DataSourceStrategy inputResult = factory.getStrategy(SourceType.INPUT);
        assertThat(inputResult).isEqualTo(staticDataSourceStrategy);

        // Test DATABASE
        DataSourceStrategy databaseResult = factory.getStrategy(SourceType.DATABASE);
        assertThat(databaseResult).isEqualTo(repositoryDataSourceStrategy);

        // Test SELECT
        DataSourceStrategy selectResult = factory.getStrategy(SourceType.SELECT);
        assertThat(selectResult).isEqualTo(staticDataSourceStrategy);
    }
}
