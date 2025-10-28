package com.resetrix.horaion.modules.branch.exceptions;

public class BranchException extends RuntimeException {
    public BranchException(String message) {
        super(message);
    }

    public BranchException(String message, Throwable cause) {
        super(message, cause);
    }
}
