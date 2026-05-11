package com.epam.gymmanagement.dto2.request;

import com.epam.gymmanagement.constant.TrainingType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class TrainingRequestDTO {
    private TrainingType trainingType;
    private UUID traineeId;
    private UUID trainerId;
    private String trainingName;
    private UUID trainingTypeId;
    private LocalDate trainingDate;
    private int trainingDuration;
}
