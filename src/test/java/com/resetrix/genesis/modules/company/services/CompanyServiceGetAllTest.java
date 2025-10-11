package com.resetrix.genesis.modules.company.services;

import com.resetrix.genesis.modules.company.entities.Company;
import com.resetrix.genesis.modules.company.mappers.CompanyMapper;
import com.resetrix.genesis.modules.company.repositories.CompanyRepository;
import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import com.resetrix.genesis.shared.helpers.JsonFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceGetAllTest {

    private static final String MODULE = "modules/company";
    private static final String ENDPOINT = "get-all";

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    private Company company;
    private CompanyResponse response;

    @BeforeEach
    void setUp() throws IOException {
        // Load response data from JSON files
        response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint(ENDPOINT)
            .scenario("success")
            .readResponse(CompanyResponse.class);

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Setup company
        company = new Company();
        company.setId(response.id());
        company.setUuid(response.uuid());
        company.setName(response.name());
        company.setRegistrationNumber(response.registrationNumber());
        company.setSoftDelete(response.softDelete());
        company.setCreatedAt(response.createdAt());
        company.setUpdatedAt(response.updatedAt());
    }

    @Test
    void getAll_shouldReturnPagedCompanies_whenValidParametersProvided() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        List<Company> companies = Arrays.asList(company);
        Page<Company> companyPage = new PageImpl<>(companies, PageRequest.of(page, size, Sort.Direction.ASC, sortBy), 1);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(companyPage);
        when(companyMapper.toResponse(company)).thenReturn(response);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(response, result.getContent().get(0));

        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.ASC, sortBy)));
        verify(companyMapper, times(1)).toResponse(company);
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoCompaniesFound() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        Page<Company> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size, Sort.Direction.ASC, sortBy), 0);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        verify(companyRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getAll_shouldUseDESCDirection_whenSortDirectionIsDESC() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "createdAt";
        String sortDirection = "DESC";

        List<Company> companies = Arrays.asList(company);
        Page<Company> companyPage = new PageImpl<>(companies, PageRequest.of(page, size, Sort.Direction.DESC, sortBy), 1);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(companyPage);
        when(companyMapper.toResponse(company)).thenReturn(response);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.DESC, sortBy)));
    }

    @Test
    void getAll_shouldDefaultToASC_whenSortDirectionIsNull() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = null;

        List<Company> companies = Arrays.asList(company);
        Page<Company> companyPage = new PageImpl<>(companies, PageRequest.of(page, size, Sort.Direction.ASC, sortBy), 1);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(companyPage);
        when(companyMapper.toResponse(company)).thenReturn(response);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.ASC, sortBy)));
    }

    @Test
    void getAll_shouldDefaultToASC_whenSortDirectionIsEmpty() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "   ";

        List<Company> companies = Arrays.asList(company);
        Page<Company> companyPage = new PageImpl<>(companies, PageRequest.of(page, size, Sort.Direction.ASC, sortBy), 1);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(companyPage);
        when(companyMapper.toResponse(company)).thenReturn(response);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.ASC, sortBy)));
    }

    @Test
    void getAll_shouldHandleCaseInsensitiveSortDirection_whenSortDirectionIsLowercase() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "desc";

        List<Company> companies = Arrays.asList(company);
        Page<Company> companyPage = new PageImpl<>(companies, PageRequest.of(page, size, Sort.Direction.DESC, sortBy), 1);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(companyPage);
        when(companyMapper.toResponse(company)).thenReturn(response);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.DESC, sortBy)));
    }

    @Test
    void getAll_shouldThrowException_whenPageIsNegative() {
        // Arrange
        int page = -1;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getAll(page, size, sortBy, sortDirection)
        );

        assertEquals("Page must be >= 0", exception.getMessage());
    }

    @Test
    void getAll_shouldThrowException_whenSizeIsZero() {
        // Arrange
        int page = 0;
        int size = 0;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getAll(page, size, sortBy, sortDirection)
        );

        assertEquals("Size must be > 0 and <= 1000", exception.getMessage());
    }

    @Test
    void getAll_shouldThrowException_whenSizeIsNegative() {
        // Arrange
        int page = 0;
        int size = -5;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getAll(page, size, sortBy, sortDirection)
        );

        assertEquals("Size must be > 0 and <= 1000", exception.getMessage());
    }

    @Test
    void getAll_shouldThrowException_whenSizeExceedsMaximum() {
        // Arrange
        int page = 0;
        int size = 1001;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getAll(page, size, sortBy, sortDirection)
        );

        assertEquals("Size must be > 0 and <= 1000", exception.getMessage());
    }

    @Test
    void getAll_shouldAcceptMaximumSize_whenSizeIsExactly1000() {
        // Arrange
        int page = 0;
        int size = 1000;
        String sortBy = "name";
        String sortDirection = "ASC";

        List<Company> companies = Arrays.asList(company);
        Page<Company> companyPage = new PageImpl<>(companies, PageRequest.of(page, size, Sort.Direction.ASC, sortBy), 1);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(companyPage);
        when(companyMapper.toResponse(company)).thenReturn(response);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.ASC, sortBy)));
    }

    @Test
    void getAll_shouldThrowException_whenSortDirectionIsInvalid() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "INVALID";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getAll(page, size, sortBy, sortDirection)
        );

        assertEquals("Invalid sortDirection: must be 'ASC' or 'DESC'", exception.getMessage());
    }

    @Test
    void getAll_shouldHandleWhitespaceInSortDirection_whenSortDirectionHasSpaces() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "  ASC  ";

        List<Company> companies = Arrays.asList(company);
        Page<Company> companyPage = new PageImpl<>(companies, PageRequest.of(page, size, Sort.Direction.ASC, sortBy), 1);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(companyPage);
        when(companyMapper.toResponse(company)).thenReturn(response);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.ASC, sortBy)));
    }

    @Test
    void getAll_shouldHandleDifferentSortFields_whenSortByIsId() {
        // Arrange
        int page = 1;
        int size = 5;
        String sortBy = "id";
        String sortDirection = "DESC";

        List<Company> companies = Arrays.asList(company);
        Page<Company> companyPage = new PageImpl<>(companies, PageRequest.of(page, size, Sort.Direction.DESC, sortBy), 6);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(companyPage);
        when(companyMapper.toResponse(company)).thenReturn(response);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(6, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.DESC, sortBy)));
    }

    @Test
    void getAll_shouldHandleLargePageNumber_whenPageIsHigh() {
        // Arrange
        int page = 100;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        Page<Company> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size, Sort.Direction.ASC, sortBy), 0);

        when(companyRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        // Act
        Page<CompanyResponse> result = companyService.getAll(page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        verify(companyRepository, times(1)).findAll(eq(PageRequest.of(page, size, Sort.Direction.ASC, sortBy)));
    }
}
