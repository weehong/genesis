package com.resetrix.genesis.modules.authentication.exceptions.handlers;

import com.resetrix.genesis.modules.authentication.exceptions.AuthenticationConfigurationException;
import com.resetrix.genesis.modules.authentication.exceptions.CryptographicException;
import com.resetrix.genesis.shared.exceptions.MethodExecutionException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationExceptionHandlerTest {

    @InjectMocks
    private AuthenticationExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    private static final String REQUEST_URI = "/api/auth/test";

    @BeforeEach
    void setUp() {
        lenient().when(request.getRequestURI()).thenReturn(REQUEST_URI);
    }

    @Test
    void handleValidationExceptions_shouldReturnBadRequestWithFieldErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "email", "must be a valid email");
        FieldError fieldError2 = new FieldError("object", "password", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ProblemDetail result = exceptionHandler.handleValidationExceptions(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Validation Failed");
        assertThat(result.getDetail()).isEqualTo("Multiple validation errors occurred. See 'errors' for details.");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) result.getProperties().get("errors");
        assertThat(errors).hasSize(2);
        assertThat(errors.get(0)).containsEntry("field", "email")
                                  .containsEntry("message", "must be a valid email");
        assertThat(errors.get(1)).containsEntry("field", "password")
                                  .containsEntry("message", "must not be blank");
    }

    @Test
    void handleMethodExecutionException_shouldReturnInternalServerError() {
        // Arrange
        String methodName = "AuthenticationService.signUp(..)";
        long executionTime = 150L;
        MethodExecutionException exception = new MethodExecutionException(
            methodName,
            executionTime,
            new RuntimeException("Database connection failed")
        );

        // Act
        ProblemDetail result = exceptionHandler.handleMethodExecutionException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Method Execution Error");
        assertThat(result.getDetail()).isEqualTo("An error occurred while executing a method");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) result.getProperties().get("errors");
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).containsEntry("field", "method")
                                  .containsEntry("message", exception.getMessage());
    }

    @Test
    void handleCryptographicException_shouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Failed to compute secret hash";
        CryptographicException exception = new CryptographicException(
            errorMessage,
            new RuntimeException("Underlying error")
        );

        // Act
        ProblemDetail result = exceptionHandler.handleCryptographicException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Cryptographic Error");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) result.getProperties().get("errors");
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).containsEntry("field", "cryptography")
                                  .containsEntry("message", errorMessage);
    }

    @Test
    void handleAuthenticationConfigurationException_shouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Missing AWS Cognito configuration";
        AuthenticationConfigurationException exception = new AuthenticationConfigurationException(
            errorMessage,
            new RuntimeException("Configuration error")
        );

        // Act
        ProblemDetail result = exceptionHandler.handleAuthenticationConfigurationException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Authentication Configuration Error");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleNotAuthorizedException_shouldReturnUnauthorized() {
        // Arrange
        NotAuthorizedException exception = NotAuthorizedException.builder()
            .message("Incorrect username or password")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleNotAuthorizedException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getTitle()).isEqualTo("Not Authorized");
        assertThat(result.getDetail()).isEqualTo("Invalid credentials or unauthorized access");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleUsernameExistsException_shouldReturnConflict() {
        // Arrange
        UsernameExistsException exception = UsernameExistsException.builder()
            .message("User already exists")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleUsernameExistsException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Username Exists");
        assertThat(result.getDetail()).isEqualTo("An account with this email already exists.");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleInvalidPasswordException_shouldReturnBadRequest() {
        // Arrange
        String errorMessage = "Password did not conform with policy.";
        InvalidPasswordException exception = InvalidPasswordException.builder()
            .message(errorMessage)
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleInvalidPasswordException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Invalid Password");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleInvalidParameterException_shouldReturnBadRequest() {
        // Arrange
        String errorMessage = "Invalid parameter value";
        InvalidParameterException exception = InvalidParameterException.builder()
            .message(errorMessage)
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleInvalidParameterException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Invalid Parameter");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleCodeDeliveryFailureException_shouldReturnServiceUnavailable() {
        // Arrange
        CodeDeliveryFailureException exception = CodeDeliveryFailureException.builder()
            .message("Failed to deliver code")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleCodeDeliveryFailureException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getTitle()).isEqualTo("Code Delivery Failed");
        assertThat(result.getDetail()).isEqualTo("Failed to deliver verification code");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleTooManyRequestsException_shouldReturnTooManyRequests() {
        // Arrange
        TooManyRequestsException exception = TooManyRequestsException.builder()
            .message("Too many requests")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleTooManyRequestsException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(result.getTitle()).isEqualTo("Too Many Requests");
        assertThat(result.getDetail()).isEqualTo("Too many requests. Please try again later");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleLimitExceededException_shouldReturnTooManyRequests() {
        // Arrange
        LimitExceededException exception = LimitExceededException.builder()
            .message("Limit exceeded")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleLimitExceededException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(result.getTitle()).isEqualTo("Limit Exceeded");
        assertThat(result.getDetail()).isEqualTo("Limit exceeded. Please try again later");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleResourceNotFoundException_shouldReturnInternalServerError() {
        // Arrange
        ResourceNotFoundException exception = ResourceNotFoundException.builder()
            .message("Resource not found")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleResourceNotFoundException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Resource Not Found");
        assertThat(result.getDetail()).isEqualTo("Authentication resource not found");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleUserNotConfirmedException_shouldReturnForbidden() {
        // Arrange
        UserNotConfirmedException exception = UserNotConfirmedException.builder()
            .message("User not confirmed")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleUserNotConfirmedException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("User Not Confirmed");
        assertThat(result.getDetail()).isEqualTo("User account is not confirmed. Please verify your email or phone");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleUserNotFoundException_shouldReturnNotFound() {
        // Arrange
        UserNotFoundException exception = UserNotFoundException.builder()
            .message("User not found")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleUserNotFoundException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("User Not Found");
        assertThat(result.getDetail()).isEqualTo("User not found");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleSdkClientException_shouldReturnServiceUnavailable() {
        // Arrange
        SdkClientException exception = SdkClientException.builder()
            .message("SDK client error")
            .build();

        // Act
        ProblemDetail result = exceptionHandler.handleSdkClientException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getTitle()).isEqualTo("Service Unavailable");
        assertThat(result.getDetail()).isEqualTo("Authentication service is temporarily unavailable");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleRuntimeException_shouldReturnInternalServerError() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected runtime error");

        // Act
        ProblemDetail result = exceptionHandler.handleRuntimeException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
        assertThat(result.getDetail()).isEqualTo("An unexpected error occurred during authentication");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException(
            "Username cannot be null or empty for SECRET_HASH calculation"
        );

        // Act
        ProblemDetail result = exceptionHandler.handleIllegalArgumentException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Invalid Argument");
        assertThat(result.getDetail()).isEqualTo("Username cannot be null or empty for SECRET_HASH calculation");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }

    @Test
    void handleIllegalStateException_shouldReturnInternalServerError() {
        // Arrange
        IllegalStateException exception = new IllegalStateException(
            "Client secret is not configured but required for SECRET_HASH"
        );

        // Act
        ProblemDetail result = exceptionHandler.handleIllegalStateException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Configuration Error");
        assertThat(result.getDetail()).isEqualTo("Authentication service is not properly configured");
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_URI));
    }
}
