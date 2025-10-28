package com.resetrix.horaion.modules.company.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private long maxSize;
    private String[] allowedTypes;
    private boolean optional;

    public ImageValidator() {
        // No-arg constructor required by Jakarta Validation
    }

    @Override
    public void initialize(ValidImage constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedTypes = constraintAnnotation.allowedTypes();
        this.optional = constraintAnnotation.optional();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // If file is null or empty and optional is true, it's valid
        if ((file == null || file.isEmpty()) && optional) {
            return true;
        }

        // If file is null or empty and optional is false, it's invalid
        if (file == null || file.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Image file is required")
                    .addConstraintViolation();
            return false;
        }

        // Check file size
        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Image file size must not exceed %d bytes (%.2f MB)",
                            maxSize, maxSize / 1024.0 / 1024.0))
                    .addConstraintViolation();
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes).contains(contentType)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Image file type must be one of: %s, but got: %s",
                            String.join(", ", allowedTypes), contentType))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}

