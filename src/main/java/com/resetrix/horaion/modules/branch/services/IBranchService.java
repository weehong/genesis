package com.resetrix.horaion.modules.branch.services;

import com.resetrix.horaion.shared.services.contracts.IGenericService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public sealed interface IBranchService<T, K>
    extends IGenericService<T, K>
    permits BranchService {

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
