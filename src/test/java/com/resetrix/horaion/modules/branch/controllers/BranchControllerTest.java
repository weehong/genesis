package com.resetrix.horaion.modules.branch.controllers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resetrix.horaion.modules.branch.requests.BranchRequest;
import com.resetrix.horaion.modules.branch.responses.BranchResponse;
import com.resetrix.horaion.modules.branch.services.IBranchService;
import com.resetrix.horaion.testsupports.securities.SecurityConfiguration;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = BranchController.class)
public class BranchControllerTest {

    private static final String BASE_URL = "/api/v1/companies/1/branches";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockitoBean
    private IBranchService<BranchRequest, BranchResponse> branchService;

    @Autowired
    public BranchControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    private BranchRequest validRequest;
    private BranchResponse validResponse;

    @BeforeEach
    void setUp() {
        UUID testUuid = UUID.randomUUID();

        // Create valid request with matching companyId
        validRequest = new BranchRequest(
            1L, // companyId matching the path variable
            "Test Branch",
            "TB001",
            "123 Test Street",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            "+1234567890",
            "test@branch.com"
        );

        // Create valid response
        validResponse = new BranchResponse(
            1L,
            testUuid,
            1L,
            "Test Company",
            "Test Branch",
            "TB001",
            "123 Test Street",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            "+1234567890",
            "test@branch.com",
            false,
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now())
        );
    }

    @Test
    @WithMockUser
    void create_shouldReturnCreatedBranch_whenCompanyIdMatches() throws Exception {
        when(branchService.save(any(BranchRequest.class))).thenReturn(validResponse);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(validResponse.id()))
            .andExpect(jsonPath("$.branchName").value(validResponse.branchName()));
    }

    @Test
    @WithMockUser
    void create_shouldReturnCreatedBranch_whenRequestCompanyIdIsNull() throws Exception {
        BranchRequest requestWithNullCompanyId = new BranchRequest(
            null, // null companyId should be allowed
            "Test Branch",
            "TB001",
            "123 Test Street",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            "+1234567890",
            "test@branch.com"
        );

        when(branchService.save(any(BranchRequest.class))).thenReturn(validResponse);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithNullCompanyId))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(validResponse.id()));
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenCompanyIdMismatch() throws Exception {
        BranchRequest mismatchedRequest = new BranchRequest(
            2L, // Different companyId from path variable (1)
            "Test Branch",
            "TB001",
            "123 Test Street",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            "+1234567890",
            "test@branch.com"
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mismatchedRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Company ID mismatch: path variable (1) does not match request body (2)"));
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenPathIsUuidButRequestIsNumeric() throws Exception {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        String uuidBaseUrl = "/api/v1/companies/" + uuid + "/branches";

        mockMvc.perform(post(uuidBaseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Company ID type mismatch: path variable is UUID but request contains numeric company ID"));
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnUpdatedBranch_whenCompanyIdMatches() throws Exception {
        when(branchService.update(eq(1L), any(BranchRequest.class))).thenReturn(validResponse);

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
    void updateById_shouldReturnBadRequest_whenCompanyIdMismatch() throws Exception {
        BranchRequest mismatchedRequest = new BranchRequest(
            2L, // Different companyId from path variable (1)
            "Test Branch",
            "TB001",
            "123 Test Street",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            "+1234567890",
            "test@branch.com"
        );

        mockMvc.perform(put(BASE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mismatchedRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Company ID mismatch: path variable (1) does not match request body (2)"));
    }

    @Test
    @WithMockUser
    void updateByUuid_shouldReturnUpdatedBranch_whenCompanyIdMatches() throws Exception {
        UUID branchUuid = UUID.randomUUID();
        when(branchService.updateByUuid(eq(branchUuid), any(BranchRequest.class))).thenReturn(validResponse);

        mockMvc.perform(put(BASE_URL + "/" + branchUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(validResponse.id()));
    }

    @Test
    @WithMockUser
    void updateByUuid_shouldReturnBadRequest_whenCompanyIdMismatch() throws Exception {
        UUID branchUuid = UUID.randomUUID();
        BranchRequest mismatchedRequest = new BranchRequest(
            2L, // Different companyId from path variable (1)
            "Test Branch",
            "TB001",
            "123 Test Street",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            "+1234567890",
            "test@branch.com"
        );

        mockMvc.perform(put(BASE_URL + "/" + branchUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mismatchedRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Company ID mismatch: path variable (1) does not match request body (2)"));
    }
}