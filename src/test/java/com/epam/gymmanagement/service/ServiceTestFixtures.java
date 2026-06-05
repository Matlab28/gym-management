package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.entity.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

final class ServiceTestFixtures {
    private ServiceTestFixtures() {
    }

    static UserEntity user(String username, UserRole role, boolean active) {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .firstName("First")
                .lastName("Last")
                .username(username)
                .password("encoded-" + username)
                .isActive(active)
                .role(role)
                .build();
    }

    static TraineeEntity trainee(String username, boolean active, TrainerEntity... trainers) {
        return TraineeEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user(username, UserRole.TRAINEE, active))
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Main street")
                .trainers(new ArrayList<>(Arrays.asList(trainers)))
                .trainings(new ArrayList<>())
                .build();
    }

    static TrainerEntity trainer(String username, boolean active, TrainingType specialization) {
        return TrainerEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user(username, UserRole.TRAINER, active))
                .specialization(trainingType(specialization))
                .trainees(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();
    }

    static TrainingTypeEntity trainingType(TrainingType trainingType) {
        return TrainingTypeEntity.builder()
                .id(UUID.randomUUID())
                .trainingTypeName(trainingType)
                .build();
    }

    static TrainingEntity training(
            String name,
            TraineeEntity trainee,
            TrainerEntity trainer,
            TrainingType trainingType
    ) {
        return TrainingEntity.builder()
                .id(UUID.randomUUID())
                .trainingName(name)
                .trainingDate(LocalDate.of(2026, 1, 20))
                .trainingDuration(60)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType(trainingType))
                .build();
    }
}
