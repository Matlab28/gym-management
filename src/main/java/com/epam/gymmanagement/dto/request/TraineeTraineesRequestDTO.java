package com.epam.gymmanagement.dto.request;

import com.epam.gymmanagement.constant.TrainingType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
public class TraineeTraineesRequestDTO {
    private String username;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodTo;
    private String trainerName;
    private TrainingType trainingType;
}
