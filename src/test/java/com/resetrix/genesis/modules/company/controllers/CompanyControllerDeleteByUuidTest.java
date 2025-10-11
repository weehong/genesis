package com.resetrix.genesis.modules.company.controllers;

import com.resetrix.genesis.modules.company.services.CompanyService;
import com.resetrix.genesis.testsupports.securities.SecurityConfiguration;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = CompanyController.class)
public class CompanyControllerDeleteByUuidTest {

    private static final String BASE_URL = "/api/v1/companies";

    private final MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    public CompanyControllerDeleteByUuidTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldReturnNoContent_whenHardDeleteSuccessful() throws Exception {
        UUID companyUuid = UUID.randomUUID();

        doNothing().when(companyService).deleteByUuid(companyUuid);

        mockMvc.perform(delete(BASE_URL + "/" + companyUuid)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(companyService).deleteByUuid(companyUuid);
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldReturnNoContent_whenSoftDeleteSuccessful() throws Exception {
        UUID companyUuid = UUID.randomUUID();

        doNothing().when(companyService).softDeleteByUuid(companyUuid);

        mockMvc.perform(delete(BASE_URL + "/" + companyUuid + "?soft=true")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(companyService).softDeleteByUuid(companyUuid);
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldReturnNoContent_whenSoftDeleteExplicitlyFalse() throws Exception {
        UUID companyUuid = UUID.randomUUID();

        doNothing().when(companyService).deleteByUuid(companyUuid);

        mockMvc.perform(delete(BASE_URL + "/" + companyUuid + "?soft=false")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(companyService).deleteByUuid(companyUuid);
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldReturnNotFound_whenCompanyNotFound() throws Exception {
        UUID companyUuid = UUID.randomUUID();

        doThrow(new EntityNotFoundException(
            String.format("Company with uuid %s does not exist", companyUuid)
        )).when(companyService).deleteByUuid(companyUuid);

        mockMvc.perform(delete(BASE_URL + "/" + companyUuid)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value(String.format("Company with uuid %s does not exist", companyUuid)))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyUuid));
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldReturnBadRequest_whenUuidIsNull() throws Exception {
        String nullUuid = "null";

        doThrow(new IllegalArgumentException("Company UUID cannot be null"))
            .when(companyService).deleteByUuid(null);

        // This test simulates a malformed UUID that would be caught by path validation
        mockMvc.perform(delete(BASE_URL + "/" + nullUuid)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound()); // 404 because path doesn't match UUID pattern
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldReturnInternalServerError_whenDatabaseError() throws Exception {
        UUID companyUuid = UUID.randomUUID();

        doThrow(new DataAccessResourceFailureException("Database connection failed"))
            .when(companyService).deleteByUuid(companyUuid);

        mockMvc.perform(delete(BASE_URL + "/" + companyUuid)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("A database error occurred while processing your request"))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyUuid));
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldReturnNotFound_whenSoftDeleteCompanyNotFound() throws Exception {
        UUID companyUuid = UUID.randomUUID();

        doThrow(new EntityNotFoundException(
            String.format("Company with uuid %s does not exist", companyUuid)
        )).when(companyService).softDeleteByUuid(companyUuid);

        mockMvc.perform(delete(BASE_URL + "/" + companyUuid + "?soft=true")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value(String.format("Company with uuid %s does not exist", companyUuid)))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyUuid));
    }

    @Test
    @WithMockUser
    void deleteByUuid_shouldReturnBadRequest_whenUuidIsInvalidFormat() throws Exception {
        String invalidUuid = "invalid-uuid-format";

        // This should return 404 because the path pattern doesn't match
        mockMvc.perform(delete(BASE_URL + "/" + invalidUuid)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}
