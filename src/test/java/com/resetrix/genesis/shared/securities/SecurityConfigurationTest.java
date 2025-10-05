package com.resetrix.genesis.shared.securities;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Test configuration that provides a mock JwtDecoder for testing.
 * This avoids network calls to OIDC endpoints during tests.
 */
@TestConfiguration
public class SecurityConfigurationTest {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return token -> {
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "RS256");
            headers.put("typ", "JWT");

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "test-user");
            claims.put("cognito:username", "testuser");
            claims.put("email", "test@example.com");
            claims.put("cognito:groups", new String[]{"test-group"});

            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> c.putAll(claims))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .subject("test-user")
                    .build();
        };
    }
}
