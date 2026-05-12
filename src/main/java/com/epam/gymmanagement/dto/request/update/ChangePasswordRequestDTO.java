package com.epam.gymmanagement.dto.request.update;

import com.epam.gymmanagement.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @ValidPassword(message = "Password must contain an uppercase letter, a lowercase letter, and a number.")
    private String newPassword;
}
