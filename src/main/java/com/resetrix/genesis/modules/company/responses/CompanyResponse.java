package com.resetrix.genesis.modules.company.responses;

import java.sql.Timestamp;
import java.util.UUID;

public record CompanyResponse(
    Long id,
    UUID uuid,
    String name,
    String registrationNumber,
    String logo,
    Boolean softDelete,
    Timestamp createdAt,
    Timestamp updatedAt
) {
}