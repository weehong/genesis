package com.resetrix.horaion.modules.constraint.services;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;
import com.resetrix.horaion.modules.constraint.properties.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConstraintSchemaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintSchemaService.class);

    private final FieldOptionService fieldOptionService;

    public ConstraintSchemaService(FieldOptionService fieldOptionService) {
        this.fieldOptionService = fieldOptionService;
    }

    public Schema resolveFieldOptions(Schema schema) {
        if (schema == null || schema.fields() == null) {
            return schema;
        }

        List<FieldDefinition> resolvedFields = schema.fields()
            .stream()
            .map(this::resolveFieldDefinitionOptions)
            .toList();

        return new Schema(resolvedFields);
    }

    public FieldDefinition resolveFieldDefinitionOptions(FieldDefinition fieldDefinition) {
        if (fieldDefinition.sourceType() != SourceType.DATABASE) {
            return fieldDefinition;
        }

        try {
            List<FieldOption> resolvedOptions = fieldOptionService.resolveOptions(fieldDefinition);
            return createFieldDefinitionWithResolvedOptions(fieldDefinition, resolvedOptions);
        } catch (Exception e) {
            LOGGER.warn("Failed to resolve options for field '{}': {}",
                        fieldDefinition.name(), e.getMessage());
            throw e;
        }
    }

    private FieldDefinition createFieldDefinitionWithResolvedOptions(
        FieldDefinition fieldDefinition,
        List<FieldOption> resolvedOptions) {

        FieldOptions updatedOptions = new FieldOptions(
            resolvedOptions,
            fieldDefinition.options() != null
            ? fieldDefinition.options().min()
            : null,
            fieldDefinition.options() != null
            ? fieldDefinition.options().max()
            : null
        );

        return new FieldDefinition(
            fieldDefinition.id(),
            fieldDefinition.name(),
            fieldDefinition.type(),
            fieldDefinition.label(),
            fieldDefinition.placeholder(),
            updatedOptions,
            fieldDefinition.sourceType()
        );
    }
}
