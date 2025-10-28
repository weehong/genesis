package com.resetrix.horaion.shared.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseErrorAnalyzerTest {

    private DatabaseErrorAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new DatabaseErrorAnalyzer();
    }

    @Test
    void shouldDetectPostgreSQLTableNotExists() {
        String errorMessage = "ERROR: relation \"constraints\" does not exist";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.TABLE_NOT_EXISTS);
        assertThat(result.getResourceName()).isEqualTo("constraints");
        assertThat(result.getUserFriendlyMessage()).contains("Database table 'constraints' does not exist");
        assertThat(result.getTechnicalDetails()).contains("PostgreSQL Error: relation \"constraints\" does not exist");
    }

    @Test
    void shouldDetectH2TableNotFound() {
        String errorMessage = "Table \"USERS\" not found";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.TABLE_NOT_EXISTS);
        assertThat(result.getResourceName()).isEqualTo("USERS");
        assertThat(result.getUserFriendlyMessage()).contains("Database table 'USERS' does not exist");
        assertThat(result.getTechnicalDetails()).contains("H2 Error: Table \"USERS\" not found");
    }

    @Test
    void shouldDetectPostgreSQLColumnNotExists() {
        String errorMessage = "ERROR: column \"invalid_column\" does not exist";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.COLUMN_NOT_EXISTS);
        assertThat(result.getResourceName()).isEqualTo("invalid_column");
        assertThat(result.getUserFriendlyMessage()).contains("Database column 'invalid_column' does not exist");
    }

    @Test
    void shouldDetectPostgreSQLSyntaxError() {
        String errorMessage = "ERROR: syntax error at or near \"SELCT\"";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.SYNTAX_ERROR);
        assertThat(result.getResourceName()).isEqualTo("SELCT");
        assertThat(result.getUserFriendlyMessage()).contains("syntax error in the database query");
    }

    @Test
    void shouldDetectPostgreSQLConstraintViolation() {
        String errorMessage = "ERROR: violates foreign key constraint \"fk_user_department\"";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.CONSTRAINT_VIOLATION);
        assertThat(result.getResourceName()).isEqualTo("fk_user_department");
        assertThat(result.getUserFriendlyMessage()).contains("violates foreign key constraint");
    }

    @Test
    void shouldDetectConnectionError() {
        String errorMessage = "connection to server at \"localhost\" (127.0.0.1), port 5432 failed: Connection refused";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.CONNECTION_ERROR);
        assertThat(result.getUserFriendlyMessage()).contains("Unable to connect to the database");
    }

    @Test
    void shouldDetectAuthenticationError() {
        String errorMessage = "FATAL: authentication failed for user \"testuser\"";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.PERMISSION_DENIED);
        assertThat(result.getResourceName()).isEqualTo("testuser");
        assertThat(result.getUserFriendlyMessage()).contains("Database authentication failed");
    }

    @Test
    void shouldDetectPermissionDeniedError() {
        String errorMessage = "ERROR: permission denied for table \"sensitive_data\"";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.PERMISSION_DENIED);
        assertThat(result.getResourceName()).isEqualTo("sensitive_data");
        assertThat(result.getUserFriendlyMessage()).contains("Permission denied for table 'sensitive_data'");
    }

    @Test
    void shouldReturnUnknownForUnrecognizedError() {
        String errorMessage = "Some unknown database error";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.UNKNOWN);
        assertThat(result.getUserFriendlyMessage()).isEqualTo("A database error occurred while processing your request.");
    }

    @Test
    void shouldDetectMissingTableError() {
        String errorMessage = "ERROR: relation \"constraints\" does not exist";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        boolean isMissingTable = analyzer.isMissingTableError(exception);
        Optional<String> tableName = analyzer.extractMissingTableName(exception);

        assertThat(isMissingTable).isTrue();
        assertThat(tableName).isPresent();
        assertThat(tableName.get()).isEqualTo("constraints");
    }

    @Test
    void shouldNotDetectMissingTableForOtherErrors() {
        String errorMessage = "ERROR: column \"invalid_column\" does not exist";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        boolean isMissingTable = analyzer.isMissingTableError(exception);
        Optional<String> tableName = analyzer.extractMissingTableName(exception);

        assertThat(isMissingTable).isFalse();
        assertThat(tableName).isEmpty();
    }

    @Test
    void shouldHandleNullMessage() {
        DataAccessException exception = new InvalidDataAccessResourceUsageException(null);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.UNKNOWN);
        assertThat(result.getUserFriendlyMessage()).isEqualTo("A database error occurred while processing your request.");
    }

    @Test
    void shouldCleanPostgreSQLPositionInformation() {
        String errorMessage = "ERROR: relation \"constraints\" does not exist\n  Position: 13";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.TABLE_NOT_EXISTS);
        assertThat(result.getResourceName()).isEqualTo("constraints");
        assertThat(result.getOriginalMessage()).isEqualTo("ERROR: relation \"constraints\" does not exist");
        assertThat(result.getOriginalMessage()).doesNotContain("Position:");
        assertThat(result.getTechnicalDetails()).contains("PostgreSQL Error: relation \"constraints\" does not exist");
        assertThat(result.getTechnicalDetails()).doesNotContain("Position:");
    }

    @Test
    void shouldCleanPostgreSQLLineInformation() {
        String errorMessage = "ERROR: syntax error at or near \"SELCT\"\nLINE 1: SELCT * FROM users;\n        ^";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.SYNTAX_ERROR);
        assertThat(result.getOriginalMessage()).isEqualTo("ERROR: syntax error at or near \"SELCT\"");
        assertThat(result.getOriginalMessage()).doesNotContain("LINE");
        assertThat(result.getOriginalMessage()).doesNotContain("^");
    }

    @Test
    void shouldCleanComplexPostgreSQLMessage() {
        String errorMessage = "ERROR: relation \"constraints\" does not exist\n  Position: 13\nLINE 1: SELECT * FROM constraints WHERE id = 1;\n                      ^";
        DataAccessException exception = new InvalidDataAccessResourceUsageException(errorMessage);

        DatabaseErrorAnalyzer.DatabaseErrorInfo result = analyzer.analyzeDatabaseError(exception);

        assertThat(result.getErrorType()).isEqualTo(DatabaseErrorAnalyzer.DatabaseErrorType.TABLE_NOT_EXISTS);
        assertThat(result.getResourceName()).isEqualTo("constraints");
        assertThat(result.getOriginalMessage()).isEqualTo("ERROR: relation \"constraints\" does not exist");
        assertThat(result.getOriginalMessage()).doesNotContain("Position:");
        assertThat(result.getOriginalMessage()).doesNotContain("LINE");
        assertThat(result.getOriginalMessage()).doesNotContain("^");
    }
}
