package com.epam.gymmanagement.dto.request;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDTO {
    @NotNull(message = "Role is required")
    private UserRole role;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private TrainingType specialization;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    private String address;
}
