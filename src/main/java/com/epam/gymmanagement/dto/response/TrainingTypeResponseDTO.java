package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class TrainingTypeResponseDTO {
    private UUID id;
    private String trainingType;

    public TrainingTypeResponseDTO(UUID id, String trainingType) {
        this.id = id;
        this.trainingType = trainingType;
    }

    public TrainingTypeResponseDTO() {
    }
}
