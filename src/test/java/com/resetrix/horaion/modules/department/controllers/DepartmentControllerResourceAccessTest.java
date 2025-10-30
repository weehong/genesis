package com.resetrix.horaion.modules.department.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resetrix.horaion.modules.department.requests.DepartmentRequest;
import com.resetrix.horaion.modules.department.responses.DepartmentResponse;
import com.resetrix.horaion.modules.department.services.IDepartmentService;
import com.resetrix.horaion.shared.enums.AccessMode;
import com.resetrix.horaion.shared.enums.ResourceType;
import com.resetrix.horaion.shared.services.ResourceAccessChecker;
import com.resetrix.horaion.testsupports.securities.SecurityConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for DepartmentController using the new ResourceAccessChecker.
 * 
 * This test verifies that the migrated controller properly uses the hierarchical
 * access control system instead of the old BranchAccessChecker.
 */
@Import(SecurityConfiguration.class)
@WebMvcTest(value = DepartmentController.class)
class DepartmentControllerResourceAccessTest {

    private static final String BASE_URL = "/api/v1/branches/1/departments";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IDepartmentService<DepartmentRequest, DepartmentResponse> departmentService;

    @MockBean
    private ResourceAccessChecker resourceAccessChecker;

    private DepartmentRequest validRequest;
    private DepartmentResponse validResponse;

    @BeforeEach
    void setUp() {
        UUID testUuid = UUID.randomUUID();

        validRequest = new DepartmentRequest(
            1L, // branchId matching the path variable
            "Test Department",
            "TD001",
            "Test Department Description"
        );

        validResponse = new DepartmentResponse(
            1L,
            testUuid,
            1L,
            "Test Branch",
            "Test Department",
            "TD001",
            "Test Department Description",
            false,
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now())
        );
    }

    @Test
    @WithMockUser
    void findAllByBranchId_shouldReturnDepartments_whenUserHasBranchAccess() throws Exception {
        // Given
        Page<DepartmentResponse> departmentPage = new PageImpl<>(List.of(validResponse));
        when(resourceAccessChecker.hasBranchAccess(any(), eq("1"))).thenReturn(true);
        when(departmentService.getAllByBranchId(eq(1L), eq(0), eq(10), eq("id"), eq("ASC")))
            .thenReturn(departmentPage);

        // When & Then
        mockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(validResponse.id()));
    }

    @Test
    @WithMockUser
    void findAllByBranchId_shouldReturnForbidden_whenUserLacksBranchAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasBranchAccess(any(), eq("1"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get(BASE_URL))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void findById_shouldReturnDepartment_whenUserHasDepartmentAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.DEPARTMENT), eq(1L))).thenReturn(true);
        when(departmentService.getById(1L)).thenReturn(validResponse);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(validResponse.id()))
            .andExpect(jsonPath("$.departmentName").value(validResponse.departmentName()));
    }

    @Test
    @WithMockUser
    void findById_shouldReturnForbidden_whenUserLacksDepartmentAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.DEPARTMENT), eq(1L))).thenReturn(false);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/1"))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void findByUuid_shouldReturnDepartment_whenUserHasDepartmentAccess() throws Exception {
        // Given
        UUID departmentUuid = UUID.randomUUID();
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.DEPARTMENT), eq(departmentUuid))).thenReturn(true);
        when(departmentService.getByUuid(departmentUuid)).thenReturn(validResponse);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/" + departmentUuid))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(validResponse.id()));
    }

    @Test
    @WithMockUser
    void create_shouldCreateDepartment_whenUserHasWriteAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.BRANCH), eq("1"), eq(AccessMode.WRITE)))
            .thenReturn(true);
        when(departmentService.save(any(DepartmentRequest.class))).thenReturn(validResponse);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(validResponse.id()));
    }

    @Test
    @WithMockUser
    void create_shouldReturnForbidden_whenUserLacksWriteAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.BRANCH), eq("1"), eq(AccessMode.WRITE)))
            .thenReturn(false);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateById_shouldUpdateDepartment_whenUserHasWriteAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.DEPARTMENT), eq(1L), eq(AccessMode.WRITE)))
            .thenReturn(true);
        when(departmentService.update(eq(1L), any(DepartmentRequest.class))).thenReturn(validResponse);

        // When & Then
        mockMvc.perform(put(BASE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(validResponse.id()));
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnForbidden_whenUserLacksWriteAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.DEPARTMENT), eq(1L), eq(AccessMode.WRITE)))
            .thenReturn(false);

        // When & Then
        mockMvc.perform(put(BASE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void deleteById_shouldDeleteDepartment_whenUserHasDeleteAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.DEPARTMENT), eq(1L), eq(AccessMode.DELETE)))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/1")
                .param("soft", "false")
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnForbidden_whenUserLacksDeleteAccess() throws Exception {
        // Given
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.DEPARTMENT), eq(1L), eq(AccessMode.DELETE)))
            .thenReturn(false);

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/1")
                .param("soft", "false")
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldDeleteDepartment_whenUserHasDeleteAccess() throws Exception {
        // Given
        UUID departmentUuid = UUID.randomUUID();
        when(resourceAccessChecker.hasAccess(any(), eq(ResourceType.DEPARTMENT), eq(departmentUuid), eq(AccessMode.DELETE)))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/" + departmentUuid)
                .param("soft", "true")
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"SYSTEM_ADMINISTRATOR"})
    void findAllByBranchId_shouldAlwaysAllow_whenUserIsSystemAdmin() throws Exception {
        // Given
        Page<DepartmentResponse> departmentPage = new PageImpl<>(List.of(validResponse));
        when(resourceAccessChecker.hasBranchAccess(any(), eq("1"))).thenReturn(true);
        when(departmentService.getAllByBranchId(eq(1L), eq(0), eq(10), eq("id"), eq("ASC")))
            .thenReturn(departmentPage);

        // When & Then
        mockMvc.perform(get(BASE_URL))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
