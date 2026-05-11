package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TraineeShortResponseDTO {
    private UUID traineeId;
    private String traineeUsername;
    private String traineeFirstName;
    private String traineeLastName;

    public TraineeShortResponseDTO() {
    }

    public TraineeShortResponseDTO(String traineeUsername,
                                   String traineeFirstName,
                                   String traineeLastName) {
        this.traineeUsername = traineeUsername;
        this.traineeFirstName = traineeFirstName;
        this.traineeLastName = traineeLastName;
    }
}
