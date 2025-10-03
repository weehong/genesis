package com.resetrix.genesis.shared.advices;

import com.resetrix.genesis.shared.exceptions.BaseException;
import com.resetrix.genesis.shared.exceptions.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    void shouldHandleBaseExceptionWithNotFoundStatus() {
        BaseException exception = new BaseException(
                "User not found",
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND") {
        };
        exception.addProperty("resourceType", "User")
                .addProperty("identifier", "123");

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBaseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getDetail()).isEqualTo("User not found");
        assertThat(response.getBody().getProperties()).containsEntry("errorCode", "RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getProperties()).containsEntry("resourceType", "User");
        assertThat(response.getBody().getProperties()).containsEntry("identifier", "123");
    }

    @Test
    void shouldHandleBaseExceptionWithUnauthorizedStatus() {
        BaseException exception = new BaseException(
                "Invalid credentials",
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED") {
        };

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBaseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getDetail()).isEqualTo("Invalid credentials");
        assertThat(response.getBody().getProperties()).containsEntry("errorCode", "UNAUTHORIZED");
    }

    @Test
    void shouldHandleBaseExceptionWithForbiddenStatus() {
        BaseException exception = new BaseException(
                "Access denied",
                HttpStatus.FORBIDDEN,
                "FORBIDDEN") {
        };

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBaseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getDetail()).isEqualTo("Access denied");
        assertThat(response.getBody().getProperties()).containsEntry("errorCode", "FORBIDDEN");
    }

    @Test
    void shouldHandleBaseExceptionWithInternalServerErrorStatus() {
        BaseException exception = new BaseException(
                "Database connection failed",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR") {
        };

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBaseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getDetail()).isEqualTo("Database connection failed");
        assertThat(response.getBody().getProperties()).containsEntry("errorCode", "INTERNAL_SERVER_ERROR");
    }

    @Test
    void shouldHandleBaseExceptionWithValidationError() {
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("email", "Invalid email format");
        fieldErrors.put("age", "Must be greater than 0");

        BaseException exception = new BaseException(
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR") {
        };
        exception.addProperty("fieldErrors", fieldErrors);

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBaseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getProperties()).containsEntry("errorCode", "VALIDATION_ERROR");
        assertThat(response.getBody().getProperties()).containsKey("fieldErrors");
    }

    @Test
    void shouldIncludeInstanceUriInProblemDetail() {
        BaseException exception = new BaseException(
                "Product not found",
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND") {
        };

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBaseException(exception, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getInstance()).isNotNull();
        assertThat(response.getBody().getInstance().toString()).isEqualTo("/api/test");
    }

    @Test
    void shouldIncludeCustomPropertiesFromBaseException() {
        BaseException exception = new BaseException(
                "Order not found",
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND") {
        };
        exception.addProperty("orderId", "ORD-123")
                .addProperty("customerId", "CUST-456");

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleBaseException(exception, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties()).containsEntry("orderId", "ORD-123");
        assertThat(response.getBody().getProperties()).containsEntry("customerId", "CUST-456");
    }
}
