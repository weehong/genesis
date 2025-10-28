package com.resetrix.horaion.modules.constraint.properties;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldOptions(
    List<FieldOption> option,
    Integer min,
    Integer max
) {
}
