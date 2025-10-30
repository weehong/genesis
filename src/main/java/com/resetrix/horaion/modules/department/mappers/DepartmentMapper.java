package com.resetrix.horaion.modules.department.mappers;

import org.springframework.stereotype.Component;

import com.resetrix.horaion.modules.branch.entities.Branch;
import com.resetrix.horaion.modules.department.entities.Department;
import com.resetrix.horaion.modules.department.requests.DepartmentRequest;
import com.resetrix.horaion.modules.department.responses.DepartmentResponse;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department) {
        // Safely extract branch and company information
        Branch branch = department.getBranch();
        Long branchId = branch != null ? branch.getId() : null;
        String branchName = branch != null ? branch.getBranchName() : null;
        
        // Safely extract company information from branch
        Long companyId = null;
        String companyName = null;
        if (branch != null && branch.getCompany() != null) {
            companyId = branch.getCompany().getId();
            companyName = branch.getCompany().getName();
        }
        
        return new DepartmentResponse(
            department.getId(),
            department.getUuid(),
            branchId,
            branchName,
            companyId,
            companyName,
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
