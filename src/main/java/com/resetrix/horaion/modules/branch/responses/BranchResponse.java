package com.resetrix.horaion.modules.branch.responses;

import java.sql.Timestamp;
import java.util.UUID;

public record BranchResponse(
    Long id,
    UUID uuid,
    Long companyId,
    String companyName,
    String branchName,
    String branchCode,
    String address,
    String city,
    String state,
    String country,
    String postalCode,
    String phoneNumber,
    String emailAddress,
    Boolean softDelete,
    Timestamp createdAt,
    Timestamp updatedAt
) {
}
