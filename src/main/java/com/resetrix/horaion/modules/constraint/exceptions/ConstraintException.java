package com.resetrix.horaion.modules.constraint.exceptions;

public class ConstraintException extends RuntimeException {

    public ConstraintException(String message) {
        super(message);
    }

    public ConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
