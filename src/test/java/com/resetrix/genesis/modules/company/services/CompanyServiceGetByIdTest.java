package com.resetrix.genesis.modules.company.services;

import com.resetrix.genesis.modules.company.entities.Company;
import com.resetrix.genesis.modules.company.mappers.CompanyMapper;
import com.resetrix.genesis.modules.company.repositories.CompanyRepository;
import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import com.resetrix.genesis.shared.helpers.JsonFileReader;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceGetByIdTest {

    private static final String MODULE = "modules/company";

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void getById_shouldReturnCompanyDetails_whenIdFound() throws IOException {
        // Arrange
        CompanyResponse expectedResponse = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("find-by-id")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        // Create company entity based on the response data
        Company company = new Company();
        company.setId(expectedResponse.id());
        company.setUuid(expectedResponse.uuid());
        company.setName(expectedResponse.name());
        company.setRegistrationNumber(expectedResponse.registrationNumber());
        company.setLogo(null); // Logo is null in the JSON response
        company.setSoftDelete(expectedResponse.softDelete());
        company.setCreatedAt(expectedResponse.createdAt());
        company.setUpdatedAt(expectedResponse.updatedAt());

        when(companyRepository.findById(1L))
            .thenReturn(Optional.of(company));
        when(companyMapper.toResponse(company))
            .thenReturn(expectedResponse);

        // Act
        CompanyResponse result = companyService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.id(), result.id());
        assertEquals(expectedResponse.name(), result.name());
        assertEquals(expectedResponse.registrationNumber(), result.registrationNumber());
        verify(companyRepository, times(1)).findById(1L);
        verify(companyMapper, times(1)).toResponse(company);
    }

    @Test
    void getById_shouldReturnEntityNotFound_whenIdNotFound() throws IOException {
        // Arrange
        when(companyRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> companyService.getById(999L)
                                                        );

        assertEquals("Company with id 999 does not exist", exception.getMessage());
        verify(companyRepository, times(1)).findById(999L);
        verify(companyMapper, never()).toResponse(any(Company.class));
    }

    @Test
    void getById_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getById(null)
                                                         );

        assertEquals("Company ID must be a positive number", exception.getMessage());
        verify(companyRepository, never()).findById(any());
        verify(companyMapper, never()).toResponse(any(Company.class));
    }

    @Test
    void getById_shouldThrowIllegalArgumentException_whenIdIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getById(0L)
                                                         );

        assertEquals("Company ID must be a positive number", exception.getMessage());
        verify(companyRepository, never()).findById(any());
        verify(companyMapper, never()).toResponse(any(Company.class));
    }

    @Test
    void getById_shouldThrowIllegalArgumentException_whenIdIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getById(-1L)
                                                         );

        assertEquals("Company ID must be a positive number", exception.getMessage());
        verify(companyRepository, never()).findById(any());
        verify(companyMapper, never()).toResponse(any(Company.class));
    }

    @Test
    void getById_shouldThrowDataAccessException_whenDatabaseError() {
        // Arrange
        when(companyRepository.findById(1L))
            .thenThrow(new DataAccessResourceFailureException("Database connection failed"));

        // Act & Assert
        DataAccessResourceFailureException exception = assertThrows(
            DataAccessResourceFailureException.class,
            () -> companyService.getById(1L)
                                                                   );

        assertEquals("Database connection failed", exception.getMessage());
        verify(companyRepository, times(1)).findById(1L);
        verify(companyMapper, never()).toResponse(any(Company.class));
    }
}
