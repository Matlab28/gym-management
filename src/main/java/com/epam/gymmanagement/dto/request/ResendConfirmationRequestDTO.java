package com.epam.gymmanagement.dto.request;

import com.epam.gymmanagement.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendConfirmationRequestDTO {
    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;
}
