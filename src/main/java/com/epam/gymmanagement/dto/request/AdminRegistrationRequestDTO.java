package com.epam.gymmanagement.dto.request;

import com.epam.gymmanagement.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRegistrationRequestDTO {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Username is required")
    private String username;

    @ValidPassword
    private String password;
}
