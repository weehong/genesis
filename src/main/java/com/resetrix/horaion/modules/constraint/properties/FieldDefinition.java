package com.resetrix.horaion.modules.constraint.properties;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FieldDefinition(
    @NotNull(message = "Field ID is required")
    @Positive(message = "Field ID must be positive")
    Integer id,

    @NotBlank(message = "Field name is required")
    String name,

    @NotBlank(message = "Field type is required")
    String type,

    @NotBlank(message = "Field label is required")
    String label,

    String placeholder,

    @Valid
    FieldOptions options,

    @NotNull(message = "Source type is required")
    SourceType sourceType
) {
}
