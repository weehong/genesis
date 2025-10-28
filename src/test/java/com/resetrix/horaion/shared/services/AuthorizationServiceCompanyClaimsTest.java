package com.resetrix.horaion.shared.services;

import com.resetrix.horaion.shared.exceptions.MissingCompanyClaimsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceCompanyClaimsTest {

    @Mock
    private Authentication authentication;

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();
    }

    @Test
    void canAccessCompanyResources_shouldReturnTrue_whenUserHasCompanyIdClaim() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_id", "1",
            "cognito:groups", List.of("user")
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        boolean result = authorizationService.canAccessCompanyResources(jwtAuth, "1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canAccessCompanyResources_shouldReturnTrue_whenUserHasCompanyUuidClaim() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b",
            "cognito:groups", List.of("user")
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        boolean result = authorizationService.canAccessCompanyResources(jwtAuth, "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canAccessCompanyResources_shouldReturnTrue_whenUserHasBothClaimsAndMatchesCompanyId() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_id", "1",
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b",
            "cognito:groups", List.of("user")
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        boolean result = authorizationService.canAccessCompanyResources(jwtAuth, "1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canAccessCompanyResources_shouldReturnTrue_whenUserHasBothClaimsAndMatchesCompanyUuid() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_id", "1",
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b",
            "cognito:groups", List.of("user")
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        boolean result = authorizationService.canAccessCompanyResources(jwtAuth, "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canAccessCompanyResources_shouldReturnFalse_whenUserCompanyIdDoesNotMatch() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_id", "1",
            "cognito:groups", List.of("user")
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        boolean result = authorizationService.canAccessCompanyResources(jwtAuth, "2");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canAccessCompanyResources_shouldThrowException_whenNoCompanyClaims() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "cognito:groups", List.of("user")
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When & Then
        assertThatThrownBy(() -> authorizationService.canAccessCompanyResources(jwtAuth, "1"))
            .isInstanceOf(MissingCompanyClaimsException.class)
            .hasMessageContaining("JWT token is missing required company claims (company_id or company_uuid)");
    }

    @Test
    void canAccessCompanyResources_shouldReturnTrue_whenSystemAdministrator() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "cognito:groups", List.of("system-administrator")
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_SYSTEM_ADMINISTRATOR"));

        // When
        boolean result = authorizationService.canAccessCompanyResources(jwtAuth, "1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void getUserCompanyId_shouldReturnCompanyId_whenCompanyIdClaimExists() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_id", "1",
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b"
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        String result = authorizationService.getUserCompanyId(jwtAuth);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void getUserCompanyId_shouldReturnCompanyUuid_whenOnlyCompanyUuidClaimExists() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b"
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        String result = authorizationService.getUserCompanyId(jwtAuth);

        // Then
        assertThat(result).isEqualTo("33ff4685-9590-4b0c-8cc7-ed2490c2cf5b");
    }

    @Test
    void getUserCompanyId_shouldReturnNull_whenNoCompanyClaims() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "cognito:groups", List.of("user")
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        String result = authorizationService.getUserCompanyId(jwtAuth);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getUserCompanyUuid_shouldReturnCompanyUuid_whenCompanyUuidClaimExists() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b"
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        String result = authorizationService.getUserCompanyUuid(jwtAuth);

        // Then
        assertThat(result).isEqualTo("33ff4685-9590-4b0c-8cc7-ed2490c2cf5b");
    }

    @Test
    void belongsToCompany_shouldReturnTrue_whenCompanyIdMatches() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_id", "1"
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        boolean result = authorizationService.belongsToCompany(jwtAuth, "1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void belongsToCompany_shouldReturnTrue_whenCompanyUuidMatches() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_uuid", "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b"
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        boolean result = authorizationService.belongsToCompany(jwtAuth, "33ff4685-9590-4b0c-8cc7-ed2490c2cf5b");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void belongsToCompany_shouldReturnFalse_whenCompanyIdDoesNotMatch() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of(
            "company_id", "1"
        ));
        JwtAuthenticationToken jwtAuth = createJwtAuthenticationToken(jwt, List.of("ROLE_USER"));

        // When
        boolean result = authorizationService.belongsToCompany(jwtAuth, "2");

        // Then
        assertThat(result).isFalse();
    }

    private Jwt createJwtWithClaims(Map<String, Object> claims) {
        return new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            claims
        );
    }

    private JwtAuthenticationToken createJwtAuthenticationToken(Jwt jwt, List<String> authorities) {
        Collection<GrantedAuthority> grantedAuthorities = authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .map(GrantedAuthority.class::cast)
            .toList();
        
        return new JwtAuthenticationToken(jwt, grantedAuthorities);
    }
}
