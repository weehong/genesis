package com.resetrix.horaion.modules.branch.controllers;

import com.resetrix.horaion.modules.branch.requests.BranchRequest;
import com.resetrix.horaion.modules.branch.responses.BranchResponse;
import com.resetrix.horaion.modules.branch.services.IBranchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping(value = "/api/v1/admin/branches")
public class AdminBranchController {

    private final IBranchService<BranchRequest, BranchResponse> service;

    public AdminBranchController(IBranchService<BranchRequest, BranchResponse> service) {
        this.service = service;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    public Page<BranchResponse> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        return service.getAll(page, size, sortBy, sortDirection);
    }

    @GetMapping("/company/{companyId:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public Page<BranchResponse> findAllByCompanyId(
        @PathVariable Long companyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        return service.getAllByCompanyId(companyId, page, size, sortBy, sortDirection);
    }

    @GetMapping("/company/{companyId:[0-9a-fA-F\\-]{36}}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public Page<BranchResponse> findAllByCompanyUuid(
        @PathVariable String companyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        return service.getAllByCompanyId(UUID.fromString(companyId), page, size, sortBy, sortDirection);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public BranchResponse create(@Valid @RequestBody BranchRequest request) {
        return service.save(request);
    }

    @PutMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public BranchResponse updateById(
        @PathVariable Long id,
        @Valid @RequestBody BranchRequest request) {
        return service.update(id, request);
    }

    @PutMapping(value = "/{uuid:[0-9a-fA-F\\-]{36}}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public BranchResponse updateByUuid(
        @PathVariable UUID uuid,
        @Valid @RequestBody BranchRequest request) {
        return service.updateByUuid(uuid, request);
    }

    @DeleteMapping("/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public void deleteById(
        @PathVariable Long id,
        @RequestParam(defaultValue = "false") boolean soft) {
        if (soft) {
            service.softDelete(id);
        } else {
            service.delete(id);
        }
    }

    @DeleteMapping("/{uuid:[0-9a-fA-F\\-]{36}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public void deleteByUuid(
        @PathVariable UUID uuid,
        @RequestParam(defaultValue = "false") boolean soft) {
        if (soft) {
            service.softDeleteByUuid(uuid);
        } else {
            service.deleteByUuid(uuid);
        }
    }
}
