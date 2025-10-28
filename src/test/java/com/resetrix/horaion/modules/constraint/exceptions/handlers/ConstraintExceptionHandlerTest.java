package com.resetrix.horaion.modules.constraint.exceptions.handlers;

import com.resetrix.horaion.modules.constraint.exceptions.ConstraintException;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import com.resetrix.horaion.shared.exceptions.MethodExecutionException;
import com.resetrix.horaion.shared.exceptions.ResourceNotFoundException;
import com.resetrix.horaion.shared.utils.DatabaseErrorAnalyzer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConstraintExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private DatabaseErrorAnalyzer databaseErrorAnalyzer;

    private ConstraintExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ConstraintExceptionHandler(databaseErrorAnalyzer);
        when(request.getRequestURI()).thenReturn("/api/v1/departments/1/constraints/2");
    }

    @Test
    void shouldHandleNoSuchBeanDefinitionException() {
        NoSuchBeanDefinitionException exception = new NoSuchBeanDefinitionException("testBean");

        ProblemDetail response = handler.handleNoSuchBeanDefinitionException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("No bean named 'testBean' available");
        assertThat(response.getTitle()).isEqualTo("Bean Not Found");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException("Constraint not found with id: 123");

        ProblemDetail response = handler.handleEntityNotFoundException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("Constraint not found with id: 123");
        assertThat(response.getTitle()).isEqualTo("Entity Not Found");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        ProblemDetail response = handler.handleResourceNotFoundException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("Resource not found");
        assertThat(response.getTitle()).isEqualTo("Resource Not Found");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleDataIntegrityViolationException() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Unique constraint violation: constraint name already exists"
        );

        ProblemDetail response = handler.handleDataIntegrityViolationException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getDetail()).isEqualTo("Unique constraint violation: constraint name already exists");
        assertThat(response.getTitle()).isEqualTo("Data Integrity Violation");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleInvalidDataAccessResourceUsageException() {
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException(
            "Invalid SQL syntax in query"
        );

        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = new DatabaseErrorAnalyzer.DatabaseErrorInfo(
                DatabaseErrorAnalyzer.DatabaseErrorType.UNKNOWN,
                null,
                "Invalid SQL syntax in query",
                "A database error occurred while processing your request.",
                "Unknown database error: InvalidDataAccessResourceUsageException"
        );
        when(databaseErrorAnalyzer.analyzeDatabaseError(any())).thenReturn(errorInfo);

        ProblemDetail response = handler.handleInvalidDataAccessResourceUsageException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("A database error occurred while processing your request. Please try again later.");
        assertThat(response.getTitle()).isEqualTo("Database Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid constraint ID");

        ProblemDetail response = handler.handleIllegalArgumentException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).isEqualTo("Invalid constraint ID");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleDataAccessException() {
        DataAccessException exception = new DataAccessException("Database connection failed") {
        };

        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = new DatabaseErrorAnalyzer.DatabaseErrorInfo(
                DatabaseErrorAnalyzer.DatabaseErrorType.UNKNOWN,
                null,
                "Database connection failed",
                "A database error occurred while processing your request.",
                "Unknown database error: DataAccessException"
        );
        when(databaseErrorAnalyzer.analyzeDatabaseError(any())).thenReturn(errorInfo);

        ProblemDetail response = handler.handleDataAccessException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("A database error occurred while processing your request. Please try again later.");
        assertThat(response.getTitle()).isEqualTo("Database Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleDataSourceException() {
        DataSourceException exception = new DataSourceException("Data source connection failed");

        ProblemDetail response = handler.handleDataSourceException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("Unable to resolve field options from data source");
        assertThat(response.getTitle()).isEqualTo("Data Source Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleConstraintException() {
        ConstraintException exception = new ConstraintException("Constraint processing failed");

        ProblemDetail response = handler.handleConstraintException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("An error occurred while processing the constraint");
        assertThat(response.getTitle()).isEqualTo("Constraint Processing Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleMethodExecutionExceptionWithResourceNotFoundCause() {
        ResourceNotFoundException rootCause = new ResourceNotFoundException("Resource not found");
        MethodExecutionException exception = new MethodExecutionException(
            "ConstraintService.getById(..)", 45L, rootCause
        );

        ProblemDetail response = handler.handleMethodExecutionException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("Resource not found");
        assertThat(response.getTitle()).isEqualTo("Resource Not Found");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleMethodExecutionExceptionWithNoSuchBeanDefinitionCause() {
        NoSuchBeanDefinitionException rootCause = new NoSuchBeanDefinitionException("testBean");
        MethodExecutionException exception = new MethodExecutionException(
            "ConstraintService.getById(..)", 45L, rootCause
        );

        ProblemDetail response = handler.handleMethodExecutionException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("No bean named 'testBean' available");
        assertThat(response.getTitle()).isEqualTo("Bean Not Found");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleMethodExecutionExceptionWithDataSourceCause() {
        DataSourceException rootCause = new DataSourceException("Data source error");
        MethodExecutionException exception = new MethodExecutionException(
            "ConstraintService.getById(..)", 45L, rootCause
        );

        ProblemDetail response = handler.handleMethodExecutionException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("Unable to resolve field options from data source");
        assertThat(response.getTitle()).isEqualTo("Data Source Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleMethodExecutionExceptionWithGenericCause() {
        RuntimeException rootCause = new RuntimeException("Generic error");
        MethodExecutionException exception = new MethodExecutionException(
            "ConstraintService.getById(..)", 45L, rootCause
        );

        ProblemDetail response = handler.handleMethodExecutionException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("An error occurred during method execution");
        assertThat(response.getTitle()).isEqualTo("Method Execution Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleRuntimeExceptionWithFieldOptionsMessage() {
        RuntimeException exception = new RuntimeException("Error resolving field options for constraint");

        ProblemDetail response = handler.handleRuntimeException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("Unable to resolve field options for constraint");
        assertThat(response.getTitle()).isEqualTo("Field Option Resolution Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleRuntimeExceptionWithNullMessage() {
        RuntimeException exception = new RuntimeException((String) null);

        ProblemDetail response = handler.handleRuntimeException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("An unexpected error occurred while processing your request");
        assertThat(response.getTitle()).isEqualTo("Internal Server Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleRuntimeExceptionWithGenericMessage() {
        RuntimeException exception = new RuntimeException("Generic runtime error");

        ProblemDetail response = handler.handleRuntimeException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getDetail()).isEqualTo("An unexpected error occurred while processing your request");
        assertThat(response.getTitle()).isEqualTo("Internal Server Error");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        // Create field errors
        FieldError fieldError1 = new FieldError("constraintRequest", "name", "Name is required");
        FieldError fieldError2 = new FieldError("constraintRequest", "type", "Type must not be null");
        List<FieldError> fieldErrors = List.of(fieldError1, fieldError2);

        // Mock the binding result
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ProblemDetail response = handler.handleMethodArgumentNotValid(methodArgumentNotValidException, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).isEqualTo("Invalid request content.");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));

        Map<String, String> errors = (Map<String, String>) response.getProperties().get("errors");
        assertThat(errors).isNotNull();
        assertThat(errors).hasSize(2);
        assertThat(errors.get("name")).isEqualTo("Name is required");
        assertThat(errors.get("type")).isEqualTo("Type must not be null");
    }

    @Test
    void shouldHandleMethodArgumentNotValidExceptionWithEmptyErrors() {
        // Mock the binding result with no field errors
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ProblemDetail response = handler.handleMethodArgumentNotValid(methodArgumentNotValidException, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).isEqualTo("Invalid request content.");
        assertThat(response.getTitle()).isEqualTo("Bad Request");
        assertThat(response.getInstance()).isEqualTo(URI.create("/api/v1/departments/1/constraints/2"));

        Map<String, String> errors = (Map<String, String>) response.getProperties().get("errors");
        assertThat(errors).isNotNull();
        assertThat(errors).isEmpty();
    }
}
