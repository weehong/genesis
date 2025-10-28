package com.resetrix.horaion.shared.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for analyzing database errors and extracting meaningful information.
 */
@Component
public class DatabaseErrorAnalyzer {

    // PostgreSQL error patterns
    private static final Pattern POSTGRES_RELATION_NOT_EXISTS = Pattern.compile(
            "relation \"([^\"]+)\" does not exist", Pattern.CASE_INSENSITIVE);
    private static final Pattern POSTGRES_COLUMN_NOT_EXISTS = Pattern.compile(
            "column \"([^\"]+)\" does not exist", Pattern.CASE_INSENSITIVE);
    private static final Pattern POSTGRES_SYNTAX_ERROR = Pattern.compile(
            "syntax error at or near \"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern POSTGRES_CONSTRAINT_VIOLATION = Pattern.compile(
            "violates ([\\w\\s]+) constraint \"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    // H2 error patterns (for testing)
    private static final Pattern H2_TABLE_NOT_FOUND = Pattern.compile(
            "Table \"([^\"]+)\" not found", Pattern.CASE_INSENSITIVE);

    // Additional PostgreSQL error patterns
    private static final Pattern POSTGRES_PERMISSION_DENIED = Pattern.compile(
            "permission denied for (\\w+) \"?([^\"\\s]+)\"?", Pattern.CASE_INSENSITIVE);

    private static final Pattern POSTGRES_CONNECTION_ERROR = Pattern.compile(
            "connection to server .* failed", Pattern.CASE_INSENSITIVE);

    private static final Pattern POSTGRES_AUTHENTICATION_FAILED = Pattern.compile(
            "authentication failed for user \"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    /**
     * Represents the result of database error analysis.
     */
    @Getter
    @AllArgsConstructor
    public static class DatabaseErrorInfo {
        private final DatabaseErrorType errorType;
        private final String resourceName;
        private final String originalMessage;
        private final String userFriendlyMessage;
        private final String technicalDetails;
    }

    /**
     * Types of database errors that can be detected.
     */
    public enum DatabaseErrorType {
        TABLE_NOT_EXISTS,
        COLUMN_NOT_EXISTS,
        SYNTAX_ERROR,
        CONSTRAINT_VIOLATION,
        CONNECTION_ERROR,
        PERMISSION_DENIED,
        UNKNOWN
    }

    /**
     * Analyzes a database exception and extracts meaningful error information.
     */
    public DatabaseErrorInfo analyzeDatabaseError(DataAccessException exception) {
        String message = exception.getMessage();
        if (message == null) {
            return createUnknownError(exception);
        }

        // Clean up the message by removing PostgreSQL position information
        message = cleanPostgreSQLMessage(message);

        // Check for table/relation not exists
        Optional<DatabaseErrorInfo> tableError = analyzeTableNotExists(message, exception);
        if (tableError.isPresent()) {
            return tableError.get();
        }

        // Check for column not exists
        Optional<DatabaseErrorInfo> columnError = analyzeColumnNotExists(message, exception);
        if (columnError.isPresent()) {
            return columnError.get();
        }

        // Check for syntax errors
        Optional<DatabaseErrorInfo> syntaxError = analyzeSyntaxError(message, exception);
        if (syntaxError.isPresent()) {
            return syntaxError.get();
        }

        // Check for constraint violations
        Optional<DatabaseErrorInfo> constraintError = analyzeConstraintViolation(message, exception);
        if (constraintError.isPresent()) {
            return constraintError.get();
        }

        // Check for connection errors
        Optional<DatabaseErrorInfo> connectionError = analyzeConnectionError(message, exception);
        if (connectionError.isPresent()) {
            return connectionError.get();
        }

        // Check for permission errors
        Optional<DatabaseErrorInfo> permissionError = analyzePermissionError(message, exception);
        if (permissionError.isPresent()) {
            return permissionError.get();
        }

        return createUnknownError(exception);
    }

    private Optional<DatabaseErrorInfo> analyzeTableNotExists(String message, DataAccessException exception) {
        // PostgreSQL pattern
        Matcher postgresMatcher = POSTGRES_RELATION_NOT_EXISTS.matcher(message);
        if (postgresMatcher.find()) {
            String tableName = postgresMatcher.group(1);
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.TABLE_NOT_EXISTS,
                    tableName,
                    message,
                    String.format("Database table '%s' does not exist. "
                            + "Please run database migrations or ensure the table is created.", tableName),
                    String.format("PostgreSQL Error: relation \"%s\" does not exist", tableName)
            ));
        }

        // H2 pattern (for testing)
        Matcher h2Matcher = H2_TABLE_NOT_FOUND.matcher(message);
        if (h2Matcher.find()) {
            String tableName = h2Matcher.group(1);
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.TABLE_NOT_EXISTS,
                    tableName,
                    message,
                    String.format("Database table '%s' does not exist. "
                            + "Please run database migrations or ensure the table is created.", tableName),
                    String.format("H2 Error: Table \"%s\" not found", tableName)
            ));
        }

        return Optional.empty();
    }

    private Optional<DatabaseErrorInfo> analyzeColumnNotExists(String message, DataAccessException exception) {
        Matcher matcher = POSTGRES_COLUMN_NOT_EXISTS.matcher(message);
        if (matcher.find()) {
            String columnName = matcher.group(1);
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.COLUMN_NOT_EXISTS,
                    columnName,
                    message,
                    String.format("Database column '%s' does not exist. "
                            + "The database schema may be outdated.", columnName),
                    String.format("PostgreSQL Error: column \"%s\" does not exist", columnName)
            ));
        }
        return Optional.empty();
    }

    private Optional<DatabaseErrorInfo> analyzeSyntaxError(String message, DataAccessException exception) {
        Matcher matcher = POSTGRES_SYNTAX_ERROR.matcher(message);
        if (matcher.find()) {
            String nearToken = matcher.group(1);
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.SYNTAX_ERROR,
                    nearToken,
                    message,
                    "There is a syntax error in the database query. "
                            + "Please contact support if this persists.",
                    String.format("PostgreSQL Syntax Error near: \"%s\"", nearToken)
            ));
        }
        return Optional.empty();
    }

    private Optional<DatabaseErrorInfo> analyzeConstraintViolation(String message, DataAccessException exception) {
        Matcher matcher = POSTGRES_CONSTRAINT_VIOLATION.matcher(message);
        if (matcher.find()) {
            String constraintType = matcher.group(1);
            String constraintName = matcher.group(2);
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.CONSTRAINT_VIOLATION,
                    constraintName,
                    message,
                    String.format("Data violates %s constraint. Please check your input data.", constraintType),
                    String.format("PostgreSQL Constraint Violation: %s constraint \"%s\"",
                            constraintType, constraintName)
            ));
        }
        return Optional.empty();
    }

    private Optional<DatabaseErrorInfo> analyzeConnectionError(String message, DataAccessException exception) {
        // Check for specific PostgreSQL connection patterns
        Matcher connectionMatcher = POSTGRES_CONNECTION_ERROR.matcher(message);
        if (connectionMatcher.find()) {
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.CONNECTION_ERROR,
                    null,
                    message,
                    "Unable to connect to the database. "
                            + "Please check your database connection and ensure the database server is running.",
                    "PostgreSQL Connection Error: " + message
            ));
        }

        // Check for authentication failures
        Matcher authMatcher = POSTGRES_AUTHENTICATION_FAILED.matcher(message);
        if (authMatcher.find()) {
            String username = authMatcher.group(1);
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.PERMISSION_DENIED,
                    username,
                    message,
                    "Database authentication failed. Please check your database credentials.",
                    String.format("PostgreSQL Authentication Error for user: %s", username)
            ));
        }

        // Generic connection error detection
        if (isGenericConnectionError(message)) {
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.CONNECTION_ERROR,
                    null,
                    message,
                    "Unable to connect to the database. Please check your database connection.",
                    "Database connection failed: " + message
            ));
        }

        return Optional.empty();
    }

    private Optional<DatabaseErrorInfo> analyzePermissionError(String message, DataAccessException exception) {
        Matcher matcher = POSTGRES_PERMISSION_DENIED.matcher(message);
        if (matcher.find()) {
            String resourceType = matcher.group(1);
            String resourceName = matcher.group(2);
            return Optional.of(new DatabaseErrorInfo(
                    DatabaseErrorType.PERMISSION_DENIED,
                    resourceName,
                    message,
                    String.format("Permission denied for %s '%s'. "
                            + "Please check database user permissions.", resourceType, resourceName),
                    String.format("PostgreSQL Permission Error: %s access denied for %s", resourceType, resourceName)
            ));
        }
        return Optional.empty();
    }

    private boolean isGenericConnectionError(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("connection")
               && (lowerMessage.contains("refused")
                || lowerMessage.contains("timeout")
                || lowerMessage.contains("closed")
                || lowerMessage.contains("failed"));
    }

    private DatabaseErrorInfo createUnknownError(DataAccessException exception) {
        return new DatabaseErrorInfo(
                DatabaseErrorType.UNKNOWN,
                null,
                exception.getMessage(),
                "A database error occurred while processing your request.",
                "Unknown database error: " + exception.getClass().getSimpleName()
        );
    }

    /**
     * Checks if the exception is specifically related to a missing table/relation.
     */
    public boolean isMissingTableError(DataAccessException exception) {
        DatabaseErrorInfo errorInfo = analyzeDatabaseError(exception);
        return errorInfo.getErrorType() == DatabaseErrorType.TABLE_NOT_EXISTS;
    }

    /**
     * Extracts the table name from a missing table error, if possible.
     */
    public Optional<String> extractMissingTableName(DataAccessException exception) {
        DatabaseErrorInfo errorInfo = analyzeDatabaseError(exception);
        if (errorInfo.getErrorType() == DatabaseErrorType.TABLE_NOT_EXISTS) {
            return Optional.ofNullable(errorInfo.getResourceName());
        }
        return Optional.empty();
    }

    /**
     * Cleans up PostgreSQL error messages by removing position information and other noise.
     */
    private String cleanPostgreSQLMessage(String message) {
        if (message == null) {
            return null;
        }

        // Remove PostgreSQL position information (e.g., "\n  Position: 13")
        String cleaned = message.replaceAll("\\s*\\n\\s*Position:\\s*\\d+", "");

        // Remove other common PostgreSQL noise patterns
        cleaned = cleaned.replaceAll("\\s*\\n\\s*LINE\\s*\\d+:.*", "");
        cleaned = cleaned.replaceAll("\\s*\\n\\s*\\^", "");

        // Remove excessive whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }
}
