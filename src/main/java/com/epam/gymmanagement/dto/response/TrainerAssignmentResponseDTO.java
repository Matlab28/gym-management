package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TrainerAssignmentResponseDTO {
    private UUID trainerAssignmentId;
    private List<TrainerShortResponseDTO> trainers;

    public TrainerAssignmentResponseDTO() {
    }

    public TrainerAssignmentResponseDTO(List<TrainerShortResponseDTO> trainers) {
        this.trainers = trainers;
    }
}
