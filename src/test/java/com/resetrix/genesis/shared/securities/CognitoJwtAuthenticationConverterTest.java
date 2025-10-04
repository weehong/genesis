package com.resetrix.genesis.shared.securities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CognitoJwtAuthenticationConverterTest {

    @Mock
    private Jwt jwt;

    private CognitoJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CognitoJwtAuthenticationConverter();
    }

    @Test
    void shouldConvertJwtWithCognitoGroupsOnly() {
        // Given
        List<String> cognitoGroups = Arrays.asList("admin", "user");

        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(cognitoGroups);
        when(jwt.getClaimAsStringList("custom:roles")).thenReturn(null);

        // When
        AbstractAuthenticationToken result = converter.convert(jwt);

        // Then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) result;
        assertThat(jwtToken.getToken()).isEqualTo(jwt);

        Collection<GrantedAuthority> authorities = jwtToken.getAuthorities();
        assertThat(authorities).hasSize(2);
        assertThat(authorities).containsExactlyInAnyOrder(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
    }

    @Test
    void shouldConvertJwtWithCustomRolesOnly() {
        // Given
        List<String> customRoles = Arrays.asList("manager", "developer");

        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(null);
        when(jwt.getClaimAsStringList("custom:roles")).thenReturn(customRoles);

        // When
        AbstractAuthenticationToken result = converter.convert(jwt);

        // Then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) result;
        assertThat(jwtToken.getToken()).isEqualTo(jwt);

        Collection<GrantedAuthority> authorities = jwtToken.getAuthorities();
        assertThat(authorities).hasSize(2);
        assertThat(authorities).containsExactlyInAnyOrder(
                new SimpleGrantedAuthority("ROLE_MANAGER"),
                new SimpleGrantedAuthority("ROLE_DEVELOPER")
        );
    }

    @Test
    void shouldConvertJwtWithBothCognitoGroupsAndCustomRoles() {
        // Given
        List<String> cognitoGroups = Arrays.asList("admin", "user");
        List<String> customRoles = Arrays.asList("manager", "developer");

        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(cognitoGroups);
        when(jwt.getClaimAsStringList("custom:roles")).thenReturn(customRoles);

        // When
        AbstractAuthenticationToken result = converter.convert(jwt);

        // Then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) result;
        assertThat(jwtToken.getToken()).isEqualTo(jwt);

        Collection<GrantedAuthority> authorities = jwtToken.getAuthorities();
        assertThat(authorities).hasSize(4);
        assertThat(authorities).containsExactlyInAnyOrder(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_MANAGER"),
                new SimpleGrantedAuthority("ROLE_DEVELOPER")
        );
    }

    @Test
    void shouldConvertJwtWithEmptyGroupsAndRoles() {
        // Given
        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(List.of());
        when(jwt.getClaimAsStringList("custom:roles")).thenReturn(List.of());

        // When
        AbstractAuthenticationToken result = converter.convert(jwt);

        // Then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) result;
        assertThat(jwtToken.getToken()).isEqualTo(jwt);

        Collection<GrantedAuthority> authorities = jwtToken.getAuthorities();
        assertThat(authorities).isEmpty();
    }

    @Test
    void shouldConvertJwtWithNullGroupsAndRoles() {
        // Given
        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(null);
        when(jwt.getClaimAsStringList("custom:roles")).thenReturn(null);

        // When
        AbstractAuthenticationToken result = converter.convert(jwt);

        // Then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) result;
        assertThat(jwtToken.getToken()).isEqualTo(jwt);

        Collection<GrantedAuthority> authorities = jwtToken.getAuthorities();
        assertThat(authorities).isEmpty();
    }

    @Test
    void shouldConvertGroupsToUppercaseRoles() {
        // Given
        List<String> cognitoGroups = Arrays.asList("admin", "User", "MANAGER");

        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(cognitoGroups);
        when(jwt.getClaimAsStringList("custom:roles")).thenReturn(null);

        // When
        AbstractAuthenticationToken result = converter.convert(jwt);

        // Then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) result;

        Collection<GrantedAuthority> authorities = jwtToken.getAuthorities();
        assertThat(authorities).hasSize(3);
        assertThat(authorities).containsExactlyInAnyOrder(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_MANAGER")
        );
    }

    @Test
    void shouldConvertCustomRolesToUppercaseRoles() {
        // Given
        List<String> customRoles = Arrays.asList("developer", "Tester", "ANALYST");

        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(null);
        when(jwt.getClaimAsStringList("custom:roles")).thenReturn(customRoles);

        // When
        AbstractAuthenticationToken result = converter.convert(jwt);

        // Then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) result;

        Collection<GrantedAuthority> authorities = jwtToken.getAuthorities();
        assertThat(authorities).hasSize(3);
        assertThat(authorities).containsExactlyInAnyOrder(
                new SimpleGrantedAuthority("ROLE_DEVELOPER"),
                new SimpleGrantedAuthority("ROLE_TESTER"),
                new SimpleGrantedAuthority("ROLE_ANALYST")
        );
    }

    @Test
    void shouldHandleSingleGroupAndRole() {
        // Given
        List<String> cognitoGroups = List.of("admin");
        List<String> customRoles = List.of("manager");

        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(cognitoGroups);
        when(jwt.getClaimAsStringList("custom:roles")).thenReturn(customRoles);

        // When
        AbstractAuthenticationToken result = converter.convert(jwt);

        // Then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) result;

        Collection<GrantedAuthority> authorities = jwtToken.getAuthorities();
        assertThat(authorities).hasSize(2);
        assertThat(authorities).containsExactlyInAnyOrder(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MANAGER")
        );
    }

    @Test
    void shouldCreateConverterInstance() {
        // Given & When
        CognitoJwtAuthenticationConverter newConverter = new CognitoJwtAuthenticationConverter();

        // Then
        assertThat(newConverter).isNotNull();
    }
}
