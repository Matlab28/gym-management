package com.epam.gymmanagement.dto2.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class TrainerRequestDTO {
    private UUID userId;
    private UUID specializationId;
}
