package com.resetrix.genesis.modules.company.exceptions;

public class CustomDatabaseException extends RuntimeException {
    public CustomDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
