package com.resetrix.horaion.modules.department.controllers;

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

import java.util.List;
import java.util.UUID;

import com.resetrix.horaion.modules.department.requests.DepartmentRequest;
import com.resetrix.horaion.modules.department.responses.DepartmentResponse;
import com.resetrix.horaion.modules.department.services.IDepartmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/branches/{branchId:(?:[0-9]+|[0-9a-fA-F\\-]{36})}/departments")
public class DepartmentController {

    private final IDepartmentService<DepartmentRequest, DepartmentResponse> service;

    public DepartmentController(IDepartmentService<DepartmentRequest, DepartmentResponse> service) {
        this.service = service;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public Page<DepartmentResponse> findAllByBranchId(
        @PathVariable String branchId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        if (isNumericId(branchId)) {
            return service.getAllByBranchId(Long.valueOf(branchId), page, size, sortBy, sortDirection);
        } else {
            return service.getAllByBranchId(UUID.fromString(branchId), page, size, sortBy, sortDirection);
        }
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public List<DepartmentResponse> findAllByBranchId(@PathVariable String branchId) {
        if (isNumericId(branchId)) {
            return service.getAllByBranchId(Long.valueOf(branchId));
        } else {
            return service.getAllByBranchId(UUID.fromString(branchId));
        }
    }

    @GetMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public DepartmentResponse findById(@PathVariable String branchId, @PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping(value = "/{uuid:[0-9a-fA-F\\-]{36}}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public DepartmentResponse findByUuid(@PathVariable String branchId, @PathVariable UUID uuid) {
        return service.getByUuid(uuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public DepartmentResponse create(
        @PathVariable String branchId,
        @Valid @RequestBody DepartmentRequest request) {
        validateBranchIdMatch(branchId, request.branchId());
        return service.save(request);
    }

    @PutMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public DepartmentResponse updateById(
        @PathVariable String branchId,
        @PathVariable Long id,
        @Valid @RequestBody DepartmentRequest request) {
        validateBranchIdMatch(branchId, request.branchId());
        return service.update(id, request);
    }

    @PutMapping(value = "/{uuid:[0-9a-fA-F\\-]{36}}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public DepartmentResponse updateByUuid(
        @PathVariable String branchId,
        @PathVariable UUID uuid,
        @Valid @RequestBody DepartmentRequest request) {
        validateBranchIdMatch(branchId, request.branchId());
        return service.updateByUuid(uuid, request);
    }

    @DeleteMapping("/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public void deleteById(
        @PathVariable String branchId,
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
        @PathVariable String branchId,
        @PathVariable UUID uuid,
        @RequestParam(defaultValue = "false") boolean soft) {
        if (soft) {
            service.softDeleteByUuid(uuid);
        } else {
            service.deleteByUuid(uuid);
        }
    }

    private boolean isNumericId(String id) {
        try {
            Long.parseLong(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void validateBranchIdMatch(String pathBranchId, Long requestBranchId) {
        if (isNumericId(pathBranchId)) {
            Long pathBranchIdLong = Long.valueOf(pathBranchId);
            if (!pathBranchIdLong.equals(requestBranchId)) {
                throw new IllegalArgumentException(
                    String.format("Branch ID in path (%d) does not match branch ID in request body (%d)",
                                  pathBranchIdLong, requestBranchId)
                );
            }
        }
        // For UUID path parameters, we would need additional validation logic
        // This is simplified for the current implementation
    }
}
