package com.resetrix.horaion.shared.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationHelperTest {

    @Test
    void validatePaginationParameters_shouldPass_whenValidParameters() {
        // Act & Assert - should not throw any exception
        ValidationHelper.validatePaginationParameters(0, 10);
        ValidationHelper.validatePaginationParameters(5, 100);
        ValidationHelper.validatePaginationParameters(0, 1000);
    }

    @Test
    void validatePaginationParameters_shouldThrowException_whenPageIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validatePaginationParameters(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Page must be >= 0");
    }

    @Test
    void validatePaginationParameters_shouldThrowException_whenSizeIsZero() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validatePaginationParameters(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Size must be > 0 and <= 1000");
    }

    @Test
    void validatePaginationParameters_shouldThrowException_whenSizeIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validatePaginationParameters(0, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Size must be > 0 and <= 1000");
    }

    @Test
    void validatePaginationParameters_shouldThrowException_whenSizeExceedsLimit() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validatePaginationParameters(0, 1001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Size must be > 0 and <= 1000");
    }

    @Test
    void validatePaginationParametersWithCustomLimit_shouldPass_whenValidParameters() {
        // Act & Assert - should not throw any exception
        ValidationHelper.validatePaginationParameters(0, 10, 50);
        ValidationHelper.validatePaginationParameters(5, 50, 50);
    }

    @Test
    void validatePaginationParametersWithCustomLimit_shouldThrowException_whenSizeExceedsCustomLimit() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validatePaginationParameters(0, 51, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Size must be > 0 and <= 50");
    }

    @Test
    void validatePaginationParametersWithCustomLimit_shouldThrowException_whenPageIsNegative() {
        // Act & Assert - This covers the missed branch at line 91-92
        assertThatThrownBy(() -> ValidationHelper.validatePaginationParameters(-1, 10, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Page must be >= 0");
    }

    @Test
    void parseSortDirection_shouldReturnASC_whenNullInput() {
        // Act
        Sort.Direction result = ValidationHelper.parseSortDirection(null);

        // Assert
        assertThat(result).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void parseSortDirection_shouldReturnASC_whenEmptyInput() {
        // Act
        Sort.Direction result = ValidationHelper.parseSortDirection("");

        // Assert
        assertThat(result).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void parseSortDirection_shouldReturnASC_whenWhitespaceInput() {
        // Act
        Sort.Direction result = ValidationHelper.parseSortDirection("   ");

        // Assert
        assertThat(result).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void parseSortDirection_shouldReturnASC_whenASCInput() {
        // Act
        Sort.Direction result = ValidationHelper.parseSortDirection("ASC");

        // Assert
        assertThat(result).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void parseSortDirection_shouldReturnDESC_whenDESCInput() {
        // Act
        Sort.Direction result = ValidationHelper.parseSortDirection("DESC");

        // Assert
        assertThat(result).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void parseSortDirection_shouldReturnASC_whenLowercaseASCInput() {
        // Act
        Sort.Direction result = ValidationHelper.parseSortDirection("asc");

        // Assert
        assertThat(result).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void parseSortDirection_shouldReturnDESC_whenLowercaseDESCInput() {
        // Act
        Sort.Direction result = ValidationHelper.parseSortDirection("desc");

        // Assert
        assertThat(result).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void parseSortDirection_shouldHandleWhitespace_whenInputHasSpaces() {
        // Act
        Sort.Direction result1 = ValidationHelper.parseSortDirection("  ASC  ");
        Sort.Direction result2 = ValidationHelper.parseSortDirection("  DESC  ");

        // Assert
        assertThat(result1).isEqualTo(Sort.Direction.ASC);
        assertThat(result2).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void parseSortDirection_shouldThrowException_whenInvalidInput() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.parseSortDirection("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid sortDirection: must be 'ASC' or 'DESC'");
    }

    @Test
    void validateId_shouldPass_whenValidId() {
        // Act & Assert - should not throw any exception
        ValidationHelper.validateId(1L);
        ValidationHelper.validateId(100L);
        ValidationHelper.validateId(Long.MAX_VALUE);
    }

    @Test
    void validateId_shouldThrowException_whenIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validateId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID must be a positive number");
    }

    @Test
    void validateId_shouldThrowException_whenIdIsZero() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validateId(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID must be a positive number");
    }

    @Test
    void validateId_shouldThrowException_whenIdIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validateId(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID must be a positive number");
    }

    @Test
    void validateIdWithEntityName_shouldPass_whenValidId() {
        // Act & Assert - should not throw any exception
        ValidationHelper.validateId(1L, "Company");
        ValidationHelper.validateId(100L, "User");
    }

    @Test
    void validateIdWithEntityName_shouldThrowException_whenIdIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validateId(null, "Company"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Company ID must be a positive number");
    }

    @Test
    void validateUuid_shouldPass_whenValidUuid() {
        // Arrange
        UUID uuid = UUID.randomUUID();

        // Act & Assert - should not throw any exception
        ValidationHelper.validateUuid(uuid);
    }

    @Test
    void validateUuid_shouldThrowException_whenUuidIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validateUuid(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UUID cannot be null");
    }

    @Test
    void validateUuidWithEntityName_shouldPass_whenValidUuid() {
        // Arrange
        UUID uuid = UUID.randomUUID();

        // Act & Assert - should not throw any exception
        ValidationHelper.validateUuid(uuid, "Company");
    }

    @Test
    void validateUuidWithEntityName_shouldThrowException_whenUuidIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> ValidationHelper.validateUuid(null, "Company"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Company UUID cannot be null");
    }

    @Test
    void constructor_shouldThrowException_whenInstantiated() {
        // Act & Assert
        assertThatThrownBy(() -> {
            // Use reflection to access the private constructor
            var constructor = ValidationHelper.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
                .hasCauseInstanceOf(IllegalStateException.class)
                .getCause()
                .hasMessage("Utility class");
    }
}
