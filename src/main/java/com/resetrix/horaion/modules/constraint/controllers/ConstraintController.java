package com.resetrix.horaion.modules.constraint.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.resetrix.horaion.modules.constraint.requests.ConstraintRequest;
import com.resetrix.horaion.modules.constraint.responses.ConstraintResponse;
import com.resetrix.horaion.modules.constraint.services.IConstraintService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/constraints")
public class ConstraintController {

    private final IConstraintService<ConstraintRequest, ConstraintResponse> service;

    public ConstraintController(IConstraintService<ConstraintRequest, ConstraintResponse> service) {
        this.service = service;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public Page<ConstraintResponse> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        return service.getAll(page, size, sortBy, sortDirection);
    }

    @GetMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public ConstraintResponse findById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping(value = "/{uuid:[0-9a-fA-F\\-]{36}}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER')")
    public ConstraintResponse findByUuid(@PathVariable UUID uuid) {
        return service.getByUuid(uuid);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public ConstraintResponse create(@Valid @RequestBody ConstraintRequest request) {
        return service.save(request);
    }

    @PutMapping(
        value = "/{id:[0-9]+}",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public ConstraintResponse updateById(
        @PathVariable Long id,
        @Valid @RequestBody ConstraintRequest request) {
        return service.update(id, request);
    }

    @PutMapping(
        value = "/{uuid:[0-9a-fA-F\\-]{36}}",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER')")
    public ConstraintResponse updateByUuid(
        @PathVariable UUID uuid,
        @Valid @RequestBody ConstraintRequest request) {
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
