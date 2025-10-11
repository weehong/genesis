package com.resetrix.genesis.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class BaseException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final Map<String, Object> customProperties;

    protected BaseException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.customProperties = new HashMap<>();
    }

    protected BaseException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.customProperties = new HashMap<>();
    }

    public BaseException addProperty(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

    public ProblemDetail toProblemDetail(String instance) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, getMessage());
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(httpStatus.getReasonPhrase());

        if (instance != null) {
            problemDetail.setInstance(URI.create(instance));
        }

        // Add error code as a custom property
        if (errorCode != null) {
            problemDetail.setProperty("errorCode", errorCode);
        }

        // Add cause type if present
        if (getCause() != null) {
            problemDetail.setProperty("causeType", getCause().getClass().getSimpleName());
        }

        // Add all custom properties
        customProperties.forEach(problemDetail::setProperty);

        return problemDetail;
    }
}
