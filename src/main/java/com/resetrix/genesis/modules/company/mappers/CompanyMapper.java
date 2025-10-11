package com.resetrix.genesis.modules.company.mappers;

import com.resetrix.genesis.modules.company.entities.Company;
import com.resetrix.genesis.modules.company.requests.CompanyRequest;
import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class CompanyMapper {

    public CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
            company.getId(),
            company.getUuid(),
            company.getName(),
            company.getRegistrationNumber(),
            encodeLogoToBase64(company.getLogo()),
            company.getSoftDelete(),
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }

    public Company toEntity(CompanyRequest request, byte[] logo) {
        Company company = new Company();
        mapRequestToEntity(company, request, logo);
        return company;
    }

    public Company updateEntity(Company company, CompanyRequest request, byte[] logo) {
        mapRequestToEntity(company, request, logo);
        return company;
    }

    private void mapRequestToEntity(Company company, CompanyRequest request, byte[] logo) {
        company.setName(request.name());
        company.setRegistrationNumber(request.registrationNumber());

        if (isLogoValid(logo)) {
            company.setLogo(logo);
        }
    }

    private String encodeLogoToBase64(byte[] logo) {
        return logo != null
               ? Base64.getEncoder().encodeToString(logo)
               : null;
    }

    private boolean isLogoValid(byte[] logo) {
        return logo != null && logo.length > 0;
    }
}