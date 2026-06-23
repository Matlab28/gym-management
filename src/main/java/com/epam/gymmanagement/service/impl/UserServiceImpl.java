package com.epam.gymmanagement.service.impl;

import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.ProfileRequestDTO;
import com.epam.gymmanagement.dto.response.UserProfileResponseDTO;
import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.exception.NotFoundException;
import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainerRepository;
import com.epam.gymmanagement.repository.TrainingTypeRepository;
import com.epam.gymmanagement.repository.UserRepository;
import com.epam.gymmanagement.security.SecurityService;
import com.epam.gymmanagement.service.UserService;
import com.epam.gymmanagement.util.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UsernameGenerator usernameGenerator;
    private final SecurityService securityService;

    @Override
    public UserSummaryResponseDTO createUser(ProfileRequestDTO dto) {
        if (dto.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Cannot create user with ADMIN role");
        }

        if (dto.getRole() == null) {
            throw new BadRequestException("Role is required");
        }

        String currentUsername = securityService.currentUsername();
        UserEntity user = userRepository.findByUsernameIgnoreCaseContains(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getProfileStatus() != ProfileStatus.ACTIVE) {
            throw new BadRequestException("Please confirm your email before creating a profile");
        }

        ensureProfileNotCreated(user);

        String username = usernameGenerator.generate(dto.getFirstName(), dto.getLastName());
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setUsername(username);
        user.setRole(dto.getRole());
        user.setIsActive(true);
        user.setProfileStatus(ProfileStatus.ACTIVE);

        UserEntity savedUser = userRepository.save(user);
        String specialization = null;

        if (dto.getRole() == UserRole.TRAINER) {
            specialization = createTrainerProfile(savedUser, dto).getTrainingTypeName().getValue();
        } else if (dto.getRole() == UserRole.TRAINEE) {
            createTraineeProfile(savedUser, dto);
        } else {
            throw new BadRequestException("Unsupported profile role");
        }

        log.info("Created {} profile for user ID: {}", dto.getRole(), savedUser.getId());
        return toUserSummary(savedUser, specialization);
    }

    private void ensureProfileNotCreated(UserEntity user) {
        if (traineeRepository.existsByUserEntity_Username(user.getUsername())
                || trainerRepository.existsByUserEntity_Username(user.getUsername())) {
            throw new BadRequestException("User profile is already created");
        }
    }

    private void createTraineeProfile(UserEntity user, ProfileRequestDTO dto) {
        TraineeEntity trainee = TraineeEntity.builder()
                .userEntity(user)
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .build();

        traineeRepository.save(trainee);
    }

    private TrainingTypeEntity createTrainerProfile(UserEntity user, ProfileRequestDTO dto) {
        TrainingType specialization = dto.getSpecialization();

        if (specialization == null) {
            throw new BadRequestException("Specialization is required for trainer profile");
        }

        TrainingTypeEntity trainingType = trainingTypeRepository.findByTrainingTypeName(specialization)
                .orElseThrow(() -> new NotFoundException("Training type not found"));

        TrainerEntity trainer = TrainerEntity.builder()
                .userEntity(user)
                .specialization(trainingType)
                .build();

        trainerRepository.save(trainer);
        return trainingType;
    }

    private UserSummaryResponseDTO toUserSummary(UserEntity user, String specialization) {
        return UserSummaryResponseDTO
                .builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .active(user.getIsActive())
                .profileStatus(user.getProfileStatus())
                .role(user.getRole())
                .specialization(specialization)
                .build();
    }

    @Override
    public List<UserSummaryResponseDTO> getAllUsers() {
        log.info("Fetching all user profiles");
        return userRepository
                .findAll()
                .stream()
                .map(m -> modelMapper.map(m, UserSummaryResponseDTO.class))
                .toList();
    }

    @Override
    public List<UserProfileResponseDTO> getAllUserProfiles() {
        log.info("Fetching all user summaries");
        return userRepository
                .findAll()
                .stream()
                .map(m -> modelMapper.map(m, UserProfileResponseDTO.class))
                .toList();
    }

    @Override
    public UserProfileResponseDTO getUserProfile(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        log.info("Fetched user profile for ID: {}", userId);
        return modelMapper.map(user, UserProfileResponseDTO.class);
    }

    @Override
    public UserSummaryResponseDTO getUserById(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        log.info("Fetched user summary for ID: {}", userId);
        return modelMapper.map(user, UserSummaryResponseDTO.class);
    }

    @Override
    public UserProfileResponseDTO updateUserProfile(UUID userId, UserProfileResponseDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        modelMapper.map(dto, user);
        userRepository.save(user);
        log.info("Updated user profile for ID: {}", userId);
        return modelMapper.map(user, UserProfileResponseDTO.class);
    }

    @Override
    public String deleteUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        user.setProfileStatus(ProfileStatus.DELETED);
        userRepository.save(user);
        log.info("Deleted user with ID: {}", userId);
        return "User has been deleted successfully";
    }
}
