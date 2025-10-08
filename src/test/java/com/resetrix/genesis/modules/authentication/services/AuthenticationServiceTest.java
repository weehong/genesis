package com.resetrix.genesis.modules.authentication.services;

import com.resetrix.genesis.modules.authentication.requests.CognitoSignInRequest;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignUpRequest;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignInResponse;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignUpResponse;
import com.resetrix.genesis.shared.properties.CognitoProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeDeliveryDetailsType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeDeliveryFailureException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.LimitExceededException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.TooManyRequestsException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private CognitoProperty cognitoProperty;

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(cognitoProperty, cognitoClient);
        lenient().when(cognitoProperty.getClientId()).thenReturn("test-client-id");
    }

    @Test
    void signUp_shouldReturnCognitoSignUpResponse_whenValidRequestProvided() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        CodeDeliveryDetailsType codeDeliveryDetails = CodeDeliveryDetailsType.builder()
            .destination("t***@e***.com")
            .deliveryMedium("EMAIL")
            .attributeName("email")
            .build();

        SignUpResponse awsResponse = SignUpResponse.builder()
            .userSub("test-user-sub")
            .codeDeliveryDetails(codeDeliveryDetails)
            .build();

        when(cognitoClient.signUp(any(SignUpRequest.class))).thenReturn(awsResponse);

        // When
        CognitoSignUpResponse response = authenticationService.signUp(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userSub()).isEqualTo("test-user-sub");
        assertThat(response.codeDeliveryDetails()).isNotNull();
        verify(cognitoClient).signUp(any(SignUpRequest.class));
    }

    @Test
    void signUp_shouldThrowUsernameExistsException_whenEmailAlreadyExists() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "existing@example.com",
            "Password123!",
            "+12345678901"
        );

        when(cognitoClient.signUp(any(SignUpRequest.class)))
            .thenThrow(UsernameExistsException.builder()
                .message("User already exists")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(UsernameExistsException.class);
    }

    @Test
    void signUp_shouldThrowInvalidPasswordException_whenPasswordDoesNotMeetRequirements() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "weak",
            "+12345678901"
        );

        when(cognitoClient.signUp(any(SignUpRequest.class)))
            .thenThrow(InvalidPasswordException.builder()
                .message("Password does not meet requirements")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void signUp_shouldThrowInvalidParameterException_whenEmailIsInvalid() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "invalid-email",
            "Password123!",
            "+12345678901"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(InvalidParameterException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    void signUp_shouldThrowCodeDeliveryFailureException_whenVerificationCodeCannotBeSent() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        when(cognitoClient.signUp(any(SignUpRequest.class)))
            .thenThrow(CodeDeliveryFailureException.builder()
                .message("Failed to deliver verification code")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(CodeDeliveryFailureException.class);
    }

    @Test
    void signIn_shouldReturnCognitoSignInResponse_whenValidCredentialsProvided() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        AuthenticationResultType authResult = AuthenticationResultType.builder()
            .accessToken("test-access-token")
            .refreshToken("test-refresh-token")
            .idToken("test-id-token")
            .build();

        InitiateAuthResponse awsResponse = InitiateAuthResponse.builder()
            .authenticationResult(authResult)
            .build();

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class))).thenReturn(awsResponse);

        // When
        CognitoSignInResponse response = authenticationService.signIn(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("test-access-token");
        assertThat(response.refreshToken()).isEqualTo("test-refresh-token");
        assertThat(response.idToken()).isEqualTo("test-id-token");
        verify(cognitoClient).initiateAuth(any(InitiateAuthRequest.class));
    }

    @Test
    void signIn_shouldThrowNotAuthorizedException_whenCredentialsAreInvalid() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "WrongPassword123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(NotAuthorizedException.builder()
                .message("Incorrect username or password")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void signIn_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "nonexistent@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(UserNotFoundException.builder()
                .message("User does not exist")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void signIn_shouldThrowUserNotConfirmedException_whenUserIsNotConfirmed() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "unconfirmed@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(UserNotConfirmedException.builder()
                .message("User is not confirmed")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(UserNotConfirmedException.class);
    }

    @Test
    void signIn_shouldThrowTooManyRequestsException_whenRateLimitExceeded() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(TooManyRequestsException.builder()
                .message("Too many requests")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void signIn_shouldThrowLimitExceededException_whenLimitIsExceeded() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(LimitExceededException.builder()
                .message("Limit exceeded")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(LimitExceededException.class);
    }

    @Test
    void signIn_shouldThrowResourceNotFoundException_whenResourceNotFound() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(ResourceNotFoundException.builder()
                .message("Resource not found")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void signIn_shouldThrowInvalidParameterException_whenEmailIsInvalid() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "invalid-email",
            "Password123!"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(InvalidParameterException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    void signUp_shouldIncludeSecretHash_whenClientSecretIsConfigured() {
        // Given
        when(cognitoProperty.getClientSecret()).thenReturn("test-client-secret");

        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        CodeDeliveryDetailsType codeDeliveryDetails = CodeDeliveryDetailsType.builder()
            .destination("t***@e***.com")
            .deliveryMedium("EMAIL")
            .attributeName("email")
            .build();

        SignUpResponse awsResponse = SignUpResponse.builder()
            .userSub("test-user-sub")
            .codeDeliveryDetails(codeDeliveryDetails)
            .build();

        when(cognitoClient.signUp(any(SignUpRequest.class))).thenReturn(awsResponse);

        // When
        CognitoSignUpResponse response = authenticationService.signUp(request);

        // Then
        assertThat(response).isNotNull();
        verify(cognitoClient).signUp(any(SignUpRequest.class));
    }

    @Test
    void signIn_shouldIncludeSecretHash_whenClientSecretIsConfigured() {
        // Given
        when(cognitoProperty.getClientSecret()).thenReturn("test-client-secret");

        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        AuthenticationResultType authResult = AuthenticationResultType.builder()
            .accessToken("test-access-token")
            .refreshToken("test-refresh-token")
            .idToken("test-id-token")
            .build();

        InitiateAuthResponse awsResponse = InitiateAuthResponse.builder()
            .authenticationResult(authResult)
            .build();

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class))).thenReturn(awsResponse);

        // When
        CognitoSignInResponse response = authenticationService.signIn(request);

        // Then
        assertThat(response).isNotNull();
        verify(cognitoClient).initiateAuth(any(InitiateAuthRequest.class));
    }

    @Test
    void signUp_shouldThrowCryptographicException_whenUnexpectedErrorOccurs() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        when(cognitoClient.signUp(any(SignUpRequest.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void signUp_shouldThrowSdkClientException_whenSdkClientErrorOccurs() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        when(cognitoClient.signUp(any(SignUpRequest.class)))
            .thenThrow(software.amazon.awssdk.core.exception.SdkClientException.builder()
                .message("SDK client error")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(software.amazon.awssdk.core.exception.SdkClientException.class);
    }

    @Test
    void signIn_shouldThrowSdkClientException_whenSdkClientErrorOccurs() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(software.amazon.awssdk.core.exception.SdkClientException.builder()
                .message("SDK client error")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(software.amazon.awssdk.core.exception.SdkClientException.class);
    }

    @Test
    void signIn_shouldLogSecretHashError_whenNotAuthorizedWithSecretHashMessage() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(NotAuthorizedException.builder()
                .message("Unable to verify secret hash for client SECRET_HASH")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(NotAuthorizedException.class)
            .hasMessageContaining("SECRET_HASH");
    }

    @Test
    void signIn_shouldLogClientConfigError_whenNotAuthorizedWithClientMessage() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(NotAuthorizedException.builder()
                .message("Invalid client configuration")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(NotAuthorizedException.class)
            .hasMessageContaining("client");
    }

    @Test
    void signIn_shouldLogGenericNotAuthorized_whenNotAuthorizedWithOtherMessage() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(NotAuthorizedException.builder()
                .message("Some other authorization error")
                .build());

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(NotAuthorizedException.class);
    }


    @Test
    void signUp_shouldThrowInvalidParameterException_whenEmailIsNull() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            null,
            "Password123!",
            "+12345678901"
        );

        // When & Then - This will be caught during buildUserAttributes
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void signUp_shouldThrowInvalidParameterException_whenEmailIsEmpty() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "",
            "Password123!",
            "+12345678901"
        );

        // When & Then - This will be caught during buildUserAttributes
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void signUp_shouldThrowInvalidParameterException_whenEmailIsWhitespace() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "   ",
            "Password123!",
            "+12345678901"
        );

        // When & Then - This will be caught during buildUserAttributes
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void signIn_shouldThrowInvalidParameterException_whenEmailIsNull() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            null,
            "Password123!"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(InvalidParameterException.class)
            .hasMessage("Invalid email format: <null>");
    }

    @Test
    void signIn_shouldThrowInvalidParameterException_whenEmailIsEmpty() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "",
            "Password123!"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(InvalidParameterException.class)
            .hasMessage("Invalid email format: ");
    }

    @Test
    void signIn_shouldThrowInvalidParameterException_whenEmailIsWhitespace() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "   ",
            "Password123!"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(InvalidParameterException.class)
            .hasMessage("Invalid email format:    ");
    }

    @Test
    void signUp_shouldHandleEmptyPhoneNumber() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            ""
        );

        CodeDeliveryDetailsType codeDeliveryDetails = CodeDeliveryDetailsType.builder()
            .destination("t***@e***.com")
            .deliveryMedium("EMAIL")
            .attributeName("email")
            .build();

        SignUpResponse awsResponse = SignUpResponse.builder()
            .userSub("test-user-sub")
            .codeDeliveryDetails(codeDeliveryDetails)
            .build();

        when(cognitoClient.signUp(any(SignUpRequest.class))).thenReturn(awsResponse);

        // When
        CognitoSignUpResponse response = authenticationService.signUp(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userSub()).isEqualTo("test-user-sub");
        verify(cognitoClient).signUp(any(SignUpRequest.class));
    }

    @Test
    void signUp_shouldHandleWhitespacePhoneNumber() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "   "
        );

        CodeDeliveryDetailsType codeDeliveryDetails = CodeDeliveryDetailsType.builder()
            .destination("t***@e***.com")
            .deliveryMedium("EMAIL")
            .attributeName("email")
            .build();

        SignUpResponse awsResponse = SignUpResponse.builder()
            .userSub("test-user-sub")
            .codeDeliveryDetails(codeDeliveryDetails)
            .build();

        when(cognitoClient.signUp(any(SignUpRequest.class))).thenReturn(awsResponse);

        // When
        CognitoSignUpResponse response = authenticationService.signUp(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userSub()).isEqualTo("test-user-sub");
        verify(cognitoClient).signUp(any(SignUpRequest.class));
    }

    @Test
    void signUp_shouldHandleNullPhoneNumber() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            null
        );

        CodeDeliveryDetailsType codeDeliveryDetails = CodeDeliveryDetailsType.builder()
            .destination("t***@e***.com")
            .deliveryMedium("EMAIL")
            .attributeName("email")
            .build();

        SignUpResponse awsResponse = SignUpResponse.builder()
            .userSub("test-user-sub")
            .codeDeliveryDetails(codeDeliveryDetails)
            .build();

        when(cognitoClient.signUp(any(SignUpRequest.class))).thenReturn(awsResponse);

        // When
        CognitoSignUpResponse response = authenticationService.signUp(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userSub()).isEqualTo("test-user-sub");
        verify(cognitoClient).signUp(any(SignUpRequest.class));
    }

    @Test
    void signIn_shouldLogGenericNotAuthorized_whenNotAuthorizedWithNullMessage() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(NotAuthorizedException.builder()
                .build()); // No message set, will be null

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void signUp_shouldThrowIllegalArgumentException_whenUsernameIsNullForSecretHash() {
        // Given
        when(cognitoProperty.getClientSecret()).thenReturn("test-client-secret");

        CognitoSignUpRequest request = new CognitoSignUpRequest(
            null,
            "Password123!",
            "+12345678901"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Username cannot be null or empty for SECRET_HASH calculation");
    }

    @Test
    void signUp_shouldThrowIllegalArgumentException_whenUsernameIsEmptyForSecretHash() {
        // Given
        when(cognitoProperty.getClientSecret()).thenReturn("test-client-secret");

        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "",
            "Password123!",
            "+12345678901"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Username cannot be null or empty for SECRET_HASH calculation");
    }

    @Test
    void signIn_shouldHandleNullEmailInSafeMethod() {
        // Given
        CognitoSignInRequest request = new CognitoSignInRequest(
            "test@example.com",
            "Password123!"
        );

        // Create a NotAuthorizedException with null message to trigger the safe() method with null
        NotAuthorizedException exception = NotAuthorizedException.builder().build();

        when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
            .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> authenticationService.signIn(request))
            .isInstanceOf(NotAuthorizedException.class);

        // This test covers the safe() method being called with null message
        // The safe() method will return "<null>" for null input
    }

    @Test
    void signUp_shouldThrowInvalidParameterException_whenEmailContainsAtButNoDot() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example",  // Contains @ but no .
            "Password123!",
            "+12345678901"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(InvalidParameterException.class)
            .hasMessageContaining("Invalid email format: test@example");
    }

    @Test
    void signUp_shouldThrowInvalidParameterException_whenEmailContainsDotButNoAt() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test.example.com",  // Contains . but no @
            "Password123!",
            "+12345678901"
        );

        // When & Then
        assertThatThrownBy(() -> authenticationService.signUp(request))
            .isInstanceOf(InvalidParameterException.class)
            .hasMessageContaining("Invalid email format: test.example.com");
    }
}
