package com.epam.gymmanagement.service;

import com.epam.gymmanagement.dto.request.TraineeRegistrationRequestDTO;
import com.epam.gymmanagement.dto.request.TrainerUsernameRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTraineeProfileRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTraineeTrainersRequestDTO;
import com.epam.gymmanagement.dto.response.*;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.NotFoundException;
import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainerRepository;
import com.epam.gymmanagement.repository.UserRepository;
import com.epam.gymmanagement.util.PasswordGenerator;
import com.epam.gymmanagement.util.UsernameGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TraineeService {
    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final GymMapper gymMapper;

    @Transactional
    public RegistrationResponseDTO registerTrainee(TraineeRegistrationRequestDTO request) {
        String username = usernameGenerator.generate(
                request.getFirstName(),
                request.getLastName()
        );

        String rawPassword = passwordGenerator.generate();

        UserEntity user = new UserEntity();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setIsActive(true);

        UserEntity savedUser = userRepository.save(user);

        TraineeEntity trainee = new TraineeEntity();
        trainee.setUserEntity(savedUser);
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        traineeRepository.save(trainee);

        return new RegistrationResponseDTO(username, rawPassword);
    }

    public TraineeProfileResponseDTO getTraineeProfile(String username) {
        TraineeEntity trainee = traineeRepository.findByUserEntity_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        return gymMapper.toTraineeProfileResponse(trainee);
    }

    @Transactional
    public TraineeProfileResponseDTO updateTraineeProfile(
            String username,
            UpdateTraineeProfileRequestDTO request
    ) {
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

        return gymMapper.toTraineeProfileResponse(updatedTrainee);
    }

    @Transactional
    public MessageResponseDTO deleteTraineeProfile(String username) {
        TraineeEntity trainee = traineeRepository.findByUserEntity_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        UserEntity user = trainee.getUserEntity();

        /**
         * Soft delete:
         * We do not remove the record from DB.
         * We only deactivate the profile.
         */
        user.setIsActive(false);
        userRepository.save(user);

        return new MessageResponseDTO("Trainee profile deactivated successfully");
    }

    public List<TrainerShortResponseDTO> getNotAssignedActiveTrainers(String traineeUsername) {
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
    public TrainerAssignmentResponseDTO updateTraineeTrainers(
            String traineeUsername,
            UpdateTraineeTrainersRequestDTO request
    ) {
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

        trainee.setTrainers(trainers);

        TraineeEntity updatedTrainee = traineeRepository.save(trainee);

        List<TrainerShortResponseDTO> trainerResponses = updatedTrainee.getTrainers()
                .stream()
                .map(gymMapper::toTrainerShortResponse)
                .toList();

        return new TrainerAssignmentResponseDTO(trainerResponses);
    }
}