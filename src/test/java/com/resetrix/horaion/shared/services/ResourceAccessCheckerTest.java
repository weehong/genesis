package com.resetrix.horaion.shared.services;

import com.resetrix.horaion.shared.enums.AccessMode;
import com.resetrix.horaion.shared.enums.ResourceType;
import com.resetrix.horaion.shared.models.ResourceIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceAccessCheckerTest {

    @Mock
    private ResourceHierarchyService hierarchyService;

    @Mock
    private UserCompanyService userCompanyService;

    @Mock
    private Authentication authentication;

    private ResourceAccessChecker resourceAccessChecker;

    @BeforeEach
    void setUp() {
        resourceAccessChecker = new ResourceAccessChecker(hierarchyService, userCompanyService);
    }

    @Test
    void hasAccess_shouldReturnFalse_whenAuthenticationIsNull() {
        // When
        boolean result = resourceAccessChecker.hasAccess(null, ResourceType.COMPANY, "1");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAccess_shouldReturnFalse_whenResourceTypeIsNull() {
        // Given
        when(authentication.getName()).thenReturn("testuser");

        // When
        boolean result = resourceAccessChecker.hasAccess(authentication, null, "1");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAccess_shouldReturnFalse_whenResourceIdIsNull() {
        // Given
        when(authentication.getName()).thenReturn("testuser");

        // When
        boolean result = resourceAccessChecker.hasAccess(authentication, ResourceType.COMPANY, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAccess_shouldReturnTrue_whenUserIsSystemAdministrator() {
        // Given
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMINISTRATOR"))
        );

        // When
        boolean result = resourceAccessChecker.hasAccess(authentication, ResourceType.COMPANY, "1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasAccess_shouldValidateHierarchy_whenUserIsNotSystemAdmin() {
        // Given
        String companyId = "1";
        String branchId = "2";
        
        when(authentication.getName()).thenReturn("user");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        List<ResourceIdentifier> hierarchyPath = List.of(
            ResourceIdentifier.of(ResourceType.COMPANY, companyId),
            ResourceIdentifier.of(ResourceType.BRANCH, branchId)
        );

        when(hierarchyService.getHierarchyPath(ResourceType.BRANCH, branchId))
            .thenReturn(hierarchyPath);
        when(userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.READ))
            .thenReturn(true);

        // When
        boolean result = resourceAccessChecker.hasAccess(authentication, ResourceType.BRANCH, branchId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasAccess_shouldReturnFalse_whenHierarchyValidationFails() {
        // Given
        String companyId = "1";
        String branchId = "2";
        
        when(authentication.getName()).thenReturn("user");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        List<ResourceIdentifier> hierarchyPath = List.of(
            ResourceIdentifier.of(ResourceType.COMPANY, companyId),
            ResourceIdentifier.of(ResourceType.BRANCH, branchId)
        );

        when(hierarchyService.getHierarchyPath(ResourceType.BRANCH, branchId))
            .thenReturn(hierarchyPath);
        when(userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.READ))
            .thenReturn(false);

        // When
        boolean result = resourceAccessChecker.hasAccess(authentication, ResourceType.BRANCH, branchId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAccess_shouldReturnFalse_whenHierarchyServiceThrowsException() {
        // Given
        when(authentication.getName()).thenReturn("user");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(hierarchyService.getHierarchyPath(ResourceType.BRANCH, "2"))
            .thenThrow(new RuntimeException("Branch not found"));

        // When
        boolean result = resourceAccessChecker.hasAccess(authentication, ResourceType.BRANCH, "2");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAccess_shouldValidateAccessMode_whenSpecified() {
        // Given
        String companyId = "1";
        String departmentId = "3";
        
        when(authentication.getName()).thenReturn("user");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        List<ResourceIdentifier> hierarchyPath = List.of(
            ResourceIdentifier.of(ResourceType.COMPANY, companyId),
            ResourceIdentifier.of(ResourceType.BRANCH, "2"),
            ResourceIdentifier.of(ResourceType.DEPARTMENT, departmentId)
        );

        when(hierarchyService.getHierarchyPath(ResourceType.DEPARTMENT, departmentId))
            .thenReturn(hierarchyPath);
        when(userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.WRITE))
            .thenReturn(true);

        // When
        boolean result = resourceAccessChecker.hasAccess(authentication, ResourceType.DEPARTMENT, departmentId, AccessMode.WRITE);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasCompanyAccess_shouldDelegateToGenericMethod() {
        // Given
        String companyId = "1";
        
        when(authentication.getName()).thenReturn("user");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        List<ResourceIdentifier> hierarchyPath = List.of(
            ResourceIdentifier.of(ResourceType.COMPANY, companyId)
        );

        when(hierarchyService.getHierarchyPath(ResourceType.COMPANY, companyId))
            .thenReturn(hierarchyPath);
        when(userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.READ))
            .thenReturn(true);

        // When
        boolean result = resourceAccessChecker.hasCompanyAccess(authentication, companyId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasBranchAccess_shouldDelegateToGenericMethod() {
        // Given
        String companyId = "1";
        String branchId = "2";
        
        when(authentication.getName()).thenReturn("user");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        List<ResourceIdentifier> hierarchyPath = List.of(
            ResourceIdentifier.of(ResourceType.COMPANY, companyId),
            ResourceIdentifier.of(ResourceType.BRANCH, branchId)
        );

        when(hierarchyService.getHierarchyPath(ResourceType.BRANCH, branchId))
            .thenReturn(hierarchyPath);
        when(userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.READ))
            .thenReturn(true);

        // When
        boolean result = resourceAccessChecker.hasBranchAccess(authentication, branchId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasDepartmentAccess_shouldDelegateToGenericMethod() {
        // Given
        String companyId = "1";
        String departmentId = "3";
        
        when(authentication.getName()).thenReturn("user");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        List<ResourceIdentifier> hierarchyPath = List.of(
            ResourceIdentifier.of(ResourceType.COMPANY, companyId),
            ResourceIdentifier.of(ResourceType.BRANCH, "2"),
            ResourceIdentifier.of(ResourceType.DEPARTMENT, departmentId)
        );

        when(hierarchyService.getHierarchyPath(ResourceType.DEPARTMENT, departmentId))
            .thenReturn(hierarchyPath);
        when(userCompanyService.hasCompanyAccess(authentication, companyId, AccessMode.READ))
            .thenReturn(true);

        // When
        boolean result = resourceAccessChecker.hasDepartmentAccess(authentication, departmentId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasAccess_shouldHandleLongIds() {
        // Given
        Long companyId = 1L;
        
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMINISTRATOR"))
        );

        // When
        boolean result = resourceAccessChecker.hasAccess(authentication, ResourceType.COMPANY, companyId);

        // Then
        assertThat(result).isTrue();
    }
}
