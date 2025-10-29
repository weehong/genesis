package com.resetrix.horaion.modules.department.services;

import com.resetrix.horaion.modules.branch.entities.Branch;
import com.resetrix.horaion.modules.branch.repositories.BranchRepository;
import com.resetrix.horaion.modules.company.entities.Company;
import com.resetrix.horaion.modules.company.repositories.CompanyRepository;
import com.resetrix.horaion.modules.department.entities.Department;
import com.resetrix.horaion.modules.department.exceptions.DepartmentException;
import com.resetrix.horaion.modules.department.mappers.DepartmentMapper;
import com.resetrix.horaion.modules.department.repositories.DepartmentRepository;
import com.resetrix.horaion.modules.department.requests.DepartmentRequest;
import com.resetrix.horaion.modules.department.responses.DepartmentResponse;
import com.resetrix.horaion.shared.helpers.RepositoryHelper;
import com.resetrix.horaion.shared.helpers.ServiceOperationExecutor;
import com.resetrix.horaion.shared.helpers.ValidationHelper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public non-sealed class DepartmentService implements IDepartmentService<DepartmentRequest, DepartmentResponse> {

    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository,
                             BranchRepository branchRepository,
                             CompanyRepository companyRepository,
                             DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.branchRepository = branchRepository;
        this.companyRepository = companyRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public Page<DepartmentResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validatePaginationParameters(page, size);
            Sort.Direction direction = ValidationHelper.parseSortDirection(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, direction, sortBy);

            return departmentRepository.findAll(pageRequest)
                .map(departmentMapper::toResponse);
        }, "retrieving all departments", DepartmentException.class);
    }

    @Override
    public Page<DepartmentResponse> getAllByBranchId(
        Long branchId,
        int page,
        int size,
        String sortBy,
        String sortDirection) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(branchId, "Branch");
            ValidationHelper.validatePaginationParameters(page, size);
            Sort.Direction direction = ValidationHelper.parseSortDirection(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, direction, sortBy);

            return departmentRepository.findByBranchId(branchId, pageRequest)
                .map(departmentMapper::toResponse);
        }, "retrieving departments by branch ID", DepartmentException.class);
    }

    @Override
    public List<DepartmentResponse> getAllByBranchId(Long branchId) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(branchId, "Branch");

            return departmentRepository.findByBranchId(branchId)
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
        }, "retrieving all departments by branch ID", DepartmentException.class);
    }

    @Override
    public Page<DepartmentResponse> getAllByBranchId(
        UUID branchId,
        int page,
        int size,
        String sortBy,
        String sortDirection) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(branchId, "Branch");
            ValidationHelper.validatePaginationParameters(page, size);
            Sort.Direction direction = ValidationHelper.parseSortDirection(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, direction, sortBy);

            // Find branch by UUID first, then get its Long ID
            Branch branch = branchRepository.findByUuid(branchId)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Branch with uuid %s does not exist", branchId)
                ));

            return departmentRepository.findByBranchId(branch.getId(), pageRequest)
                .map(departmentMapper::toResponse);
        }, "retrieving departments by branch UUID", DepartmentException.class);
    }

    @Override
    public List<DepartmentResponse> getAllByBranchId(UUID branchId) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(branchId, "Branch");

            // Find branch by UUID first, then get its Long ID
            Branch branch = branchRepository.findByUuid(branchId)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Branch with uuid %s does not exist", branchId)
                ));

            return departmentRepository.findByBranchId(branch.getId())
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
        }, "retrieving all departments by branch UUID", DepartmentException.class);
    }

    @Override
    public Page<DepartmentResponse> getAllByCompanyId(
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

            return departmentRepository.findByCompanyId(companyId, pageRequest)
                .map(departmentMapper::toResponse);
        }, "retrieving departments by company ID", DepartmentException.class);
    }

    @Override
    public List<DepartmentResponse> getAllByCompanyId(Long companyId) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(companyId, "Company");

            return departmentRepository.findByCompanyId(companyId)
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
        }, "retrieving all departments by company ID", DepartmentException.class);
    }

    @Override
    public Page<DepartmentResponse> getAllByCompanyId(
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

            return departmentRepository.findByCompanyId(company.getId(), pageRequest)
                .map(departmentMapper::toResponse);
        }, "retrieving departments by company UUID", DepartmentException.class);
    }

    @Override
    public List<DepartmentResponse> getAllByCompanyId(UUID companyId) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(companyId, "Company");

            // Find company by UUID first, then get its Long ID
            Company company = companyRepository.findByUuid(companyId)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Company with uuid %s does not exist", companyId)
                ));

            return departmentRepository.findByCompanyId(company.getId())
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
        }, "retrieving all departments by company UUID", DepartmentException.class);
    }

    @Override
    public DepartmentResponse getById(Long id) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(id, "Department");
            Department department = RepositoryHelper.findByIdOrThrow(departmentRepository, id, Department.class);
            return departmentMapper.toResponse(department);
        }, "retrieving department by ID", DepartmentException.class);
    }

    @Override
    public DepartmentResponse getByUuid(UUID uuid) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(uuid, "Department");
            Department department = departmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Department with uuid %s does not exist", uuid)
                ));
            return departmentMapper.toResponse(department);
        }, "retrieving department by UUID", DepartmentException.class);
    }

    @Override
    @Transactional
    public DepartmentResponse save(DepartmentRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            Branch branch = RepositoryHelper.findByIdOrThrow(branchRepository, request.branchId(), Branch.class);

            // Check for duplicate department code within the same branch
            departmentRepository.findByBranchIdAndDepartmentCode(request.branchId(), request.departmentCode())
                .ifPresent(existingDepartment -> {
                    throw new IllegalArgumentException(
                        String.format("Department with code '%s' already exists for branch ID %d",
                                      request.departmentCode(), request.branchId())
                    );
                });

            Department department = departmentMapper.toEntity(request, branch);
            Department savedDepartment = departmentRepository.save(department);
            return departmentMapper.toResponse(savedDepartment);
        }, "saving department", DepartmentException.class);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(id, "Department");
            Department existingDepartment = RepositoryHelper.findByIdOrThrow(departmentRepository,
                                                                             id,
                                                                             Department.class);
            Branch branch = RepositoryHelper.findByIdOrThrow(branchRepository, request.branchId(), Branch.class);

            // Check for duplicate department code within the same branch (excluding current department)
            departmentRepository.findByBranchIdAndDepartmentCode(request.branchId(), request.departmentCode())
                .ifPresent(foundDepartment -> {
                    if (!foundDepartment.getId().equals(id)) {
                        throw new IllegalArgumentException(
                            String.format("Department with code '%s' already exists for branch ID %d",
                                          request.departmentCode(), request.branchId())
                        );
                    }
                });

            departmentMapper.mapRequestToEntity(existingDepartment, request, branch);
            Department updatedDepartment = departmentRepository.save(existingDepartment);
            return departmentMapper.toResponse(updatedDepartment);
        }, "updating department by ID", DepartmentException.class);
    }

    @Override
    @Transactional
    public DepartmentResponse updateByUuid(UUID uuid, DepartmentRequest request) {
        return ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(uuid, "Department");
            Department existingDepartment = departmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Department with uuid %s does not exist", uuid)
                ));
            Branch branch = RepositoryHelper.findByIdOrThrow(branchRepository, request.branchId(), Branch.class);

            // Check for duplicate department code within the same branch (excluding current department)
            departmentRepository.findByBranchIdAndDepartmentCode(request.branchId(), request.departmentCode())
                .ifPresent(foundDepartment -> {
                    if (!foundDepartment.getUuid().equals(uuid)) {
                        throw new IllegalArgumentException(
                            String.format("Department with code '%s' already exists for branch ID %d",
                                          request.departmentCode(), request.branchId())
                        );
                    }
                });

            departmentMapper.mapRequestToEntity(existingDepartment, request, branch);
            Department updatedDepartment = departmentRepository.save(existingDepartment);
            return departmentMapper.toResponse(updatedDepartment);
        }, "updating department by UUID", DepartmentException.class);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(id, "Department");
            Department department = RepositoryHelper.findByIdOrThrow(departmentRepository, id, Department.class);
            department.setSoftDelete(true);
            departmentRepository.save(department);
            return null;
        }, "soft deleting department by ID", DepartmentException.class);
    }

    @Override
    @Transactional
    public void softDeleteByUuid(UUID uuid) {
        ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(uuid, "Department");
            Department department = departmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Department with uuid %s does not exist", uuid)
                ));
            department.setSoftDelete(true);
            departmentRepository.save(department);
            return null;
        }, "soft deleting department by UUID", DepartmentException.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateId(id, "Department");
            Department department = RepositoryHelper.findByIdOrThrow(departmentRepository, id, Department.class);
            departmentRepository.delete(department);
            return null;
        }, "deleting department by ID", DepartmentException.class);
    }

    @Override
    @Transactional
    public void deleteByUuid(UUID uuid) {
        ServiceOperationExecutor.execute(() -> {
            ValidationHelper.validateUuid(uuid, "Department");
            Department department = departmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Department with uuid %s does not exist", uuid)
                ));
            departmentRepository.delete(department);
            return null;
        }, "deleting department by UUID", DepartmentException.class);
    }
}
