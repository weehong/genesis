package com.resetrix.genesis.modules.authentication.validators;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class E164PhoneValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidation_whenPhoneNumberIsValid() {
        TestObject obj = new TestObject("+1234567890");
        Set<ConstraintViolation<TestObject>> violations = validator.validate(obj);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidation_whenPhoneNumberIsNull_andNotOptional() {
        TestObject obj = new TestObject(null);
        Set<ConstraintViolation<TestObject>> violations = validator.validate(obj);
        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailValidation_whenPhoneNumberIsEmpty_andNotOptional() {
        TestObject obj = new TestObject("");
        Set<ConstraintViolation<TestObject>> violations = validator.validate(obj);
        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldPassValidation_whenPhoneNumberIsNull_andOptional() {
        TestObjectOptional obj = new TestObjectOptional(null);
        Set<ConstraintViolation<TestObjectOptional>> violations = validator.validate(obj);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassValidation_whenPhoneNumberIsEmpty_andOptional() {
        TestObjectOptional obj = new TestObjectOptional("");
        Set<ConstraintViolation<TestObjectOptional>> violations = validator.validate(obj);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidation_whenPhoneNumberIsInvalid_evenIfOptional() {
        TestObjectOptional obj = new TestObjectOptional("1234567890"); // Missing +
        Set<ConstraintViolation<TestObjectOptional>> violations = validator.validate(obj);
        assertThat(violations).hasSize(1);
    }

    // Test class with required phone (optional = false)
    static class TestObject {
        @ValidE164Phone
        private String phoneNumber;

        public TestObject(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    // Test class with optional phone (optional = true)
    static class TestObjectOptional {
        @ValidE164Phone(optional = true)
        private String phoneNumber;

        public TestObjectOptional(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }
}