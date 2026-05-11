package com.epam.gymmanagement.dto.request.update;

import com.epam.gymmanagement.dto.request.TrainerUsernameRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateTraineeTrainersRequestDTO {
    @NotEmpty(message = "Trainers list is required")
    private List<@Valid TrainerUsernameRequestDTO> trainers;
}