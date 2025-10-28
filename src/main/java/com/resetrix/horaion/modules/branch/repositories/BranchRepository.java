package com.resetrix.horaion.modules.branch.repositories;

import com.resetrix.horaion.modules.branch.entities.Branch;
import com.resetrix.horaion.shared.repositories.UuidRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends UuidRepository<Branch> {

    @Query("SELECT b FROM Branch b WHERE b.company.id = :companyId")
    Page<Branch> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT b FROM Branch b WHERE b.company.id = :companyId")
    List<Branch> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT b FROM Branch b WHERE b.company.id = :companyId AND b.branchCode = :branchCode")
    Optional<Branch> findByCompanyIdAndBranchCode(@Param("companyId") Long companyId,
                                                   @Param("branchCode") String branchCode);

    @Query("SELECT b FROM Branch b WHERE b.branchCode = :branchCode")
    List<Branch> findByBranchCode(@Param("branchCode") String branchCode);
}
