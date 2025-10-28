package com.resetrix.horaion.modules.constraint.services;

import com.resetrix.horaion.modules.constraint.entities.Constraint;
import com.resetrix.horaion.modules.constraint.exceptions.ConstraintException;
import com.resetrix.horaion.modules.constraint.mappers.ConstraintMapper;
import com.resetrix.horaion.modules.constraint.properties.Schema;
import com.resetrix.horaion.modules.constraint.repositories.ConstraintRepository;
import com.resetrix.horaion.modules.constraint.requests.ConstraintRequest;
import com.resetrix.horaion.modules.constraint.responses.ConstraintResponse;
import com.resetrix.horaion.shared.helpers.HashHelper;
import com.resetrix.horaion.shared.helpers.RepositoryHelper;
import com.resetrix.horaion.shared.helpers.ServiceOperationExecutor;
import com.resetrix.horaion.shared.helpers.ValidationHelper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public non-sealed class ConstraintService implements IConstraintService<ConstraintRequest, ConstraintResponse> {

    private final ConstraintRepository constraintRepository;
    private final ConstraintMapper constraintMapper;
    private final ConstraintSchemaService schemaService;

    public ConstraintService(ConstraintRepository constraintRepository,
                             ConstraintMapper constraintMapper,
                             ConstraintSchemaService schemaService) {
        this.constraintRepository = constraintRepository;
        this.constraintMapper = constraintMapper;
        this.schemaService = schemaService;
    }

    @Override
    public Page<ConstraintResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        return ServiceOperationExecutor.execute(
            () -> {
                ValidationHelper.validatePaginationParameters(page, size);
                Sort.Direction direction = ValidationHelper.parseSortDirection(sortDirection);
                PageRequest pageRequest = PageRequest.of(page, size, direction, sortBy);

                return constraintRepository.findAll(pageRequest)
                    .map(constraint -> {
                        Schema resolvedSchema = schemaService.resolveFieldOptions(constraint.getFields());
                        return constraintMapper.toResponse(constraint, resolvedSchema);
                    });
            }, "retrieving all constraints", ConstraintException.class);
    }

    @Override
    public ConstraintResponse getById(Long id) {
        return ServiceOperationExecutor.execute(
            () -> {
                ValidationHelper.validateId(id, "Constraint");
                Constraint constraint = RepositoryHelper.findByIdOrThrow(constraintRepository, id, Constraint.class);
                Schema resolvedSchema = schemaService.resolveFieldOptions(constraint.getFields());
                return constraintMapper.toResponse(constraint, resolvedSchema);
            }, "retrieving constraint by ID", ConstraintException.class);
    }

    @Override
    public ConstraintResponse getByUuid(UUID uuid) {
        return ServiceOperationExecutor.execute(
            () -> {
                ValidationHelper.validateUuid(uuid, "Constraint");
                Constraint constraint = RepositoryHelper.findByUuidOrThrow(constraintRepository,
                                                                           uuid,
                                                                           Constraint.class);
                Schema resolvedSchema = schemaService.resolveFieldOptions(constraint.getFields());
                return constraintMapper.toResponse(constraint, resolvedSchema);
            }, "retrieving constraint by UUID", ConstraintException.class);
    }

    @Override
    @Transactional
    public ConstraintResponse save(ConstraintRequest request) {
        return ServiceOperationExecutor.execute(
            () -> {
                Constraint constraint = constraintMapper.toEntity(request);
                computeAndSetSchemaHash(constraint);
                Constraint savedConstraint = constraintRepository.save(constraint);
                return constraintMapper.toResponse(savedConstraint);
            }, "saving the constraint", ConstraintException.class);
    }

    @Override
    @Transactional
    public ConstraintResponse update(Long id, ConstraintRequest request) {
        return ServiceOperationExecutor.execute(
            () -> {
                Constraint constraint = RepositoryHelper.findByIdOrThrow(constraintRepository,
                                                                         id,
                                                                         Constraint.class);
                Constraint updatedConstraint = constraintMapper.updateEntity(constraint, request);
                computeAndSetSchemaHash(updatedConstraint);
                Constraint savedConstraint = constraintRepository.save(updatedConstraint);
                return constraintMapper.toResponse(savedConstraint);
            }, "updating the constraint", ConstraintException.class);
    }

    @Override
    @Transactional
    public ConstraintResponse updateByUuid(UUID uuid, ConstraintRequest request) {
        return ServiceOperationExecutor.execute(
            () -> {
                Constraint constraint = RepositoryHelper.findByUuidOrThrow(constraintRepository,
                                                                           uuid,
                                                                           Constraint.class);
                Constraint updatedConstraint = constraintMapper.updateEntity(constraint, request);
                computeAndSetSchemaHash(updatedConstraint);
                Constraint savedConstraint = constraintRepository.save(updatedConstraint);
                return constraintMapper.toResponse(savedConstraint);
            }, "updating the constraint", ConstraintException.class);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        ServiceOperationExecutor.executeVoid(
            () -> {
                Constraint constraint = RepositoryHelper.findByIdOrThrow(constraintRepository, id, Constraint.class);
                constraint.setSoftDelete(true);
                constraintRepository.save(constraint);
            }, "soft-deleting the constraint", ConstraintException.class);
    }

    @Override
    @Transactional
    public void softDeleteByUuid(UUID uuid) {
        ServiceOperationExecutor.executeVoid(
            () -> {
                Constraint constraint = RepositoryHelper.findByUuidOrThrow(constraintRepository,
                                                                           uuid,
                                                                           Constraint.class);
                constraint.setSoftDelete(true);
                constraintRepository.save(constraint);
            }, "soft-deleting the constraint", ConstraintException.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ServiceOperationExecutor.executeVoid(
            () -> {
                Constraint constraint = RepositoryHelper.findByIdOrThrow(constraintRepository, id, Constraint.class);
                constraintRepository.delete(constraint);
            }, "deleting the constraint", ConstraintException.class);
    }

    @Override
    @Transactional
    public void deleteByUuid(UUID uuid) {
        ServiceOperationExecutor.executeVoid(
            () -> {
                Constraint constraint = RepositoryHelper.findByUuidOrThrow(constraintRepository,
                                                                           uuid,
                                                                           Constraint.class);
                constraintRepository.delete(constraint);
            }, "deleting the constraint", ConstraintException.class);
    }

    private void computeAndSetSchemaHash(Constraint constraint) {
        if (constraint.getFields() != null) {
            String hash = HashHelper.computeHash(constraint.getFields());
            constraint.setFieldsHash(hash);
        }
    }
}
