package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.AddTrainingRequestDTO;
import com.epam.gymmanagement.dto.request.TraineeTraineesRequestDTO;
import com.epam.gymmanagement.dto.request.TrainerTrainingsRequestDTO;
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
import com.epam.gymmanagement.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final GymMapper gymMapper;
    private final SecurityService securityService;

    @Transactional
    public MessageResponseDTO addTraining(AddTrainingRequestDTO request) {
        securityService.requireRole(UserRole.ADMIN, UserRole.TRAINER);

        if (!securityService.hasRole(UserRole.ADMIN)
                && !securityService.currentUsername().equals(request.getTrainerUsername())) {
            throw new org.springframework.security.access.AccessDeniedException("Trainer can add only own trainings");
        }

        TraineeEntity trainee = traineeRepository
                .findByUserEntity_Username(request.getTraineeUsername())
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        TrainerEntity trainer = trainerRepository
                .findByUserUsername(request.getTrainerUsername())
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        TrainingType requestedTrainingType = parseTrainingType(request.getTrainingType());
        TrainingTypeEntity trainingType = trainingTypeRepository
                .findByTrainingTypeName(requestedTrainingType)
                .orElseThrow(() -> new NotFoundException("Training type not found"));

        if (!trainer.getSpecialization().getTrainingTypeName().equals(requestedTrainingType)) {
            throw new BadRequestException("Training type must match trainer specialization");
        }

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

        TrainingEntity training = TrainingEntity
                .builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingDate(request.getTrainingDate())
                .trainingType(trainingType)
                .trainingDuration(request.getTrainingDuration())
                .build();

        trainingRepository.save(training);

        log.info(
                "Added training name={} trainee={} trainer={}",
                request.getTrainingName(),
                request.getTraineeUsername(),
                request.getTrainerUsername()
        );

        return new MessageResponseDTO("Training added successfully");
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDTO> getTraineeTrainings(TraineeTraineesRequestDTO dto) {
        securityService.requireSelfOrAdmin(dto.getUsername(), UserRole.TRAINEE);
        validatePeriod(dto.getPeriodFrom(), dto.getPeriodTo());

        if (!traineeRepository.existsByUserEntity_Username(dto.getUsername())) {
            throw new NotFoundException("Trainee not found");
        }

        TrainingType normalizedTrainingType = normalizeTrainingType(dto.getTrainingType());

        List<TrainingEntity> trainings = trainingRepository.findTraineeTrainings(
                dto.getUsername(),
                dto.getPeriodFrom(),
                dto.getPeriodTo(),
                dto.getTrainerName(),
                normalizedTrainingType
        );

        return trainings
                .stream()
                .map(gymMapper::toTrainingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDTO> getTrainerTrainings(TrainerTrainingsRequestDTO dto) {
        securityService.requireSelfOrAdmin(dto.getUsername(), UserRole.TRAINER);
        validatePeriod(dto.getPeriodFrom(), dto.getPeriodTo());

        if (!trainerRepository.existsByUserUsername(dto.getUsername())) {
            throw new NotFoundException("Trainer not found");
        }

        List<TrainingEntity> trainings = trainingRepository.findTrainerTrainings(
                dto.getUsername(),
                dto.getPeriodFrom(),
                dto.getPeriodTo(),
                dto.getTraineeName()
        );

        return trainings.stream()
                .map(gymMapper::toTrainingResponse)
                .toList();
    }

    private TrainingType parseTrainingType(String trainingType) {
        try {
            return TrainingType.fromValue(trainingType);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }

    private TrainingType normalizeTrainingType(TrainingType trainingType) {
        return trainingType;
    }

    private void validatePeriod(LocalDate periodFrom, LocalDate periodTo) {
        if (periodFrom != null && periodTo != null && periodFrom.isAfter(periodTo)) {
            throw new BadRequestException("Period from must be before or equal to period to");
        }
    }
}
