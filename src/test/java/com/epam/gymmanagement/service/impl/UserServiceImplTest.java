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
import com.epam.gymmanagement.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private SecurityService securityService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserRejectsAdminRole() {
        ProfileRequestDTO request = profileRequest(UserRole.ADMIN);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createUserCompletesConfirmedTraineeAccount() {
        ProfileRequestDTO request = profileRequest(UserRole.TRAINEE);
        request.setSpecialization(null);
        UserEntity user = user("pending.user@gmail.com");
        ArgumentCaptor<TraineeEntity> traineeCaptor = ArgumentCaptor.forClass(TraineeEntity.class);

        when(securityService.currentUsername()).thenReturn("pending.user@gmail.com");
        when(userRepository.findByUsernameIgnoreCaseContains("pending.user@gmail.com"))
                .thenReturn(Optional.of(user));
        when(usernameGenerator.generate("First", "Last")).thenReturn("first.last");
        when(userRepository.save(user)).thenReturn(user);

        UserSummaryResponseDTO response = userService.createUser(request);

        assertEquals("first.last", user.getUsername());
        assertEquals("First", user.getFirstName());
        assertEquals("Last", user.getLastName());
        assertEquals(UserRole.TRAINEE, user.getRole());
        assertEquals(ProfileStatus.ACTIVE, user.getProfileStatus());
        assertEquals("first.last", response.getUsername());
        assertEquals(UserRole.TRAINEE, response.getRole());
        assertNull(response.getSpecialization());

        verify(traineeRepository).save(traineeCaptor.capture());
        assertSame(user, traineeCaptor.getValue().getUserEntity());
        assertEquals(LocalDate.of(2000, 1, 1), traineeCaptor.getValue().getDateOfBirth());
        assertEquals("Main street", traineeCaptor.getValue().getAddress());
        verify(trainerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createUserCompletesConfirmedTrainerAccount() {
        ProfileRequestDTO request = profileRequest(UserRole.TRAINER);
        request.setSpecialization(TrainingType.YOGA);
        UserEntity user = user("trainer.user@gmail.com");
        TrainingTypeEntity specialization = TrainingTypeEntity.builder()
                .id(UUID.randomUUID())
                .trainingTypeName(TrainingType.YOGA)
                .build();
        ArgumentCaptor<TrainerEntity> trainerCaptor = ArgumentCaptor.forClass(TrainerEntity.class);

        when(securityService.currentUsername()).thenReturn("trainer.user@gmail.com");
        when(userRepository.findByUsernameIgnoreCaseContains("trainer.user@gmail.com"))
                .thenReturn(Optional.of(user));
        when(usernameGenerator.generate("First", "Last")).thenReturn("first.last");
        when(userRepository.save(user)).thenReturn(user);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.YOGA))
                .thenReturn(Optional.of(specialization));

        UserSummaryResponseDTO response = userService.createUser(request);

        assertEquals("first.last", response.getUsername());
        assertEquals(UserRole.TRAINER, response.getRole());
        assertEquals("Yoga", response.getSpecialization());

        verify(trainerRepository).save(trainerCaptor.capture());
        assertSame(user, trainerCaptor.getValue().getUserEntity());
        assertSame(specialization, trainerCaptor.getValue().getSpecialization());
        verify(traineeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createUserRejectsUnconfirmedAccount() {
        ProfileRequestDTO request = profileRequest(UserRole.TRAINEE);
        UserEntity user = user("pending.user@gmail.com");
        user.setProfileStatus(ProfileStatus.PENDING);

        when(securityService.currentUsername()).thenReturn("pending.user@gmail.com");
        when(userRepository.findByUsernameIgnoreCaseContains("pending.user@gmail.com"))
                .thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.createUser(request));

        verify(userRepository, never()).save(user);
    }

    @Test
    void createUserRejectsAlreadyCreatedProfile() {
        ProfileRequestDTO request = profileRequest(UserRole.TRAINEE);
        UserEntity user = user("profile.user@gmail.com");

        when(securityService.currentUsername()).thenReturn("profile.user@gmail.com");
        when(userRepository.findByUsernameIgnoreCaseContains("profile.user@gmail.com"))
                .thenReturn(Optional.of(user));
        when(traineeRepository.existsByUserEntity_Username("profile.user@gmail.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.createUser(request));

        verify(usernameGenerator, never()).generate(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void createUserRequiresTrainerSpecialization() {
        ProfileRequestDTO request = profileRequest(UserRole.TRAINER);
        request.setSpecialization(null);
        UserEntity user = user("trainer.user@gmail.com");

        when(securityService.currentUsername()).thenReturn("trainer.user@gmail.com");
        when(userRepository.findByUsernameIgnoreCaseContains("trainer.user@gmail.com"))
                .thenReturn(Optional.of(user));
        when(usernameGenerator.generate("First", "Last")).thenReturn("first.last");
        when(userRepository.save(user)).thenReturn(user);

        assertThrows(BadRequestException.class, () -> userService.createUser(request));

        verify(trainerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getAllUsersMapsSummaries() {
        UserEntity first = user("first.user");
        UserEntity second = user("second.user");
        UserSummaryResponseDTO firstResponse = new UserSummaryResponseDTO();
        UserSummaryResponseDTO secondResponse = new UserSummaryResponseDTO();

        when(userRepository.findAll()).thenReturn(List.of(first, second));
        when(modelMapper.map(first, UserSummaryResponseDTO.class)).thenReturn(firstResponse);
        when(modelMapper.map(second, UserSummaryResponseDTO.class)).thenReturn(secondResponse);

        assertEquals(List.of(firstResponse, secondResponse), userService.getAllUsers());
    }

    @Test
    void getAllUserProfilesMapsProfiles() {
        UserEntity user = user("profile.user");
        UserProfileResponseDTO response = new UserProfileResponseDTO();

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(modelMapper.map(user, UserProfileResponseDTO.class)).thenReturn(response);

        assertEquals(List.of(response), userService.getAllUserProfiles());
    }

    @Test
    void getUserProfileAndSummaryReturnMappedResponses() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user("lookup.user");
        UserProfileResponseDTO profile = new UserProfileResponseDTO();
        UserSummaryResponseDTO summary = new UserSummaryResponseDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserProfileResponseDTO.class)).thenReturn(profile);
        when(modelMapper.map(user, UserSummaryResponseDTO.class)).thenReturn(summary);

        assertEquals(profile, userService.getUserProfile(userId));
        assertEquals(summary, userService.getUserById(userId));
    }

    @Test
    void updateUserProfileMapsIntoExistingUserAndReturnsProfile() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user("update.user");
        UserProfileResponseDTO request = new UserProfileResponseDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        assertNull(userService.updateUserProfile(userId, request));

        verify(modelMapper).map(request, user);
        verify(modelMapper).map(user, UserProfileResponseDTO.class);
    }

    @Test
    void deleteUserMarksProfileDeleted() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user("delete.user");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertEquals("User has been deleted successfully", userService.deleteUser(userId));
        assertEquals(ProfileStatus.DELETED, user.getProfileStatus());
        verify(userRepository).save(user);
    }

    @Test
    void getUserProfileThrowsWhenMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserProfile(userId));
    }

    private ProfileRequestDTO profileRequest(UserRole role) {
        ProfileRequestDTO request = new ProfileRequestDTO();
        request.setRole(role);
        request.setFirstName("First");
        request.setLastName("Last");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("Main street");
        request.setSpecialization(TrainingType.FITNESS);
        return request;
    }

    private UserEntity user(String username) {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .email(username + "@example.com")
                .username(username)
                .firstName("")
                .lastName("")
                .password("encoded-password")
                .isActive(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .role(UserRole.TRAINEE)
                .build();
    }
}
