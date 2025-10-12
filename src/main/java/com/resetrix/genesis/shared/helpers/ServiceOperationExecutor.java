package com.resetrix.genesis.shared.helpers;

import com.resetrix.genesis.modules.company.exceptions.CustomDatabaseException;
import com.resetrix.genesis.modules.company.exceptions.InvalidFileException;
import com.resetrix.genesis.shared.exceptions.FeignServiceException;
import feign.FeignException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Utility class that provides template methods for executing service operations
 * (database and external service calls) with consistent exception handling across all service classes.
 * <p>
 * This class implements the Template Method Pattern to eliminate duplicate
 * exception handling code in service layers.
 */
public final class ServiceOperationExecutor {

    private ServiceOperationExecutor() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Executes a service operation that returns a value with standardized exception handling.
     *
     * @param <T>            the return type of the operation
     * @param operation      the service operation to execute
     * @param context        a description of the operation context for error messages
     * @param exceptionClass the exception class to throw for unexpected errors
     * @return the result of the operation
     * @throws RuntimeException if any error occurs
     */
    public static <T> T execute(
            Supplier<T> operation,
            String context,
            Class<? extends RuntimeException> exceptionClass) {
        try {
            return operation.get();
        } catch (IllegalArgumentException ex) {
            // Re-throw validation exceptions as-is
            throw ex;
        } catch (EntityNotFoundException ex) {
            throw handleEntityNotFoundException(ex, context);
        } catch (EntityExistsException ex) {
            throw new CustomDatabaseException("The entity already exists with the given ID", ex);
        } catch (DataIntegrityViolationException ex) {
            throw handleDataIntegrityViolationException(ex, context);
        } catch (ConstraintViolationException ex) {
            throw handleConstraintViolationException(ex, context);
        } catch (OptimisticLockingFailureException ex) {
            throw new CustomDatabaseException("Concurrent modification detected", ex);
        } catch (JpaSystemException ex) {
            throw handleJpaSystemException(ex, context);
        }  catch (PersistenceException ex) {
            throw handlePersistenceException(ex, context);
        } catch (InvalidDataAccessApiUsageException ex) {
            throw new CustomDatabaseException("Invalid usage of the Data Access API", ex);
        } catch (FeignException ex) {
            // Handle Feign client errors
            throw handleFeignException(ex, context);
        } catch (InvalidFileException ex) {
            // Preserve the original InvalidFileException message by wrapping it in the target exception class
            throw createException(exceptionClass, ex.getMessage(), ex);
        } catch (DataAccessResourceFailureException ex) {
            // For retrieval operations, throw the original exception
            if (context != null && context.toLowerCase().contains("retriev")) {
                throw ex;
            }
            // For other operations, wrap it
            throw createException(exceptionClass, "Unexpected error occurred while " + context, ex);
        } catch (RuntimeException ex) {
            // For any other runtime exception, wrap it in the provided exception class
            throw createException(exceptionClass, "Unexpected error occurred while " + context, ex);
        }
    }

    /**
     * Executes a service operation that returns void with standardized exception handling.
     *
     * @param operation      the service operation to execute
     * @param context        a description of the operation context for error messages
     * @param exceptionClass the exception class to throw for unexpected errors
     * @throws RuntimeException if any error occurs
     */
    public static void executeVoid(
            Runnable operation,
            String context,
            Class<? extends RuntimeException> exceptionClass) {
        execute(() -> {
            operation.run();
            return null;
        }, context, exceptionClass);
    }

    /**
     * Handles FeignException and converts it to FeignServiceException with proper context.
     *
     * @param ex      the FeignException to handle
     * @param context the operation context for error messages
     * @return a FeignServiceException with details from the Feign error
     */
    private static FeignServiceException handleFeignException(FeignException ex, String context) {
        int statusCode = ex.status();
        String responseBody = extractResponseBody(ex);

        String message = buildFeignErrorMessage(statusCode, context);

        return new FeignServiceException(message, ex, statusCode, responseBody);
    }

    /**
     * Extracts the response body from a FeignException.
     *
     * @param ex the FeignException
     * @return the response body as a string, or empty string if not available
     */
    private static String extractResponseBody(FeignException ex) {
        if (ex.responseBody() == null) {
            return "";
        }

        var buffer = ex.responseBody().get();
        buffer.mark();

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.reset();

        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Builds an appropriate error message based on the HTTP status code.
     *
     * @param statusCode the HTTP status code
     * @param context    the operation context
     * @return a descriptive error message
     */
    private static String buildFeignErrorMessage(int statusCode, String context) {
        String prefix = "External service error while " + context;

        return switch (statusCode) {
            case 400 -> prefix + ": Bad request";
            case 401 -> prefix + ": Unauthorized";
            case 403 -> prefix + ": Forbidden";
            case 404 -> prefix + ": Resource not found";
            case 408 -> prefix + ": Request timeout";
            case 429 -> prefix + ": Too many requests";
            case 500 -> prefix + ": Internal server error";
            case 502 -> prefix + ": Bad gateway";
            case 503 -> prefix + ": Service unavailable";
            case 504 -> prefix + ": Gateway timeout";
            default -> prefix + ": HTTP " + statusCode;
        };
    }

    /**
     * Creates an instance of the specified exception class with a message and cause.
     *
     * @param exceptionClass the exception class to instantiate
     * @param message        the error message
     * @param cause          the underlying cause
     * @return a new exception instance
     */
    private static RuntimeException createException(
            Class<? extends RuntimeException> exceptionClass,
            String message,
            Throwable cause) {
        try {
            Constructor<? extends RuntimeException> constructor =
                    exceptionClass.getConstructor(String.class, Throwable.class);
            return constructor.newInstance(message, cause);
        } catch (Exception e) {
            // Fallback if reflection fails
            return new RuntimeException(message, cause);
        }
    }

    private static RuntimeException handleEntityNotFoundException(EntityNotFoundException ex, String context) {
        // For retrieval operations, throw the original exception
        if (context != null && context.toLowerCase().contains("retriev")) {
            throw ex;
        }
        // Check context to determine appropriate message
        if (context != null && context.toLowerCase().contains("updat")) {
            throw new CustomDatabaseException("The entity does not exist or was deleted", ex);
        } else {
            throw new CustomDatabaseException("The entity does not exist or was already deleted", ex);
        }
    }

    private static RuntimeException handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                                                                          String context) {
        // Check context to determine appropriate message
        if (context != null && context.toLowerCase().contains("delet")) {
            throw new CustomDatabaseException("Data integrity violation (e.g., foreign key constraint)", ex);
        } else {
            throw new CustomDatabaseException("Data integrity violation (e.g., unique constraint failure)", ex);
        }
    }

    private static RuntimeException handleConstraintViolationException(ConstraintViolationException ex,
                                                                       String context) {
        // Check context to determine appropriate message
        if (context != null && context.toLowerCase().contains("delet")) {
            throw new CustomDatabaseException("Database constraint violation", ex);
        } else {
            throw new CustomDatabaseException("Database constraint violation (e.g., foreign key failure)", ex);
        }
    }

    private static RuntimeException handleJpaSystemException(JpaSystemException ex, String context) {
        // Check context to determine appropriate message
        if (context != null && context.toLowerCase().contains("sav")) {
            throw new CustomDatabaseException("System error with JPA provider", ex);
        } else {
            throw new CustomDatabaseException("System or persistence error occurred", ex);
        }
    }

    private static RuntimeException handlePersistenceException(PersistenceException ex, String context) {
        // Check context to determine appropriate message
        if (context != null && context.toLowerCase().contains("sav")) {
            throw new CustomDatabaseException("Persistence error occurred", ex);
        } else {
            throw new CustomDatabaseException("System or persistence error occurred", ex);
        }
    }
}