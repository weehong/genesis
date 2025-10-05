package com.resetrix.genesis.shared.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resetrix.genesis.shared.exceptions.handlers.CustomAccessDeniedHandler;
import com.resetrix.genesis.shared.properties.CognitoProperty;
import com.resetrix.genesis.shared.properties.CorsProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfigurationSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationUnitTest {

    @Mock
    private CognitoProperty cognitoProperty;

    @Mock
    private CorsProperty corsProperty;

    @Mock
    private CognitoJwtValidator cognitoJwtValidator;

    @Mock
    private CognitoJwtAuthenticationConverter cognitoJwtAuthenticationConverter;

    @Mock
    private ObjectMapper objectMapper;

    private SecurityConfiguration securityConfiguration;

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
    void shouldCreateCorsConfigurationSourceWithValidProperties() {
        // Given
        String pathPattern = "/api/**";
        List<String> allowedOrigins = Arrays.asList("http://localhost:3000", "https://example.com");
        List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE");
        List<String> allowedHeaders = Arrays.asList("Authorization", "Content-Type");
        Boolean allowCredentials = true;
        Long maxAge = 3600L;

        when(corsProperty.getPathPattern()).thenReturn(pathPattern);
        when(corsProperty.getAllowedOrigins()).thenReturn(allowedOrigins);
        when(corsProperty.getAllowedMethods()).thenReturn(allowedMethods);
        when(corsProperty.getAllowedHeaders()).thenReturn(allowedHeaders);
        when(corsProperty.getAllowCredentials()).thenReturn(allowCredentials);
        when(corsProperty.getMaxAge()).thenReturn(maxAge);

        // When
        CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();

        // Then
        assertThat(source).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenPathPatternIsNull() {
        // Given
        when(corsProperty.getPathPattern()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> securityConfiguration.corsConfigurationSource())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CORS path pattern must be configured and non-empty");
    }

    @Test
    void shouldThrowExceptionWhenPathPatternIsEmpty() {
        // Given
        when(corsProperty.getPathPattern()).thenReturn("");

        // When & Then
        assertThatThrownBy(() -> securityConfiguration.corsConfigurationSource())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CORS path pattern must be configured and non-empty");
    }

    @Test
    void shouldThrowExceptionWhenPathPatternIsBlank() {
        // Given
        when(corsProperty.getPathPattern()).thenReturn("   ");

        // When & Then
        assertThatThrownBy(() -> securityConfiguration.corsConfigurationSource())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CORS path pattern must be configured and non-empty");
    }

    @Test
    void shouldHandleNullAllowedOrigins() {
        // Given
        String pathPattern = "/api/**";
        when(corsProperty.getPathPattern()).thenReturn(pathPattern);
        when(corsProperty.getAllowedOrigins()).thenReturn(null);

        // When
        CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();

        // Then
        assertThat(source).isNotNull();
    }

    @Test
    void shouldHandleEmptyAllowedOrigins() {
        // Given
        String pathPattern = "/api/**";
        when(corsProperty.getPathPattern()).thenReturn(pathPattern);
        when(corsProperty.getAllowedOrigins()).thenReturn(new ArrayList<>());

        // When
        CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();

        // Then
        assertThat(source).isNotNull();
    }

    @Test
    void shouldHandleNullCorsProperties() {
        // Given
        String pathPattern = "/api/**";
        when(corsProperty.getPathPattern()).thenReturn(pathPattern);
        when(corsProperty.getAllowedOrigins()).thenReturn(List.of("*"));
        when(corsProperty.getAllowedMethods()).thenReturn(null);
        when(corsProperty.getAllowedHeaders()).thenReturn(null);
        when(corsProperty.getAllowCredentials()).thenReturn(null);
        when(corsProperty.getMaxAge()).thenReturn(null);

        // When
        CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();

        // Then
        assertThat(source).isNotNull();
    }

    @Test
    void shouldCreateAuthenticationEntryPoint() {
        // When
        AuthenticationEntryPoint entryPoint = securityConfiguration.authenticationEntryPoint(objectMapper);

        // Then
        assertThat(entryPoint).isNotNull();
        assertThat(entryPoint).isInstanceOf(CustomAuthenticationEntryPoint.class);
    }

    @Test
    void shouldCreateAccessDeniedHandler() {
        // When
        AccessDeniedHandler handler = securityConfiguration.accessDeniedHandler(objectMapper);

        // Then
        assertThat(handler).isNotNull();
        assertThat(handler).isInstanceOf(CustomAccessDeniedHandler.class);
    }

    @Test
    void shouldCreateSecurityConfigurationWithAllDependencies() {
        // When
        SecurityConfiguration config = new SecurityConfiguration(
                cognitoProperty,
                corsProperty,
                cognitoJwtValidator,
                cognitoJwtAuthenticationConverter
        );

        // Then
        assertThat(config).isNotNull();
    }

    @Test
    void shouldCreateJwtValidatorWithTimestampAndCognitoValidators() {
        // Given - SecurityConfiguration is already set up with mocked dependencies

        // When - Use reflection to call the private method
        try {
            Method method = SecurityConfiguration.class.getDeclaredMethod("createJwtValidator");
            method.setAccessible(true);
            OAuth2TokenValidator<Jwt> validator = (OAuth2TokenValidator<Jwt>) method.invoke(securityConfiguration);

            // Then
            assertThat(validator).isNotNull();
            assertThat(validator).isInstanceOf(DelegatingOAuth2TokenValidator.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to test createJwtValidator method", e);
        }
    }

    @Test
    void shouldThrowExceptionWhenJwtDecoderCalledWithInvalidIssuerUri() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn("invalid-uri");

        // When & Then
        assertThatThrownBy(() -> securityConfiguration.jwtDecoder())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenJwtDecoderCalledWithNullIssuerUri() {
        // Given
        when(cognitoProperty.getIssuerUri()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> securityConfiguration.jwtDecoder())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
