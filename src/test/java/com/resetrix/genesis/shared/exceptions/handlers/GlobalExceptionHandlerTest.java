package com.resetrix.genesis.shared.exceptions.handlers;

import com.resetrix.genesis.shared.exceptions.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void shouldHandleBaseException() {
        BaseException exception = new BaseException("Test error", HttpStatus.INTERNAL_SERVER_ERROR, "TEST_ERROR") {};

        ResponseEntity<ProblemDetail> response = handler.handleBaseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(500);
        assertThat(problemDetail.getDetail()).isEqualTo("Test error");
        assertThat(problemDetail.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problemDetail.getInstance().toString()).isEqualTo("/api/test");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "TEST_ERROR");
    }

    @Test
    void shouldHandleBaseExceptionWithCause() {
        Throwable cause = new NumberFormatException("Not a number");
        BaseException exception = new BaseException("Invalid value", cause, HttpStatus.BAD_REQUEST, "INVALID_VALUE") {};

        ResponseEntity<ProblemDetail> response = handler.handleBaseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getDetail()).isEqualTo("Invalid value");
        assertThat(problemDetail.getProperties()).containsEntry("causeType", "NumberFormatException");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "INVALID_VALUE");
    }
}
