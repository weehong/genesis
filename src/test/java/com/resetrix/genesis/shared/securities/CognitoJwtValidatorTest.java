package com.resetrix.genesis.shared.securities;

import com.resetrix.genesis.shared.properties.CognitoProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URL;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CognitoJwtValidatorTest {

    private static final String EXPECTED_ISSUER = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_test";
    private static final String EXPECTED_CLIENT_ID = "test-client-id";

    @Mock
    private CognitoProperty cognitoProperty;

    @Mock
    private Jwt jwt;

    private CognitoJwtValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CognitoJwtValidator(cognitoProperty);
    }

    @Test
    void shouldValidateSuccessfullyWithValidJwt() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);
        when(cognitoProperty.getClientId()).thenReturn(EXPECTED_CLIENT_ID);

        URL issuerUrl = new URL(EXPECTED_ISSUER);

        when(jwt.getIssuer()).thenReturn(issuerUrl);
        when(jwt.getClaimAsString("client_id")).thenReturn(EXPECTED_CLIENT_ID);
        when(jwt.getClaimAsString("token_use")).thenReturn("access");
        when(jwt.getIssuedAt()).thenReturn(Instant.now().minusSeconds(60));

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void shouldFailValidationWithInvalidIssuer() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);

        String invalidIssuer = "https://invalid-issuer.com";
        URL issuerUrl = new URL(invalidIssuer);

        when(jwt.getIssuer()).thenReturn(issuerUrl);

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().iterator().next().getErrorCode()).isEqualTo("invalid_issuer");
        assertThat(result.getErrors().iterator().next().getDescription())
                .contains("Expected issuer " + EXPECTED_ISSUER + " but got " + invalidIssuer);
    }

    @Test
    void shouldFailValidationWithInvalidClientId() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);
        when(cognitoProperty.getClientId()).thenReturn(EXPECTED_CLIENT_ID);

        URL issuerUrl = new URL(EXPECTED_ISSUER);
        String invalidClientId = "invalid-client-id";

        when(jwt.getIssuer()).thenReturn(issuerUrl);
        when(jwt.getClaimAsString("client_id")).thenReturn(invalidClientId);

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().iterator().next().getErrorCode()).isEqualTo("invalid_client");
        assertThat(result.getErrors().iterator().next().getDescription())
                .contains("Expected client_id " + EXPECTED_CLIENT_ID + " but got " + invalidClientId);
    }

    @Test
    void shouldFailValidationWithNullClientId() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);
        when(cognitoProperty.getClientId()).thenReturn(EXPECTED_CLIENT_ID);

        URL issuerUrl = new URL(EXPECTED_ISSUER);

        when(jwt.getIssuer()).thenReturn(issuerUrl);
        when(jwt.getClaimAsString("client_id")).thenReturn(null);

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().iterator().next().getErrorCode()).isEqualTo("invalid_client");
        assertThat(result.getErrors().iterator().next().getDescription())
                .contains("Expected client_id " + EXPECTED_CLIENT_ID + " but got null");
    }

    @Test
    void shouldFailValidationWithInvalidTokenUse() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);
        when(cognitoProperty.getClientId()).thenReturn(EXPECTED_CLIENT_ID);

        URL issuerUrl = new URL(EXPECTED_ISSUER);

        when(jwt.getIssuer()).thenReturn(issuerUrl);
        when(jwt.getClaimAsString("client_id")).thenReturn(EXPECTED_CLIENT_ID);
        when(jwt.getClaimAsString("token_use")).thenReturn("id");

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().iterator().next().getErrorCode()).isEqualTo("invalid_token_use");
        assertThat(result.getErrors().iterator().next().getDescription())
                .contains("ID tokens are not acceptable for API authorization")
                .contains("Expected token_use to be 'access' but got 'id'");
    }

    @Test
    void shouldFailValidationWithNullTokenUse() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);
        when(cognitoProperty.getClientId()).thenReturn(EXPECTED_CLIENT_ID);

        URL issuerUrl = new URL(EXPECTED_ISSUER);

        when(jwt.getIssuer()).thenReturn(issuerUrl);
        when(jwt.getClaimAsString("client_id")).thenReturn(EXPECTED_CLIENT_ID);
        when(jwt.getClaimAsString("token_use")).thenReturn(null);

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().iterator().next().getErrorCode()).isEqualTo("invalid_token_use");
        assertThat(result.getErrors().iterator().next().getDescription())
                .contains("Expected token_use to be 'access' but got 'null'");
    }

    @Test
    void shouldFailValidationWithNullIssuedAt() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);
        when(cognitoProperty.getClientId()).thenReturn(EXPECTED_CLIENT_ID);

        URL issuerUrl = new URL(EXPECTED_ISSUER);

        when(jwt.getIssuer()).thenReturn(issuerUrl);
        when(jwt.getClaimAsString("client_id")).thenReturn(EXPECTED_CLIENT_ID);
        when(jwt.getClaimAsString("token_use")).thenReturn("access");
        when(jwt.getIssuedAt()).thenReturn(null);

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().iterator().next().getErrorCode()).isEqualTo("invalid_issued_at");
        assertThat(result.getErrors().iterator().next().getDescription())
                .isEqualTo("JWT issuedAt is missing or in the future");
    }

    @Test
    void shouldFailValidationWithFutureIssuedAt() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);
        when(cognitoProperty.getClientId()).thenReturn(EXPECTED_CLIENT_ID);

        URL issuerUrl = new URL(EXPECTED_ISSUER);

        when(jwt.getIssuer()).thenReturn(issuerUrl);
        when(jwt.getClaimAsString("client_id")).thenReturn(EXPECTED_CLIENT_ID);
        when(jwt.getClaimAsString("token_use")).thenReturn("access");
        when(jwt.getIssuedAt()).thenReturn(Instant.now().plusSeconds(60));

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().iterator().next().getErrorCode()).isEqualTo("invalid_issued_at");
        assertThat(result.getErrors().iterator().next().getDescription())
                .isEqualTo("JWT issuedAt is missing or in the future");
    }

    @Test
    void shouldValidateSuccessfullyWithIssuedAtAtCurrentTime() throws Exception {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);
        when(cognitoProperty.getClientId()).thenReturn(EXPECTED_CLIENT_ID);

        URL issuerUrl = new URL(EXPECTED_ISSUER);

        when(jwt.getIssuer()).thenReturn(issuerUrl);
        when(jwt.getClaimAsString("client_id")).thenReturn(EXPECTED_CLIENT_ID);
        when(jwt.getClaimAsString("token_use")).thenReturn("access");
        when(jwt.getIssuedAt()).thenReturn(Instant.now().minusSeconds(1)); // Use past time to avoid timing issues

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void shouldFailValidationWithNullIssuer() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(EXPECTED_ISSUER);

        when(jwt.getIssuer()).thenReturn(null);

        // When
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().iterator().next().getErrorCode()).isEqualTo("invalid_issuer");
        assertThat(result.getErrors().iterator().next().getDescription())
                .contains("Expected issuer " + EXPECTED_ISSUER + " but got null");
    }

    @Test
    void shouldCreateValidatorWithCognitoProperty() {
        // Given & When
        CognitoJwtValidator newValidator = new CognitoJwtValidator(cognitoProperty);

        // Then
        assertThat(newValidator).isNotNull();
    }
}
