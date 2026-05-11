package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TrainingResponseDTO {
    private UUID trainingId;
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String trainerName;
    private String traineeName;

    public TrainingResponseDTO() {
    }

    public TrainingResponseDTO(String trainingName,
                               LocalDate trainingDate,
                               String trainingType,
                               Integer trainingDuration,
                               String trainerName,
                               String traineeName) {
        this.trainingName = trainingName;
        this.trainingDate = trainingDate;
        this.trainingType = trainingType;
        this.trainingDuration = trainingDuration;
        this.trainerName = trainerName;
        this.traineeName = traineeName;
    }
}
