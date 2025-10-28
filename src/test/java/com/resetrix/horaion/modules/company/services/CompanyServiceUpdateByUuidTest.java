package com.resetrix.horaion.modules.company.services;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.orm.jpa.JpaSystemException;

import com.resetrix.horaion.modules.company.entities.Company;
import com.resetrix.horaion.modules.company.exceptions.CompanyException;
import com.resetrix.horaion.modules.company.exceptions.CustomDatabaseException;
import com.resetrix.horaion.modules.company.mappers.CompanyMapper;
import com.resetrix.horaion.modules.company.repositories.CompanyRepository;
import com.resetrix.horaion.modules.company.requests.CompanyRequest;
import com.resetrix.horaion.modules.company.responses.CompanyResponse;
import com.resetrix.horaion.shared.helpers.JsonFileReader;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
class CompanyServiceUpdateByUuidTest {

    private static final String MODULE = "modules/company";

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRequest  request;
    private Company         existingCompany;
    private Company         updatedCompany;
    private CompanyResponse response;
    private Long            companyId;
    private UUID            companyUuid;

    @BeforeEach
    void setUp() throws IOException {
        companyId = 1L;
        companyUuid = UUID.randomUUID();

        request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("update")
            .scenario("valid")
            .readRequest(CompanyRequest.class);

        response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("update")
            .scenario("success")
            .readResponse(CompanyResponse.class);

        existingCompany = JsonFileReader.readEntity(MODULE, "success", Company.class);

        updatedCompany = JsonFileReader.readEntity(MODULE, "success", Company.class);
    }

    @Test
    void updateByUuid_shouldReturnUpdatedCompanyResponse_whenValidRequestWithoutLogo() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenReturn(updatedCompany);
        when(companyMapper.toResponse(updatedCompany))
            .thenReturn(response);

        CompanyResponse result = companyService.updateByUuid(companyUuid, request);

        assertNotNull(result);
        assertEquals(response.id(), result.id());
        assertEquals(response.uuid(), result.uuid());
        assertEquals(response.name(), result.name());
        assertEquals(response.registrationNumber(), result.registrationNumber());

        verify(companyRepository).findByUuid(companyUuid);
        verify(companyMapper).updateEntity(any(Company.class), any(CompanyRequest.class), any());
        verify(companyRepository).save(updatedCompany);
        verify(companyMapper).toResponse(updatedCompany);
    }

    @Test
    void updateByUuid_shouldReturnUpdatedCompanyResponse_whenValidRequestWithLogo() throws IOException {
        MockMultipartFile logoFile = new MockMultipartFile(
            "logo",
            "updated-logo.png",
            "image/png",
            "updated logo content".getBytes()
        );

        byte[] logoBytes = logoFile.getBytes();
        updatedCompany.setLogo(logoBytes);

        // Create request with logo file attached
        CompanyRequest requestWithLogo = new CompanyRequest(
            request.name(),
            request.registrationNumber(),
            logoFile
        );

        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any(byte[].class)))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenReturn(updatedCompany);
        when(companyMapper.toResponse(updatedCompany))
            .thenReturn(response);

        CompanyResponse result = companyService.updateByUuid(companyUuid, requestWithLogo);

        assertNotNull(result);
        assertEquals(response.id(), result.id());

        verify(companyRepository).findByUuid(companyUuid);
        verify(companyMapper).updateEntity(any(Company.class), any(CompanyRequest.class), any(byte[].class));
        verify(companyRepository).save(updatedCompany);
        verify(companyMapper).toResponse(updatedCompany);
    }

    @Test
    void updateByUuid_shouldReturnUpdatedCompanyResponse_whenEmptyLogoFile() throws IOException {
        MockMultipartFile emptyLogoFile = new MockMultipartFile(
            "logo",
            "empty-logo.png",
            "image/png",
            new byte[0]
        );

        // Create request with empty logo file attached
        CompanyRequest requestWithEmptyLogo = new CompanyRequest(
            request.name(),
            request.registrationNumber(),
            emptyLogoFile
        );

        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenReturn(updatedCompany);
        when(companyMapper.toResponse(updatedCompany))
            .thenReturn(response);

        CompanyResponse result = companyService.updateByUuid(companyUuid, requestWithEmptyLogo);

        assertNotNull(result);
        assertEquals(response.id(), result.id());

        verify(companyRepository).findByUuid(companyUuid);
        verify(companyMapper).updateEntity(any(Company.class), any(CompanyRequest.class), any());
        verify(companyRepository).save(updatedCompany);
        verify(companyMapper).toResponse(updatedCompany);
    }

    @Test
    void updateByUuid_shouldThrowCompanyException_whenCompanyNotFound() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.empty());

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("Unexpected error occurred while updating the company", exception.getMessage());
        verify(companyRepository).findByUuid(companyUuid);
    }

    @Test
    void updateByUuid_shouldThrowCompanyException_whenLogoFileCannotBeRead() throws IOException {
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

        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.updateByUuid(companyUuid, requestWithCorruptedLogo)
        );

        assertEquals("Failed to process company logo", exception.getMessage());
    }

    @Test
    void updateByUuid_shouldThrowCustomDatabaseException_whenEntityNotFoundExceptionThrown() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenThrow(new EntityNotFoundException("Entity not found"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("The entity does not exist or was deleted", exception.getMessage());
    }

    @Test
    void updateByUuid_shouldThrowCustomDatabaseException_whenDataIntegrityViolation() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("Data integrity violation (e.g., unique constraint failure)", exception.getMessage());
    }

    @Test
    void updateByUuid_shouldThrowCustomDatabaseException_whenConstraintViolation() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new ConstraintViolationException("Constraint violation", null));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("Database constraint violation (e.g., foreign key failure)", exception.getMessage());
    }

    @Test
    void updateByUuid_shouldThrowCustomDatabaseException_whenOptimisticLockingFailure() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new OptimisticLockingFailureException("Optimistic locking failure"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("Concurrent modification detected", exception.getMessage());
    }

    @Test
    void updateByUuid_shouldThrowCustomDatabaseException_whenInvalidDataAccessApiUsage() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new InvalidDataAccessApiUsageException("Invalid API usage"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("Invalid usage of the Data Access API", exception.getMessage());
    }

    @Test
    void updateByUuid_shouldThrowCustomDatabaseException_whenJpaSystemException() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new JpaSystemException(new RuntimeException("JPA error")));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void updateByUuid_shouldThrowCustomDatabaseException_whenPersistenceException() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new PersistenceException("Persistence error"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void updateByUuid_shouldThrowCompanyException_whenUnexpectedErrorOccurs() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(any(Company.class), any(CompanyRequest.class), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new RuntimeException("Unexpected error"));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.updateByUuid(companyUuid, request)
        );

        assertEquals("Unexpected error occurred while updating the company", exception.getMessage());
    }
}
