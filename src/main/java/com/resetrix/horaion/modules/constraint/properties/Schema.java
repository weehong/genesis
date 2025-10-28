package com.resetrix.horaion.modules.constraint.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record Schema(
    @NotEmpty(message = "At least one field is required")
    @Valid
    List<FieldDefinition> fields
) {
}
