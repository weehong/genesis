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
class CompanyServiceDeleteTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private Company company;
    private Long    companyId;

    @BeforeEach
    void setUp() {
        companyId = 1L;

        Timestamp now = new Timestamp(System.currentTimeMillis());

        company = new Company();
        company.setId(companyId);
        company.setUuid(UUID.randomUUID());
        company.setName("Test Company");
        company.setRegistrationNumber("REG123456");
        company.setSoftDelete(false);
        company.setCreatedAt(now);
        company.setUpdatedAt(now);
    }

    // ========== softDelete Tests ==========

    @Test
    void softDelete_shouldMarkCompanyAsDeleted_whenValidId() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        companyService.softDelete(companyId);

        assertTrue(company.getSoftDelete());
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
    }

    @Test
    void softDelete_shouldThrowCompanyException_whenCompanyNotFound() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.empty());

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("Unexpected error occurred while soft-deleting the company", exception.getMessage());
        verify(companyRepository).findById(companyId);
    }

    @Test
    void softDelete_shouldThrowCustomDatabaseException_whenEntityNotFoundExceptionThrown() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new EntityNotFoundException("Entity not found"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("The entity does not exist or was already deleted", exception.getMessage());
    }

    @Test
    void softDelete_shouldThrowCustomDatabaseException_whenDataIntegrityViolation() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("Data integrity violation (e.g., foreign key constraint)", exception.getMessage());
    }

    @Test
    void softDelete_shouldThrowCustomDatabaseException_whenConstraintViolation() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new ConstraintViolationException("Constraint violation", null));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("Database constraint violation", exception.getMessage());
    }

    @Test
    void softDelete_shouldThrowCustomDatabaseException_whenOptimisticLockingFailure() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new OptimisticLockingFailureException("Optimistic locking failure"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("Concurrent modification detected", exception.getMessage());
    }

    @Test
    void softDelete_shouldThrowCustomDatabaseException_whenInvalidDataAccessApiUsage() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new InvalidDataAccessApiUsageException("Invalid API usage"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("Invalid usage of the Data Access API", exception.getMessage());
    }

    @Test
    void softDelete_shouldThrowCustomDatabaseException_whenJpaSystemException() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new JpaSystemException(new RuntimeException("JPA error")));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void softDelete_shouldThrowCustomDatabaseException_whenPersistenceException() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new PersistenceException("Persistence error"));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void softDelete_shouldThrowCompanyException_whenUnexpectedErrorOccurs() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.softDelete(companyId)
        );

        assertEquals("Unexpected error occurred while soft-deleting the company", exception.getMessage());
    }

    // ========== delete Tests ==========

    @Test
    void delete_shouldDeleteCompany_whenValidId() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));

        companyService.delete(companyId);

        verify(companyRepository).findById(companyId);
        verify(companyRepository).delete(company);
    }

    @Test
    void delete_shouldThrowCompanyException_whenCompanyNotFound() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.empty());

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("Unexpected error occurred while deleting the company", exception.getMessage());
        verify(companyRepository).findById(companyId);
    }

    @Test
    void delete_shouldThrowCustomDatabaseException_whenEntityNotFoundExceptionThrown() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        doThrow(new EntityNotFoundException("Entity not found"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("The entity does not exist or was already deleted", exception.getMessage());
    }

    @Test
    void delete_shouldThrowCustomDatabaseException_whenDataIntegrityViolation() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        doThrow(new DataIntegrityViolationException("Data integrity violation"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("Data integrity violation (e.g., foreign key constraint)", exception.getMessage());
    }

    @Test
    void delete_shouldThrowCustomDatabaseException_whenConstraintViolation() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        doThrow(new ConstraintViolationException("Constraint violation", null))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("Database constraint violation", exception.getMessage());
    }

    @Test
    void delete_shouldThrowCustomDatabaseException_whenOptimisticLockingFailure() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        doThrow(new OptimisticLockingFailureException("Optimistic locking failure"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("Concurrent modification detected", exception.getMessage());
    }

    @Test
    void delete_shouldThrowCustomDatabaseException_whenInvalidDataAccessApiUsage() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        doThrow(new InvalidDataAccessApiUsageException("Invalid API usage"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("Invalid usage of the Data Access API", exception.getMessage());
    }

    @Test
    void delete_shouldThrowCustomDatabaseException_whenJpaSystemException() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        doThrow(new JpaSystemException(new RuntimeException("JPA error")))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void delete_shouldThrowCustomDatabaseException_whenPersistenceException() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        doThrow(new PersistenceException("Persistence error"))
            .when(companyRepository).delete(any(Company.class));

        CustomDatabaseException exception = assertThrows(
            CustomDatabaseException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("System or persistence error occurred", exception.getMessage());
    }

    @Test
    void delete_shouldThrowCompanyException_whenUnexpectedErrorOccurs() {
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));
        doThrow(new RuntimeException("Unexpected error"))
            .when(companyRepository).delete(any(Company.class));

        CompanyException exception = assertThrows(
            CompanyException.class,
            () -> companyService.delete(companyId)
        );

        assertEquals("Unexpected error occurred while deleting the company", exception.getMessage());
    }
}
