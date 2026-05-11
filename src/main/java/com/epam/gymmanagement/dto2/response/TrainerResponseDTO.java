package com.epam.gymmanagement.dto2.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class TrainerResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID specializationId;
}
