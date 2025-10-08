package com.resetrix.genesis.shared.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resetrix.genesis.shared.exceptions.handlers.CustomAccessDeniedHandler;
import com.resetrix.genesis.shared.properties.CognitoProperty;
import com.resetrix.genesis.shared.properties.CorsProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static com.resetrix.genesis.shared.constants.SecurityConstants.PUBLIC_ENDPOINTS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SecurityConfiguration.class);

    private final CognitoProperty cognitoProperty;
    private final CorsProperty corsProperty;
    private final CognitoJwtValidator cognitoJwtValidator;
    private final CognitoJwtAuthenticationConverter
            cognitoJwtAuthenticationConverter;

    public SecurityConfiguration(
            CognitoProperty cognitoProperty,
            CorsProperty corsProperty,
            CognitoJwtValidator cognitoJwtValidator,
            CognitoJwtAuthenticationConverter cognitoJwtAuthenticationConverter) {
        this.cognitoProperty = cognitoProperty;
        this.corsProperty = corsProperty;
        this.cognitoJwtValidator = cognitoJwtValidator;
        this.cognitoJwtAuthenticationConverter = cognitoJwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectMapper objectMapper)
            throws Exception {
        LOGGER.info("Configuring security filter chain");

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(this::configureSessionManagement)
                .authorizeHttpRequests(this::configureAuthorization)
                .oauth2ResourceServer(this::configureOAuth2ResourceServer)
                .exceptionHandling(
                        exceptions -> configureExceptionHandling(exceptions, objectMapper))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        LOGGER.info("Configuring JWT decoder for issuer: {}",
                cognitoProperty.getIssuerUri());

        // Consider adding a separate JWKS/issuer health-check bean to monitor availability in production
        NimbusJwtDecoder decoder;

        try {
            decoder = JwtDecoders.fromOidcIssuerLocation(
                    cognitoProperty.getIssuerUri());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to configure JWT decoder from OIDC issuer location: {}. Error: {}",
                    cognitoProperty.getIssuerUri(), e.getMessage(), e);
            throw e; // Re-throw IllegalArgumentException as-is
        } catch (Exception e) {
            LOGGER.error("Failed to configure JWT decoder from OIDC issuer location: {}. Error: {}",
                    cognitoProperty.getIssuerUri(), e.getMessage(), e);
            throw new RuntimeException("JWT decoder configuration failed for issuer: "
                    + cognitoProperty.getIssuerUri(), e);
        }

        OAuth2TokenValidator<Jwt> validator = createJwtValidator();
        decoder.setJwtValidator(validator);

        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        LOGGER.debug("Configuring CORS with allowed origins: {}",
                corsProperty.getAllowedOrigins());

        String pathPattern = corsProperty.getPathPattern();
        if (pathPattern == null || pathPattern.trim().isEmpty()) {
            throw new IllegalStateException(
                    "CORS path pattern must be configured and non-empty");
        }

        if (corsProperty.getAllowedOrigins() == null) {
            LOGGER.warn("CORS allowed origins is null, using empty list");
        } else if (corsProperty.getAllowedOrigins().isEmpty()) {
            LOGGER.warn(
                    "CORS allowed origins is empty, CORS may not work as expected");
        }

        CorsConfiguration configuration = createCorsConfiguration();
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(pathPattern, configuration);

        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(
            ObjectMapper objectMapper) {
        return new CustomAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return new CustomAccessDeniedHandler(objectMapper);
    }

    private void configureSessionManagement(
            SessionManagementConfigurer<HttpSecurity> session) {
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        LOGGER.info("Configuring authorization rules");

        authorize
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/v1/**", "/api/**").permitAll()
                .anyRequest()
                .authenticated();

        LOGGER.info("Authorization configured - public endpoints: {}", PUBLIC_ENDPOINTS);
    }

    private void configureOAuth2ResourceServer(
            OAuth2ResourceServerConfigurer<HttpSecurity> oauth) {
        oauth.jwt(jwt -> jwt
                .decoder(jwtDecoder())
                .jwtAuthenticationConverter(cognitoJwtAuthenticationConverter)
        );
    }

    private void configureExceptionHandling(
            ExceptionHandlingConfigurer<HttpSecurity> exceptions,
            ObjectMapper objectMapper) {
        exceptions
                .authenticationEntryPoint(authenticationEntryPoint(objectMapper))
                .accessDeniedHandler(accessDeniedHandler(objectMapper));
    }

    private OAuth2TokenValidator<Jwt> createJwtValidator() {
        return new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                cognitoJwtValidator
        );
    }

    private CorsConfiguration createCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (corsProperty.getAllowedOrigins() != null) {
            configuration.setAllowedOriginPatterns(corsProperty.getAllowedOrigins());
        }

        if (corsProperty.getAllowedMethods() != null) {
            configuration.setAllowedMethods(corsProperty.getAllowedMethods());
        }

        if (corsProperty.getAllowedHeaders() != null) {
            configuration.setAllowedHeaders(corsProperty.getAllowedHeaders());
        }

        if (corsProperty.getAllowCredentials() != null) {
            configuration.setAllowCredentials(corsProperty.getAllowCredentials());
        }

        if (corsProperty.getMaxAge() != null) {
            configuration.setMaxAge(corsProperty.getMaxAge());
        }

        return configuration;
    }
}