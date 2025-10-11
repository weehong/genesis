package com.resetrix.genesis.modules.company.services;

import com.resetrix.genesis.modules.company.entities.Company;
import com.resetrix.genesis.modules.company.exceptions.CompanyException;
import com.resetrix.genesis.modules.company.exceptions.CustomDatabaseException;
import com.resetrix.genesis.modules.company.mappers.CompanyMapper;
import com.resetrix.genesis.modules.company.repositories.CompanyRepository;
import com.resetrix.genesis.modules.company.requests.CompanyRequest;
import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import com.resetrix.genesis.shared.helpers.JsonFileReader;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.orm.jpa.JpaSystemException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceCreateTest {

    private static final String MODULE = "modules/company";
    private static final String ENDPOINT = "create";

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRequest request;
    private Company company;
    private CompanyResponse response;

    @BeforeEach
    void setUp() throws IOException {
        request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint(ENDPOINT)
            .scenario("valid")
            .readRequest(CompanyRequest.class);

        response = JsonFileReader.builder()
                .module(MODULE)
                .endpoint(ENDPOINT)
                .scenario("success")
                .readResponse(CompanyResponse.class);

        // Create company entity based on the request data
        company = new Company();
        company.setId(response.id());
        company.setUuid(response.uuid());
        company.setName(request.name());
        company.setRegistrationNumber(request.registrationNumber());
        company.setSoftDelete(response.softDelete());
        company.setCreatedAt(response.createdAt());
        company.setUpdatedAt(response.updatedAt());
    }

    @Test
    void save_shouldReturnCompanyResponse_whenValidRequestWithoutLogo() {
        when(companyMapper.toEntity(any(CompanyRequest.class), any()))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenReturn(company);
        when(companyMapper.toResponse(any(Company.class)))
            .thenReturn(response);

        CompanyResponse result = companyService.save(request);

        assertNotNull(result);
        assertEquals(response.id(), result.id());
        assertEquals(response.name(), result.name());
        assertEquals(response.registrationNumber(), result.registrationNumber());

        verify(companyMapper).toEntity(any(CompanyRequest.class), any());
        verify(companyRepository).save(any(Company.class));
        verify(companyMapper).toResponse(any(Company.class));
    }

    @Test
    void save_shouldReturnCompanyResponse_whenValidRequestWithLogo() throws IOException {
        MockMultipartFile logoFile = new MockMultipartFile(
            "logo",
            "logo.png",
            "image/png",
            "test logo content".getBytes()
        );

        CompanyRequest requestWithLogo = new CompanyRequest(
            request.name(),
            request.registrationNumber(),
            logoFile
        );

        byte[] logoBytes = logoFile.getBytes();
        company.setLogo(logoBytes);

        when(companyMapper.toEntity(any(CompanyRequest.class), any(byte[].class)))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenReturn(company);
        when(companyMapper.toResponse(any(Company.class)))
            .thenReturn(response);

        CompanyResponse result = companyService.save(requestWithLogo);

        assertNotNull(result);
        assertEquals(response.id(), result.id());

        verify(companyMapper).toEntity(any(CompanyRequest.class), any(byte[].class));
        verify(companyRepository).save(any(Company.class));
        verify(companyMapper).toResponse(any(Company.class));
    }

    @Test
    void save_shouldThrowCompanyException_whenLogoFileCannotBeRead() throws IOException {
        MockMultipartFile corruptedFile = new MockMultipartFile(
            "logo",
            "logo.png",
            "image/png",
            "test".getBytes()
        ) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("Cannot read file");
            }
        };

        CompanyRequest requestWithCorruptedLogo = new CompanyRequest(
            request.name(),
            request.registrationNumber(),
            corruptedFile
        );

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.save(requestWithCorruptedLogo)
        );

        assertEquals("Failed to process company logo", exception.getMessage());
    }

    @Test
    void save_shouldThrowCustomDatabaseException_whenEntityAlreadyExists() {
        when(companyMapper.toEntity(any(CompanyRequest.class), any()))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new EntityExistsException("Entity already exists"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.save(request)
                                                        );

        assertEquals("The entity already exists with the given ID", exception.getMessage());
    }

    @Test
    void save_shouldThrowCustomDatabaseException_whenDataIntegrityViolation() {
        when(companyMapper.toEntity(any(CompanyRequest.class), any()))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.save(request)
                                                        );

        assertEquals("Data integrity violation (e.g., unique constraint failure)", exception.getMessage());
    }

    @Test
    void save_shouldThrowCustomDatabaseException_whenConstraintViolation() {
        when(companyMapper.toEntity(any(CompanyRequest.class), any()))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new ConstraintViolationException("Constraint violation", null));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.save(request)
                                                        );

        assertEquals("Database constraint violation (e.g., foreign key failure)", exception.getMessage());
    }

    @Test
    void save_shouldThrowCustomDatabaseException_whenJpaSystemException() {
        when(companyMapper.toEntity(any(CompanyRequest.class), any()))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new JpaSystemException(new RuntimeException("JPA error")));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.save(request)
                                                        );

        assertEquals("System error with JPA provider", exception.getMessage());
    }

    @Test
    void save_shouldThrowCustomDatabaseException_whenPersistenceException() {
        when(companyMapper.toEntity(any(CompanyRequest.class), any()))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new PersistenceException("Persistence error"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.save(request)
                                                        );

        assertEquals("Persistence error occurred", exception.getMessage());
    }

    @Test
    void save_shouldThrowCustomDatabaseException_whenInvalidDataAccessApiUsage() {
        when(companyMapper.toEntity(any(CompanyRequest.class), any()))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new InvalidDataAccessApiUsageException("Invalid API usage"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.save(request)
                                                        );

        assertEquals("Invalid usage of the Data Access API", exception.getMessage());
    }

    @Test
    void save_shouldThrowCompanyException_whenUnexpectedErrorOccurs() {
        when(companyMapper.toEntity(any(CompanyRequest.class), any()))
            .thenReturn(company);
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.save(request)
        );

        assertEquals("Unexpected error occurred while saving the company", exception.getMessage());
    }
}
