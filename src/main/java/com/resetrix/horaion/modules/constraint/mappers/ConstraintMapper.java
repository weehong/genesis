package com.resetrix.horaion.modules.constraint.mappers;

import com.resetrix.horaion.modules.constraint.entities.Constraint;
import com.resetrix.horaion.modules.constraint.properties.Schema;
import com.resetrix.horaion.modules.constraint.requests.ConstraintRequest;
import com.resetrix.horaion.modules.constraint.responses.ConstraintResponse;
import org.springframework.stereotype.Component;

@Component
public class ConstraintMapper {

    public ConstraintResponse toResponse(Constraint constraint) {
        return new ConstraintResponse(
            constraint.getId(),
            constraint.getUuid(),
            constraint.getName(),
            constraint.getDescription(),
            constraint.getSentence(),
            constraint.getFields(),
            constraint.getSoftDelete(),
            constraint.getCreatedAt(),
            constraint.getUpdatedAt()
        );
    }

    public ConstraintResponse toResponse(Constraint constraint, Schema resolvedSchema) {
        return new ConstraintResponse(
            constraint.getId(),
            constraint.getUuid(),
            constraint.getName(),
            constraint.getDescription(),
            constraint.getSentence(),
            resolvedSchema,
            constraint.getSoftDelete(),
            constraint.getCreatedAt(),
            constraint.getUpdatedAt()
        );
    }

    public Constraint toEntity(ConstraintRequest request) {
        Constraint constraint = new Constraint();
        mapRequestToEntity(constraint, request);
        return constraint;
    }

    public Constraint updateEntity(Constraint constraint, ConstraintRequest request) {
        mapRequestToEntity(constraint, request);
        return constraint;
    }

    private void mapRequestToEntity(Constraint constraint, ConstraintRequest request) {
        constraint.setName(request.name());
        constraint.setDescription(request.description());
        constraint.setSentence(request.sentence());
        constraint.setFields(request.schema());
    }
}
