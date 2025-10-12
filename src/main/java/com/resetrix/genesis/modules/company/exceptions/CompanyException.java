package com.resetrix.genesis.modules.company.exceptions;

public class CompanyException extends RuntimeException {
    public CompanyException(String message) {
        super(message);
    }

    public CompanyException(String message, Throwable cause) {
        super(message, cause);
    }
}
