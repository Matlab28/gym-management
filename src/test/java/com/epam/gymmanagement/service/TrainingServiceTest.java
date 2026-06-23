package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.AddTrainingRequestDTO;
import com.epam.gymmanagement.dto.request.TrainingSearchRequestDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private GymMapper gymMapper;
    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void addTrainingCreatesTrainingForAdminWhenTrainerIsAssignedAndProfilesAreActive() {
        AddTrainingRequestDTO request = addTrainingRequest("Yoga Basics", "Yoga");
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.YOGA);
        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true, trainer);
        TrainingTypeEntity trainingType = ServiceTestFixtures.trainingType(TrainingType.YOGA);

        when(securityService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserEntity_Username("trainer.user")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.YOGA)).thenReturn(Optional.of(trainingType));

        MessageResponseDTO response = trainingService.addTraining(request);

        assertEquals("Training added successfully", response.getMessage());

        ArgumentCaptor<TrainingEntity> trainingCaptor = ArgumentCaptor.forClass(TrainingEntity.class);
        verify(trainingRepository).save(trainingCaptor.capture());
        TrainingEntity savedTraining = trainingCaptor.getValue();
        assertEquals(trainee, savedTraining.getTrainee());
        assertEquals(trainer, savedTraining.getTrainer());
        assertEquals("Yoga Basics", savedTraining.getTrainingName());
        assertEquals(LocalDate.of(2026, 1, 20), savedTraining.getTrainingDate());
        assertEquals(trainingType, savedTraining.getTrainingType());
        assertEquals(45, savedTraining.getTrainingDuration());
        verify(securityService).requireRole(UserRole.ADMIN, UserRole.TRAINER);
        verify(securityService, never()).currentUsername();
    }

    @Test
    void addTrainingDeniesTrainerAddingTrainingForAnotherTrainer() {
        AddTrainingRequestDTO request = addTrainingRequest("Yoga Basics", "Yoga");

        when(securityService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(securityService.currentUsername()).thenReturn("other.trainer");

        assertThrows(AccessDeniedException.class, () -> trainingService.addTraining(request));

        verifyNoInteractions(traineeRepository, trainerRepository, trainingTypeRepository, trainingRepository);
    }

    @Test
    void addTrainingRejectsTrainingTypeThatDoesNotMatchTrainerSpecialization() {
        AddTrainingRequestDTO request = addTrainingRequest("Yoga Basics", "Yoga");
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.FITNESS);
        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true, trainer);
        TrainingTypeEntity trainingType = ServiceTestFixtures.trainingType(TrainingType.YOGA);

        when(securityService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserEntity_Username("trainer.user")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.YOGA)).thenReturn(Optional.of(trainingType));

        assertThrows(BadRequestException.class, () -> trainingService.addTraining(request));

        verify(trainingRepository, never()).save(any(TrainingEntity.class));
    }

    @Test
    void addTrainingRejectsTrainerWhoIsNotAssignedToTrainee() {
        AddTrainingRequestDTO request = addTrainingRequest("Yoga Basics", "Yoga");
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.YOGA);
        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
        TrainingTypeEntity trainingType = ServiceTestFixtures.trainingType(TrainingType.YOGA);

        when(securityService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserEntity_Username("trainer.user")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.YOGA)).thenReturn(Optional.of(trainingType));

        assertThrows(BadRequestException.class, () -> trainingService.addTraining(request));

        verify(trainingRepository, never()).save(any(TrainingEntity.class));
    }

    @Test
    void addTrainingRejectsInactiveProfiles() {
        AddTrainingRequestDTO request = addTrainingRequest("Yoga Basics", "Yoga");
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.YOGA);
        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", false, trainer);
        TrainingTypeEntity trainingType = ServiceTestFixtures.trainingType(TrainingType.YOGA);

        when(securityService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserEntity_Username("trainer.user")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.YOGA)).thenReturn(Optional.of(trainingType));

        assertThrows(BadRequestException.class, () -> trainingService.addTraining(request));

        verify(trainingRepository, never()).save(any(TrainingEntity.class));
    }

    @Test
    void getTraineeTrainingsValidatesAccessNormalizesTypeAndMapsResults() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        TrainingSearchRequestDTO request = traineeTrainingsRequest(from, to, TrainingType.YOGA);
        TrainingEntity training = ServiceTestFixtures.training(
                "Yoga Basics",
                ServiceTestFixtures.trainee("trainee.user", true),
                ServiceTestFixtures.trainer("trainer.user", true, TrainingType.YOGA),
                TrainingType.YOGA
        );
        TrainingResponseDTO mapped = new TrainingResponseDTO();

        when(traineeRepository.existsByUserEntity_Username("trainee.user")).thenReturn(true);
        when(trainingRepository.findTraineeTrainings(
                "trainee.user",
                from,
                to,
                "Jane Trainer",
                TrainingType.YOGA
        )).thenReturn(List.of(training));
        when(gymMapper.toTrainingResponse(training)).thenReturn(mapped);

        List<TrainingResponseDTO> response = trainingService.getTraineeTrainings(request);

        assertEquals(List.of(mapped), response);
        verify(securityService).requireSelfOrAdmin("trainee.user", UserRole.TRAINEE);
    }

    @Test
    void getTraineeTrainingsAllowsMissingTrainingTypeCriteria() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        TrainingSearchRequestDTO request = traineeTrainingsRequest(from, to, null);

        when(traineeRepository.existsByUserEntity_Username("trainee.user")).thenReturn(true);
        when(trainingRepository.findTraineeTrainings(
                "trainee.user",
                from,
                to,
                "Jane Trainer",
                null
        )).thenReturn(List.of());

        List<TrainingResponseDTO> response = trainingService.getTraineeTrainings(request);

        assertEquals(List.of(), response);
    }

    @Test
    void getTraineeTrainingsRejectsInvalidPeriod() {
        TrainingSearchRequestDTO request = traineeTrainingsRequest(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1),
                TrainingType.YOGA
        );

        assertThrows(BadRequestException.class, () -> trainingService.getTraineeTrainings(request));

        verify(traineeRepository, never()).existsByUserEntity_Username("trainee.user");
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void getTraineeTrainingsThrowsWhenTraineeDoesNotExist() {
        TrainingSearchRequestDTO request = traineeTrainingsRequest(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                TrainingType.YOGA
        );
        when(traineeRepository.existsByUserEntity_Username("trainee.user")).thenReturn(false);

        assertThrows(NotFoundException.class, () -> trainingService.getTraineeTrainings(request));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void getTrainerTrainingsValidatesAccessAndMapsResults() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        TrainingSearchRequestDTO request = trainerTrainingsRequest(from, to);
        TrainingEntity training = ServiceTestFixtures.training(
                "Yoga Basics",
                ServiceTestFixtures.trainee("trainee.user", true),
                ServiceTestFixtures.trainer("trainer.user", true, TrainingType.YOGA),
                TrainingType.YOGA
        );
        TrainingResponseDTO mapped = new TrainingResponseDTO();

        when(trainerRepository.existsByUserEntity_Username("trainer.user")).thenReturn(true);
        when(trainingRepository.findTrainerTrainings("trainer.user", from, to, "Tina Trainee"))
                .thenReturn(List.of(training));
        when(gymMapper.toTrainingResponse(training)).thenReturn(mapped);

        List<TrainingResponseDTO> response = trainingService.getTrainerTrainings(request);

        assertEquals(List.of(mapped), response);
        verify(securityService).requireSelfOrAdmin("trainer.user", UserRole.TRAINER);
    }

    @Test
    void getTrainerTrainingsRejectsInvalidPeriod() {
        TrainingSearchRequestDTO request = trainerTrainingsRequest(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1)
        );

        assertThrows(BadRequestException.class, () -> trainingService.getTrainerTrainings(request));

        verify(trainerRepository, never()).existsByUserEntity_Username("trainer.user");
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void getTrainerTrainingsThrowsWhenTrainerDoesNotExist() {
        TrainingSearchRequestDTO request = trainerTrainingsRequest(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );
        when(trainerRepository.existsByUserEntity_Username("trainer.user")).thenReturn(false);

        assertThrows(NotFoundException.class, () -> trainingService.getTrainerTrainings(request));

        verifyNoInteractions(trainingRepository);
    }

    private AddTrainingRequestDTO addTrainingRequest(String trainingName, String trainingType) {
        return AddTrainingRequestDTO.builder()
                .traineeUsername("trainee.user")
                .trainerUsername("trainer.user")
                .trainingName(trainingName)
                .trainingDate(LocalDate.of(2026, 1, 20))
                .trainingType(trainingType)
                .trainingDuration(45)
                .build();
    }

    private TrainingSearchRequestDTO traineeTrainingsRequest(
            LocalDate periodFrom,
            LocalDate periodTo,
            TrainingType trainingType
    ) {
        return TrainingSearchRequestDTO.builder()
                .username("trainee.user")
                .periodFrom(periodFrom)
                .periodTo(periodTo)
                .trainerName("Jane Trainer")
                .trainingType(trainingType)
                .build();
    }

    private TrainingSearchRequestDTO trainerTrainingsRequest(LocalDate periodFrom, LocalDate periodTo) {
        return TrainingSearchRequestDTO.builder()
                .username("trainer.user")
                .periodFrom(periodFrom)
                .periodTo(periodTo)
                .traineeName("Tina Trainee")
                .build();
    }
}
