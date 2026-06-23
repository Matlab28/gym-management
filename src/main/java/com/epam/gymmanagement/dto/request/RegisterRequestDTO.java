package com.epam.gymmanagement.dto.request;

import com.epam.gymmanagement.validation.PasswordConfirmation;
import com.epam.gymmanagement.validation.ValidEmail;
import com.epam.gymmanagement.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@PasswordConfirmation
public class RegisterRequestDTO {
    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

//    @NotBlank(message = "Password confirmation is required")
    private String passConfirm;
}
