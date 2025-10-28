package com.resetrix.horaion.modules.constraint.strategies;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy implementation for fetching field options from database using repository interfaces.
 * Handles the DATABASE source type by calling appropriate repository methods.
 */
@Component
public class RepositoryDataSourceStrategy implements DataSourceStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDataSourceStrategy.class);

    private final ApplicationContext applicationContext;

    public RepositoryDataSourceStrategy(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<FieldOption> fetchOptions(FieldDefinition fieldDefinition) {
        if (!supports(fieldDefinition.sourceType().name())) {
            throw new DataSourceException("RepositoryDataSourceStrategy does not support source type: "
                                              + fieldDefinition.sourceType());
        }

        String entityName = determineEntityName(fieldDefinition.name());
        JpaRepository<?, ?> repository = getRepository(entityName);

        LOGGER.debug("Fetching options from repository for entity: {}", entityName);

        try {
            List<?> entities = repository.findAll();
            return mapEntitiesToOptions(entities);
        } catch (Exception e) {
            LOGGER.error("Error fetching field options from repository", e);
            throw new DataSourceException("Failed to fetch field options from repository: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String sourceType) {
        return SourceType.DATABASE.name().equalsIgnoreCase(sourceType);
    }

    /**
     * Determines entity name from field name using naming conventions.
     * Examples: "company" -> "Company", "user_role" -> "UserRole"
     */
    private String determineEntityName(String fieldName) {
        // Remove common suffixes like "_id", "_selection", etc.
        String cleanName = fieldName.replaceAll("_(id|selection|option)$", "");

        // Convert snake_case to PascalCase
        String[] parts = cleanName.split("_");
        StringBuilder entityName = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                entityName.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase());
            }
        }

        return entityName.toString();
    }

    /**
     * Gets the repository bean for the specified entity.
     */
    @SuppressWarnings("unchecked")
    private JpaRepository<?, ?> getRepository(String entityName) {
        String repositoryBeanName = entityName.toLowerCase() + "Repository";

        try {
            return (JpaRepository<?, ?>) applicationContext.getBean(repositoryBeanName);
        } catch (Exception e) {
            throw new DataSourceException("Repository not found for entity: " + entityName
                                              + ". Expected bean name: " + repositoryBeanName, e);
        }
    }

    /**
     * Maps entity objects to FieldOption objects using standard conventions.
     * Uses "id" as value and "name" as label by default.
     */
    private List<FieldOption> mapEntitiesToOptions(List<?> entities) {
        return entities.stream()
            .map(this::mapEntityToOption)
            .collect(Collectors.toList());
    }

    /**
     * Maps a single entity to a FieldOption using standard field conventions.
     * Tries "id" then "uuid" for value, and "name" then "title" for label.
     */
    private FieldOption mapEntityToOption(Object entity) {
        try {
            Object value = getValueField(entity);
            Object label = getLabelField(entity);

            return new FieldOption(value, label != null ? label.toString() : "");
        } catch (Exception e) {
            throw new DataSourceException("Error mapping entity to FieldOption: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the value field from entity, trying common field names.
     */
    private Object getValueField(Object entity) {
        // Try common value field names in order of preference
        String[] valueFields = {"id", "uuid", "code"};

        for (String fieldName : valueFields) {
            try {
                return getFieldValue(entity, fieldName);
            } catch (Exception e) {
                // Continue to next field name
            }
        }

        throw new DataSourceException("No suitable value field found in entity: " + entity.getClass().getSimpleName());
    }

    /**
     * Gets the label field from entity, trying common field names.
     */
    private Object getLabelField(Object entity) {
        // Try common label field names in order of preference
        String[] labelFields = {"name", "title", "description", "label"};

        for (String fieldName : labelFields) {
            try {
                return getFieldValue(entity, fieldName);
            } catch (Exception e) {
                // Continue to next field name
            }
        }

        throw new DataSourceException("No suitable label field found in entity: " + entity.getClass().getSimpleName());
    }

    /**
     * Gets field value from entity using getter method.
     */
    private Object getFieldValue(Object entity, String fieldName) {
        try {
            String getterName = "get" + capitalize(fieldName);
            Method getter = entity.getClass().getMethod(getterName);
            return getter.invoke(entity);
        } catch (Exception e) {
            throw new DataSourceException("Failed to get field value for: " + fieldName, e);
        }
    }

    /**
     * Capitalizes the first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
