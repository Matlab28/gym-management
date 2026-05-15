package com.epam.gymmanagement.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomValidatorTest {

    @Test
    void onlyDigitsAllowsNullEmptyAndDigitsOnlyValues() {
        OnlyDigitsValidator validator = new OnlyDigitsValidator();

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("12345", null));
        assertFalse(validator.isValid("123a", null));
    }

    @Test
    void passwordStrengthRequiresUppercaseLowercaseAndDigit() {
        PasswordStrengthValidator validator = new PasswordStrengthValidator();

        assertTrue(validator.isValid("Strong1", null));
        assertFalse(validator.isValid(null, null));
        assertFalse(validator.isValid("strong1", null));
        assertFalse(validator.isValid("STRONG1", null));
        assertFalse(validator.isValid("Strong", null));
    }

    @Test
    void passwordConfirmationRequiresMatchingPasswordFields() {
        PasswordConfirmationValidator validator = new PasswordConfirmationValidator();

        assertTrue(validator.isValid(new PasswordForm("Strong1", "Strong1"), null));
        assertFalse(validator.isValid(new PasswordForm("Strong1", "Different1"), null));
        assertFalse(validator.isValid(new PasswordForm("Strong1", null), null));
    }

    @SuppressWarnings("unused")
    private record PasswordForm(String password, String passConfirm) {
    }
}
