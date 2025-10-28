package com.resetrix.horaion.modules.branch.services;

import com.resetrix.horaion.modules.branch.entities.Branch;
import com.resetrix.horaion.modules.branch.exceptions.BranchException;
import com.resetrix.horaion.modules.branch.mappers.BranchMapper;
import com.resetrix.horaion.modules.branch.repositories.BranchRepository;
import com.resetrix.horaion.modules.branch.requests.BranchRequest;
import com.resetrix.horaion.modules.branch.responses.BranchResponse;
import com.resetrix.horaion.modules.company.entities.Company;
import com.resetrix.horaion.modules.company.repositories.CompanyRepository;
import com.resetrix.horaion.shared.helpers.RepositoryHelper;
import com.resetrix.horaion.shared.helpers.ServiceOperationExecutor;
import com.resetrix.horaion.shared.helpers.ValidationHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public non-sealed class BranchService implements IBranchService<BranchRequest, BranchResponse> {
    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;
    private final BranchMapper branchMapper;

    public BranchService(BranchRepository branchRepository,
                         CompanyRepository companyRepository,
                         BranchMapper branchMapper) {
        this.branchRepository = branchRepository;
        this.companyRepository = companyRepository;
        this.branchMapper = branchMapper;
    }

    @Override
    public Page<BranchResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validatePaginationParameters(page, size);
            Sort.Direction direction = ValidationHelper.parseSortDirection(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, direction, sortBy);

            return branchRepository.findAll(pageRequest)
                .map(branchMapper::toResponse);
        }, "retrieving all branches", BranchException.class);
    }

    @Override
    public Page<BranchResponse> getAllByCompanyId(
            Long companyId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(companyId, "Company");
            ValidationHelper.validatePaginationParameters(page, size);
            Sort.Direction direction = ValidationHelper.parseSortDirection(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, direction, sortBy);

            return branchRepository.findByCompanyId(companyId, pageRequest)
                .map(branchMapper::toResponse);
        }, "retrieving branches by company ID", BranchException.class);
    }

    @Override
    public List<BranchResponse> getAllByCompanyId(Long companyId) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(companyId, "Company");

            return branchRepository.findByCompanyId(companyId)
                .stream()
                .map(branchMapper::toResponse)
                .toList();
        }, "retrieving all branches by company ID", BranchException.class);
    }

    @Override
    public Page<BranchResponse> getAllByCompanyId(
            UUID companyId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(companyId, "Company");
            ValidationHelper.validatePaginationParameters(page, size);
            Sort.Direction direction = ValidationHelper.parseSortDirection(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, direction, sortBy);

            // Find company by UUID first, then get its Long ID
            Company company = companyRepository.findByUuid(companyId)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Company with uuid %s does not exist", companyId)
                ));

            return branchRepository.findByCompanyId(company.getId(), pageRequest)
                .map(branchMapper::toResponse);
        }, "retrieving branches by company UUID", BranchException.class);
    }

    @Override
    public List<BranchResponse> getAllByCompanyId(UUID companyId) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(companyId, "Company");

            // Find company by UUID first, then get its Long ID
            Company company = companyRepository.findByUuid(companyId)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Company with uuid %s does not exist", companyId)
                ));

            return branchRepository.findByCompanyId(company.getId())
                .stream()
                .map(branchMapper::toResponse)
                .toList();
        }, "retrieving all branches by company UUID", BranchException.class);
    }

    @Override
    public BranchResponse getById(Long id) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(id, "Branch");
            Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Branch with id %d does not exist", id)
                ));
            return branchMapper.toResponse(branch);
        }, "retrieving branch by ID", BranchException.class);
    }

    @Override
    public BranchResponse getByUuid(UUID uuid) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(uuid, "Branch");
            Branch branch = branchRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Branch with uuid %s does not exist", uuid)
                ));
            return branchMapper.toResponse(branch);
        }, "retrieving branch by UUID", BranchException.class);
    }

    @Override
    @Transactional
    public BranchResponse save(BranchRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            Company company = RepositoryHelper.findByIdOrThrow(companyRepository, request.companyId(), Company.class);

            // Check for duplicate branch code within the same company
            branchRepository.findByCompanyIdAndBranchCode(request.companyId(), request.branchCode())
                .ifPresent(existingBranch -> {
                    throw new IllegalArgumentException(
                        String.format("Branch with code '%s' already exists for company ID %d",
                                      request.branchCode(), request.companyId())
                    );
                });

            Branch branch = branchMapper.toEntity(request, company);
            Branch savedBranch = branchRepository.save(branch);
            return branchMapper.toResponse(savedBranch);
        }, "saving the branch", BranchException.class);
    }

    @Override
    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            Branch branch = RepositoryHelper.findByIdOrThrow(branchRepository, id, Branch.class);
            Company company = RepositoryHelper.findByIdOrThrow(companyRepository, request.companyId(), Company.class);

            // Check for duplicate branch code within the same company (excluding current branch)
            branchRepository.findByCompanyIdAndBranchCode(request.companyId(), request.branchCode())
                .ifPresent(existingBranch -> {
                    if (!existingBranch.getId().equals(id)) {
                        throw new IllegalArgumentException(
                            String.format("Branch with code '%s' already exists for company ID %d",
                                          request.branchCode(), request.companyId())
                        );
                    }
                });

            Branch savedBranch = branchMapper.updateEntity(branch, request, company);
            savedBranch = branchRepository.save(savedBranch);
            return branchMapper.toResponse(savedBranch);
        }, "updating the branch", BranchException.class);
    }

    @Override
    @Transactional
    public BranchResponse updateByUuid(UUID uuid, BranchRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            Branch branch = RepositoryHelper.findByUuidOrThrow(branchRepository, uuid, Branch.class);
            Company company = RepositoryHelper.findByIdOrThrow(companyRepository, request.companyId(), Company.class);

            // Check for duplicate branch code within the same company (excluding current branch)
            branchRepository.findByCompanyIdAndBranchCode(request.companyId(), request.branchCode())
                .ifPresent(existingBranch -> {
                    if (!existingBranch.getUuid().equals(uuid)) {
                        throw new IllegalArgumentException(
                            String.format("Branch with code '%s' already exists for company ID %d",
                                          request.branchCode(), request.companyId())
                        );
                    }
                });

            Branch savedBranch = branchMapper.updateEntity(branch, request, company);
            savedBranch = branchRepository.save(savedBranch);
            return branchMapper.toResponse(savedBranch);
        }, "updating the branch", BranchException.class);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        ServiceOperationExecutor.executeVoid(() -> {
            Branch branch = RepositoryHelper.findByIdOrThrow(branchRepository, id, Branch.class);
            branch.setSoftDelete(true);
            branchRepository.save(branch);
        }, "soft-deleting the branch", BranchException.class);
    }

    @Override
    @Transactional
    public void softDeleteByUuid(UUID uuid) {
        ServiceOperationExecutor.executeVoid(() -> {
            Branch branch = RepositoryHelper.findByUuidOrThrow(branchRepository, uuid, Branch.class);
            branch.setSoftDelete(true);
            branchRepository.save(branch);
        }, "soft-deleting the branch", BranchException.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ServiceOperationExecutor.executeVoid(() -> {
            Branch branch = RepositoryHelper.findByIdOrThrow(branchRepository, id, Branch.class);
            branchRepository.delete(branch);
        }, "deleting the branch", BranchException.class);
    }

    @Override
    @Transactional
    public void deleteByUuid(UUID uuid) {
        ServiceOperationExecutor.executeVoid(() -> {
            Branch branch = RepositoryHelper.findByUuidOrThrow(branchRepository, uuid, Branch.class);
            branchRepository.delete(branch);
        }, "deleting the branch", BranchException.class);
    }
}
