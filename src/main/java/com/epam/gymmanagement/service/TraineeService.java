package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.TraineeRegistrationRequestDTO;
import com.epam.gymmanagement.dto.request.TrainerUsernameRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTraineeProfileRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTraineeTrainersRequestDTO;
import com.epam.gymmanagement.dto.response.*;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.exception.NotFoundException;
import com.epam.gymmanagement.metrics.TraineeMetrics;
import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainerRepository;
import com.epam.gymmanagement.repository.UserRepository;
import com.epam.gymmanagement.security.SecurityService;
import com.epam.gymmanagement.util.PasswordGenerator;
import com.epam.gymmanagement.util.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraineeService {
    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final GymMapper gymMapper;
    private final SecurityService securityService;
    private final ModelMapper modelMapper;
    private final TraineeMetrics traineeMetrics;

    @Transactional
    public RegistrationResponseDTO registerTrainee(TraineeRegistrationRequestDTO request) {
        String username = usernameGenerator.generate(
                request.getFirstName(),
                request.getLastName()
        );

        String rawPassword = passwordGenerator.generate();

        UserEntity entity = modelMapper.map(request, UserEntity.class);
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(rawPassword));
        entity.setIsActive(true);
        entity.setRole(UserRole.TRAINEE);

        UserEntity savedUser = userRepository.save(entity);

        TraineeEntity trainee = TraineeEntity.builder()
                .userEntity(savedUser)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        traineeRepository.save(trainee);
        traineeMetrics.getTraineeCreatedCounter().increment();
        log.info("Registered trainee profile for username={}", username);
        return new RegistrationResponseDTO(username, rawPassword);
    }

    @Transactional(readOnly = true)
    public TraineeProfileResponseDTO getTraineeProfile(String username) {
        securityService.requireSelfOrAdmin(username, UserRole.TRAINEE);

        TraineeEntity trainee = traineeRepository.findByUserEntity_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        return gymMapper.toTraineeProfileResponse(trainee);
    }

    @Transactional
    public TraineeProfileResponseDTO updateTraineeProfile(String username, UpdateTraineeProfileRequestDTO request) {
        securityService.requireSelfOrAdmin(username, UserRole.TRAINEE);

        TraineeEntity trainee = traineeRepository.findByUserEntity_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        UserEntity user = trainee.getUserEntity();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(request.getIsActive());

        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        userRepository.save(user);
        TraineeEntity updatedTrainee = traineeRepository.save(trainee);

        traineeMetrics.getTraineeUpdatedCounter().increment();
        log.info("Updated trainee profile for username={}", username);
        return gymMapper.toTraineeProfileResponse(updatedTrainee);
    }

    @Transactional
    public MessageResponseDTO deleteTraineeProfile(String username) {
        securityService.requireSelfOrAdmin(username, UserRole.TRAINEE);

        TraineeEntity trainee = traineeRepository.findByUserEntity_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        UserEntity user = trainee.getUserEntity();

        trainee.getTrainers().clear();
        traineeRepository.delete(trainee);
        userRepository.delete(user);

        traineeMetrics.getTraineeDeletedCounter().increment();
        log.info("Deleted trainee profile for username={}", username);
        return new MessageResponseDTO("Trainee profile deleted successfully");
    }

    @Transactional
    public MessageResponseDTO activateTraineeProfile(String username) {
        MessageResponseDTO response = changeTraineeStatus(
                username,
                true,
                "Trainee profile activated successfully"
        );

        traineeMetrics.getTraineeActivatedCounter().increment();
        return response;
    }

    @Transactional
    public MessageResponseDTO deactivateTraineeProfile(String username) {
        MessageResponseDTO response = changeTraineeStatus(
                username,
                false,
                "Trainee profile deactivated successfully"
        );

        traineeMetrics.getTraineeDeactivatedCounter().increment();
        return response;
    }

    @Transactional(readOnly = true)
    public List<TrainerShortResponseDTO> getNotAssignedActiveTrainers(String traineeUsername) {
        securityService.requireSelfOrAdmin(traineeUsername, UserRole.TRAINEE);

        TraineeEntity trainee = traineeRepository.findByUserEntity_Username(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        List<UUID> assignedTrainerIds = trainee.getTrainers()
                .stream()
                .map(TrainerEntity::getId)
                .toList();

        return trainerRepository.findByUserIsActiveTrue()
                .stream()
                .filter(trainer -> !assignedTrainerIds.contains(trainer.getId()))
                .map(gymMapper::toTrainerShortResponse)
                .toList();
    }

    @Transactional
    public TrainerAssignmentResponseDTO updateTraineeTrainers(String traineeUsername, UpdateTraineeTrainersRequestDTO request) {
        securityService.requireSelfOrAdmin(traineeUsername, UserRole.TRAINEE);

        TraineeEntity trainee = traineeRepository.findByUserEntity_Username(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        List<String> trainerUsernames = request.getTrainers()
                .stream()
                .map(TrainerUsernameRequestDTO::getTrainerUsername)
                .toList();

        List<TrainerEntity> trainers = trainerRepository.findByUserUsernameIn(trainerUsernames);

        if (trainers.size() != trainerUsernames.size()) {
            throw new NotFoundException("One or more trainers were not found");
        }

        boolean allTrainersActive = trainers.stream()
                .allMatch(trainer -> Boolean.TRUE.equals(trainer.getUserEntity().getIsActive()));

        if (!allTrainersActive) {
            throw new BadRequestException("Only active trainers can be assigned to trainee");
        }

        trainee.setTrainers(trainers);

        TraineeEntity updatedTrainee = traineeRepository.save(trainee);

        List<TrainerShortResponseDTO> trainerResponses = updatedTrainee.getTrainers()
                .stream()
                .map(gymMapper::toTrainerShortResponse)
                .toList();

        traineeMetrics.getTraineeTrainerAssignmentUpdatedCounter().increment();
        log.info("Updated trainer assignments for trainee username={}", traineeUsername);
        return new TrainerAssignmentResponseDTO(trainerResponses);
    }

    private MessageResponseDTO changeTraineeStatus(String username, boolean active, String successMessage) {
        securityService.requireSelfOrAdmin(username, UserRole.TRAINEE);

        TraineeEntity trainee = traineeRepository.findByUserEntity_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        UserEntity user = trainee.getUserEntity();

        if (Boolean.TRUE.equals(user.getIsActive()) == active) {
            throw new BadRequestException("Trainee profile is already " + (active ? "active" : "inactive"));
        }

        user.setIsActive(active);
        userRepository.save(user);
        log.info("{} trainee profile for username={}", active ? "Activated" : "Deactivated", username);
        return new MessageResponseDTO(successMessage);
    }
}
