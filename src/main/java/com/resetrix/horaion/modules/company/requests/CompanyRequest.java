package com.resetrix.horaion.modules.company.requests;

import org.springframework.web.multipart.MultipartFile;

import com.resetrix.horaion.modules.company.validators.ValidImage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    String name,

    @NotBlank(message = "Registration number is required")
    @Size(max = 20, message = "Registration number must not exceed 20 characters")
    String registrationNumber,

    @ValidImage(
        maxSize = 5_242_880,
        allowedTypes = {"image/jpeg", "image/png", "image/gif"},
        optional = true
    )
    MultipartFile logo
) {
}