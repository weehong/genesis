package com.resetrix.horaion.modules.branch.mappers;

import com.resetrix.horaion.modules.branch.entities.Branch;
import com.resetrix.horaion.modules.branch.requests.BranchRequest;
import com.resetrix.horaion.modules.branch.responses.BranchResponse;
import com.resetrix.horaion.modules.company.entities.Company;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
            branch.getId(),
            branch.getUuid(),
            branch.getCompany().getId(),
            branch.getCompany().getName(),
            branch.getBranchName(),
            branch.getBranchCode(),
            branch.getAddress(),
            branch.getCity(),
            branch.getState(),
            branch.getCountry(),
            branch.getPostalCode(),
            branch.getPhoneNumber(),
            branch.getEmailAddress(),
            branch.getSoftDelete(),
            branch.getCreatedAt(),
            branch.getUpdatedAt()
        );
    }

    public Branch toEntity(BranchRequest request, Company company) {
        Branch branch = new Branch();
        mapRequestToEntity(branch, request, company);
        return branch;
    }

    public Branch updateEntity(Branch branch, BranchRequest request, Company company) {
        mapRequestToEntity(branch, request, company);
        return branch;
    }

    private void mapRequestToEntity(Branch branch, BranchRequest request, Company company) {
        branch.setCompany(company);
        branch.setBranchName(request.branchName());
        branch.setBranchCode(request.branchCode());
        branch.setAddress(request.address());
        branch.setCity(request.city());
        branch.setState(request.state());
        branch.setCountry(request.country());
        branch.setPostalCode(request.postalCode());
        branch.setPhoneNumber(request.phoneNumber());
        branch.setEmailAddress(request.emailAddress());
    }
}
