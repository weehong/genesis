package com.resetrix.horaion.modules.department.repositories;

import com.resetrix.horaion.modules.department.entities.Department;
import com.resetrix.horaion.shared.repositories.UuidRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends UuidRepository<Department> {

    @Query("SELECT d FROM Department d WHERE d.branch.id = :branchId")
    Page<Department> findByBranchId(@Param("branchId") Long branchId, Pageable pageable);

    @Query("SELECT d FROM Department d WHERE d.branch.id = :branchId")
    List<Department> findByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT d FROM Department d WHERE d.branch.id = :branchId AND d.departmentCode = :departmentCode")
    Optional<Department> findByBranchIdAndDepartmentCode(@Param("branchId") Long branchId,
                                                          @Param("departmentCode") String departmentCode);

    @Query("SELECT d FROM Department d WHERE d.branch.company.id = :companyId")
    Page<Department> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT d FROM Department d WHERE d.branch.company.id = :companyId")
    List<Department> findByCompanyId(@Param("companyId") Long companyId);
}
