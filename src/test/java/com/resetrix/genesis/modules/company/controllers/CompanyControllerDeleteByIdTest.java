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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = CompanyController.class)
public class CompanyControllerDeleteByIdTest {

    private static final String BASE_URL = "/api/v1/companies";

    private final MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    public CompanyControllerDeleteByIdTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnNoContent_whenHardDeleteSuccessful() throws Exception {
        Long companyId = 1L;

        doNothing().when(companyService).delete(companyId);

        mockMvc.perform(delete(BASE_URL + "/" + companyId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(companyService).delete(companyId);
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnNoContent_whenSoftDeleteSuccessful() throws Exception {
        Long companyId = 1L;

        doNothing().when(companyService).softDelete(companyId);

        mockMvc.perform(delete(BASE_URL + "/" + companyId + "?soft=true")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(companyService).softDelete(companyId);
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnNoContent_whenSoftDeleteExplicitlyFalse() throws Exception {
        Long companyId = 1L;

        doNothing().when(companyService).delete(companyId);

        mockMvc.perform(delete(BASE_URL + "/" + companyId + "?soft=false")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(companyService).delete(companyId);
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnNotFound_whenCompanyNotFound() throws Exception {
        Long companyId = 999L;

        doThrow(new EntityNotFoundException(
            String.format("Company with id %d does not exist", companyId)
        )).when(companyService).delete(companyId);

        mockMvc.perform(delete(BASE_URL + "/" + companyId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value(String.format("Company with id %d does not exist", companyId)))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnBadRequest_whenIdIsZero() throws Exception {
        Long companyId = 0L;

        doThrow(new IllegalArgumentException("Company ID must be a positive number"))
            .when(companyService).delete(companyId);

        mockMvc.perform(delete(BASE_URL + "/" + companyId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Company ID must be a positive number"))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnNotFound_whenIdIsNegative() throws Exception {
        Long companyId = -1L;

        // Negative ID will not match the path pattern [0-9]+ so it returns 404 Not Found
        mockMvc.perform(delete(BASE_URL + "/" + companyId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnInternalServerError_whenDatabaseError() throws Exception {
        Long companyId = 1L;

        doThrow(new DataAccessResourceFailureException("Database connection failed"))
            .when(companyService).delete(companyId);

        mockMvc.perform(delete(BASE_URL + "/" + companyId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("A database error occurred while processing your request"))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void deleteById_shouldReturnNotFound_whenSoftDeleteCompanyNotFound() throws Exception {
        Long companyId = 999L;

        doThrow(new EntityNotFoundException(
            String.format("Company with id %d does not exist", companyId)
        )).when(companyService).softDelete(companyId);

        mockMvc.perform(delete(BASE_URL + "/" + companyId + "?soft=true")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value(String.format("Company with id %d does not exist", companyId)))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }
}
