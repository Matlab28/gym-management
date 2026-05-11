package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TraineeProfileResponseDTO {
    private UUID traineeId;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive;
    private List<TrainerShortResponseDTO> trainers;

    public TraineeProfileResponseDTO() {
    }

    public TraineeProfileResponseDTO(String username,
                                     String firstName,
                                     String lastName,
                                     LocalDate dateOfBirth,
                                     String address,
                                     Boolean isActive,
                                     List<TrainerShortResponseDTO> trainers) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.isActive = isActive;
        this.trainers = trainers;
    }
}
