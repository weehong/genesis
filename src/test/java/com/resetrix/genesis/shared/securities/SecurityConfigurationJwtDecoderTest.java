package com.resetrix.genesis.shared.securities;

import com.resetrix.genesis.shared.properties.CognitoProperty;
import com.resetrix.genesis.shared.properties.CorsProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationJwtDecoderTest {

    @Mock
    private CognitoProperty cognitoProperty;

    @Mock
    private CorsProperty corsProperty;

    @Mock
    private CognitoJwtValidator cognitoJwtValidator;

    @Mock
    private CognitoJwtAuthenticationConverter cognitoJwtAuthenticationConverter;

    @Mock
    private NimbusJwtDecoder mockJwtDecoder;

    private SecurityConfiguration securityConfiguration;

    private static final String VALID_ISSUER_URI = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_test";

    @BeforeEach
    void setUp() {
        securityConfiguration = new SecurityConfiguration(
                cognitoProperty,
                corsProperty,
                cognitoJwtValidator,
                cognitoJwtAuthenticationConverter
        );
    }

    @Test
    void shouldCreateJwtDecoderSuccessfully() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(VALID_ISSUER_URI);

        // When & Then - Use static mocking to mock JwtDecoders.fromOidcIssuerLocation
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER_URI))
                    .thenReturn(mockJwtDecoder);

            // When
            JwtDecoder result = securityConfiguration.jwtDecoder();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(mockJwtDecoder);
            
            // Verify that the validator was set on the decoder
            verify(mockJwtDecoder).setJwtValidator(any());
            
            // Verify that the static method was called with correct issuer URI
            jwtDecodersMock.verify(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER_URI));
        }
    }

    @Test
    void shouldLogIssuerUriWhenCreatingJwtDecoder() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(VALID_ISSUER_URI);

        // When & Then - Use static mocking
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(anyString()))
                    .thenReturn(mockJwtDecoder);

            // When
            JwtDecoder result = securityConfiguration.jwtDecoder();

            // Then
            assertThat(result).isNotNull();
            
            // Verify the issuer URI was used
            jwtDecodersMock.verify(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER_URI));
        }
    }

    @Test
    void shouldHandleExceptionWhenJwtDecodersThrowsException() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(VALID_ISSUER_URI);

        // When & Then - Use static mocking to simulate exception
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER_URI))
                    .thenThrow(new IllegalArgumentException("Invalid issuer URI"));

            // When & Then
            assertThatThrownBy(() -> securityConfiguration.jwtDecoder())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid issuer URI");
        }
    }

    @Test
    void shouldCreateJwtDecoderWithDifferentIssuerUri() {
        // Given
        String differentIssuerUri = "https://cognito-idp.eu-west-1.amazonaws.com/eu-west-1_different";
        when(cognitoProperty.getIssuerUri()).thenReturn(differentIssuerUri);

        // When & Then - Use static mocking
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(differentIssuerUri))
                    .thenReturn(mockJwtDecoder);

            // When
            JwtDecoder result = securityConfiguration.jwtDecoder();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(mockJwtDecoder);
            
            // Verify the correct issuer URI was used
            jwtDecodersMock.verify(() -> JwtDecoders.fromOidcIssuerLocation(differentIssuerUri));
        }
    }

    @Test
    void shouldSetValidatorOnJwtDecoder() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(VALID_ISSUER_URI);

        // When & Then - Use static mocking
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER_URI))
                    .thenReturn(mockJwtDecoder);

            // When
            securityConfiguration.jwtDecoder();

            // Then - Verify that setJwtValidator was called on the decoder
            verify(mockJwtDecoder).setJwtValidator(any());
        }
    }

    @Test
    void shouldHandleNullIssuerUri() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(null);

        // When & Then - Use static mocking
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(null))
                    .thenThrow(new IllegalArgumentException("Issuer URI cannot be null"));

            // When & Then
            assertThatThrownBy(() -> securityConfiguration.jwtDecoder())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Issuer URI cannot be null");
        }
    }

    @Test
    void shouldHandleEmptyIssuerUri() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn("");

        // When & Then - Use static mocking
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(""))
                    .thenThrow(new IllegalArgumentException("Issuer URI cannot be empty"));

            // When & Then
            assertThatThrownBy(() -> securityConfiguration.jwtDecoder())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Issuer URI cannot be empty");
        }
    }

    @Test
    void shouldWrapNonIllegalArgumentExceptionInRuntimeException() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(VALID_ISSUER_URI);

        // When & Then - Use static mocking to simulate a non-IllegalArgumentException
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER_URI))
                    .thenThrow(new RuntimeException("Network connection failed"));

            // When & Then
            assertThatThrownBy(() -> securityConfiguration.jwtDecoder())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("JWT decoder configuration failed for issuer: " + VALID_ISSUER_URI)
                    .hasCauseInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("Network connection failed");
        }
    }

    @Test
    void shouldCreateJwtDecoderWithValidConfiguration() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(VALID_ISSUER_URI);

        // When & Then - Use static mocking
        try (MockedStatic<JwtDecoders> jwtDecodersMock = mockStatic(JwtDecoders.class)) {
            jwtDecodersMock.when(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER_URI))
                    .thenReturn(mockJwtDecoder);

            // When
            JwtDecoder result = securityConfiguration.jwtDecoder();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(mockJwtDecoder);

            // Verify all expected interactions - getIssuerUri() is called twice (logging + decoder creation)
            verify(cognitoProperty, org.mockito.Mockito.times(2)).getIssuerUri();
            verify(mockJwtDecoder).setJwtValidator(any());
            jwtDecodersMock.verify(() -> JwtDecoders.fromOidcIssuerLocation(VALID_ISSUER_URI));
        }
    }
}
