package com.epam.gymmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class UpdateTraineeTrainersRequestDTO {
    @NotEmpty(message = "Trainers list is required")
    private List<@NotBlank(message = "Trainer username is required") String> trainers;

    public void setTrainers(List<?> trainers) {
        this.trainers = trainers == null
                ? null
                : trainers.stream()
                .map(this::trainerUsername)
                .toList();
    }

    private String trainerUsername(Object trainer) {
        if (trainer instanceof String username) {
            return username;
        }

        if (trainer instanceof Map<?, ?> trainerMap) {
            Object username = trainerMap.get("trainerUsername");
            return username == null ? null : username.toString();
        }

        return trainer == null ? null : trainer.toString();
    }
}
