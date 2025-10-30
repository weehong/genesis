package com.resetrix.horaion.shared.services;

import com.resetrix.horaion.shared.enums.AccessMode;
import com.resetrix.horaion.shared.properties.AccessControlProperty;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCompanyServiceTest {

    @Mock
    private AccessControlProperty accessControlProperty;

    @Mock
    private AccessControlProperty.ResourceAccessPattern resourceAccessPattern;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtAuthenticationToken jwtAuthenticationToken;

    @Mock
    private Jwt jwt;

    private UserCompanyService userCompanyService;

    @BeforeEach
    void setUp() {
        userCompanyService = new UserCompanyService(accessControlProperty);
        
        // Setup default access pattern
        when(accessControlProperty.getResourcePattern("DEFAULT")).thenReturn(resourceAccessPattern);
        when(resourceAccessPattern.getReadRoles()).thenReturn(List.of("SYSTEM_ADMINISTRATOR", "USER"));
        when(resourceAccessPattern.getWriteRoles()).thenReturn(List.of("SYSTEM_ADMINISTRATOR", "PRIVILEGED_SYSTEM_USER"));
        when(resourceAccessPattern.getDeleteRoles()).thenReturn(List.of("SYSTEM_ADMINISTRATOR"));
        when(resourceAccessPattern.getAdminRoles()).thenReturn(List.of("SYSTEM_ADMINISTRATOR"));
    }

    @Test
    void hasCompanyAccess_shouldReturnFalse_whenAuthenticationIsNull() {
        // When
        boolean result = userCompanyService.hasCompanyAccess(null, "1", AccessMode.READ);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasCompanyAccess_shouldReturnFalse_whenCompanyIdIsNull() {
        // Given
        when(authentication.getName()).thenReturn("testuser");

        // When
        boolean result = userCompanyService.hasCompanyAccess(authentication, null, AccessMode.READ);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasCompanyAccess_shouldReturnFalse_whenCompanyIdIsEmpty() {
        // Given
        when(authentication.getName()).thenReturn("testuser");

        // When
        boolean result = userCompanyService.hasCompanyAccess(authentication, "", AccessMode.READ);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasCompanyAccess_shouldReturnTrue_whenJwtContainsCompanyId() {
        // Given
        String companyId = "1";
        when(jwtAuthenticationToken.getName()).thenReturn("testuser");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(jwt.getClaimAsStringList("company_ids")).thenReturn(List.of("1", "2", "3"));

        // When
        boolean result = userCompanyService.hasCompanyAccess(jwtAuthenticationToken, companyId, AccessMode.READ);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasCompanyAccess_shouldReturnTrue_whenJwtContainsCompanyUuid() {
        // Given
        String companyUuid = "550e8400-e29b-41d4-a716-446655440000";
        when(jwtAuthenticationToken.getName()).thenReturn("testuser");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(jwt.getClaimAsStringList("company_ids")).thenReturn(null);
        when(jwt.getClaimAsStringList("company_uuids")).thenReturn(List.of(companyUuid));

        // When
        boolean result = userCompanyService.hasCompanyAccess(jwtAuthenticationToken, companyUuid, AccessMode.READ);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasCompanyAccess_shouldReturnFalse_whenJwtDoesNotContainCompanyId() {
        // Given
        String companyId = "999";
        when(jwtAuthenticationToken.getName()).thenReturn("testuser");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(jwt.getClaimAsStringList("company_ids")).thenReturn(List.of("1", "2", "3"));
        when(jwt.getClaimAsStringList("company_uuids")).thenReturn(null);
        when(jwt.getClaimAsStringList("user_companies")).thenReturn(null);

        // When
        boolean result = userCompanyService.hasCompanyAccess(jwtAuthenticationToken, companyId, AccessMode.READ);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasCompanyAccess_shouldReturnTrue_whenUserHasCompanySpecificRole() {
        // Given
        String companyId = "1";
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_COMPANY_1_USER"))
        );

        // When
        boolean result = userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.READ);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasCompanyAccess_shouldReturnTrue_whenUserHasCompanyAdminRole() {
        // Given
        String companyId = "1";
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_COMPANY_1_ADMIN"))
        );

        // When
        boolean result = userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.WRITE);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasCompanyAccess_shouldReturnFalse_whenUserHasInsufficientRole() {
        // Given
        String companyId = "1";
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_COMPANY_1_USER"))
        );

        // When
        boolean result = userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.ADMIN);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasCompanyAccess_shouldReturnFalse_whenUserHasWrongCompanyRole() {
        // Given
        String companyId = "1";
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_COMPANY_2_USER"))
        );

        // When
        boolean result = userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.READ);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasCompanyAccess_shouldHandleJwtClaimsException() {
        // Given
        String companyId = "1";
        when(jwtAuthenticationToken.getName()).thenReturn("testuser");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(jwt.getClaimAsStringList("company_ids")).thenThrow(new RuntimeException("JWT parsing error"));

        // When
        boolean result = userCompanyService.hasCompanyAccess(jwtAuthenticationToken, companyId, AccessMode.READ);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getUserCompanyIds_shouldReturnCompanyIds_whenJwtContainsClaim() {
        // Given
        List<String> expectedCompanyIds = List.of("1", "2", "3");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        when(jwt.getClaimAsStringList("company_ids")).thenReturn(expectedCompanyIds);

        // When
        List<String> result = userCompanyService.getUserCompanyIds(jwtAuthenticationToken);

        // Then
        assertThat(result).isEqualTo(expectedCompanyIds);
    }

    @Test
    void getUserCompanyIds_shouldReturnEmptyList_whenNotJwtAuthentication() {
        // When
        List<String> result = userCompanyService.getUserCompanyIds(authentication);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getUserCompanyIds_shouldReturnEmptyList_whenJwtDoesNotContainClaim() {
        // Given
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        when(jwt.getClaimAsStringList("company_ids")).thenReturn(null);

        // When
        List<String> result = userCompanyService.getUserCompanyIds(jwtAuthenticationToken);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getUserCompanyIds_shouldReturnEmptyList_whenExceptionOccurs() {
        // Given
        when(jwtAuthenticationToken.getName()).thenReturn("testuser");
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
        when(jwt.getClaimAsStringList("company_ids")).thenThrow(new RuntimeException("JWT parsing error"));

        // When
        List<String> result = userCompanyService.getUserCompanyIds(jwtAuthenticationToken);

        // Then
        assertThat(result).isEmpty();
    }
}
