package com.resetrix.horaion.modules.constraint.exceptions.handlers;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.resetrix.horaion.modules.constraint.exceptions.ConstraintException;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import com.resetrix.horaion.shared.exceptions.MethodExecutionException;
import com.resetrix.horaion.shared.exceptions.ResourceNotFoundException;
import com.resetrix.horaion.shared.utils.DatabaseErrorAnalyzer;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackages = "com.resetrix.horaion.modules.constraint")
public class ConstraintExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintExceptionHandler.class);

    private final DatabaseErrorAnalyzer databaseErrorAnalyzer;

    public ConstraintExceptionHandler(DatabaseErrorAnalyzer databaseErrorAnalyzer) {
        this.databaseErrorAnalyzer = databaseErrorAnalyzer;
    }

    @ExceptionHandler(NoSuchBeanDefinitionException.class)
    public ProblemDetail handleNoSuchBeanDefinitionException(NoSuchBeanDefinitionException ex,
                                                             HttpServletRequest request) {
        LOGGER.warn("Bean not found for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage());

        problemDetail.setTitle("Bean Not Found");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex,
                                                       HttpServletRequest request) {
        LOGGER.warn("Entity not found for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage());

        problemDetail.setTitle("Entity Not Found");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex,
                                                         HttpServletRequest request) {
        LOGGER.warn("Resource not found for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage());

        problemDetail.setTitle("Resource Not Found");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ProblemDetail handleInvalidDataAccessResourceUsageException(InvalidDataAccessResourceUsageException ex,
                                                                       HttpServletRequest request) {
        LOGGER.error("Invalid data access resource for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Analyze the database error for detailed information
        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = databaseErrorAnalyzer.analyzeDatabaseError(ex);

        // Create appropriate response based on error type
        ProblemDetail problemDetail = createDatabaseErrorResponse(errorInfo, request, ex);

        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                                                               HttpServletRequest request) {
        LOGGER.warn("Data integrity violation for request {}", request.getRequestURI(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage());

        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex,
                                                        HttpServletRequest request) {
        LOGGER.warn("Invalid argument for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage());

        problemDetail.setTitle("Bad Request");
        problemDetail.setInstance(URI.create(request
                                                 .getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccessException(DataAccessException ex,
                                                   HttpServletRequest request) {
        LOGGER.error("Database error for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Analyze the database error for detailed information
        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = databaseErrorAnalyzer.analyzeDatabaseError(ex);

        // Create appropriate response based on error type
        ProblemDetail problemDetail = createDatabaseErrorResponse(errorInfo, request, ex);

        return problemDetail;
    }

    @ExceptionHandler(DataSourceException.class)
    public ProblemDetail handleDataSourceException(DataSourceException ex,
                                                   HttpServletRequest request) {
        LOGGER.error("Data source error for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unable to resolve field options from data source");

        problemDetail.setTitle("Data Source Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(ConstraintException.class)
    public ProblemDetail handleConstraintException(ConstraintException ex,
                                                   HttpServletRequest request) {
        LOGGER.error("Constraint processing error for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An error occurred while processing the constraint");

        problemDetail.setTitle("Constraint Processing Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(MethodExecutionException.class)
    public ProblemDetail handleMethodExecutionException(MethodExecutionException ex,
                                                        HttpServletRequest request) {
        LOGGER.error("Method execution error for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Check if the root cause is a ResourceNotFoundException
        Throwable rootCause = getRootCause(ex);
        if (rootCause instanceof ResourceNotFoundException) {
            return handleResourceNotFoundException((ResourceNotFoundException) rootCause, request);
        }

        // Check if the root cause is a NoSuchBeanDefinitionException
        if (rootCause instanceof NoSuchBeanDefinitionException) {
            return handleNoSuchBeanDefinitionException((NoSuchBeanDefinitionException) rootCause, request);
        }

        // Check if the root cause is a DataSourceException
        if (rootCause instanceof DataSourceException) {
            return handleDataSourceException((DataSourceException) rootCause, request);
        }

        // Check if the root cause is any DataAccessException (including InvalidDataAccessResourceUsageException)
        if (rootCause instanceof DataAccessException) {
            DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo =
                databaseErrorAnalyzer.analyzeDatabaseError((DataAccessException) rootCause);
            return createDatabaseErrorResponse(errorInfo, request, (DataAccessException) rootCause);
        }

        if (rootCause instanceof PSQLException) {
            LOGGER.error("PostgreSQL error for request {}: {}",
                         request.getRequestURI(), rootCause.getMessage(), rootCause);

            // Wrap PSQLException in DataAccessException to use existing analyzer
            DataAccessException wrappedException = new InvalidDataAccessResourceUsageException(
                rootCause.getMessage(), rootCause);
            DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo =
                databaseErrorAnalyzer.analyzeDatabaseError(wrappedException);
            return createDatabaseErrorResponse(errorInfo, request, wrappedException);
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            getCleanedRootCauseMessage(rootCause));

        problemDetail.setTitle("Method Execution Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex,
                                                HttpServletRequest request) {
        LOGGER.error("Unexpected runtime error for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Check if it's a field option resolution error
        if (ex.getMessage() != null && ex.getMessage().contains("field options")) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to resolve field options for constraint");

            problemDetail.setTitle("Field Option Resolution Error");
            problemDetail.setInstance(URI.create(request.getRequestURI()));

            return problemDetail;
        }

        // Generic runtime exception handling
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred while processing your request");

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                      HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                                                           errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Invalid request content.");

        problemDetail.setTitle("Bad Request");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("errors", errors);

        LOGGER.error("Validation failed: {}", errors);

        return problemDetail;
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        return getRootCause(cause);
    }

    /**
     * Creates a database error response based on the analyzed error information.
     */
    private ProblemDetail createDatabaseErrorResponse(DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo,
                                                      HttpServletRequest request,
                                                      DataAccessException originalException) {
        HttpStatus status = determineHttpStatus(errorInfo.getErrorType());
        String title = determineErrorTitle(errorInfo.getErrorType());

        String detailMessage = getGenericDatabaseErrorMessage(errorInfo.getErrorType());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        // Add error type for client handling
        problemDetail.setProperty("errorType", errorInfo.getErrorType().name());
        problemDetail.setProperty("detailMessage", errorInfo.getUserFriendlyMessage());

        // Add resource name if available
        if (errorInfo.getResourceName() != null) {
            problemDetail.setProperty("resourceName", errorInfo.getResourceName());
        }

        return problemDetail;
    }

    private HttpStatus determineHttpStatus(DatabaseErrorAnalyzer.DatabaseErrorType errorType) {
        return switch (errorType) {
            case TABLE_NOT_EXISTS, COLUMN_NOT_EXISTS -> HttpStatus.SERVICE_UNAVAILABLE;
            case SYNTAX_ERROR -> HttpStatus.BAD_REQUEST;
            case CONSTRAINT_VIOLATION -> HttpStatus.CONFLICT;
            case CONNECTION_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String determineErrorTitle(DatabaseErrorAnalyzer.DatabaseErrorType errorType) {
        return switch (errorType) {
            case TABLE_NOT_EXISTS -> "Database Schema Not Initialized";
            case COLUMN_NOT_EXISTS -> "Database Schema Outdated";
            case SYNTAX_ERROR -> "Invalid Database Query";
            case CONSTRAINT_VIOLATION -> "Data Integrity Violation";
            case CONNECTION_ERROR -> "Database Connection Error";
            case PERMISSION_DENIED -> "Database Permission Denied";
            default -> "Database Error";
        };
    }

    private String getGenericDatabaseErrorMessage(DatabaseErrorAnalyzer.DatabaseErrorType errorType) {
        return switch (errorType) {
            case TABLE_NOT_EXISTS, COLUMN_NOT_EXISTS ->
                "The database schema is not properly initialized. Please contact support.";
            case SYNTAX_ERROR -> "There was an error processing your request. Please contact support if this persists.";
            case CONSTRAINT_VIOLATION -> "The data provided violates database constraints. Please check your input.";
            case CONNECTION_ERROR -> "Unable to connect to the database. Please try again later.";
            case PERMISSION_DENIED -> "Insufficient database permissions. Please contact support.";
            default -> "A database error occurred while processing your request. Please try again later.";
        };
    }

    /**
     * Gets a cleaned version of the root cause message, removing PostgreSQL position information
     * and other noise if the root cause is a DataAccessException.
     */
    private String getCleanedRootCauseMessage(Throwable rootCause) {
        String rootCauseMessage = rootCause.getMessage();
        if (rootCause instanceof DataAccessException) {
            DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo =
                databaseErrorAnalyzer.analyzeDatabaseError((DataAccessException) rootCause);
            rootCauseMessage = errorInfo.getOriginalMessage();
        }
        return rootCauseMessage;
    }
}
