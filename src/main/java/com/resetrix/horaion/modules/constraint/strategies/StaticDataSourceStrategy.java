package com.resetrix.horaion.modules.constraint.strategies;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Strategy implementation for static field options.
 * Handles INPUT and SELECT source types by returning predefined options.
 */
@Component
public class StaticDataSourceStrategy implements DataSourceStrategy {

    @Override
    public List<FieldOption> fetchOptions(FieldDefinition fieldDefinition) {
        if (!supports(fieldDefinition.sourceType().name())) {
            throw new DataSourceException("StaticDataSourceStrategy does not support source type: "
                + fieldDefinition.sourceType());
        }

        // For static source types, return the options directly from the field definition
        if (fieldDefinition.options() != null && fieldDefinition.options().option() != null) {
            return fieldDefinition.options().option();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean supports(String sourceType) {
        return SourceType.INPUT.name().equalsIgnoreCase(sourceType)
               || SourceType.SELECT.name().equalsIgnoreCase(sourceType);
    }
}
