package com.resetrix.genesis.modules.company.services;

import com.resetrix.genesis.modules.company.entities.Company;
import com.resetrix.genesis.modules.company.exceptions.CompanyException;
import com.resetrix.genesis.modules.company.exceptions.CustomDatabaseException;
import com.resetrix.genesis.modules.company.repositories.CompanyRepository;
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
import org.springframework.orm.jpa.JpaSystemException;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceDeleteByUuidTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private Company company;
    private UUID    companyUuid;

    @BeforeEach
    void setUp() {
        companyUuid = UUID.randomUUID();

        Timestamp now = new Timestamp(System.currentTimeMillis());

        company = new Company();
        company.setId(1L);
        company.setUuid(companyUuid);
        company.setName("Test Company");
        company.setRegistrationNumber("REG123456");
        company.setSoftDelete(false);
        company.setCreatedAt(now);
        company.setUpdatedAt(now);
    }

    // ========== softDeleteByUuid Tests ==========

    @Test
    void softDeleteByUuid_shouldMarkCompanyAsDeleted_whenValidUuid() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        companyService.softDeleteByUuid(companyUuid);

        assertTrue(company.getSoftDelete());
        verify(companyRepository).findByUuid(companyUuid);
        verify(companyRepository).save(company);
    }

    @Test
    void softDeleteByUuid_shouldThrowCompanyException_whenCompanyNotFound() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.empty());

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("Unexpected error occurred while soft-deleting the company", exception.getMessage());
        verify(companyRepository).findByUuid(companyUuid);
    }

    @Test
    void softDeleteByUuid_shouldThrowCustomDatabaseException_whenEntityNotFoundExceptionThrown() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new EntityNotFoundException("Entity not found"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("The entity does not exist or was already deleted", exception.getMessage());
    }

    @Test
    void softDeleteByUuid_shouldThrowCustomDatabaseException_whenDataIntegrityViolation() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("Data integrity violation (e.g., foreign key constraint)", exception.getMessage());
    }

    @Test
    void softDeleteByUuid_shouldThrowCustomDatabaseException_whenConstraintViolation() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new ConstraintViolationException("Constraint violation", null));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("Database constraint violation", exception.getMessage());
    }

    @Test
    void softDeleteByUuid_shouldThrowCustomDatabaseException_whenOptimisticLockingFailure() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new OptimisticLockingFailureException("Optimistic locking failure"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("Concurrent modification detected", exception.getMessage());
    }

    @Test
    void softDeleteByUuid_shouldThrowCustomDatabaseException_whenInvalidDataAccessApiUsage() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new InvalidDataAccessApiUsageException("Invalid API usage"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("Invalid usage of the Data Access API", exception.getMessage());
    }

    @Test
    void softDeleteByUuid_shouldThrowCustomDatabaseException_whenJpaSystemException() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new JpaSystemException(new RuntimeException("JPA error")));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void softDeleteByUuid_shouldThrowCustomDatabaseException_whenPersistenceException() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new PersistenceException("Persistence error"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void softDeleteByUuid_shouldThrowCompanyException_whenUnexpectedErrorOccurs() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.softDeleteByUuid(companyUuid)
        );

        assertEquals("Unexpected error occurred while soft-deleting the company", exception.getMessage());
    }

    // ========== deleteByUuid Tests ==========

    @Test
    void deleteByUuid_shouldDeleteCompany_whenValidUuid() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));

        companyService.deleteByUuid(companyUuid);

        verify(companyRepository).findByUuid(companyUuid);
        verify(companyRepository).delete(company);
    }

    @Test
    void deleteByUuid_shouldThrowCompanyException_whenCompanyNotFound() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.empty());

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("Unexpected error occurred while deleting the company", exception.getMessage());
        verify(companyRepository).findByUuid(companyUuid);
    }

    @Test
    void deleteByUuid_shouldThrowCustomDatabaseException_whenEntityNotFoundExceptionThrown() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        doThrow(new EntityNotFoundException("Entity not found"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("The entity does not exist or was already deleted", exception.getMessage());
    }

    @Test
    void deleteByUuid_shouldThrowCustomDatabaseException_whenDataIntegrityViolation() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        doThrow(new DataIntegrityViolationException("Data integrity violation"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("Data integrity violation (e.g., foreign key constraint)", exception.getMessage());
    }

    @Test
    void deleteByUuid_shouldThrowCustomDatabaseException_whenConstraintViolation() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        doThrow(new ConstraintViolationException("Constraint violation", null))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("Database constraint violation", exception.getMessage());
    }

    @Test
    void deleteByUuid_shouldThrowCustomDatabaseException_whenOptimisticLockingFailure() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        doThrow(new OptimisticLockingFailureException("Optimistic locking failure"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("Concurrent modification detected", exception.getMessage());
    }

    @Test
    void deleteByUuid_shouldThrowCustomDatabaseException_whenInvalidDataAccessApiUsage() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        doThrow(new InvalidDataAccessApiUsageException("Invalid API usage"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("Invalid usage of the Data Access API", exception.getMessage());
    }

    @Test
    void deleteByUuid_shouldThrowCustomDatabaseException_whenJpaSystemException() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        doThrow(new JpaSystemException(new RuntimeException("JPA error")))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void deleteByUuid_shouldThrowCustomDatabaseException_whenPersistenceException() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        doThrow(new PersistenceException("Persistence error"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void deleteByUuid_shouldThrowCompanyException_whenUnexpectedErrorOccurs() {
        when(companyRepository.findByUuid(companyUuid))
            .thenReturn(Optional.of(company));
        doThrow(new RuntimeException("Unexpected error"))
            .when(companyRepository).delete(any(Company.class));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.deleteByUuid(companyUuid)
        );

        assertEquals("Unexpected error occurred while deleting the company", exception.getMessage());
    }
}
