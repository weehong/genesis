package com.resetrix.genesis.shared.responses;

import java.util.List;

public record ValidationErrorResponse(
        String type,
        String title,
        Integer status,
        String detail,
        String instance,
        List<ValidationError> errors
) {
    public record ValidationError(
            String field,
            String message
    ) {
    }
}