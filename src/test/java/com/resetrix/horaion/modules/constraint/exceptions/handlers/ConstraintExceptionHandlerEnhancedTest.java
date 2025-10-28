package com.resetrix.horaion.modules.constraint.exceptions.handlers;

import com.resetrix.horaion.shared.exceptions.MethodExecutionException;
import com.resetrix.horaion.shared.utils.DatabaseErrorAnalyzer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConstraintExceptionHandlerEnhancedTest {

    @Mock
    private DatabaseErrorAnalyzer databaseErrorAnalyzer;

    @Mock
    private HttpServletRequest request;

    private ConstraintExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ConstraintExceptionHandler(databaseErrorAnalyzer);
        when(request.getRequestURI()).thenReturn("/api/v1/departments/1/constraints");
    }

    @Test
    void shouldProvideDetailedErrorInDevelopmentEnvironment() {
        // Arrange
        String errorMessage = "ERROR: relation \"constraints\" does not exist";
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = new DatabaseErrorAnalyzer.DatabaseErrorInfo(
            DatabaseErrorAnalyzer.DatabaseErrorType.TABLE_NOT_EXISTS,
            "constraints",
            errorMessage,
            "Database table 'constraints' does not exist. Please run database migrations or ensure the table is created.",
            "PostgreSQL Error: relation \"constraints\" does not exist"
        );

        when(databaseErrorAnalyzer.analyzeDatabaseError(any())).thenReturn(errorInfo);

        // Act
        ProblemDetail result = handler.handleInvalidDataAccessResourceUsageException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getTitle()).isEqualTo("Database Schema Not Initialized");
        assertThat(result.getDetail()).contains("Database table 'constraints' does not exist");
        assertThat(result.getProperties()).containsEntry("errorType", "TABLE_NOT_EXISTS");
        assertThat(result.getProperties()).containsEntry("resourceName", "constraints");
        assertThat(result.getProperties()).containsEntry("technicalDetails",
                                                         "PostgreSQL Error: relation \"constraints\" does not exist");
        assertThat(result.getProperties()).containsEntry("originalMessage", errorMessage);
        assertThat(result.getProperties()).containsEntry("environmentType", "development");
    }

    @Test
    void shouldProvideGenericErrorInProductionEnvironment() {
        // Arrange
        String errorMessage = "ERROR: relation \"constraints\" does not exist";
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = new DatabaseErrorAnalyzer.DatabaseErrorInfo(
            DatabaseErrorAnalyzer.DatabaseErrorType.TABLE_NOT_EXISTS,
            "constraints",
            errorMessage,
            "Database table 'constraints' does not exist. Please run database migrations or ensure the table is created.",
            "PostgreSQL Error: relation \"constraints\" does not exist"
        );

        when(databaseErrorAnalyzer.analyzeDatabaseError(any())).thenReturn(errorInfo);

        // Act
        ProblemDetail result = handler.handleInvalidDataAccessResourceUsageException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getTitle()).isEqualTo("Database Schema Not Initialized");
        assertThat(result.getDetail()).isEqualTo(
            "The database schema is not properly initialized. Please contact support.");
        assertThat(result.getProperties()).containsEntry("errorType", "TABLE_NOT_EXISTS");
        assertThat(result.getProperties()).doesNotContainKey("resourceName");
        assertThat(result.getProperties()).doesNotContainKey("technicalDetails");
        assertThat(result.getProperties()).doesNotContainKey("originalMessage");
    }

    @Test
    void shouldHandleMethodExecutionExceptionWithDatabaseRootCause() {
        // Arrange
        String errorMessage = "ERROR: relation \"constraints\" does not exist\n  Position: 13";
        InvalidDataAccessResourceUsageException rootCause = new InvalidDataAccessResourceUsageException(errorMessage);
        MethodExecutionException exception = new MethodExecutionException("ConstraintService.create", 150L, rootCause);

        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = new DatabaseErrorAnalyzer.DatabaseErrorInfo(
            DatabaseErrorAnalyzer.DatabaseErrorType.TABLE_NOT_EXISTS,
            "constraints",
            "ERROR: relation \"constraints\" does not exist",
            // cleaned message without position
            "Database table 'constraints' does not exist. Please run database migrations or ensure the table is created.",
            "PostgreSQL Error: relation \"constraints\" does not exist"
        );

        when(databaseErrorAnalyzer.analyzeDatabaseError(any())).thenReturn(errorInfo);

        // Act
        ProblemDetail result = handler.handleMethodExecutionException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getTitle()).isEqualTo("Database Schema Not Initialized");
        assertThat(result.getDetail()).contains("Database table 'constraints' does not exist");
        assertThat(result.getProperties()).containsEntry("errorType", "TABLE_NOT_EXISTS");
        assertThat(result.getProperties()).containsEntry("resourceName", "constraints");

        // Verify that rootCauseMessage does not contain position information
        if (result.getProperties().containsKey("rootCauseMessage")) {
            String rootCauseMessage = (String) result.getProperties().get("rootCauseMessage");
            assertThat(rootCauseMessage).doesNotContain("Position:");
            assertThat(rootCauseMessage).isEqualTo("ERROR: relation \"constraints\" does not exist");
        }
    }

    @Test
    void shouldHandleConstraintViolationError() {
        // Arrange
        String errorMessage = "ERROR: violates foreign key constraint \"fk_user_department\"";
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = new DatabaseErrorAnalyzer.DatabaseErrorInfo(
            DatabaseErrorAnalyzer.DatabaseErrorType.CONSTRAINT_VIOLATION,
            "fk_user_department",
            errorMessage,
            "Data violates foreign key constraint. Please check your input data.",
            "PostgreSQL Constraint Violation: foreign key constraint \"fk_user_department\""
        );

        when(databaseErrorAnalyzer.analyzeDatabaseError(any())).thenReturn(errorInfo);

        // Act
        ProblemDetail result = handler.handleInvalidDataAccessResourceUsageException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Data Integrity Violation");
        assertThat(result.getDetail()).contains("Data violates foreign key constraint");
        assertThat(result.getProperties()).containsEntry("errorType", "CONSTRAINT_VIOLATION");
        assertThat(result.getProperties()).containsEntry("resourceName", "fk_user_department");
    }

    @Test
    void shouldHandleConnectionError() {
        // Arrange
        String errorMessage = "connection to server at \"localhost\" (127.0.0.1), port 5432 failed: Connection refused";
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = new DatabaseErrorAnalyzer.DatabaseErrorInfo(
            DatabaseErrorAnalyzer.DatabaseErrorType.CONNECTION_ERROR,
            null,
            errorMessage,
            "Unable to connect to the database. Please check your database connection and ensure the database server is running.",
            "PostgreSQL Connection Error: " + errorMessage
        );

        when(databaseErrorAnalyzer.analyzeDatabaseError(any())).thenReturn(errorInfo);

        // Act
        ProblemDetail result = handler.handleInvalidDataAccessResourceUsageException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getTitle()).isEqualTo("Database Connection Error");
        assertThat(result.getDetail()).contains("Unable to connect to the database");
        assertThat(result.getProperties()).containsEntry("errorType", "CONNECTION_ERROR");
    }

    @Test
    void shouldHandleUnknownDatabaseError() {
        // Arrange
        String errorMessage = "Some unknown database error";
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo errorInfo = new DatabaseErrorAnalyzer.DatabaseErrorInfo(
            DatabaseErrorAnalyzer.DatabaseErrorType.UNKNOWN,
            null,
            errorMessage,
            "A database error occurred while processing your request.",
            "Unknown database error: InvalidDataAccessResourceUsageException"
        );

        when(databaseErrorAnalyzer.analyzeDatabaseError(any())).thenReturn(errorInfo);

        // Act
        ProblemDetail result = handler.handleInvalidDataAccessResourceUsageException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Database Error");
        assertThat(result.getDetail()).isEqualTo(
            "A database error occurred while processing your request. Please try again later.");
        assertThat(result.getProperties()).containsEntry("errorType", "UNKNOWN");
    }
}
