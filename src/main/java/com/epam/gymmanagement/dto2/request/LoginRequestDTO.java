package com.epam.gymmanagement.dto2.request;

import com.epam.gymmanagement.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @ValidPassword(message = "Password must contain an uppercase letter, a lowercase letter, and a number.")
    private String password;
}
