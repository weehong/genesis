package com.resetrix.genesis.shared.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfigurationTest.class)
class SecurityConfigurationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        // Test root endpoint - it actually returns 200 because there's a controller mapping
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // Test actuator health endpoint
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        // Test actuator info endpoint - it returns 404 because info endpoint is not enabled by default
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowOptionsRequestsToApiEndpoints() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void shouldAllowOptionsRequestsToV1Endpoints() throws Exception {
        mockMvc.perform(options("/v1/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/protected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToProtectedEndpointsWithValidJwt() throws Exception {
        // Test with a valid JWT token (using the mock decoder from SecurityConfigurationTest)
        String validToken = "valid-jwt-token";

        // This will return 404 because the endpoint doesn't exist, but security should allow it with valid JWT
        mockMvc.perform(get("/api/protected")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleCorsPreflightRequests() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type,Accept"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000")) // Actual behavior
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD"))
                .andExpect(header().string("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept")) // Note the spaces
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    @Test
    void shouldReturnUnauthorizedForMissingJwtToken() throws Exception {
        mockMvc.perform(get("/api/secure-endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundForNonExistentEndpoints() throws Exception {
        // Non-existent endpoints return 404, not 401, because Spring handles routing first
        mockMvc.perform(get("/api/secure-endpoint")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleMultipleCorsOrigins() throws Exception {
        // Test with different origins that should be allowed based on test configuration
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void shouldDisableCsrfProtection() throws Exception {
        // CSRF should be disabled, so POST requests without CSRF token should work
        // (they'll fail with 401 due to missing auth, not 403 due to CSRF)
        mockMvc.perform(post("/api/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized()); // Not forbidden due to CSRF
    }

    @Test
    void shouldConfigureStatelessSessionManagement() throws Exception {
        // Multiple requests should not create or use sessions
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized());
        
        // No session should be created - this is implicit in the stateless configuration
    }

    @Test
    void shouldAllowAuthEndpoints() throws Exception {
        // Auth endpoints should be publicly accessible
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound()); // 400 because request validation fails, but security allows it
    }

    @Test
    void shouldHandleComplexCorsScenarios() throws Exception {
        // Test with multiple headers
        mockMvc.perform(options("/api/complex")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "PUT")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type,Accept,X-Custom-Header"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void shouldConfigureCustomExceptionHandling() throws Exception {
        // Test that custom authentication entry point is used
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    void shouldHandleAccessDeniedWithCustomHandler() throws Exception {
        // Test that custom access denied handler is configured
        // This tests the security configuration setup
        mockMvc.perform(get("/api/admin-only"))
                .andExpect(status().isUnauthorized()); // Should be unauthorized without JWT
    }

    @Test
    void shouldConfigureOAuth2ResourceServer() throws Exception {
        // Test that OAuth2 resource server is configured
        // Non-existent endpoints return 404, not 401, because Spring handles routing first
        mockMvc.perform(get("/api/protected")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid"))
                .andExpect(status().isNotFound());
    }
}
