package com.resetrix.genesis.modules.company.controllers;

import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import com.resetrix.genesis.modules.company.services.CompanyService;
import com.resetrix.genesis.shared.helpers.JsonFileReader;
import com.resetrix.genesis.testsupports.securities.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = CompanyController.class)
public class CompanyControllerFindAllTest {

    private static final String BASE_URL = "/api/v1/companies";
    private static final String MODULE = "modules/company";

    private final MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    public CompanyControllerFindAllTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnPagedCompanies_whenDefaultParameters() throws Exception {
        CompanyResponse response1 = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("create")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        CompanyResponse response2 = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("update")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        List<CompanyResponse> companies = Arrays.asList(response1, response2);
        Page<CompanyResponse> page = new PageImpl<>(companies, PageRequest.of(0, 10, Sort.Direction.ASC, "id"), 2);

        when(companyService.getAll(0, 10, "id", "ASC"))
            .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.data.size").value(10))
            .andExpect(jsonPath("$.data.number").value(0))
            .andExpect(jsonPath("$.data.first").value(true))
            .andExpect(jsonPath("$.data.last").value(true))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnPagedCompanies_whenCustomParameters() throws Exception {
        CompanyResponse response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("create")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        List<CompanyResponse> companies = Collections.singletonList(response);
        Page<CompanyResponse> page = new PageImpl<>(companies, PageRequest.of(1, 5, Sort.Direction.DESC, "name"), 6);

        when(companyService.getAll(1, 5, "name", "DESC"))
            .thenReturn(page);

        mockMvc.perform(get(BASE_URL + "?page=1&size=5&sortBy=name&sortDirection=DESC")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.totalElements").value(6))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.size").value(5))
            .andExpect(jsonPath("$.data.number").value(1))
            .andExpect(jsonPath("$.data.first").value(false))
            .andExpect(jsonPath("$.data.last").value(true))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnEmptyPage_whenNoCompaniesFound() throws Exception {
        Page<CompanyResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10, Sort.Direction.ASC, "id"), 0);

        when(companyService.getAll(0, 10, "id", "ASC"))
            .thenReturn(emptyPage);

        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(0))
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andExpect(jsonPath("$.data.totalPages").value(0))
            .andExpect(jsonPath("$.data.size").value(10))
            .andExpect(jsonPath("$.data.number").value(0))
            .andExpect(jsonPath("$.data.first").value(true))
            .andExpect(jsonPath("$.data.last").value(true))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnBadRequest_whenPageIsNegative() throws Exception {
        when(companyService.getAll(-1, 10, "id", "ASC"))
            .thenThrow(new IllegalArgumentException("Page must be >= 0"));

        mockMvc.perform(get(BASE_URL + "?page=-1")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Page must be >= 0"))
            .andExpect(jsonPath("$.instance").value(BASE_URL));
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnBadRequest_whenSizeIsZero() throws Exception {
        when(companyService.getAll(0, 0, "id", "ASC"))
            .thenThrow(new IllegalArgumentException("Size must be > 0 and <= 1000"));

        mockMvc.perform(get(BASE_URL + "?size=0")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Size must be > 0 and <= 1000"))
            .andExpect(jsonPath("$.instance").value(BASE_URL));
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnBadRequest_whenSizeExceedsMaximum() throws Exception {
        when(companyService.getAll(0, 1001, "id", "ASC"))
            .thenThrow(new IllegalArgumentException("Size must be > 0 and <= 1000"));

        mockMvc.perform(get(BASE_URL + "?size=1001")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Size must be > 0 and <= 1000"))
            .andExpect(jsonPath("$.instance").value(BASE_URL));
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnBadRequest_whenSortDirectionIsInvalid() throws Exception {
        when(companyService.getAll(0, 10, "id", "INVALID"))
            .thenThrow(new IllegalArgumentException("Invalid sortDirection: must be 'ASC' or 'DESC'"));

        mockMvc.perform(get(BASE_URL + "?sortDirection=INVALID")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid sortDirection: must be 'ASC' or 'DESC'"))
            .andExpect(jsonPath("$.instance").value(BASE_URL));
    }

    @Test
    @WithMockUser
    void findAll_shouldReturnInternalServerError_whenDatabaseError() throws Exception {
        when(companyService.getAll(anyInt(), anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("Database connection failed"));

        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("A database error occurred while processing your request"))
            .andExpect(jsonPath("$.instance").value(BASE_URL));
    }
}
