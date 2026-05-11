package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.dto.request.AddTrainingRequestDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.TrainingResponseDTO;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingEntity;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.exception.NotFoundException;
import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainerRepository;
import com.epam.gymmanagement.repository.TrainingRepository;
import com.epam.gymmanagement.repository.TrainingTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final GymMapper gymMapper;

    @Transactional
    public MessageResponseDTO addTraining(AddTrainingRequestDTO request) {
        TraineeEntity trainee = traineeRepository
                .findByUserEntity_Username(request.getTraineeUsername())
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        TrainerEntity trainer = trainerRepository
                .findByUserUsername(request.getTrainerUsername())
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        TrainingTypeEntity trainingType = trainingTypeRepository
                .findByTrainingTypeNameIgnoreCase(TrainingType.valueOf(request.getTrainingType()))
                .orElseThrow(() -> new NotFoundException("Training type not found"));

        boolean trainerAssignedToTrainee = trainee.getTrainers()
                .stream()
                .anyMatch(existingTrainer -> existingTrainer.getId().equals(trainer.getId()));

        if (!trainerAssignedToTrainee) {
            throw new BadRequestException("Trainer is not assigned to this trainee");
        }

        if (!Boolean.TRUE.equals(trainee.getUserEntity().getIsActive())) {
            throw new BadRequestException("Trainee profile is inactive");
        }

        if (!Boolean.TRUE.equals(trainer.getUser().getIsActive())) {
            throw new BadRequestException("Trainer profile is inactive");
        }

        TrainingEntity training = new TrainingEntity();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(request.getTrainingName());
        training.setTrainingDate(request.getTrainingDate());
        training.setTrainingType(trainingType);
        training.setTrainingDuration(request.getTrainingDuration());

        trainingRepository.save(training);

        /**
         * Later you can call DynamoDB summary update here.
         *
         * epam:
         * trainingSummaryService.updateTrainerSummary(training);
         */

        return new MessageResponseDTO("Training added successfully");
    }

    public List<TrainingResponseDTO> getTraineeTrainings(
            String username,
            LocalDate periodFrom,
            LocalDate periodTo,
            String trainerName,
            String trainingType
    ) {
        if (!traineeRepository.existsByUserEntity_Username(username)) {
            throw new NotFoundException("Trainee not found");
        }

        List<TrainingEntity> trainings = trainingRepository.findTraineeTrainings(
                username,
                periodFrom,
                periodTo,
                trainerName,
                trainingType
        );

        return trainings.stream()
                .map(gymMapper::toTrainingResponse)
                .toList();
    }

    public List<TrainingResponseDTO> getTrainerTrainings(
            String username,
            LocalDate periodFrom,
            LocalDate periodTo,
            String traineeName
    ) {
        if (!trainerRepository.existsByUserUsername(username)) {
            throw new NotFoundException("Trainer not found");
        }

        List<TrainingEntity> trainings = trainingRepository.findTrainerTrainings(
                username,
                periodFrom,
                periodTo,
                traineeName
        );

        return trainings.stream()
                .map(gymMapper::toTrainingResponse)
                .toList();
    }
}