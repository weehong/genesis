package com.resetrix.horaion.integration;

import com.resetrix.horaion.shared.services.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test demonstrating the JWT company claims extraction
 * working with the actual token structure from AWS Cognito.
 */
class JwtCompanyClaimsIntegrationTest {

    private final AuthorizationService authorizationService = new AuthorizationService();

    @Test
    void shouldExtractCompanyClaimsFromRealTokenStructure() {
        // Given - JWT token structure matching the actual AWS Cognito access token
        Jwt jwt = createRealWorldJwtToken();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
            jwt, 
            List.of(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMINISTRATOR"))
        );

        // When - Extract company information
        String companyId = authorizationService.getUserCompanyId(authentication);
        String companyUuid = authorizationService.getUserCompanyUuid(authentication);
        boolean canAccessCompany1 = authorizationService.canAccessCompanyResources(authentication, "1");
        boolean canAccessCompanyUuid = authorizationService.canAccessCompanyResources(
            authentication, "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b"
        );

        // Then - Verify correct extraction
        assertThat(companyId).isEqualTo("1");
        assertThat(companyUuid).isEqualTo("33ff4685-9590-4b0c-8cc7-ed2490c2cf5b");
        assertThat(canAccessCompany1).isTrue();
        assertThat(canAccessCompanyUuid).isTrue();
    }

    @Test
    void shouldWorkWithUserRoleAndCompanyRestrictions() {
        // Given - Regular user (not system admin) with company claims
        Jwt jwt = createUserJwtToken();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
            jwt, 
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When - Test company access
        boolean canAccessOwnCompany = authorizationService.canAccessCompanyResources(authentication, "1");
        boolean canAccessOtherCompany = authorizationService.canAccessCompanyResources(authentication, "2");

        // Then - Can access own company but not others
        assertThat(canAccessOwnCompany).isTrue();
        assertThat(canAccessOtherCompany).isFalse();
    }

    @Test
    void shouldDemonstrateSystemAdminBypass() {
        // Given - System admin without company claims
        Jwt jwt = createSystemAdminJwtToken();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
            jwt, 
            List.of(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMINISTRATOR"))
        );

        // When - Test access to any company
        boolean canAccessCompany1 = authorizationService.canAccessCompanyResources(authentication, "1");
        boolean canAccessCompany2 = authorizationService.canAccessCompanyResources(authentication, "2");
        boolean canAccessAnyCompany = authorizationService.canAccessCompanyResources(authentication, "999");

        // Then - System admin can access any company
        assertThat(canAccessCompany1).isTrue();
        assertThat(canAccessCompany2).isTrue();
        assertThat(canAccessAnyCompany).isTrue();
    }

    @Test
    void shouldHandleBothCompanyIdAndUuidClaims() {
        // Given - Token with both company_id and company_uuid
        Jwt jwt = createJwtWithBothCompanyClaims();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
            jwt, 
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When - Extract company information
        String primaryCompanyId = authorizationService.getUserCompanyId(authentication);
        boolean belongsToCompanyById = authorizationService.belongsToCompany(authentication, "1");
        boolean belongsToCompanyByUuid = authorizationService.belongsToCompany(
            authentication, "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b"
        );

        // Then - Should prioritize company_id but accept both
        assertThat(primaryCompanyId).isEqualTo("1"); // Prioritizes company_id
        assertThat(belongsToCompanyById).isTrue();
        assertThat(belongsToCompanyByUuid).isTrue();
    }

    /**
     * Creates a JWT token matching the real AWS Cognito access token structure
     * provided in the requirements.
     */
    private Jwt createRealWorldJwtToken() {
        Map<String, Object> claims = Map.of(
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b",
            "company_id", "1",
            "cognito:groups", List.of("system-administrator"),
            "token_use", "access",
            "username", "59aa851c-b051-7072-fa58-8858e5d860fb"
        );

        return new Jwt(
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
            Instant.ofEpochSecond(1761549007),
            Instant.ofEpochSecond(1761552606),
            Map.of("alg", "RS256", "typ", "JWT"),
            claims
        );
    }

    /**
     * Creates a JWT token for a regular user with company claims.
     */
    private Jwt createUserJwtToken() {
        Map<String, Object> claims = Map.of(
            "company_id", "1",
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b",
            "cognito:groups", List.of("user"),
            "token_use", "access",
            "username", "regular-user"
        );

        return new Jwt(
            "user-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            claims
        );
    }

    /**
     * Creates a JWT token for a system administrator without company claims.
     */
    private Jwt createSystemAdminJwtToken() {
        Map<String, Object> claims = Map.of(
            "cognito:groups", List.of("system-administrator"),
            "token_use", "access",
            "username", "system-admin"
        );

        return new Jwt(
            "admin-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            claims
        );
    }

    /**
     * Creates a JWT token with both company_id and company_uuid claims.
     */
    private Jwt createJwtWithBothCompanyClaims() {
        Map<String, Object> claims = Map.of(
            "company_id", "1",
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b",
            "cognito:groups", List.of("user"),
            "token_use", "access",
            "username", "user-with-both-claims"
        );

        return new Jwt(
            "both-claims-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            claims
        );
    }
}
