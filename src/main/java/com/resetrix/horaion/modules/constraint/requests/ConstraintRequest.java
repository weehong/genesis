package com.resetrix.horaion.modules.constraint.requests;

import com.resetrix.horaion.modules.constraint.properties.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConstraintRequest(
    @NotBlank(message = "Constraint name is required")
    @Size(max = 255, message = "Constraint name must not exceed 255 characters")
    String name,

    String description,

    @NotBlank(message = "Constraint sentence is required")
    @Size(max = 1000, message = "Constraint sentence must not exceed 1000 characters")
    String sentence,

    @NotNull(message = "Schemas is required")
    @Valid
    Schema schema
) {
}
