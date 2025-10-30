package com.resetrix.horaion.modules.department.services;

import com.resetrix.horaion.shared.services.contracts.IGenericService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public sealed interface IDepartmentService<T, K>
    extends IGenericService<T, K>
    permits DepartmentService {

    // Methods for Long branch ID
    Page<K> getAllByBranchId(
            Long branchId,
            int page,
            int size,
            String sortBy,
            String sortDirection);

    List<K> getAllByBranchId(Long branchId);

    // Methods for UUID branch ID
    Page<K> getAllByBranchId(
            UUID branchId,
            int page,
            int size,
            String sortBy,
            String sortDirection);

    List<K> getAllByBranchId(UUID branchId);

    // Methods for Long company ID
    Page<K> getAllByCompanyId(
            Long companyId,
            int page,
            int size,
            String sortBy,
            String sortDirection);

    List<K> getAllByCompanyId(Long companyId);

    // Methods for UUID company ID
    Page<K> getAllByCompanyId(
            UUID companyId,
            int page,
            int size,
            String sortBy,
            String sortDirection);

    List<K> getAllByCompanyId(UUID companyId);
}
