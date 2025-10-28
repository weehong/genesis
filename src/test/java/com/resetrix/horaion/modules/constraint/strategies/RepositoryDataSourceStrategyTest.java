package com.resetrix.horaion.modules.constraint.strategies;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryDataSourceStrategyTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private JpaRepository<TestEntity, Long> testRepository;

    @Mock
    private JpaRepository<Object, Object> genericRepository;

    private RepositoryDataSourceStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new RepositoryDataSourceStrategy(applicationContext);
    }

    @Test
    void supports_shouldReturnTrueForDatabaseSourceType() {
        assertThat(strategy.supports(SourceType.DATABASE.name())).isTrue();
        assertThat(strategy.supports("DATABASE")).isTrue();
        assertThat(strategy.supports("database")).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForOtherSourceTypes() {
        assertThat(strategy.supports(SourceType.INPUT.name())).isFalse();
        assertThat(strategy.supports(SourceType.SELECT.name())).isFalse();
        assertThat(strategy.supports(SourceType.EXTERNAL.name())).isFalse();
    }

    @Test
    void fetchOptions_shouldReturnOptionsFromRepository() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "company", "select", "Company", "Select a company",
            options, SourceType.DATABASE
        );

        List<TestEntity> entities = Arrays.asList(
            new TestEntity(1L, "Company One"),
            new TestEntity(2L, "Company Two")
        );

        when(applicationContext.getBean("companyRepository")).thenReturn(testRepository);
        when(testRepository.findAll()).thenReturn(entities);

        // When
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).value()).isEqualTo(1L);
        assertThat(result.get(0).label()).isEqualTo("Company One");
        assertThat(result.get(1).value()).isEqualTo(2L);
        assertThat(result.get(1).label()).isEqualTo("Company Two");
    }

    @Test
    void fetchOptions_shouldThrowExceptionWhenRepositoryNotFound() {
        // Given
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "unknownEntity", "select", "Unknown Entity", "Select an unknown entity",
            null, SourceType.DATABASE
        );

        when(applicationContext.getBean("unknownentityRepository"))
            .thenThrow(new RuntimeException("Bean not found"));

        // When & Then
        assertThatThrownBy(() -> strategy.fetchOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessageContaining("Repository not found for entity: Unknownentity");
    }



    @Test
    void fetchOptions_shouldThrowExceptionForUnsupportedSourceType() {
        // Given
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "testEntity", "select", "Test Entity", "Select a test entity",
            null, SourceType.INPUT
        );

        // When & Then
        assertThatThrownBy(() -> strategy.fetchOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessage("RepositoryDataSourceStrategy does not support source type: INPUT");
    }

    @Test
    void fetchOptions_shouldThrowExceptionWhenRepositoryFindAllFails() {
        // Given
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "company", "select", "Company", "Select a company",
            null, SourceType.DATABASE
        );

        when(applicationContext.getBean("companyRepository")).thenReturn(testRepository);
        when(testRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> strategy.fetchOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessageContaining("Failed to fetch field options from repository")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void fetchOptions_shouldHandleEntityNameWithUnderscores() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "user_role_id", "select", "User Role", "Select a user role",
            options, SourceType.DATABASE
        );

        List<TestEntity> entities = Arrays.asList(
            new TestEntity(1L, "Admin"),
            new TestEntity(2L, "User")
        );

        when(applicationContext.getBean("userroleRepository")).thenReturn(testRepository);
        when(testRepository.findAll()).thenReturn(entities);

        // When
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).value()).isEqualTo(1L);
        assertThat(result.get(0).label()).isEqualTo("Admin");
    }

    @Test
    void fetchOptions_shouldHandleEntityNameWithEmptyParts() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "test__entity_selection", "select", "Test Entity", "Select a test entity",
            options, SourceType.DATABASE
        );

        List<TestEntity> entities = Arrays.asList(
            new TestEntity(1L, "Test One")
        );

        when(applicationContext.getBean("testentityRepository")).thenReturn(testRepository);
        when(testRepository.findAll()).thenReturn(entities);

        // When
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualTo(1L);
        assertThat(result.get(0).label()).isEqualTo("Test One");
    }



    @Test
    void fetchOptions_shouldHandleEntityWithNullLabel() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "entity", "select", "Entity", "Select an entity",
            options, SourceType.DATABASE
        );

        List<EntityWithNullLabel> entities = Arrays.asList(
            new EntityWithNullLabel(1L, null)
        );

        when(applicationContext.getBean("entityRepository")).thenReturn(genericRepository);
        when(genericRepository.findAll()).thenReturn((List) entities);

        // When
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualTo(1L);
        assertThat(result.get(0).label()).isEqualTo("");
    }

    @Test
    void fetchOptions_shouldThrowExceptionWhenEntityHasNoValueField() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "entity", "select", "Entity", "Select an entity",
            options, SourceType.DATABASE
        );

        List<EntityWithoutValueField> entities = Arrays.asList(
            new EntityWithoutValueField("Test")
        );

        when(applicationContext.getBean("entityRepository")).thenReturn(genericRepository);
        when(genericRepository.findAll()).thenReturn((List) entities);

        // When & Then
        assertThatThrownBy(() -> strategy.fetchOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessageContaining("No suitable value field found in entity");
    }

    @Test
    void fetchOptions_shouldThrowExceptionWhenEntityHasNoLabelField() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "entity", "select", "Entity", "Select an entity",
            options, SourceType.DATABASE
        );

        List<EntityWithoutLabelField> entities = Arrays.asList(
            new EntityWithoutLabelField(1L)
        );

        when(applicationContext.getBean("entityRepository")).thenReturn(genericRepository);
        when(genericRepository.findAll()).thenReturn((List) entities);

        // When & Then
        assertThatThrownBy(() -> strategy.fetchOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessageContaining("No suitable label field found in entity");
    }

    @Test
    void fetchOptions_shouldHandleEntityWithUuidAndTitle() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "entity", "select", "Entity", "Select an entity",
            options, SourceType.DATABASE
        );

        List<EntityWithUuidAndTitle> entities = Arrays.asList(
            new EntityWithUuidAndTitle("uuid-123", "Entity Title")
        );

        when(applicationContext.getBean("entityRepository")).thenReturn(genericRepository);
        when(genericRepository.findAll()).thenReturn((List) entities);

        // When
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualTo("uuid-123");
        assertThat(result.get(0).label()).isEqualTo("Entity Title");
    }

    @Test
    void fetchOptions_shouldThrowExceptionWhenGetterMethodFails() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "entity", "select", "Entity", "Select an entity",
            options, SourceType.DATABASE
        );

        List<EntityWithFailingGetter> entities = Arrays.asList(
            new EntityWithFailingGetter()
        );

        when(applicationContext.getBean("entityRepository")).thenReturn(genericRepository);
        when(genericRepository.findAll()).thenReturn((List) entities);

        // When & Then
        assertThatThrownBy(() -> strategy.fetchOptions(fieldDefinition))
            .isInstanceOf(DataSourceException.class)
            .hasMessageContaining("Error mapping entity to FieldOption");
    }

    @Test
    void fetchOptions_shouldHandleEntityWithEmptyStringFields() {
        // Given
        FieldOptions options = new FieldOptions(null, null, null);
        FieldDefinition fieldDefinition = new FieldDefinition(
            1, "", "select", "Entity", "Select an entity",
            options, SourceType.DATABASE
        );

        List<EntityWithEmptyFields> entities = Arrays.asList(
            new EntityWithEmptyFields(1L, "")
        );

        when(applicationContext.getBean("Repository")).thenReturn(genericRepository);
        when(genericRepository.findAll()).thenReturn((List) entities);

        // When
        List<FieldOption> result = strategy.fetchOptions(fieldDefinition);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualTo(1L);
        assertThat(result.get(0).label()).isEqualTo("");
    }

    @Test
    void capitalize_shouldHandleNullString() throws Exception {
        // Given - use reflection to access private method
        Method capitalizeMethod = RepositoryDataSourceStrategy.class.getDeclaredMethod("capitalize", String.class);
        capitalizeMethod.setAccessible(true);

        // When
        String result = (String) capitalizeMethod.invoke(strategy, (String) null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void capitalize_shouldHandleEmptyString() throws Exception {
        // Given - use reflection to access private method
        Method capitalizeMethod = RepositoryDataSourceStrategy.class.getDeclaredMethod("capitalize", String.class);
        capitalizeMethod.setAccessible(true);

        // When
        String result = (String) capitalizeMethod.invoke(strategy, "");

        // Then
        assertThat(result).isEqualTo("");
    }

    @Test
    void capitalize_shouldHandleNormalString() throws Exception {
        // Given - use reflection to access private method
        Method capitalizeMethod = RepositoryDataSourceStrategy.class.getDeclaredMethod("capitalize", String.class);
        capitalizeMethod.setAccessible(true);

        // When
        String result = (String) capitalizeMethod.invoke(strategy, "test");

        // Then
        assertThat(result).isEqualTo("Test");
    }

    // Test entity class for testing purposes
    public static class TestEntity {
        private Long id;
        private String name;

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
    }

    // Entity with null label for testing
    public static class EntityWithNullLabel {
        private Long id;
        private String name;

        public EntityWithNullLabel(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    // Entity without value fields for testing
    public static class EntityWithoutValueField {
        private String description;

        public EntityWithoutValueField(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Entity without label fields for testing
    public static class EntityWithoutLabelField {
        private Long id;

        public EntityWithoutLabelField(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

    // Entity with UUID and title for testing
    public static class EntityWithUuidAndTitle {
        private String uuid;
        private String title;

        public EntityWithUuidAndTitle(String uuid, String title) {
            this.uuid = uuid;
            this.title = title;
        }

        public String getUuid() {
            return uuid;
        }

        public String getTitle() {
            return title;
        }
    }

    // Entity with failing getter for testing
    public static class EntityWithFailingGetter {
        public Long getId() {
            throw new RuntimeException("Getter failed");
        }

        public String getName() {
            return "Test";
        }
    }

    // Entity with empty string fields for testing
    public static class EntityWithEmptyFields {
        private Long id;
        private String name;

        public EntityWithEmptyFields(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }


}
