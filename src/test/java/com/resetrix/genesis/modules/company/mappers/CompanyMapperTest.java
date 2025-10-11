package com.resetrix.genesis.modules.company.mappers;

import com.resetrix.genesis.modules.company.entities.Company;
import com.resetrix.genesis.modules.company.requests.CompanyRequest;
import com.resetrix.genesis.modules.company.responses.CompanyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CompanyMapperTest {

    private CompanyMapper companyMapper;

    @BeforeEach
    void setUp() {
        companyMapper = new CompanyMapper();
    }

    @Test
    void toResponseDTO_shouldMapAllFields_whenCompanyHasAllData() {
        // Arrange
        Company company = new Company();
        company.setId(1L);
        company.setUuid(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
        company.setName("Test Company");
        company.setRegistrationNumber("REG123456");
        company.setLogo("logo".getBytes());
        company.setSoftDelete(false);
        company.setCreatedAt(Timestamp.from(Instant.parse("2025-10-08T10:30:00Z")));
        company.setUpdatedAt(Timestamp.from(Instant.parse("2025-10-08T10:30:00Z")));

        // Act
        CompanyResponse response = companyMapper.toResponse(company);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"), response.uuid());
        assertEquals("Test Company", response.name());
        assertEquals("REG123456", response.registrationNumber());
        assertEquals(Base64.getEncoder().encodeToString("logo".getBytes()), response.logo());
        assertFalse(response.softDelete());
        assertEquals(Timestamp.from(Instant.parse("2025-10-08T10:30:00Z")), response.createdAt());
        assertEquals(Timestamp.from(Instant.parse("2025-10-08T10:30:00Z")), response.updatedAt());
    }

    @Test
    void toResponseDTO_shouldHandleNullLogo_whenLogoIsNull() {
        // Arrange
        Company company = new Company();
        company.setId(2L);
        company.setUuid(UUID.randomUUID());
        company.setName("Company Without Logo");
        company.setRegistrationNumber("REG789012");
        company.setLogo(null);
        company.setSoftDelete(false);
        company.setCreatedAt(Timestamp.from(Instant.now()));
        company.setUpdatedAt(Timestamp.from(Instant.now()));

        // Act
        CompanyResponse response = companyMapper.toResponse(company);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("Company Without Logo", response.name());
        assertNull(response.logo());
    }

    @Test
    void toResponseDTO_shouldEncodeLogo_whenLogoIsPresent() {
        // Arrange
        byte[] logoBytes = new byte[]{1, 2, 3, 4, 5};
        Company company = new Company();
        company.setId(3L);
        company.setUuid(UUID.randomUUID());
        company.setName("Company With Logo");
        company.setRegistrationNumber("REG345678");
        company.setLogo(logoBytes);
        company.setSoftDelete(false);
        company.setCreatedAt(Timestamp.from(Instant.now()));
        company.setUpdatedAt(Timestamp.from(Instant.now()));

        // Act
        CompanyResponse response = companyMapper.toResponse(company);

        // Assert
        assertNotNull(response);
        assertNotNull(response.logo());
        String expectedBase64 = Base64.getEncoder().encodeToString(logoBytes);
        assertEquals(expectedBase64, response.logo());

        // Verify we can decode back to original bytes
        byte[] decodedBytes = Base64.getDecoder().decode(response.logo());
        assertArrayEquals(logoBytes, decodedBytes);
    }

    @Test
    void toResponseDTO_shouldMapSoftDeleteTrue_whenSoftDeleteIsTrue() {
        // Arrange
        Company company = new Company();
        company.setId(4L);
        company.setUuid(UUID.randomUUID());
        company.setName("Deleted Company");
        company.setRegistrationNumber("REG999999");
        company.setLogo(null);
        company.setSoftDelete(true);
        company.setCreatedAt(Timestamp.from(Instant.now()));
        company.setUpdatedAt(Timestamp.from(Instant.now()));

        // Act
        CompanyResponse response = companyMapper.toResponse(company);

        // Assert
        assertNotNull(response);
        assertTrue(response.softDelete());
    }

    @Test
    void toResponseDTO_shouldMapEmptyLogo_whenLogoIsEmptyArray() {
        // Arrange
        Company company = new Company();
        company.setId(5L);
        company.setUuid(UUID.randomUUID());
        company.setName("Company With Empty Logo");
        company.setRegistrationNumber("REG111111");
        company.setLogo(new byte[0]);
        company.setSoftDelete(false);
        company.setCreatedAt(Timestamp.from(Instant.now()));
        company.setUpdatedAt(Timestamp.from(Instant.now()));

        // Act
        CompanyResponse response = companyMapper.toResponse(company);

        // Assert
        assertNotNull(response);
        assertNotNull(response.logo());
        assertEquals("", response.logo());
    }

    @Test
    void toResponseDTO_shouldHandleLargeLogo_whenLogoIsBinary() {
        // Arrange
        byte[] largeLogo = new byte[1024]; // 1KB logo
        for (int i = 0; i < largeLogo.length; i++) {
            largeLogo[i] = (byte) (i % 256);
        }

        Company company = new Company();
        company.setId(6L);
        company.setUuid(UUID.randomUUID());
        company.setName("Company With Large Logo");
        company.setRegistrationNumber("REG222222");
        company.setLogo(largeLogo);
        company.setSoftDelete(false);
        company.setCreatedAt(Timestamp.from(Instant.now()));
        company.setUpdatedAt(Timestamp.from(Instant.now()));

        // Act
        CompanyResponse response = companyMapper.toResponse(company);

        // Assert
        assertNotNull(response);
        assertNotNull(response.logo());

        // Verify the encoded logo can be decoded back correctly
        byte[] decodedLogo = Base64.getDecoder().decode(response.logo());
        assertArrayEquals(largeLogo, decodedLogo);
    }

    @Test
    void toResponseDTO_shouldPreserveTimestamps_whenTimestampsAreSet() {
        // Arrange
        Timestamp createdAt = Timestamp.from(Instant.parse("2025-01-01T00:00:00Z"));
        Timestamp updatedAt = Timestamp.from(Instant.parse("2025-10-08T12:00:00Z"));

        Company company = new Company();
        company.setId(7L);
        company.setUuid(UUID.randomUUID());
        company.setName("Timestamp Test Company");
        company.setRegistrationNumber("REG333333");
        company.setLogo(null);
        company.setSoftDelete(false);
        company.setCreatedAt(createdAt);
        company.setUpdatedAt(updatedAt);

        // Act
        CompanyResponse response = companyMapper.toResponse(company);

        // Assert
        assertNotNull(response);
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
    }

    // Tests for toEntity method
    @Test
    void toEntity_shouldCreateNewCompany_whenRequestHasValidData() {
        // Arrange
        MultipartFile mockLogo = mock(MultipartFile.class);
        CompanyRequest request = new CompanyRequest("Test Company", "REG123456", mockLogo);
        byte[] logoBytes = "test-logo".getBytes();

        // Act
        Company company = companyMapper.toEntity(request, logoBytes);

        // Assert
        assertNotNull(company);
        assertEquals("Test Company", company.getName());
        assertEquals("REG123456", company.getRegistrationNumber());
        assertArrayEquals(logoBytes, company.getLogo());
    }

    @Test
    void toEntity_shouldCreateCompanyWithoutLogo_whenLogoIsNull() {
        // Arrange
        CompanyRequest request = new CompanyRequest("Test Company", "REG123456", null);
        byte[] logoBytes = null;

        // Act
        Company company = companyMapper.toEntity(request, logoBytes);

        // Assert
        assertNotNull(company);
        assertEquals("Test Company", company.getName());
        assertEquals("REG123456", company.getRegistrationNumber());
        assertNull(company.getLogo());
    }

    @Test
    void toEntity_shouldCreateCompanyWithoutLogo_whenLogoIsEmpty() {
        // Arrange
        MultipartFile mockLogo = mock(MultipartFile.class);
        CompanyRequest request = new CompanyRequest("Test Company", "REG123456", mockLogo);
        byte[] logoBytes = new byte[0]; // Empty array

        // Act
        Company company = companyMapper.toEntity(request, logoBytes);

        // Assert
        assertNotNull(company);
        assertEquals("Test Company", company.getName());
        assertEquals("REG123456", company.getRegistrationNumber());
        assertNull(company.getLogo()); // Should not set empty logo
    }

    @Test
    void toEntity_shouldCreateCompanyWithLogo_whenLogoIsValid() {
        // Arrange
        MultipartFile mockLogo = mock(MultipartFile.class);
        CompanyRequest request = new CompanyRequest("Test Company", "REG123456", mockLogo);
        byte[] logoBytes = new byte[]{1, 2, 3, 4, 5};

        // Act
        Company company = companyMapper.toEntity(request, logoBytes);

        // Assert
        assertNotNull(company);
        assertEquals("Test Company", company.getName());
        assertEquals("REG123456", company.getRegistrationNumber());
        assertArrayEquals(logoBytes, company.getLogo());
    }

    // Tests for updateEntity method
    @Test
    void updateEntity_shouldUpdateExistingCompany_whenRequestHasValidData() {
        // Arrange
        Company existingCompany = new Company();
        existingCompany.setId(1L);
        existingCompany.setUuid(UUID.randomUUID());
        existingCompany.setName("Old Company Name");
        existingCompany.setRegistrationNumber("OLD123");
        existingCompany.setLogo("old-logo".getBytes());

        MultipartFile mockLogo = mock(MultipartFile.class);
        CompanyRequest request = new CompanyRequest("Updated Company", "NEW456", mockLogo);
        byte[] newLogoBytes = "new-logo".getBytes();

        // Act
        Company updatedCompany = companyMapper.updateEntity(existingCompany, request, newLogoBytes);

        // Assert
        assertSame(existingCompany, updatedCompany); // Should return the same instance
        assertEquals("Updated Company", updatedCompany.getName());
        assertEquals("NEW456", updatedCompany.getRegistrationNumber());
        assertArrayEquals(newLogoBytes, updatedCompany.getLogo());
        // ID and UUID should remain unchanged
        assertEquals(1L, updatedCompany.getId());
        assertNotNull(updatedCompany.getUuid());
    }

    @Test
    void updateEntity_shouldUpdateCompanyWithoutChangingLogo_whenLogoIsNull() {
        // Arrange
        Company existingCompany = new Company();
        existingCompany.setId(1L);
        existingCompany.setUuid(UUID.randomUUID());
        existingCompany.setName("Old Company Name");
        existingCompany.setRegistrationNumber("OLD123");
        byte[] originalLogo = "original-logo".getBytes();
        existingCompany.setLogo(originalLogo);

        CompanyRequest request = new CompanyRequest("Updated Company", "NEW456", null);
        byte[] newLogoBytes = null;

        // Act
        Company updatedCompany = companyMapper.updateEntity(existingCompany, request, newLogoBytes);

        // Assert
        assertSame(existingCompany, updatedCompany);
        assertEquals("Updated Company", updatedCompany.getName());
        assertEquals("NEW456", updatedCompany.getRegistrationNumber());
        assertArrayEquals(originalLogo, updatedCompany.getLogo()); // Logo should remain unchanged
    }

    @Test
    void updateEntity_shouldUpdateCompanyWithoutChangingLogo_whenLogoIsEmpty() {
        // Arrange
        Company existingCompany = new Company();
        existingCompany.setId(1L);
        existingCompany.setUuid(UUID.randomUUID());
        existingCompany.setName("Old Company Name");
        existingCompany.setRegistrationNumber("OLD123");
        byte[] originalLogo = "original-logo".getBytes();
        existingCompany.setLogo(originalLogo);

        MultipartFile mockLogo = mock(MultipartFile.class);
        CompanyRequest request = new CompanyRequest("Updated Company", "NEW456", mockLogo);
        byte[] newLogoBytes = new byte[0]; // Empty array

        // Act
        Company updatedCompany = companyMapper.updateEntity(existingCompany, request, newLogoBytes);

        // Assert
        assertSame(existingCompany, updatedCompany);
        assertEquals("Updated Company", updatedCompany.getName());
        assertEquals("NEW456", updatedCompany.getRegistrationNumber());
        assertArrayEquals(originalLogo, updatedCompany.getLogo()); // Logo should remain unchanged
    }

    @Test
    void updateEntity_shouldUpdateCompanyAndLogo_whenLogoIsValid() {
        // Arrange
        Company existingCompany = new Company();
        existingCompany.setId(1L);
        existingCompany.setUuid(UUID.randomUUID());
        existingCompany.setName("Old Company Name");
        existingCompany.setRegistrationNumber("OLD123");
        existingCompany.setLogo("old-logo".getBytes());

        MultipartFile mockLogo = mock(MultipartFile.class);
        CompanyRequest request = new CompanyRequest("Updated Company", "NEW456", mockLogo);
        byte[] newLogoBytes = new byte[]{10, 20, 30, 40, 50};

        // Act
        Company updatedCompany = companyMapper.updateEntity(existingCompany, request, newLogoBytes);

        // Assert
        assertSame(existingCompany, updatedCompany);
        assertEquals("Updated Company", updatedCompany.getName());
        assertEquals("NEW456", updatedCompany.getRegistrationNumber());
        assertArrayEquals(newLogoBytes, updatedCompany.getLogo());
    }
}
