package com.epam.gymmanagement.dto2.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TrainerUsernameRequestDTO {
    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;
}
