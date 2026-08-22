package com.epam.gymmanagement.bdd;

import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.constant.WorkloadActionType;
import com.epam.gymmanagement.dto.request.AddTrainingRequestDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingEntity;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainerRepository;
import com.epam.gymmanagement.repository.TrainingRepository;
import com.epam.gymmanagement.repository.TrainingTypeRepository;
import com.epam.gymmanagement.security.SecurityService;
import com.epam.gymmanagement.service.GymMapper;
import com.epam.gymmanagement.service.TrainerWorkloadIntegrationService;
import com.epam.gymmanagement.service.TrainingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GymManagementComponentSteps {

    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private SecurityService securityService;
    private TrainerWorkloadIntegrationService workloadIntegrationService;
    private TrainingService trainingService;
    private TrainerEntity trainer;
    private TraineeEntity trainee;
    private MessageResponseDTO response;
    private RuntimeException failure;

    @Before("@component")
    public void setUpComponent() {
        trainingRepository = mock(TrainingRepository.class);
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        securityService = mock(SecurityService.class);
        workloadIntegrationService = mock(TrainerWorkloadIntegrationService.class);
        trainingService = new TrainingService(
                trainingRepository,
                traineeRepository,
                trainerRepository,
                trainingTypeRepository,
                mock(GymMapper.class),
                securityService,
                new ModelMapper(),
                workloadIntegrationService
        );
        response = null;
        failure = null;
    }

    @Given("an active trainee and Yoga trainer exist")
    public void activeProfilesExist() {
        TrainingTypeEntity yoga = TrainingTypeEntity.builder()
                .id(UUID.randomUUID())
                .trainingTypeName(TrainingType.YOGA)
                .build();
        trainer = TrainerEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user("trainer.yoga", UserRole.TRAINER))
                .specialization(yoga)
                .trainees(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();
        trainee = TraineeEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user("trainee.member", UserRole.TRAINEE))
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Main street")
                .trainers(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();

        when(securityService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(traineeRepository.findByUserEntity_Username("trainee.member"))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserEntity_Username("trainer.yoga"))
                .thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.YOGA))
                .thenReturn(Optional.of(yoga));
    }

    @Given("the trainer is assigned to the trainee")
    public void trainerIsAssigned() {
        trainee.getTrainers().add(trainer);
    }

    @Given("the trainer is not assigned to the trainee")
    public void trainerIsNotAssigned() {
        trainee.getTrainers().clear();
    }

    @When("the administrator adds a {int} minute Yoga training")
    public void administratorAddsTraining(int duration) {
        try {
            response = trainingService.addTraining(AddTrainingRequestDTO.builder()
                    .traineeUsername("trainee.member")
                    .trainerUsername("trainer.yoga")
                    .trainingName("BDD Yoga")
                    .trainingDate(LocalDate.of(2026, 8, 21))
                    .trainingType("Yoga")
                    .trainingDuration(duration)
                    .build());
        } catch (RuntimeException exception) {
            failure = exception;
        }
    }

    @Then("the training is stored and an ADD workload event is published")
    public void trainingIsStoredAndPublished() {
        assertNotNull(response);
        assertEquals("Training added successfully", response.getMessage());
        verify(trainingRepository).save(any(TrainingEntity.class));
        verify(workloadIntegrationService).updateWorkload(any(TrainingEntity.class),
                org.mockito.ArgumentMatchers.eq(WorkloadActionType.ADD));
    }

    @Then("the training is rejected because the trainer is not assigned")
    public void trainingIsRejected() {
        BadRequestException exception = assertInstanceOf(BadRequestException.class, failure);
        assertEquals("Trainer is not assigned to this trainee", exception.getMessage());
        verify(trainingRepository, never()).save(any());
        verify(workloadIntegrationService, never()).updateWorkload(any(), any());
    }

    private UserEntity user(String username, UserRole role) {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .email(username + "@example.com")
                .firstName("BDD")
                .lastName("User")
                .username(username)
                .password("encoded-password")
                .isActive(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .role(role)
                .build();
    }
}
