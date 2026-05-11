package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TrainerProfileResponseDTO {
    private UUID trainerId;
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean isActive;
    private List<TraineeShortResponseDTO> trainees;

    public TrainerProfileResponseDTO() {
    }

    public TrainerProfileResponseDTO(String username,
                                     String firstName,
                                     String lastName,
                                     String specialization,
                                     Boolean isActive,
                                     List<TraineeShortResponseDTO> trainees) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.isActive = isActive;
        this.trainees = trainees;
    }
}
