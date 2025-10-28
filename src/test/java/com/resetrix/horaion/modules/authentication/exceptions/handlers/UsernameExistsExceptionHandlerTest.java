package com.resetrix.horaion.modules.authentication.exceptions.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameExistsExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private AuthenticationExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new AuthenticationExceptionHandler();
    }

    @Test
    void handleUsernameExistsException_shouldReturnConflictStatus() {
        // Given
        String requestUri = "/api/v1/authentication/sign-up";
        when(request.getRequestURI()).thenReturn(requestUri);
        
        UsernameExistsException exception = UsernameExistsException.builder()
            .message("User already exists")
            .build();

        // When
        ProblemDetail result = exceptionHandler.handleUsernameExistsException(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Account Already Exists");
        assertThat(result.getDetail()).contains("An account with this email address already exists");
        assertThat(result.getInstance()).isEqualTo(URI.create(requestUri));
    }

    @Test
    void handleUsernameExistsException_shouldIncludeErrorDetails() {
        // Given
        String requestUri = "/api/v1/authentication/sign-up";
        when(request.getRequestURI()).thenReturn(requestUri);
        
        UsernameExistsException exception = UsernameExistsException.builder()
            .message("User already exists")
            .build();

        // When
        ProblemDetail result = exceptionHandler.handleUsernameExistsException(exception, request);

        // Then
        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) result.getProperties().get("errors");
        
        assertThat(errors).hasSize(1);
        
        Map<String, String> error = errors.get(0);
        assertThat(error.get("field")).isEqualTo("email");
        assertThat(error.get("message")).isEqualTo("An account with this email address already exists");
        assertThat(error.get("code")).isEqualTo("USERNAME_EXISTS");
    }

    @Test
    void handleUsernameExistsException_shouldIncludeAdditionalProperties() {
        // Given
        String requestUri = "/api/v1/authentication/sign-up";
        when(request.getRequestURI()).thenReturn(requestUri);
        
        UsernameExistsException exception = UsernameExistsException.builder()
            .message("User already exists")
            .build();

        // When
        ProblemDetail result = exceptionHandler.handleUsernameExistsException(exception, request);

        // Then
        assertThat(result.getProperties().get("errorCode")).isEqualTo("USERNAME_EXISTS");
        assertThat(result.getProperties().get("suggestion"))
            .isEqualTo("Please use a different email address or try signing in if you already have an account");
    }

    @Test
    void handleUsernameExistsException_shouldHandleNullMessage() {
        // Given
        String requestUri = "/api/v1/authentication/sign-up";
        when(request.getRequestURI()).thenReturn(requestUri);
        
        UsernameExistsException exception = UsernameExistsException.builder()
            .message(null)
            .build();

        // When
        ProblemDetail result = exceptionHandler.handleUsernameExistsException(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Account Already Exists");
        assertThat(result.getDetail()).contains("An account with this email address already exists");
    }

    @Test
    void handleUsernameExistsException_shouldHandleEmptyRequestUri() {
        // Given
        when(request.getRequestURI()).thenReturn("");
        
        UsernameExistsException exception = UsernameExistsException.builder()
            .message("User already exists")
            .build();

        // When
        ProblemDetail result = exceptionHandler.handleUsernameExistsException(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getInstance()).isEqualTo(URI.create(""));
    }

    @Test
    void handleUsernameExistsException_shouldProvideUserFriendlyMessage() {
        // Given
        String requestUri = "/api/v1/authentication/sign-up";
        when(request.getRequestURI()).thenReturn(requestUri);
        
        UsernameExistsException exception = UsernameExistsException.builder()
            .message("User already exists (Service: CognitoIdentityProvider, Status Code: 400, Request ID: 532a2123-a309-4ad2-968e-88434e3bad60)")
            .build();

        // When
        ProblemDetail result = exceptionHandler.handleUsernameExistsException(exception, request);

        // Then
        // Should provide user-friendly message instead of technical AWS error
        assertThat(result.getDetail()).doesNotContain("Service: CognitoIdentityProvider");
        assertThat(result.getDetail()).doesNotContain("Status Code: 400");
        assertThat(result.getDetail()).doesNotContain("Request ID:");
        assertThat(result.getDetail()).contains("An account with this email address already exists");
    }
}
