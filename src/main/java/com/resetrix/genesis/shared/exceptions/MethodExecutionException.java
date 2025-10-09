package com.resetrix.genesis.shared.exceptions;

import lombok.Getter;

@Getter
public class MethodExecutionException extends RuntimeException {

    private final String methodName;
    private final long executionTimeMs;

    public MethodExecutionException(String methodName, long executionTimeMs, Throwable cause) {
        super(String.format("Execution failed for method: %s after %dms", methodName, executionTimeMs), cause);
        this.methodName = methodName;
        this.executionTimeMs = executionTimeMs;
    }
}
