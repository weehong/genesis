package com.resetrix.horaion.modules.department.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resetrix.horaion.modules.department.requests.DepartmentRequest;
import com.resetrix.horaion.modules.department.responses.DepartmentResponse;
import com.resetrix.horaion.modules.department.services.IDepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IDepartmentService<DepartmentRequest, DepartmentResponse> departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private DepartmentRequest validRequest;
    private DepartmentResponse validResponse;

    @BeforeEach
    void setUp() {
        UUID testUuid = UUID.randomUUID();

        // Create valid request with matching branchId
        validRequest = new DepartmentRequest(
            1L, // branchId matching the path variable
            "Test Department",
            "TD001",
            "Test department description"
        );

        // Create valid response
        validResponse = new DepartmentResponse(
            1L,
            testUuid,
            1L,
            "Test Branch",
            1L,
            "Test Company",
            "Test Department",
            "TD001",
            "Test department description",
            false,
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now())
        );
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void findAllByBranchId_WithNumericBranchId_ShouldReturnPagedResults() throws Exception {
        Page<DepartmentResponse> page = new PageImpl<>(List.of(validResponse));
        when(departmentService.getAllByBranchId(eq(1L), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(page);

        mockMvc.perform(get("/api/v1/branches/1/departments")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].departmentName").value("Test Department"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void findAllByBranchId_WithUuidBranchId_ShouldReturnPagedResults() throws Exception {
        UUID branchUuid = UUID.randomUUID();
        Page<DepartmentResponse> page = new PageImpl<>(List.of(validResponse));
        when(departmentService.getAllByBranchId(eq(branchUuid), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(page);

        mockMvc.perform(get("/api/v1/branches/" + branchUuid + "/departments")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].departmentName").value("Test Department"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void findAllByBranchId_WithoutPagination_ShouldReturnList() throws Exception {
        when(departmentService.getAllByBranchId(eq(1L)))
            .thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/v1/branches/1/departments/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].departmentName").value("Test Department"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void findById_ShouldReturnDepartment() throws Exception {
        when(departmentService.getById(eq(1L)))
            .thenReturn(validResponse);

        mockMvc.perform(get("/api/v1/branches/1/departments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.departmentName").value("Test Department"))
            .andExpect(jsonPath("$.departmentCode").value("TD001"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void findByUuid_ShouldReturnDepartment() throws Exception {
        UUID departmentUuid = UUID.randomUUID();
        when(departmentService.getByUuid(eq(departmentUuid)))
            .thenReturn(validResponse);

        mockMvc.perform(get("/api/v1/branches/1/departments/" + departmentUuid))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.departmentName").value("Test Department"))
            .andExpect(jsonPath("$.departmentCode").value("TD001"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void create_WithValidRequest_ShouldReturnCreatedDepartment() throws Exception {
        when(departmentService.save(any(DepartmentRequest.class)))
            .thenReturn(validResponse);

        mockMvc.perform(post("/api/v1/branches/1/departments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.departmentName").value("Test Department"))
            .andExpect(jsonPath("$.departmentCode").value("TD001"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void updateById_WithValidRequest_ShouldReturnUpdatedDepartment() throws Exception {
        when(departmentService.update(eq(1L), any(DepartmentRequest.class)))
            .thenReturn(validResponse);

        mockMvc.perform(put("/api/v1/branches/1/departments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.departmentName").value("Test Department"))
            .andExpect(jsonPath("$.departmentCode").value("TD001"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void updateByUuid_WithValidRequest_ShouldReturnUpdatedDepartment() throws Exception {
        UUID departmentUuid = UUID.randomUUID();
        when(departmentService.updateByUuid(eq(departmentUuid), any(DepartmentRequest.class)))
            .thenReturn(validResponse);

        mockMvc.perform(put("/api/v1/branches/1/departments/" + departmentUuid)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.departmentName").value("Test Department"))
            .andExpect(jsonPath("$.departmentCode").value("TD001"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteById_WithSoftDelete_ShouldReturnNoContent() throws Exception {
        doNothing().when(departmentService).softDelete(eq(1L));

        mockMvc.perform(delete("/api/v1/branches/1/departments/1")
                .with(csrf())
                .param("soft", "true"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteById_WithHardDelete_ShouldReturnNoContent() throws Exception {
        doNothing().when(departmentService).delete(eq(1L));

        mockMvc.perform(delete("/api/v1/branches/1/departments/1")
                .with(csrf())
                .param("soft", "false"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteByUuid_WithSoftDelete_ShouldReturnNoContent() throws Exception {
        UUID departmentUuid = UUID.randomUUID();
        doNothing().when(departmentService).softDeleteByUuid(eq(departmentUuid));

        mockMvc.perform(delete("/api/v1/branches/1/departments/" + departmentUuid)
                .with(csrf())
                .param("soft", "true"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void create_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        DepartmentRequest invalidRequest = new DepartmentRequest(
            null, // Invalid: branchId is null
            "", // Invalid: departmentName is blank
            "", // Invalid: departmentCode is blank
            "Description"
        );

        mockMvc.perform(post("/api/v1/branches/1/departments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
}
