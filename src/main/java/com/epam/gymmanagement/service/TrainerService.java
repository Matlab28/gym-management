package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.TrainerRegistrationRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTrainerProfileRequestDTO;
import com.epam.gymmanagement.dto.response.RegistrationResponseDTO;
import com.epam.gymmanagement.dto.response.TrainerProfileResponseDTO;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.exception.NotFoundException;
import com.epam.gymmanagement.repository.TrainerRepository;
import com.epam.gymmanagement.repository.TrainingTypeRepository;
import com.epam.gymmanagement.repository.UserRepository;
import com.epam.gymmanagement.security.SecurityService;
import com.epam.gymmanagement.util.PasswordGenerator;
import com.epam.gymmanagement.util.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerService {
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final GymMapper gymMapper;
    private final SecurityService securityService;

    @Transactional
    public RegistrationResponseDTO registerTrainer(TrainerRegistrationRequestDTO request) {
        TrainingType trainingType = parseTrainingType(request.getSpecialization());
        TrainingTypeEntity specialization = trainingTypeRepository
                .findByTrainingTypeName(trainingType)
                .orElseThrow(() -> new NotFoundException("Training type not found"));

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
        user.setRole(UserRole.TRAINER);

        UserEntity savedUser = userRepository.save(user);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(savedUser);
        trainer.setSpecialization(specialization);

        trainerRepository.save(trainer);

        log.info("Registered trainer profile for username={}", username);

        return new RegistrationResponseDTO(username, rawPassword);
    }

    @Transactional(readOnly = true)
    public TrainerProfileResponseDTO getTrainerProfile(String username) {
        securityService.requireSelfOrAdmin(username, UserRole.TRAINER);

        TrainerEntity trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        return gymMapper.toTrainerProfileResponse(trainer);
    }

    @Transactional
    public TrainerProfileResponseDTO updateTrainerProfile(
            String username,
            UpdateTrainerProfileRequestDTO request
    ) {
        securityService.requireSelfOrAdmin(username, UserRole.TRAINER);

        TrainerEntity trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        UserEntity user = trainer.getUser();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(request.getIsActive());

        userRepository.save(user);

        TrainerEntity updatedTrainer = trainerRepository.save(trainer);

        log.info("Updated trainer profile for username={}", username);

        return gymMapper.toTrainerProfileResponse(updatedTrainer);
    }

    private TrainingType parseTrainingType(String trainingType) {
        try {
            return TrainingType.fromValue(trainingType);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }
}
