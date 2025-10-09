package com.resetrix.genesis.shared.exceptions.handlers;

import com.resetrix.genesis.shared.exceptions.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleBaseException(BaseException ex, HttpServletRequest request) {
        LOGGER.error("BaseException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ex.toProblemDetail(request.getRequestURI());
        return ResponseEntity.status(ex.getHttpStatus()).body(problemDetail);
    }
}
