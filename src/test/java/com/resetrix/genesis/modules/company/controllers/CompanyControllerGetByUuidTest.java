package com.resetrix.genesis.modules.company.controllers;

import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import com.resetrix.genesis.modules.company.services.CompanyService;
import com.resetrix.genesis.shared.helpers.JsonFileReader;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = CompanyController.class)
public class CompanyControllerGetByUuidTest {

    private static final String BASE_URL = "/api/v1/companies";
    private static final String MODULE = "modules/company";

    private final MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    public CompanyControllerGetByUuidTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void findByUuid_shouldReturnCompanyDetails_whenUuidFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        CompanyResponse response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("/find-by-uuid")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        when(companyService.getByUuid(any(UUID.class)))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.data.id").value(response.id()))
            .andExpect(jsonPath("$.data.uuid").value(response.uuid().toString()))
            .andExpect(jsonPath("$.data.name").value(response.name()))
            .andExpect(jsonPath("$.data.registrationNumber").value(response.registrationNumber()))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser
    void findByUuid_shouldReturnNotFound_whenUuidNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        when(companyService.getByUuid(uuid))
            .thenThrow(new EntityNotFoundException(
                String.format("Company with uuid %s does not exist", uuid)
            ));

        mockMvc.perform(get(BASE_URL + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value(String.format("Company with uuid %s does not exist", uuid)))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + uuid));
    }

    @Test
    @WithMockUser
    void findByUuid_shouldReturnInternalServerError_whenDatabaseError() throws Exception {
        UUID uuid = UUID.randomUUID();

        when(companyService.getByUuid(uuid))
            .thenThrow(new DataAccessResourceFailureException("Database connection failed"));

        mockMvc.perform(get(BASE_URL + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("A database error occurred while processing your request"))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + uuid));
    }
}
