package com.resetrix.horaion.modules.department.responses;

import java.sql.Timestamp;
import java.util.UUID;

public record DepartmentResponse(
    Long id,
    UUID uuid,
    Long branchId,
    String branchName,
    Long companyId,
    String companyName,
    String departmentName,
    String departmentCode,
    String description,
    Boolean softDelete,
    Timestamp createdAt,
    Timestamp updatedAt
) {
}
