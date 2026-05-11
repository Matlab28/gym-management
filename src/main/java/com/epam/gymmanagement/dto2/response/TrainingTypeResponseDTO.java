package com.epam.gymmanagement.dto2.response;

import com.epam.gymmanagement.constant.TrainingType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class TrainingTypeResponseDTO {
    private UUID id;
    private TrainingType trainingType;
}
