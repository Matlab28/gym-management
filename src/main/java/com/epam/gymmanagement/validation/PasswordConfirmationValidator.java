package com.epam.gymmanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordConfirmationValidator implements ConstraintValidator<PasswordConfirmation, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        String password = (String) beanWrapper.getPropertyValue("password");
        String passConfirm = (String) beanWrapper.getPropertyValue("passConfirm");

        if (password == null || passConfirm == null) {
            return false;
        }

        return password.equals(passConfirm);
    }
}
