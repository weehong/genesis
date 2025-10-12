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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceGetByUuidTest {

    private static final String MODULE = "modules/company";

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void getByUuid_shouldReturnCompanyDetails_whenUuidFound() throws IOException {
        UUID uuid = UUID.randomUUID();

        CompanyResponse expectedResponse = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("/find-by-uuid")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        Company company = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("/find-by-uuid")
            .scenario("success")
            .readResponse(Company.class);

        when(companyRepository.findByUuid(any(UUID.class)))
            .thenReturn(Optional.of(company));
        when(companyMapper.toResponse(company))
            .thenReturn(expectedResponse);

        CompanyResponse result = companyService.getByUuid(uuid);

        assertNotNull(result);
        assertEquals(expectedResponse.id(), result.id());
        assertEquals(expectedResponse.name(), result.name());
        assertEquals(expectedResponse.registrationNumber(), result.registrationNumber());
        verify(companyRepository, times(1)).findByUuid(uuid);
        verify(companyMapper, times(1)).toResponse(company);
    }

    @Test
    void getByUuid_shouldReturnEntityNotFound_whenUuidNotFound() throws IOException {
        UUID uuid = UUID.randomUUID();

        when(companyRepository.findByUuid(any(UUID.class)))
            .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> companyService.getByUuid(uuid)
                                                        );

        assertEquals(String.format("Company with uuid %s does not exist", uuid), exception.getMessage());
        verify(companyRepository, times(1)).findByUuid(uuid);
        verify(companyMapper, never()).toResponse(any(Company.class));
    }

    @Test
    void getByUuid_shouldThrowIllegalArgumentException_whenUuidIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.getByUuid(null)
                                                         );

        assertEquals("Company UUID cannot be null", exception.getMessage());
        verify(companyRepository, never()).findByUuid(any());
        verify(companyMapper, never()).toResponse(any(Company.class));
    }

    @Test
    void getByUuid_shouldThrowDataAccessException_whenDatabaseError() {
        UUID uuid = UUID.randomUUID();

        // Arrange
        when(companyRepository.findByUuid(uuid))
            .thenThrow(new DataAccessResourceFailureException("Database connection failed"));

        // Act & Assert
        DataAccessResourceFailureException exception = assertThrows(
            DataAccessResourceFailureException.class,
            () -> companyService.getByUuid(uuid)
                                                                   );

        assertEquals("Database connection failed", exception.getMessage());
        verify(companyRepository, times(1)).findByUuid(uuid);
        verify(companyMapper, never()).toResponse(any(Company.class));
    }
}
