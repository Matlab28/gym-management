package com.epam.gymmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainerUsernameRequestDTO {
    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;
}
