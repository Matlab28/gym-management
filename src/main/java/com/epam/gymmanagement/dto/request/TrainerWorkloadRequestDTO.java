package com.epam.gymmanagement.dto.request;

import com.epam.gymmanagement.constant.WorkloadActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TrainerWorkloadRequestDTO {
    private UUID eventId;
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;
    private LocalDate trainingDate;
    private Integer trainingDuration;
    private WorkloadActionType actionType;
}
