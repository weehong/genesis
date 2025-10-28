package com.resetrix.horaion.modules.branch.controllers;

import java.util.List;
import java.util.UUID;

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

import com.resetrix.horaion.modules.branch.requests.BranchRequest;
import com.resetrix.horaion.modules.branch.responses.BranchResponse;
import com.resetrix.horaion.modules.branch.services.IBranchService;

import jakarta.validation.Valid;


@RestController
@RequestMapping(value = "/api/v1/companies/{companyId:(?:[0-9]+|[0-9a-fA-F\\-]{36})}/branches")
public class BranchController {

    private final IBranchService<BranchRequest, BranchResponse> service;

    public BranchController(IBranchService<BranchRequest, BranchResponse> service) {
        this.service = service;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.canAccessCompanyResources(authentication, #companyId)")
    public Page<BranchResponse> findAllByCompanyId(
        @PathVariable String companyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        if (isNumericId(companyId)) {
            return service.getAllByCompanyId(Long.valueOf(companyId), page, size, sortBy, sortDirection);
        } else {
            // Assuming your service has a method that accepts UUID
            return service.getAllByCompanyId(UUID.fromString(companyId), page, size, sortBy, sortDirection);
        }
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.canAccessCompanyResources(authentication, #companyId)")
    public List<BranchResponse> findAllByCompanyId(@PathVariable String companyId) {
        if (isNumericId(companyId)) {
            return service.getAllByCompanyId(Long.valueOf(companyId));
        } else {
            // Assuming your service has a method that accepts UUID
            return service.getAllByCompanyId(UUID.fromString(companyId));
        }
    }

    @GetMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.canAccessCompanyResources(authentication, #companyId)")
    public BranchResponse findById(@PathVariable String companyId, @PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping(value = "/{uuid:[0-9a-fA-F\\-]{36}}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.canAccessCompanyResources(authentication, #companyId)")
    public BranchResponse findByUuid(@PathVariable String companyId, @PathVariable UUID uuid) {
        return service.getByUuid(uuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.hasElevatedAccess(authentication)")
    public BranchResponse create(
        @PathVariable String companyId,
        @Valid @RequestBody BranchRequest request) {
        validateCompanyIdMatch(companyId, request.companyId());
        return service.save(request);
    }

    @PutMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.hasElevatedAccess(authentication)")
    public BranchResponse updateById(
        @PathVariable String companyId,
        @PathVariable Long id,
        @Valid @RequestBody BranchRequest request) {
        validateCompanyIdMatch(companyId, request.companyId());
        return service.update(id, request);
    }

    @PutMapping(value = "/{uuid:[0-9a-fA-F\\-]{36}}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authorizationService.hasElevatedAccess(authentication)")
    public BranchResponse updateByUuid(
        @PathVariable String companyId,
        @PathVariable UUID uuid,
        @Valid @RequestBody BranchRequest request) {
        validateCompanyIdMatch(companyId, request.companyId());
        return service.updateByUuid(uuid, request);
    }

    @DeleteMapping("/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authorizationService.hasElevatedAccess(authentication)")
    public void deleteById(
        @PathVariable String companyId,
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
    @PreAuthorize("@authorizationService.hasElevatedAccess(authentication)")
    public void deleteByUuid(
        @PathVariable String companyId,
        @PathVariable UUID uuid,
        @RequestParam(defaultValue = "false") boolean soft) {
        if (soft) {
            service.softDeleteByUuid(uuid);
        } else {
            service.deleteByUuid(uuid);
        }
    }

    private boolean isNumericId(String companyId) {
        return companyId.matches("[0-9]+");
    }

    private void validateCompanyIdMatch(String pathCompanyId, Long requestCompanyId) {
        if (requestCompanyId == null) {
            return; // Allow null in request - path variable will be authoritative
        }

        // Convert path variable to Long for comparison if it's numeric
        if (isNumericId(pathCompanyId)) {
            if (!Long.valueOf(pathCompanyId).equals(requestCompanyId)) {
                throw new IllegalArgumentException(
                    String.format("Company ID mismatch: path variable (%s) does not match request body (%d)",
                        pathCompanyId, requestCompanyId));
            }
        } else {
            // For UUID path variables, we can't directly compare with Long request companyId
            // This indicates a configuration/usage issue
            throw new IllegalArgumentException(
                "Company ID type mismatch: path variable is UUID but request contains numeric company ID");
        }
    }
}
