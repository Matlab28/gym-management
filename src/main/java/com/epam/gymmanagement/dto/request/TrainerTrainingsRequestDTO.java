package com.epam.gymmanagement.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
public class TrainerTrainingsRequestDTO {
    private String username;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodTo;
    private String traineeName;
}
