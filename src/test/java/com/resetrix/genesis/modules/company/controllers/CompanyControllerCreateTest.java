package com.resetrix.genesis.modules.company.controllers;

import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import com.resetrix.genesis.modules.company.services.CompanyService;
import com.resetrix.genesis.shared.helpers.JsonFileReader;
import com.resetrix.genesis.testsupports.securities.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = CompanyController.class)
public class CompanyControllerCreateTest {

    private static final String BASE_URL = "/api/v1/companies";
    private static final String MODULE = "modules/company";

    private final MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    public CompanyControllerCreateTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void create_shouldReturnCreatedCompany_whenValidRequestProvided() throws Exception {
        CompanyResponse response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("create")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        when(companyService.save(any()))
            .thenReturn(response);

        mockMvc.perform(multipart(BASE_URL)
                .param("name", "Test Company")
                .param("registrationNumber", "REG123456")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isCreated())
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
    void create_shouldReturnCreatedCompany_whenValidRequestWithLogoProvided() throws Exception {
        CompanyResponse response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("create")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        MockMultipartFile logoFile = new MockMultipartFile(
            "logo",
            "logo.png",
            MediaType.IMAGE_PNG_VALUE,
            "test logo content".getBytes()
        );

        when(companyService.save(any()))
            .thenReturn(response);

        mockMvc.perform(multipart(BASE_URL)
                .file(logoFile)
                .param("name", "Test Company")
                .param("registrationNumber", "REG123456")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(response.id()))
            .andExpect(jsonPath("$.data.name").value(response.name()))
            .andExpect(jsonPath("$.data.registrationNumber").value(response.registrationNumber()));
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenNameIsMissing() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                .param("registrationNumber", "REG123456")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenRegistrationNumberIsMissing() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                .param("name", "Test Company")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenNameIsEmpty() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                .param("name", "")
                .param("registrationNumber", "REG123456")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenRegistrationNumberIsEmpty() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                .param("name", "Test Company")
                .param("registrationNumber", "")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenNameExceedsMaxLength() throws Exception {
        String longName = "A".repeat(256);

        mockMvc.perform(multipart(BASE_URL)
                .param("name", longName)
                .param("registrationNumber", "REG123456")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenRegistrationNumberExceedsMaxLength() throws Exception {
        String longRegNumber = "A".repeat(21);

        mockMvc.perform(multipart(BASE_URL)
                .param("name", "Test Company")
                .param("registrationNumber", longRegNumber)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
