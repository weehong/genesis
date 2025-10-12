package com.resetrix.genesis.shared.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BaseExceptionTest {

    @Test
    void shouldCreateProblemDetailWithInstanceAndErrorCode() {
        BaseException exception = new BaseException("Test error", HttpStatus.BAD_REQUEST, "TEST_ERROR") {};

        ProblemDetail problemDetail = exception.toProblemDetail("/api/test");

        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getDetail()).isEqualTo("Test error");
        assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
        assertThat(problemDetail.getInstance()).isNotNull();
        assertThat(problemDetail.getInstance().toString()).isEqualTo("/api/test");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "TEST_ERROR");
    }

    @Test
    void shouldCreateProblemDetailWithNullInstance() {
        BaseException exception = new BaseException("Test error", HttpStatus.BAD_REQUEST, "TEST_ERROR") {};

        ProblemDetail problemDetail = exception.toProblemDetail(null);

        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getDetail()).isEqualTo("Test error");
        assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
        assertThat(problemDetail.getInstance()).isNull();
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "TEST_ERROR");
    }

    @Test
    void shouldCreateProblemDetailWithNullErrorCode() {
        BaseException exception = new BaseException("Test error", HttpStatus.INTERNAL_SERVER_ERROR, null) {};

        ProblemDetail problemDetail = exception.toProblemDetail("/api/test");

        assertThat(problemDetail.getStatus()).isEqualTo(500);
        assertThat(problemDetail.getDetail()).isEqualTo("Test error");
        assertThat(problemDetail.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problemDetail.getInstance()).isNotNull();
        assertThat(problemDetail.getInstance().toString()).isEqualTo("/api/test");

        // When errorCode is null, it should not be in the properties
        Map<String, Object> properties = problemDetail.getProperties();
        if (properties != null) {
            assertThat(properties).doesNotContainKey("errorCode");
        }
    }

    @Test
    void shouldCreateProblemDetailWithNullInstanceAndNullErrorCode() {
        BaseException exception = new BaseException("Test error", HttpStatus.NOT_FOUND, null) {};

        ProblemDetail problemDetail = exception.toProblemDetail(null);

        assertThat(problemDetail.getStatus()).isEqualTo(404);
        assertThat(problemDetail.getDetail()).isEqualTo("Test error");
        assertThat(problemDetail.getTitle()).isEqualTo("Not Found");
        assertThat(problemDetail.getInstance()).isNull();

        // When errorCode is null, it should not be in the properties
        Map<String, Object> properties = problemDetail.getProperties();
        if (properties != null) {
            assertThat(properties).doesNotContainKey("errorCode");
        }
    }

    @Test
    void shouldAddCustomProperties() {
        BaseException exception = new BaseException("Test error", HttpStatus.BAD_REQUEST, "TEST_ERROR") {};
        exception.addProperty("key1", "value1")
                .addProperty("key2", 123)
                .addProperty("key3", true);

        ProblemDetail problemDetail = exception.toProblemDetail("/api/test");

        assertThat(problemDetail.getProperties()).containsEntry("key1", "value1");
        assertThat(problemDetail.getProperties()).containsEntry("key2", 123);
        assertThat(problemDetail.getProperties()).containsEntry("key3", true);
    }

    @Test
    void shouldReturnSelfWhenAddingProperty() {
        BaseException exception = new BaseException("Test error", HttpStatus.BAD_REQUEST, "TEST_ERROR") {};

        BaseException result = exception.addProperty("key", "value");

        assertThat(result).isSameAs(exception);
    }

    @Test
    void shouldCreateBaseExceptionWithCause() {
        Throwable cause = new IllegalArgumentException("Root cause");
        BaseException exception = new BaseException("Test error", cause, HttpStatus.BAD_REQUEST, "TEST_ERROR") {};

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getErrorCode()).isEqualTo("TEST_ERROR");
    }

    @Test
    void shouldCreateProblemDetailWithCause() {
        Throwable cause = new IllegalArgumentException("Root cause");
        BaseException exception = new BaseException("Test error", cause, HttpStatus.BAD_REQUEST, "TEST_ERROR") {};

        ProblemDetail problemDetail = exception.toProblemDetail("/api/test");

        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getDetail()).isEqualTo("Test error");
        assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
        assertThat(problemDetail.getInstance()).isNotNull();
        assertThat(problemDetail.getInstance().toString()).isEqualTo("/api/test");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "TEST_ERROR");
    }
}
