package com.resetrix.genesis.modules.company.controllers;

import com.resetrix.genesis.modules.company.requests.CompanyRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = CompanyController.class)
public class CompanyControllerUpdateByIdTest {

    private static final String BASE_URL = "/api/v1/companies";
    private static final String MODULE = "modules/company";

    private final MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    public CompanyControllerUpdateByIdTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnUpdatedCompany_whenValidRequest() throws Exception {
        Long companyId = 1L;
        CompanyResponse response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("update")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        when(companyService.update(eq(companyId), any(CompanyRequest.class)))
            .thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", "Updated Company")
                .param("registrationNumber", "REG123456")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(response.id()))
            .andExpect(jsonPath("$.data.uuid").value(response.uuid().toString()))
            .andExpect(jsonPath("$.data.name").value(response.name()))
            .andExpect(jsonPath("$.data.registrationNumber").value(response.registrationNumber()))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnBadRequest_whenNameIsMissing() throws Exception {
        Long companyId = 1L;

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("registrationNumber", "REG123456")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnBadRequest_whenRegistrationNumberIsMissing() throws Exception {
        Long companyId = 1L;

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", "Test Company")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnBadRequest_whenNameIsTooLong() throws Exception {
        Long companyId = 1L;
        String longName = "A".repeat(256); // Assuming max length is 255

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", longName)
                .param("registrationNumber", "REG123456")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnBadRequest_whenRegistrationNumberIsTooLong() throws Exception {
        Long companyId = 1L;
        String longRegNumber = "A".repeat(21); // Assuming max length is 20

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", "Test Company")
                .param("registrationNumber", longRegNumber)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnBadRequest_whenNameIsEmpty() throws Exception {
        Long companyId = 1L;

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", "")
                .param("registrationNumber", "REG123456")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnBadRequest_whenRegistrationNumberIsEmpty() throws Exception {
        Long companyId = 1L;

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", "Test Company")
                .param("registrationNumber", "")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }

    @Test
    @WithMockUser
    void updateById_shouldReturnNotFound_whenCompanyNotFound() throws Exception {
        Long companyId = 999L;

        when(companyService.update(eq(companyId), any(CompanyRequest.class)))
            .thenThrow(new EntityNotFoundException(
                String.format("Company with id %d does not exist", companyId)
            ));

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", "Test Company")
                .param("registrationNumber", "REG123456")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
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
    void updateById_shouldReturnBadRequest_whenIdIsZero() throws Exception {
        Long companyId = 0L;

        when(companyService.update(eq(companyId), any(CompanyRequest.class)))
            .thenThrow(new IllegalArgumentException("Company ID must be a positive number"));

        // ID 0 matches the path pattern [0-9]+ but service validates it
        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", "Test Company")
                .param("registrationNumber", "REG123456")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
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
    void updateById_shouldReturnInternalServerError_whenDatabaseError() throws Exception {
        Long companyId = 1L;

        when(companyService.update(eq(companyId), any(CompanyRequest.class)))
            .thenThrow(new DataAccessResourceFailureException("Database connection failed"));

        mockMvc.perform(put(BASE_URL + "/" + companyId)
                .param("name", "Test Company")
                .param("registrationNumber", "REG123456")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("A database error occurred while processing your request"))
            .andExpect(jsonPath("$.instance").value(BASE_URL + "/" + companyId));
    }
}
