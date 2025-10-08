package com.resetrix.genesis.modules.authentication.exceptions.handlers;

import com.resetrix.genesis.modules.authentication.exceptions.AuthenticationConfigurationException;
import com.resetrix.genesis.modules.authentication.exceptions.CryptographicException;
import com.resetrix.genesis.shared.exceptions.MethodExecutionException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeDeliveryFailureException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.LimitExceededException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.TooManyRequestsException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.resetrix.genesis.modules.authentication")
public class AuthenticationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex,
                                                    HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Multiple validation errors occurred. See 'errors' for details."
        );

        problemDetail.setTitle("Validation Failed");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> error = new HashMap<>();
            error.put("field", fieldError.getField());
            error.put("message", fieldError.getDefaultMessage());
            errors.add(error);
        }

        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(MethodExecutionException.class)
    public ProblemDetail handleMethodExecutionException(MethodExecutionException ex,
                                                          HttpServletRequest request) {
        LOGGER.error("MethodExecutionException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An error occurred while executing a method"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Method Execution Error");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "method");
        error.put("message", ex.getMessage());
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(CryptographicException.class)
    public ProblemDetail handleCryptographicException(CryptographicException ex,
                                                       HttpServletRequest request) {
        LOGGER.error("CryptographicException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage()
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Cryptographic Error");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "cryptography");
        error.put("message", ex.getMessage());
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(AuthenticationConfigurationException.class)
    public ProblemDetail handleAuthenticationConfigurationException(
        AuthenticationConfigurationException ex,
        HttpServletRequest request) {
        LOGGER.error("AuthenticationConfigurationException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage()
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Authentication Configuration Error");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "configuration");
        error.put("message", ex.getMessage());
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ProblemDetail handleNotAuthorizedException(NotAuthorizedException ex,
                                                       HttpServletRequest request) {
        LOGGER.warn("NotAuthorizedException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "Invalid credentials or unauthorized access"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Not Authorized");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "credentials");
        error.put("message", "Invalid credentials or unauthorized access");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(UsernameExistsException.class)
    public ProblemDetail handleUsernameExistsException(UsernameExistsException ex,
                                                        HttpServletRequest request) {
        LOGGER.warn("UsernameExistsException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "An account with this email already exists."
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Username Exists");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "email");
        error.put("message", "An account with this email already exists");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPasswordException(InvalidPasswordException ex,
                                                        HttpServletRequest request) {
        LOGGER.warn("InvalidPasswordException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Password did not conform with policy.");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Invalid Password");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "password");
        error.put("message", ex.getMessage());
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ProblemDetail handleInvalidParameterException(InvalidParameterException ex,
                                                         HttpServletRequest request) {
        LOGGER.warn("InvalidParameterException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Invalid Parameter");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "parameter");
        error.put("message", ex.getMessage());
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(CodeDeliveryFailureException.class)
    public ProblemDetail handleCodeDeliveryFailureException(CodeDeliveryFailureException ex,
                                                             HttpServletRequest request) {
        LOGGER.error("CodeDeliveryFailureException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Failed to deliver verification code"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Code Delivery Failed");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "verification");
        error.put("message", "Failed to deliver verification code");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ProblemDetail handleTooManyRequestsException(TooManyRequestsException ex,
                                                         HttpServletRequest request) {
        LOGGER.warn("TooManyRequestsException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "Too many requests. Please try again later"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Too Many Requests");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "request");
        error.put("message", "Too many requests. Please try again later");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(LimitExceededException.class)
    public ProblemDetail handleLimitExceededException(LimitExceededException ex,
                                                       HttpServletRequest request) {
        LOGGER.warn("LimitExceededException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "Limit exceeded. Please try again later"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Limit Exceeded");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "limit");
        error.put("message", "Limit exceeded. Please try again later");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex,
                                                          HttpServletRequest request) {
        LOGGER.error("ResourceNotFoundException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Authentication resource not found"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Resource Not Found");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "resource");
        error.put("message", "Authentication resource not found");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(UserNotConfirmedException.class)
    public ProblemDetail handleUserNotConfirmedException(UserNotConfirmedException ex,
                                                          HttpServletRequest request) {
        LOGGER.warn("UserNotConfirmedException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "User account is not confirmed. Please verify your email or phone"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("User Not Confirmed");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "user");
        error.put("message", "User account is not confirmed. Please verify your email or phone");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex,
                                                     HttpServletRequest request) {
        LOGGER.warn("UserNotFoundException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "User not found"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("User Not Found");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "user");
        error.put("message", "User not found");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(SdkClientException.class)
    public ProblemDetail handleSdkClientException(SdkClientException ex,
                                                   HttpServletRequest request) {
        LOGGER.error("SdkClientException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Authentication service is temporarily unavailable"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Service Unavailable");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "service");
        error.put("message", "Authentication service is temporarily unavailable");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex,
                                                         HttpServletRequest request) {
        LOGGER.warn("IllegalArgumentException: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Invalid Argument");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "argument");
        error.put("message", ex.getMessage());
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex,
                                                      HttpServletRequest request) {
        LOGGER.error("IllegalStateException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Authentication service is not properly configured"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Configuration Error");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "state");
        error.put("message", "Authentication service is not properly configured");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex,
                                                 HttpServletRequest request) {
        LOGGER.error("Unexpected RuntimeException: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred during authentication"
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setTitle("Internal Server Error");

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        error.put("field", "runtime");
        error.put("message", "An unexpected error occurred during authentication");
        errors.add(error);
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }
}
