package com.resetrix.genesis.modules.company.services;

import com.resetrix.genesis.modules.company.entities.Company;
import com.resetrix.genesis.modules.company.exceptions.CompanyException;
import com.resetrix.genesis.modules.company.exceptions.InvalidFileException;
import com.resetrix.genesis.modules.company.mappers.CompanyMapper;
import com.resetrix.genesis.modules.company.repositories.CompanyRepository;
import com.resetrix.genesis.modules.company.requests.CompanyRequest;
import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import com.resetrix.genesis.shared.helpers.ServiceOperationExecutor;
import com.resetrix.genesis.shared.helpers.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public non-sealed class CompanyService implements ICompanyService<CompanyRequest, CompanyResponse> {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository,
                          CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Override
    public Page<CompanyResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        return ServiceOperationExecutor.execute(() -> {
            validatePaginationParameters(page, size);
            Sort.Direction direction = parseSortDirection(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, direction, sortBy);

            return companyRepository.findAll(pageRequest)
                    .map(companyMapper::toResponse);
        }, "retrieving all companies", CompanyException.class);
    }

    @Override
    public CompanyResponse getById(Long id) {
        return ServiceOperationExecutor.execute(() -> {
            validateId(id);
            Company company = companyRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Company with id %d does not exist", id)
                    ));
            return companyMapper.toResponse(company);
        }, "retrieving company by ID", CompanyException.class);
    }

    @Override
    public CompanyResponse getByUuid(UUID uuid) {
        return ServiceOperationExecutor.execute(() -> {
            validateUuid(uuid);
            Company company = companyRepository.findByUuid(uuid)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Company with uuid %s does not exist", uuid)
                    ));
            return companyMapper.toResponse(company);
        }, "retrieving company by UUID", CompanyException.class);
    }

    @Override
    @Transactional
    public CompanyResponse save(CompanyRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            byte[] logo = extractLogo(request);
            Company company = companyMapper.toEntity(request, logo);
            Company savedCompany = companyRepository.save(company);
            return companyMapper.toResponse(savedCompany);
        }, "saving the company", CompanyException.class);
    }

    @Override
    @Transactional
    public CompanyResponse update(Long id, CompanyRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            Company company = RepositoryHelper.findByIdOrThrow(companyRepository, id, Company.class);
            byte[] logo = extractLogo(request);
            Company savedCompany = companyMapper.updateEntity(company, request, logo);
            savedCompany = companyRepository.save(savedCompany);
            return companyMapper.toResponse(savedCompany);
        }, "updating the company", CompanyException.class);
    }

    @Override
    @Transactional
    public CompanyResponse updateByUuid(UUID uuid, CompanyRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            Company company = RepositoryHelper.findByUuidOrThrow(companyRepository, uuid, Company.class);
            byte[] logo = extractLogo(request);
            Company savedCompany = companyMapper.updateEntity(company, request, logo);
            savedCompany = companyRepository.save(savedCompany);
            return companyMapper.toResponse(savedCompany);
        }, "updating the company", CompanyException.class);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        ServiceOperationExecutor.executeVoid(() -> {
            Company company = RepositoryHelper.findByIdOrThrow(companyRepository, id, Company.class);
            company.setSoftDelete(true);
            companyRepository.save(company);
        }, "soft-deleting the company", CompanyException.class);
    }

    @Override
    @Transactional
    public void softDeleteByUuid(UUID uuid) {
        ServiceOperationExecutor.executeVoid(() -> {
            Company company = RepositoryHelper.findByUuidOrThrow(companyRepository, uuid, Company.class);
            company.setSoftDelete(true);
            companyRepository.save(company);
        }, "soft-deleting the company", CompanyException.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ServiceOperationExecutor.executeVoid(() -> {
            Company company = RepositoryHelper.findByIdOrThrow(companyRepository, id, Company.class);
            companyRepository.delete(company);
        }, "deleting the company", CompanyException.class);
    }

    @Override
    @Transactional
    public void deleteByUuid(UUID uuid) {
        ServiceOperationExecutor.executeVoid(() -> {
            Company company = RepositoryHelper.findByUuidOrThrow(companyRepository, uuid, Company.class);
            companyRepository.delete(company);
        }, "deleting the company", CompanyException.class);
    }

    // Private helper methods
    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 1000) {
            throw new IllegalArgumentException("Size must be > 0 and <= 1000");
        }
    }

    private Sort.Direction parseSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return Sort.Direction.ASC;
        }

        String normalizedDirection = sortDirection.trim().toUpperCase();
        if ("ASC".equals(normalizedDirection)) {
            return Sort.Direction.ASC;
        } else if ("DESC".equals(normalizedDirection)) {
            return Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException("Invalid sortDirection: must be 'ASC' or 'DESC'");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Company ID must be a positive number");
        }
    }

    private void validateUuid(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Company UUID cannot be null");
        }
    }

    private byte[] extractLogo(CompanyRequest request) {
        if (request.logo() == null || request.logo().isEmpty()) {
            return new byte[]{};
        }

        try {
            return request.logo().getBytes();
        } catch (IOException e) {
            throw new InvalidFileException("Failed to process company logo", e);
        }
    }
}