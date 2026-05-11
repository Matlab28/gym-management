package com.epam.gymmanagement.service;

import com.epam.gymmanagement.dto.response.*;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GymMapper {

    public TrainerShortResponseDTO toTrainerShortResponse(TrainerEntity trainer) {
        return new TrainerShortResponseDTO(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName().getValue()
        );
    }

    public TraineeShortResponseDTO toTraineeShortResponse(TraineeEntity trainee) {
        return new TraineeShortResponseDTO(
                trainee.getUserEntity().getUsername(),
                trainee.getUserEntity().getFirstName(),
                trainee.getUserEntity().getLastName()
        );
    }

    public TraineeProfileResponseDTO toTraineeProfileResponse(TraineeEntity trainee) {
        List<TrainerShortResponseDTO> trainers = trainee.getTrainers()
                .stream()
                .map(this::toTrainerShortResponse)
                .toList();

        return new TraineeProfileResponseDTO(
                trainee.getUserEntity().getUsername(),
                trainee.getUserEntity().getFirstName(),
                trainee.getUserEntity().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUserEntity().getIsActive(),
                trainers
        );
    }

    public TrainerProfileResponseDTO toTrainerProfileResponse(TrainerEntity trainer) {
        List<TraineeShortResponseDTO> trainees = trainer.getTrainees()
                .stream()
                .map(this::toTraineeShortResponse)
                .toList();

        return new TrainerProfileResponseDTO(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName().getValue(),
                trainer.getUser().getIsActive(),
                trainees
        );
    }

    public TrainingResponseDTO toTrainingResponse(TrainingEntity training) {
        String trainerName = training.getTrainer().getUser().getFirstName()
                + " "
                + training.getTrainer().getUser().getLastName();

        String traineeName = training.getTrainee().getUserEntity().getFirstName()
                + " "
                + training.getTrainee().getUserEntity().getLastName();

        return new TrainingResponseDTO(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName().getValue(),
                training.getTrainingDuration(),
                trainerName,
                traineeName
        );
    }
}
