package com.epam.gymmanagement.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TrainerTrainingSearchRequestDTO {
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String traineeName;
}