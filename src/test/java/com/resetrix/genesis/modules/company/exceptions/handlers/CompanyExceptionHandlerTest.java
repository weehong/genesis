package com.resetrix.genesis.modules.company.exceptions.handlers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private CompanyExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CompanyExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/companies/test");
    }

    @Test
    void shouldHandleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException("Company not found with id: 123");

        ProblemDetail response = handler.handleEntityNotFoundException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("Company not found with id: 123");
        assertThat(response.getTitle()).isEqualTo("Entity Not Found");
        assertThat(response.getInstance().toString()).isEqualTo("/api/companies/test");
    }

    @Test
    void shouldHandleDataIntegrityViolationException() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Unique constraint violation: company name already exists"
        );

        ProblemDetail response = handler.handleDataIntegrityViolationException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getDetail()).isEqualTo("Unique constraint violation: company name already exists");
        assertThat(response.getTitle()).isEqualTo("Data Integrity Violation");
        assertThat(response.getInstance().toString()).isEqualTo("/api/companies/test");
    }

    @Test
    void shouldHandleInvalidDataAccessResourceUsageException() {
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException(
            "Invalid SQL syntax in query"
        );

        ProblemDetail response = handler.handleInvalidDataAccessResourceUsageException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).isEqualTo("Invalid SQL syntax in query");
        assertThat(response.getTitle()).isEqualTo("Invalid Data Access Resource");
        assertThat(response.getInstance().toString()).isEqualTo("/api/companies/test");
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid company status value");

        ProblemDetail response = handler.handleIllegalArgumentException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).isEqualTo("Invalid company status value");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getInstance().toString()).isEqualTo("/api/companies/test");
    }

    @Test
    void shouldHandleDataAccessException() {
        DataAccessException exception = new DataAccessException("Database connection failed") {};

        ProblemDetail response = handler.handleDataAccessException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("A database error occurred while processing your request");
        assertThat(response.getTitle()).isEqualTo("Internal Server Error");
        assertThat(response.getInstance().toString()).isEqualTo("/api/companies/test");
    }

    @Test
    void shouldHandleDataIntegrityViolationExceptionWithDifferentMessage() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Foreign key constraint violation"
        );

        ProblemDetail response = handler.handleDataIntegrityViolationException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getDetail()).isEqualTo("Foreign key constraint violation");
        assertThat(response.getTitle()).isEqualTo("Data Integrity Violation");
    }

    @Test
    void shouldHandleInvalidDataAccessResourceUsageExceptionWithNullMessage() {
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException(
            "Resource not available"
        );

        ProblemDetail response = handler.handleInvalidDataAccessResourceUsageException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getTitle()).isEqualTo("Invalid Data Access Resource");
    }
}
