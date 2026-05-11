package com.epam.gymmanagement.dto2.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class TraineeResponseDTO {
    private UUID id;
    private UUID userId;
    private LocalDate dateOfBirth;
    private String address;
}
