package com.epam.gymmanagement.dto.request;

import com.epam.gymmanagement.constant.TrainingType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSearchRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodTo;

    private String trainerName;
    private String traineeName;
    private TrainingType trainingType;
}
