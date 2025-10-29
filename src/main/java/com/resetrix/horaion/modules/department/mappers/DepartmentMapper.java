package com.resetrix.horaion.modules.department.mappers;

import com.resetrix.horaion.modules.branch.entities.Branch;
import com.resetrix.horaion.modules.department.entities.Department;
import com.resetrix.horaion.modules.department.requests.DepartmentRequest;
import com.resetrix.horaion.modules.department.responses.DepartmentResponse;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
            department.getId(),
            department.getUuid(),
            department.getBranch().getId(),
            department.getBranch().getBranchName(),
            department.getBranch().getCompany().getId(),
            department.getBranch().getCompany().getName(),
            department.getDepartmentName(),
            department.getDepartmentCode(),
            department.getDescription(),
            department.getSoftDelete(),
            department.getCreatedAt(),
            department.getUpdatedAt()
        );
    }

    public Department toEntity(DepartmentRequest request, Branch branch) {
        Department department = new Department();
        mapRequestToEntity(department, request, branch);
        return department;
    }

    public void mapRequestToEntity(Department department, DepartmentRequest request, Branch branch) {
        department.setBranch(branch);
        department.setDepartmentName(request.departmentName());
        department.setDepartmentCode(request.departmentCode());
        department.setDescription(request.description());
    }
}
