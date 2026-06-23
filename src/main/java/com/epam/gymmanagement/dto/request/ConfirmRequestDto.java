package com.epam.gymmanagement.dto.request;

import com.epam.gymmanagement.validation.OnlyDigits;
import com.epam.gymmanagement.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConfirmRequestDto {
    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    @NotBlank(message = "Confirmation code is required")
    @OnlyDigits(message = "Confirmation code must contain only digits")
    private String confirmation;
}
