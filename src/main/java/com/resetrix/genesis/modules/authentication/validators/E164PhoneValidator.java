package com.resetrix.genesis.modules.authentication.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.resetrix.genesis.modules.authentication.constants.ValidationConstants.E164_PATTERN;

public class E164PhoneValidator implements ConstraintValidator<ValidE164Phone, String> {

    private boolean optional;

    public E164PhoneValidator() {
        // No-arg constructor required by Jakarta Validation
    }

    @Override
    public void initialize(ValidE164Phone constraintAnnotation) {
        this.optional = constraintAnnotation.optional();
    }

    @Override
    public boolean isValid(String phoneNumber,
                           ConstraintValidatorContext context) {
        boolean isPhoneNumberNullOrEmpty = phoneNumber == null || phoneNumber.trim().isEmpty();

        if (optional && isPhoneNumberNullOrEmpty) {
            return true;
        } else if (isPhoneNumberNullOrEmpty) {
            return false;
        }

        phoneNumber = phoneNumber.trim();

        return E164_PATTERN.matcher(phoneNumber).matches();
    }
}
