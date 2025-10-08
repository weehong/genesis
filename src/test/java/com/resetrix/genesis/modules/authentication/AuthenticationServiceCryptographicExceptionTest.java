package com.resetrix.genesis.modules.authentication;

import com.resetrix.genesis.modules.authentication.exceptions.AuthenticationConfigurationException;
import com.resetrix.genesis.modules.authentication.exceptions.CryptographicException;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignUpRequest;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignUpResponse;
import com.resetrix.genesis.modules.authentication.services.AuthenticationService;
import com.resetrix.genesis.shared.properties.CognitoProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeDeliveryDetailsType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceCryptographicExceptionTest {

    @Mock
    private CognitoProperty cognitoProperty;

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    @Mock
    private Mac mockMac;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(cognitoProperty, cognitoClient);
        lenient().when(cognitoProperty.getClientId()).thenReturn("test-client-id");
        lenient().when(cognitoProperty.getClientSecret()).thenReturn("test-secret");
    }

    @Test
    void signUp_shouldThrowCryptographicException_whenNoSuchAlgorithmException() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        // Mock Mac.getInstance to throw NoSuchAlgorithmException
        try (MockedStatic<Mac> macMock = mockStatic(Mac.class)) {
            macMock.when(() -> Mac.getInstance(anyString()))
                .thenThrow(new NoSuchAlgorithmException("Algorithm not available"));

            // When & Then
            assertThatThrownBy(() -> authenticationService.signUp(request))
                .isInstanceOf(CryptographicException.class)
                .hasMessage("HmacSHA256 algorithm not available for SECRET_HASH calculation")
                .hasCauseInstanceOf(NoSuchAlgorithmException.class);
        }
    }

    @Test
    void signUp_shouldThrowAuthenticationConfigurationException_whenInvalidKeyException() throws InvalidKeyException {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        // Mock Mac.getInstance to return a mock that throws InvalidKeyException on init
        try (MockedStatic<Mac> macMock = mockStatic(Mac.class)) {
            macMock.when(() -> Mac.getInstance(anyString())).thenReturn(mockMac);

            doThrow(new InvalidKeyException("Invalid key"))
                .when(mockMac).init(any(SecretKeySpec.class));

            // When & Then
            assertThatThrownBy(() -> authenticationService.signUp(request))
                .isInstanceOf(AuthenticationConfigurationException.class)
                .hasMessage("Invalid client secret key for SECRET_HASH calculation")
                .hasCauseInstanceOf(InvalidKeyException.class);
        }
    }

    @Test
    void signUp_shouldThrowCryptographicException_whenUnexpectedExceptionInSecretHash() {
        // Given
        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        // Mock Mac.getInstance to return a mock that throws a generic RuntimeException
        try (MockedStatic<Mac> macMock = mockStatic(Mac.class)) {
            macMock.when(() -> Mac.getInstance(anyString())).thenReturn(mockMac);
            
            when(mockMac.doFinal(any(byte[].class)))
                .thenThrow(new RuntimeException("Unexpected crypto error"));

            // When & Then
            assertThatThrownBy(() -> authenticationService.signUp(request))
                .isInstanceOf(CryptographicException.class)
                .hasMessage("Unexpected error during SECRET_HASH calculation for user: test@example.com")
                .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void signUp_shouldProceedWithoutSecretHash_whenClientSecretIsNull() {
        // Given
        when(cognitoProperty.getClientSecret()).thenReturn(null);
        when(cognitoProperty.getClientId()).thenReturn("test-client-id");

        CognitoSignUpRequest request = new CognitoSignUpRequest(
            "test@example.com",
            "Password123!",
            "+12345678901"
        );

        // Mock a successful response
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

        // Verify that the request was made without a secret hash
        verify(cognitoClient).signUp(argThat((SignUpRequest signUpRequest) ->
            signUpRequest.secretHash() == null
        ));
    }

    

    @Test
    void calculateSecretHash_shouldThrowIllegalStateException_whenClientSecretBecomesNullAfterTrimming() throws Exception {
        // Given
        // We need to directly test the calculateSecretHash method when client secret becomes null
        // This can happen if the client secret is a whitespace-only string that gets trimmed to null

        // Set up a client secret that will be trimmed to null (e.g., whitespace-only string)
        lenient().when(cognitoProperty.getClientSecret()).thenReturn("   "); // whitespace-only string
        lenient().when(cognitoProperty.getClientId()).thenReturn("test-client-id");

        // Use reflection to access the private calculateSecretHash method
        Method calculateSecretHashMethod = AuthenticationService.class.getDeclaredMethod(
            "calculateSecretHash",
            String.class
        );
        calculateSecretHashMethod.setAccessible(true);

        // When & Then
        assertThatThrownBy(() -> {
            try {
                calculateSecretHashMethod.invoke(authenticationService, "test@example.com");
            } catch (InvocationTargetException e) {
                // Unwrap the reflection exception to get the actual exception
                if (e.getCause() != null) {
                    throw e.getCause();
                }
                throw e;
            }
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Client secret is not configured but required for SECRET_HASH");
    }

    @Test
    void safe_shouldReturnNullPlaceholder_whenInputIsNull() throws Exception {
        // Given
        // We need to test the private safe method when input is null
        // This method is used in logging to safely handle null strings

        // Use reflection to access the private safe method
        Method safeMethod = AuthenticationService.class.getDeclaredMethod("safe", String.class);
        safeMethod.setAccessible(true);

        // When
        String result = (String) safeMethod.invoke(authenticationService, (String) null);

        // Then
        assertThat(result).isEqualTo("<null>");
    }

    @Test
    void safe_shouldReturnOriginalString_whenInputIsNotNull() throws Exception {
        // Given
        String testString = "test-string";

        // Use reflection to access the private safe method
        Method safeMethod = AuthenticationService.class.getDeclaredMethod("safe", String.class);
        safeMethod.setAccessible(true);

        // When
        String result = (String) safeMethod.invoke(authenticationService, testString);

        // Then
        assertThat(result).isEqualTo("test-string");
    }
}
