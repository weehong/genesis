package com.resetrix.horaion.modules.constraint.responses;

import com.resetrix.horaion.modules.constraint.properties.Schema;

import java.sql.Timestamp;
import java.util.UUID;

public record ConstraintResponse(
    Long id,
    UUID uuid,
    String name,
    String description,
    String sentence,
    Schema schemas,
    Boolean softDelete,
    Timestamp createdAt,
    Timestamp updatedAt
) {
}
