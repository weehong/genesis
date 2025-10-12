package com.resetrix.genesis.shared.helpers;

import com.resetrix.genesis.modules.company.exceptions.CompanyException;
import com.resetrix.genesis.modules.company.exceptions.CustomDatabaseException;
import com.resetrix.genesis.modules.company.exceptions.InvalidFileException;
import com.resetrix.genesis.shared.exceptions.FeignServiceException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.springframework.orm.jpa.JpaSystemException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

class ServiceOperationExecutorTest {

    @Test
    void execute_shouldReturnResult_whenOperationSucceeds() {
        // Given
        Supplier<String> operation = () -> "success";
        String context = "test operation";

        // When
        String result = ServiceOperationExecutor.execute(operation, context, RuntimeException.class);

        // Then
        assertThat(result).isEqualTo("success");
    }

    @Test
    void execute_shouldReturnNullResult_whenOperationReturnsNull() {
        // Given
        Supplier<String> operation = () -> null;
        String context = "test operation returning null";

        // When
        String result = ServiceOperationExecutor.execute(operation, context, RuntimeException.class);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void execute_shouldReturnComplexObject_whenOperationSucceeds() {
        // Given
        Object expectedResult = new Object();
        Supplier<Object> operation = () -> expectedResult;
        String context = "complex object operation";

        // When
        Object result = ServiceOperationExecutor.execute(operation, context, RuntimeException.class);

        // Then
        assertThat(result).isSameAs(expectedResult);
    }

    @Test
    void execute_shouldReturnInteger_whenOperationSucceeds() {
        // Given
        Supplier<Integer> operation = () -> 42;
        String context = "integer operation";

        // When
        Integer result = ServiceOperationExecutor.execute(operation, context, RuntimeException.class);

        // Then
        assertThat(result).isEqualTo(42);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenEntityNotFoundExceptionOccurs() {
        // Given
        Supplier<String> operation = () -> {
            throw new EntityNotFoundException("Entity not found");
        };
        String context = "find entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("The entity does not exist or was already deleted")
            .hasCauseInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_shouldThrowEntityNotFoundException_whenEntityNotFoundExceptionOccursInRetrievalContext() {
        // Given
        Supplier<String> operation = () -> {
            throw new EntityNotFoundException("Entity not found");
        };
        String context = "retrieve entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Entity not found");
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenEntityNotFoundExceptionOccursInUpdateContext() {
        // Given
        Supplier<String> operation = () -> {
            throw new EntityNotFoundException("Entity not found");
        };
        String context = "update entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("The entity does not exist or was deleted")
            .hasCauseInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenEntityExistsExceptionOccurs() {
        // Given
        Supplier<String> operation = () -> {
            throw new EntityExistsException("Entity already exists");
        };
        String context = "create entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("The entity already exists with the given ID")
            .hasCauseInstanceOf(EntityExistsException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenDataIntegrityViolationExceptionOccurs() {
        // Given
        Supplier<String> operation = () -> {
            throw new DataIntegrityViolationException("Data integrity violation");
        };
        String context = "save entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Data integrity violation (e.g., unique constraint failure)")
            .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenDataIntegrityViolationExceptionOccursInDeleteContext() {
        // Given
        Supplier<String> operation = () -> {
            throw new DataIntegrityViolationException("Data integrity violation");
        };
        String context = "delete entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Data integrity violation (e.g., foreign key constraint)")
            .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenConstraintViolationExceptionOccurs() {
        // Given
        Supplier<String> operation = () -> {
            throw new ConstraintViolationException("Constraint violation", Collections.emptySet());
        };
        String context = "validate entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Database constraint violation (e.g., foreign key failure)")
            .hasCauseInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenConstraintViolationExceptionOccursInDeleteContext() {
        // Given
        Supplier<String> operation = () -> {
            throw new ConstraintViolationException("Constraint violation", Collections.emptySet());
        };
        String context = "delete entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Database constraint violation")
            .hasCauseInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenJpaSystemExceptionOccurs() {
        // Given
        RuntimeException cause = new RuntimeException("Database error");
        Supplier<String> operation = () -> {
            throw new JpaSystemException(cause);
        };
        String context = "database operation";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System or persistence error occurred")
            .hasCauseInstanceOf(JpaSystemException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenJpaSystemExceptionOccursInSaveContext() {
        // Given
        RuntimeException cause = new RuntimeException("Database error");
        Supplier<String> operation = () -> {
            throw new JpaSystemException(cause);
        };
        String context = "save entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System error with JPA provider")
            .hasCauseInstanceOf(JpaSystemException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenPersistenceExceptionOccurs() {
        // Given
        Supplier<String> operation = () -> {
            throw new PersistenceException("Persistence error");
        };
        String context = "persist entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System or persistence error occurred")
            .hasCauseInstanceOf(PersistenceException.class);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenPersistenceExceptionOccursInSaveContext() {
        // Given
        Supplier<String> operation = () -> {
            throw new PersistenceException("Persistence error");
        };
        String context = "save entity";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Persistence error occurred")
            .hasCauseInstanceOf(PersistenceException.class);
    }

    @Test
    void execute_shouldThrowFeignServiceException_whenFeignExceptionOccurs() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "http://example.com",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        Map<String, Collection<String>> headers = Collections.emptyMap();
        FeignException feignException = new FeignException.BadRequest("Bad request", request, "Error response".getBytes(), headers);

        Supplier<String> operation = () -> {
            throw feignException;
        };
        String context = "external service call";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(FeignServiceException.class)
            .hasMessage("External service error while external service call: Bad request");
    }

    @Test
    void execute_shouldThrowCustomException_whenInvalidFileExceptionOccurs() {
        // Given
        InvalidFileException cause = new InvalidFileException("Invalid file", new RuntimeException());
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "file processing";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, InvalidFileException.class))
            .isInstanceOf(InvalidFileException.class)
            .hasMessage("Invalid file")
            .hasCause(cause);
    }

    @Test
    void execute_shouldThrowDataAccessResourceFailureException_whenDataAccessResourceFailureExceptionOccursInRetrievalContext() {
        // Given
        DataAccessResourceFailureException cause = new DataAccessResourceFailureException("Resource failure");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "retrieve data";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(DataAccessResourceFailureException.class)
            .hasMessage("Resource failure");
    }

    @Test
    void execute_shouldThrowCustomException_whenDataAccessResourceFailureExceptionOccursInNonRetrievalContext() {
        // Given
        DataAccessResourceFailureException cause = new DataAccessResourceFailureException("Resource failure");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "save data";
        
        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while save data")
            .hasCause(cause);
    }

    @Test
    void execute_shouldThrowCustomException_whenUnknownRuntimeExceptionOccurs() {
        // Given
        RuntimeException cause = new RuntimeException("Unknown error");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "unknown operation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while unknown operation")
            .hasCause(cause);
    }

    @Test
    void execute_shouldRethrowIllegalArgumentException() {
        // Given
        IllegalArgumentException cause = new IllegalArgumentException("Invalid argument");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "validation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid argument")
            .isEqualTo(cause);
    }

    @Test
    void executeVoid_shouldCompleteSuccessfully_whenOperationSucceeds() {
        // Given
        Runnable operation = () -> {
            // Do nothing - successful operation
        };
        String context = "void operation";

        // When & Then
        assertThatCode(() -> ServiceOperationExecutor.executeVoid(operation, context, RuntimeException.class))
            .doesNotThrowAnyException();
    }

    @Test
    void executeVoid_shouldThrowException_whenOperationFails() {
        // Given
        Runnable operation = () -> {
            throw new RuntimeException("Operation failed");
        };
        String context = "failing operation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.executeVoid(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while failing operation");
    }

    @Test
    void execute_shouldHandleFeignException_withDifferentStatusCodes() {
        testFeignExceptionWithStatus(400, "External service error while test call: Bad request");
        testFeignExceptionWithStatus(401, "External service error while test call: Unauthorized");
        testFeignExceptionWithStatus(403, "External service error while test call: Forbidden");
        testFeignExceptionWithStatus(404, "External service error while test call: Resource not found");
        testFeignExceptionWithStatus(408, "External service error while test call: Request timeout");
        testFeignExceptionWithStatus(429, "External service error while test call: Too many requests");
        testFeignExceptionWithStatus(500, "External service error while test call: Internal server error");
        testFeignExceptionWithStatus(502, "External service error while test call: Bad gateway");
        testFeignExceptionWithStatus(503, "External service error while test call: Service unavailable");
        testFeignExceptionWithStatus(504, "External service error while test call: Gateway timeout");
        testFeignExceptionWithStatus(999, "External service error while test call: HTTP 999");
        testFeignExceptionWithStatus(-1, "External service error while test call: HTTP -1");
    }

    private void testFeignExceptionWithStatus(int statusCode, String expectedMessage) {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "http://example.com",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        Map<String, Collection<String>> headers = Collections.emptyMap();
        FeignException feignException = new FeignException.FeignClientException(statusCode, "Error", request, "Response body".getBytes(), headers);

        Supplier<String> operation = () -> {
            throw feignException;
        };
        String context = "test call";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(FeignServiceException.class)
            .hasMessage(expectedMessage);
    }

    @Test
    void execute_shouldHandleFeignException_withEmptyResponseBody() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "http://example.com",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        Map<String, Collection<String>> headers = Collections.emptyMap();
        FeignException feignException = new FeignException.InternalServerError("Internal error", request, new byte[0], headers);

        Supplier<String> operation = () -> {
            throw feignException;
        };
        String context = "service call";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(FeignServiceException.class)
            .hasMessage("External service error while service call: Internal server error");
    }

    @Test
    void execute_shouldHandleFeignException_withNotFoundStatus() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "http://example.com",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        Map<String, Collection<String>> headers = Collections.emptyMap();
        FeignException feignException = new FeignException.NotFound("Not found", request, new byte[0], headers);

        Supplier<String> operation = () -> {
            throw feignException;
        };
        String context = "find resource";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(FeignServiceException.class)
            .hasMessage("External service error while find resource: Resource not found");
    }

    @Test
    void execute_shouldCreateCustomExceptionWithCompanyException() {
        // Given
        CompanyException cause = new CompanyException("Company error");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "company operation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, CompanyException.class))
            .isInstanceOf(CompanyException.class)
            .hasMessage("Unexpected error occurred while company operation")
            .hasCause(cause);
    }

    @Test
    void execute_shouldCreateCustomExceptionWithCustomDatabaseException() {
        // Given
        CustomDatabaseException cause = new CustomDatabaseException("Database error", new RuntimeException());
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "database operation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, CustomDatabaseException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Unexpected error occurred while database operation")
            .hasCause(cause);
    }

    @Test
    void constructor_shouldThrowUnsupportedOperationException() {
        // When & Then
        assertThatThrownBy(() -> {
            Constructor<ServiceOperationExecutor> constructor = ServiceOperationExecutor.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .hasCauseInstanceOf(UnsupportedOperationException.class)
        .getCause()
        .hasMessage("Utility class cannot be instantiated");
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenOptimisticLockingFailureExceptionOccurs() {
        // Given
        OptimisticLockingFailureException cause = new OptimisticLockingFailureException("Optimistic locking failure");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "update operation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Concurrent modification detected")
            .hasCause(cause);
    }

    @Test
    void execute_shouldThrowCustomDatabaseException_whenInvalidDataAccessApiUsageExceptionOccurs() {
        // Given
        InvalidDataAccessApiUsageException cause = new InvalidDataAccessApiUsageException("Invalid API usage");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "data access operation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Invalid usage of the Data Access API")
            .hasCause(cause);
    }

    @Test
    void execute_shouldFallbackToRuntimeException_whenReflectionFails() {
        // Given
        RuntimeException cause = new RuntimeException("Test exception");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "test operation";

        // Create a class that doesn't have the expected constructor
        class InvalidExceptionClass extends RuntimeException {
            // This class intentionally doesn't have a (String, Throwable) constructor
            public InvalidExceptionClass() {
                super();
            }
        }

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, InvalidExceptionClass.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while test operation")
            .hasCause(cause);
    }

    @Test
    void execute_shouldRethrowEntityNotFoundException_inRetrievalContext() {
        // Given
        EntityNotFoundException cause = new EntityNotFoundException("Entity not found");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "retrieve data";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Entity not found")
            .isEqualTo(cause);
    }

    // Tests for uncovered lines - Line 56: EntityExistsException handling
    @Test
    void execute_shouldThrowCustomDatabaseException_whenEntityExistsExceptionOccursInCreateOperation() {
        // Given
        EntityExistsException cause = new EntityExistsException("Entity already exists");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "create operation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("The entity already exists with the given ID")
            .hasCause(cause);
    }

    // Tests for uncovered lines - Line 62: OptimisticLockingFailureException handling (already covered above)
    // Tests for uncovered lines - Line 68: InvalidDataAccessApiUsageException handling (already covered above)

    // Tests for uncovered lines - Line 70: FeignException handling
    @Test
    void execute_shouldHandleFeignException_directCall() {
        // Given
        Request request = Request.create(Request.HttpMethod.POST, "http://example.com",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        Map<String, Collection<String>> headers = Collections.emptyMap();
        FeignException feignException = new FeignException.ServiceUnavailable("Service unavailable", request, "Service down".getBytes(), headers);

        Supplier<String> operation = () -> {
            throw feignException;
        };
        String context = "external API call";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(FeignServiceException.class)
            .hasMessage("External service error while external API call: Service unavailable");
    }

    // Tests for uncovered lines - Line 72: InvalidFileException handling
    @Test
    void execute_shouldHandleInvalidFileException_withCustomExceptionClass() {
        // Given
        InvalidFileException cause = new InvalidFileException("Invalid file format", new RuntimeException("File error"));
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "file upload";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, CompanyException.class))
            .isInstanceOf(CompanyException.class)
            .hasMessage("Invalid file format")
            .hasCause(cause);
    }

    // Tests for branch coverage - Line 77: DataAccessResourceFailureException with non-retrieval context
    @Test
    void execute_shouldWrapDataAccessResourceFailureException_inNonRetrievalContext() {
        // Given
        DataAccessResourceFailureException cause = new DataAccessResourceFailureException("Database connection failed");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "update operation"; // Non-retrieval context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while update operation")
            .hasCause(cause);
    }

    // Tests for branch coverage - Line 129: Response body extraction with null responseBody
    @Test
    void execute_shouldHandleFeignException_withNullResponseBody() {
        // Given - Create a custom FeignException that returns null for responseBody()
        Request request = Request.create(Request.HttpMethod.GET, "http://example.com",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        Map<String, Collection<String>> headers = Collections.emptyMap();

        FeignException feignException = new FeignException(500, "Internal Server Error", request, null, headers) {
            @Override
            public java.util.Optional<java.nio.ByteBuffer> responseBody() {
                return null; // This will trigger the null check in extractResponseBody
            }
        };

        Supplier<String> operation = () -> {
            throw feignException;
        };
        String context = "api call";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(FeignServiceException.class)
            .hasMessage("External service error while api call: Internal server error");
    }

    // Tests for branch coverage - Line 192: EntityNotFoundException with update context
    @Test
    void execute_shouldHandleEntityNotFoundException_inUpdateContext() {
        // Given
        EntityNotFoundException cause = new EntityNotFoundException("Entity not found for update");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "update entity"; // Contains "updat"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("The entity does not exist or was deleted")
            .hasCause(cause);
    }

    // Tests for branch coverage - Line 196: DataIntegrityViolationException with delete context
    @Test
    void execute_shouldHandleDataIntegrityViolationException_inDeleteContext() {
        // Given
        DataIntegrityViolationException cause = new DataIntegrityViolationException("Foreign key constraint");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "delete operation"; // Contains "delet"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Data integrity violation (e.g., foreign key constraint)")
            .hasCause(cause);
    }

    // Tests for branch coverage - Line 206: ConstraintViolationException with delete context
    @Test
    void execute_shouldHandleConstraintViolationException_inDeleteContext() {
        // Given
        ConstraintViolationException cause = new ConstraintViolationException("Constraint violation", Collections.emptySet());
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "delete record"; // Contains "delet"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Database constraint violation")
            .hasCause(cause);
    }

    // Tests for branch coverage - Line 216: JpaSystemException with save context
    @Test
    void execute_shouldHandleJpaSystemException_inSaveContext() {
        // Given
        RuntimeException rootCause = new RuntimeException("JPA error");
        JpaSystemException cause = new JpaSystemException(rootCause);
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "save entity"; // Contains "sav"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System error with JPA provider")
            .hasCause(cause);
    }

    // Tests for branch coverage - Line 225 & 234: PersistenceException with save context
    @Test
    void execute_shouldHandlePersistenceException_inSaveContext() {
        // Given
        PersistenceException cause = new PersistenceException("Persistence error during save");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "save operation"; // Contains "sav"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Persistence error occurred")
            .hasCause(cause);
    }

    // Additional tests to improve branch coverage for edge cases
    @Test
    void execute_shouldHandleEntityNotFoundException_withNullContext() {
        // Given
        EntityNotFoundException cause = new EntityNotFoundException("Entity not found");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = null; // Null context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("The entity does not exist or was already deleted")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleDataIntegrityViolationException_withNullContext() {
        // Given
        DataIntegrityViolationException cause = new DataIntegrityViolationException("Data integrity violation");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = null; // Null context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Data integrity violation (e.g., unique constraint failure)")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleConstraintViolationException_withNullContext() {
        // Given
        ConstraintViolationException cause = new ConstraintViolationException("Constraint violation", Collections.emptySet());
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = null; // Null context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Database constraint violation (e.g., foreign key failure)")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleJpaSystemException_withNullContext() {
        // Given
        RuntimeException rootCause = new RuntimeException("JPA error");
        JpaSystemException cause = new JpaSystemException(rootCause);
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = null; // Null context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System or persistence error occurred")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandlePersistenceException_withNullContext() {
        // Given
        PersistenceException cause = new PersistenceException("Persistence error");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = null; // Null context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System or persistence error occurred")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleDataAccessResourceFailureException_withNullContext() {
        // Given
        DataAccessResourceFailureException cause = new DataAccessResourceFailureException("Resource failure");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = null; // Null context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while null")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleEntityNotFoundException_withEmptyContext() {
        // Given
        EntityNotFoundException cause = new EntityNotFoundException("Entity not found");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = ""; // Empty context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("The entity does not exist or was already deleted")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleDataAccessResourceFailureException_withEmptyContext() {
        // Given
        DataAccessResourceFailureException cause = new DataAccessResourceFailureException("Resource failure");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = ""; // Empty context

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while ")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleDataIntegrityViolationException_withMixedCaseContext() {
        // Given
        DataIntegrityViolationException cause = new DataIntegrityViolationException("Data integrity violation");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "DELETE operation"; // Mixed case with "delet"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Data integrity violation (e.g., foreign key constraint)")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleConstraintViolationException_withMixedCaseContext() {
        // Given
        ConstraintViolationException cause = new ConstraintViolationException("Constraint violation", Collections.emptySet());
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "DELETE record"; // Mixed case with "delet"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Database constraint violation")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleJpaSystemException_withMixedCaseContext() {
        // Given
        RuntimeException rootCause = new RuntimeException("JPA error");
        JpaSystemException cause = new JpaSystemException(rootCause);
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "SAVE entity"; // Mixed case with "sav"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System error with JPA provider")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandlePersistenceException_withMixedCaseContext() {
        // Given
        PersistenceException cause = new PersistenceException("Persistence error");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "SAVE operation"; // Mixed case with "sav"

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Persistence error occurred")
            .hasCause(cause);
    }

    // Additional edge case tests to improve instruction coverage
    @Test
    void execute_shouldHandleGenericRuntimeException_withNullMessage() {
        // Given
        RuntimeException cause = new RuntimeException((String) null); // Null message
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "test operation";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while test operation")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleGenericRuntimeException_withEmptyMessage() {
        // Given
        RuntimeException cause = new RuntimeException(""); // Empty message
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "empty message test";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while empty message test")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleNestedRuntimeException() {
        // Given
        RuntimeException rootCause = new RuntimeException("Root cause");
        RuntimeException cause = new RuntimeException("Nested exception", rootCause);
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "nested exception test";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while nested exception test")
            .hasCause(cause);
    }

    @Test
    void execute_shouldHandleCustomRuntimeExceptionSubclass() {
        // Given
        class CustomRuntimeException extends RuntimeException {
            public CustomRuntimeException(String message) {
                super(message);
            }
        }

        CustomRuntimeException cause = new CustomRuntimeException("Custom runtime exception");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "custom runtime exception test";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected error occurred while custom runtime exception test")
            .hasCause(cause);
    }

    @Test
    void execute_shouldReturnResultFromComplexOperation() {
        // Given
        Supplier<String> operation = () -> {
            // Simulate some complex operation that might involve multiple steps
            String step1 = "step1";
            String step2 = "step2";
            return step1 + "-" + step2 + "-result";
        };
        String context = "complex operation";

        // When
        String result = ServiceOperationExecutor.execute(operation, context, RuntimeException.class);

        // Then
        assertThat(result).isEqualTo("step1-step2-result");
    }

    // Targeted tests to ensure specific throw statements in main execute method are covered
    @Test
    void execute_shouldExecuteThrowStatementForEntityNotFoundException() {
        // Given - Direct EntityNotFoundException to trigger line 54: throw handleEntityNotFoundException(ex, context);
        EntityNotFoundException cause = new EntityNotFoundException("Entity not found");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "retrieval operation";

        // When & Then - This should execute the throw statement at line 54
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Entity not found")
            .isEqualTo(cause);
    }

    @Test
    void execute_shouldExecuteThrowStatementForDataIntegrityViolationException() {
        // Given - Direct DataIntegrityViolationException to trigger line 58: throw handleDataIntegrityViolationException(ex, context);
        DataIntegrityViolationException cause = new DataIntegrityViolationException("Data integrity violation");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "update operation";

        // When & Then - This should execute the throw statement at line 58
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Data integrity violation (e.g., unique constraint failure)")
            .hasCause(cause);
    }

    @Test
    void execute_shouldExecuteThrowStatementForConstraintViolationException() {
        // Given - Direct ConstraintViolationException to trigger line 60: throw handleConstraintViolationException(ex, context);
        ConstraintViolationException cause = new ConstraintViolationException("Constraint violation", Collections.emptySet());
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "save operation";

        // When & Then - This should execute the throw statement at line 60
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Database constraint violation (e.g., foreign key failure)")
            .hasCause(cause);
    }

    @Test
    void execute_shouldExecuteThrowStatementForJpaSystemException() {
        // Given - Direct JpaSystemException to trigger line 64: throw handleJpaSystemException(ex, context);
        RuntimeException rootCause = new RuntimeException("JPA system error");
        JpaSystemException cause = new JpaSystemException(rootCause);
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "database operation";

        // When & Then - This should execute the throw statement at line 64
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System or persistence error occurred")
            .hasCause(cause);
    }

    @Test
    void execute_shouldExecuteThrowStatementForPersistenceException() {
        // Given - Direct PersistenceException to trigger line 66: throw handlePersistenceException(ex, context);
        PersistenceException cause = new PersistenceException("Persistence error");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "database operation";

        // When & Then - This should execute the throw statement at line 66
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System or persistence error occurred")
            .hasCause(cause);
    }

    // Additional tests with different contexts to ensure all branches in handler methods are covered
    @Test
    void execute_shouldExecuteThrowStatementForEntityNotFoundExceptionInUpdateContext() {
        // Given - EntityNotFoundException with update context
        EntityNotFoundException cause = new EntityNotFoundException("Entity not found for update");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "updating entity";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("The entity does not exist or was deleted")
            .hasCause(cause);
    }

    @Test
    void execute_shouldExecuteThrowStatementForDataIntegrityViolationExceptionInDeleteContext() {
        // Given - DataIntegrityViolationException with delete context
        DataIntegrityViolationException cause = new DataIntegrityViolationException("Foreign key constraint");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "deleting entity";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Data integrity violation (e.g., foreign key constraint)")
            .hasCause(cause);
    }

    @Test
    void execute_shouldExecuteThrowStatementForConstraintViolationExceptionInDeleteContext() {
        // Given - ConstraintViolationException with delete context
        ConstraintViolationException cause = new ConstraintViolationException("Constraint violation on delete", Collections.emptySet());
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "deleting record";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Database constraint violation")
            .hasCause(cause);
    }

    @Test
    void execute_shouldExecuteThrowStatementForJpaSystemExceptionInSaveContext() {
        // Given - JpaSystemException with save context
        RuntimeException rootCause = new RuntimeException("JPA save error");
        JpaSystemException cause = new JpaSystemException(rootCause);
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "saving entity";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("System error with JPA provider")
            .hasCause(cause);
    }

    @Test
    void execute_shouldExecuteThrowStatementForPersistenceExceptionInSaveContext() {
        // Given - PersistenceException with save context
        PersistenceException cause = new PersistenceException("Persistence save error");
        Supplier<String> operation = () -> {
            throw cause;
        };
        String context = "saving data";

        // When & Then
        assertThatThrownBy(() -> ServiceOperationExecutor.execute(operation, context, RuntimeException.class))
            .isInstanceOf(CustomDatabaseException.class)
            .hasMessage("Persistence error occurred")
            .hasCause(cause);
    }
}
