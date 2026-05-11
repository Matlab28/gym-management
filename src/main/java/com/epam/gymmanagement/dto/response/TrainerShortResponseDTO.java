package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TrainerShortResponseDTO {
    private UUID trainerId;
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private String trainerSpecialization;

    public TrainerShortResponseDTO() {
    }

    public TrainerShortResponseDTO(String trainerUsername,
                                   String trainerFirstName,
                                   String trainerLastName,
                                   String trainerSpecialization) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.trainerSpecialization = trainerSpecialization;
    }
}
