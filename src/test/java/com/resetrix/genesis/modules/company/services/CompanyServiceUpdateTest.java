package com.resetrix.genesis.modules.company.services;

import com.resetrix.genesis.modules.company.entities.Company;
import com.resetrix.genesis.modules.company.exceptions.CompanyException;
import com.resetrix.genesis.modules.company.exceptions.CustomDatabaseException;
import com.resetrix.genesis.modules.company.mappers.CompanyMapper;
import com.resetrix.genesis.modules.company.repositories.CompanyRepository;
import com.resetrix.genesis.modules.company.requests.CompanyRequest;
import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import com.resetrix.genesis.shared.helpers.JsonFileReader;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.orm.jpa.JpaSystemException;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceUpdateTest {

    private static final String MODULE = "modules/company";

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRequest request;
    private Company existingCompany;
    private Company updatedCompany;
    private CompanyResponse response;
    private Long companyId;
    private UUID companyUuid;

    @BeforeEach
    void setUp() throws IOException {
        companyId = 1L;
        companyUuid = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

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

        // Create existing company (before update)
        existingCompany = new Company();
        existingCompany.setId(companyId);
        existingCompany.setUuid(companyUuid);
        existingCompany.setName("Test Company");
        existingCompany.setRegistrationNumber("REG123456");
        existingCompany.setSoftDelete(false);

        // Create updated company (after update)
        updatedCompany = new Company();
        updatedCompany.setId(response.id());
        updatedCompany.setUuid(response.uuid());
        updatedCompany.setName(response.name());
        updatedCompany.setRegistrationNumber(response.registrationNumber());
        updatedCompany.setSoftDelete(response.softDelete());
        updatedCompany.setCreatedAt(response.createdAt());
        updatedCompany.setUpdatedAt(response.updatedAt());
    }

    @Test
    void update_shouldReturnUpdatedCompanyResponse_whenValidRequestWithoutLogo() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenReturn(updatedCompany);
        when(companyMapper.toResponse(updatedCompany))
            .thenReturn(response);

        CompanyResponse result = companyService.update(companyId, request);

        assertNotNull(result);
        assertEquals(response.id(), result.id());
        assertEquals(response.uuid(), result.uuid());
        assertEquals(response.name(), result.name());
        assertEquals(response.registrationNumber(), result.registrationNumber());

        verify(companyRepository).findById(companyId);
        verify(companyMapper).updateEntity(eq(existingCompany), eq(request), any());
        verify(companyRepository).save(updatedCompany);
        verify(companyMapper).toResponse(updatedCompany);
    }

    @Test
    void update_shouldReturnUpdatedCompanyResponse_whenValidRequestWithLogo() throws IOException {
        MockMultipartFile logoFile = new MockMultipartFile(
            "logo",
            "updated-logo.png",
            "image/png",
            "updated logo content".getBytes()
        );

        CompanyRequest requestWithLogo = new CompanyRequest(
            "Updated Company Name",
            "REG789012",
            logoFile
        );

        byte[] logoBytes = logoFile.getBytes();
        updatedCompany.setLogo(logoBytes);

        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(requestWithLogo), any(byte[].class)))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenReturn(updatedCompany);
        when(companyMapper.toResponse(updatedCompany))
            .thenReturn(response);

        CompanyResponse result = companyService.update(companyId, requestWithLogo);

        assertNotNull(result);
        assertEquals(response.id(), result.id());

        verify(companyRepository).findById(companyId);
        verify(companyMapper).updateEntity(eq(existingCompany), eq(requestWithLogo), any(byte[].class));
        verify(companyRepository).save(updatedCompany);
        verify(companyMapper).toResponse(updatedCompany);
    }

    @Test
    void update_shouldReturnUpdatedCompanyResponse_whenEmptyLogoFile() throws IOException {
        MockMultipartFile emptyLogoFile = new MockMultipartFile(
            "logo",
            "empty-logo.png",
            "image/png",
            new byte[0]
        );

        CompanyRequest requestWithEmptyLogo = new CompanyRequest(
            "Updated Company Name",
            "REG789012",
            emptyLogoFile
        );

        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(requestWithEmptyLogo), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenReturn(updatedCompany);
        when(companyMapper.toResponse(updatedCompany))
            .thenReturn(response);

        CompanyResponse result = companyService.update(companyId, requestWithEmptyLogo);

        assertNotNull(result);
        assertEquals(response.id(), result.id());

        verify(companyRepository).findById(companyId);
        verify(companyMapper).updateEntity(eq(existingCompany), eq(requestWithEmptyLogo), any());
        verify(companyRepository).save(updatedCompany);
        verify(companyMapper).toResponse(updatedCompany);
    }

    @Test
    void update_shouldThrowCompanyException_whenLogoFileCannotBeRead() throws IOException {
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
            "Updated Company Name",
            "REG789012",
            corruptedFile
        );

        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.update(companyId, requestWithCorruptedLogo)
        );

        assertEquals("Failed to process company logo", exception.getMessage());
    }

    @Test
    void update_shouldThrowCompanyException_whenCompanyNotFound() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.empty());

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("Unexpected error occurred while updating the company", exception.getMessage());
        verify(companyRepository).findById(companyId);
    }

    @Test
    void update_shouldThrowCustomDatabaseException_whenEntityNotFoundExceptionThrown() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenThrow(new EntityNotFoundException("Entity not found"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("The entity does not exist or was deleted", exception.getMessage());
    }

    @Test
    void update_shouldThrowCustomDatabaseException_whenDataIntegrityViolation() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("Data integrity violation (e.g., unique constraint failure)", exception.getMessage());
    }

    @Test
    void update_shouldThrowCustomDatabaseException_whenConstraintViolation() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new ConstraintViolationException("Constraint violation", null));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("Database constraint violation (e.g., foreign key failure)", exception.getMessage());
    }

    @Test
    void update_shouldThrowCustomDatabaseException_whenOptimisticLockingFailure() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new OptimisticLockingFailureException("Optimistic locking failure"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("Concurrent modification detected", exception.getMessage());
    }

    @Test
    void update_shouldThrowCustomDatabaseException_whenInvalidDataAccessApiUsage() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new InvalidDataAccessApiUsageException("Invalid API usage"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("Invalid usage of the Data Access API", exception.getMessage());
    }

    @Test
    void update_shouldThrowCustomDatabaseException_whenJpaSystemException() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new JpaSystemException(new RuntimeException("JPA error")));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void update_shouldThrowCustomDatabaseException_whenPersistenceException() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new PersistenceException("Persistence error"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void update_shouldThrowCompanyException_whenUnexpectedErrorOccurs() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(existingCompany));
        when(companyMapper.updateEntity(eq(existingCompany), eq(request), any()))
            .thenReturn(updatedCompany);
        when(companyRepository.save(updatedCompany))
            .thenThrow(new RuntimeException("Unexpected error"));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.update(companyId, request)
        );

        assertEquals("Unexpected error occurred while updating the company", exception.getMessage());
    }
}
