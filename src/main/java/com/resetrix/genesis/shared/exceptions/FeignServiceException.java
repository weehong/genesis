package com.resetrix.genesis.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when Feign client operations fail.
 * Used for handling errors from external service calls via Feign clients.
 */
@Getter
public class FeignServiceException extends BaseException {

    private final Integer statusCode;
    private final String responseBody;

    public FeignServiceException(String message, Throwable cause) {
        this(message, cause, null, null);
    }

    public FeignServiceException(String message, Throwable cause, Integer statusCode, String responseBody) {
        super(message, mapStatusCode(statusCode), "FEIGN_SERVICE_ERROR");
        this.statusCode = statusCode;
        this.responseBody = responseBody;

        if (statusCode != null) {
            addProperty("httpStatusCode", statusCode);
        }
        if (responseBody != null && !responseBody.isEmpty()) {
            addProperty("responseBody", responseBody);
        }
        if (cause != null) {
            addProperty("causeType", cause.getClass().getSimpleName());
        }
    }

    private static HttpStatus mapStatusCode(Integer statusCode) {
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        try {
            return HttpStatus.resolve(statusCode);
        } catch (IllegalArgumentException ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
