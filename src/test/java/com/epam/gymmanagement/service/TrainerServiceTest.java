package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.TrainerRegistrationRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTrainerProfileRequestDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GymMapper gymMapper;
    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void registerTrainerCreatesUserAndTrainerProfile() {
        TrainerRegistrationRequestDTO request = registrationRequest("Jane", "Trainer", "Yoga");
        TrainingTypeEntity specialization = ServiceTestFixtures.trainingType(TrainingType.YOGA);

        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.YOGA))
                .thenReturn(Optional.of(specialization));
        when(usernameGenerator.generate("Jane", "Trainer")).thenReturn("jane.trainer");
        when(passwordGenerator.generate()).thenReturn("RawPass1");
        when(passwordEncoder.encode("RawPass1")).thenReturn("encoded-pass");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationResponseDTO response = trainerService.registerTrainer(request);

        assertEquals("jane.trainer", response.getUsername());
        assertEquals("RawPass1", response.getPassword());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertEquals("Jane", savedUser.getFirstName());
        assertEquals("Trainer", savedUser.getLastName());
        assertEquals("jane.trainer", savedUser.getUsername());
        assertEquals("encoded-pass", savedUser.getPassword());
        assertTrue(savedUser.getIsActive());
        assertEquals(UserRole.TRAINER, savedUser.getRole());

        ArgumentCaptor<TrainerEntity> trainerCaptor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(trainerRepository).save(trainerCaptor.capture());
        assertEquals(savedUser, trainerCaptor.getValue().getUser());
        assertEquals(specialization, trainerCaptor.getValue().getSpecialization());
    }

    @Test
    void registerTrainerRejectsUnknownSpecialization() {
        TrainerRegistrationRequestDTO request = registrationRequest("Jane", "Trainer", "Boxing");

        assertThrows(BadRequestException.class, () -> trainerService.registerTrainer(request));

        verify(trainingTypeRepository, never()).findByTrainingTypeName(any());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerTrainerThrowsWhenTrainingTypeIsMissingFromRepository() {
        TrainerRegistrationRequestDTO request = registrationRequest("Jane", "Trainer", "Yoga");
        when(trainingTypeRepository.findByTrainingTypeName(TrainingType.YOGA)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.registerTrainer(request));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(trainerRepository, never()).save(any(TrainerEntity.class));
    }

    @Test
    void getTrainerProfileRequiresAccessAndMapsProfile() {
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.FITNESS);
        TrainerProfileResponseDTO mapped = new TrainerProfileResponseDTO();

        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(trainer));
        when(gymMapper.toTrainerProfileResponse(trainer)).thenReturn(mapped);

        TrainerProfileResponseDTO response = trainerService.getTrainerProfile("trainer.user");

        assertEquals(mapped, response);
        verify(securityService).requireSelfOrAdmin("trainer.user", UserRole.TRAINER);
    }

    @Test
    void updateTrainerProfilePersistsUserChangesAndReturnsMappedProfile() {
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.FITNESS);
        UpdateTrainerProfileRequestDTO request = updateRequest("Updated", "Trainer", false);
        TrainerProfileResponseDTO mapped = new TrainerProfileResponseDTO();

        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);
        when(gymMapper.toTrainerProfileResponse(trainer)).thenReturn(mapped);

        TrainerProfileResponseDTO response = trainerService.updateTrainerProfile("trainer.user", request);

        assertEquals(mapped, response);
        assertEquals("Updated", trainer.getUser().getFirstName());
        assertEquals("Trainer", trainer.getUser().getLastName());
        assertEquals(false, trainer.getUser().getIsActive());
        verify(securityService).requireSelfOrAdmin("trainer.user", UserRole.TRAINER);
        verify(userRepository).save(trainer.getUser());
    }

    @Test
    void updateTrainerProfileThrowsWhenTrainerDoesNotExist() {
        UpdateTrainerProfileRequestDTO request = updateRequest("Updated", "Trainer", true);
        when(trainerRepository.findByUserUsername("missing.trainer")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.updateTrainerProfile("missing.trainer", request));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void activateTrainerProfileChangesInactiveProfileToActive() {
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", false, TrainingType.FITNESS);
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(trainer));

        MessageResponseDTO response = trainerService.activateTrainerProfile("trainer.user");

        assertEquals("Trainer profile activated successfully", response.getMessage());
        assertTrue(trainer.getUser().getIsActive());
        verify(userRepository).save(trainer.getUser());
        verify(securityService).requireSelfOrAdmin("trainer.user", UserRole.TRAINER);
    }

    @Test
    void activateTrainerProfileThrowsWhenAlreadyActive() {
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.FITNESS);
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(trainer));

        assertThrows(BadRequestException.class, () -> trainerService.activateTrainerProfile("trainer.user"));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void deactivateTrainerProfileChangesActiveProfileToInactive() {
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.FITNESS);
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(trainer));

        MessageResponseDTO response = trainerService.deactivateTrainerProfile("trainer.user");

        assertEquals("Trainer profile deactivated successfully", response.getMessage());
        assertEquals(false, trainer.getUser().getIsActive());
        verify(userRepository).save(trainer.getUser());
    }

    @Test
    void deactivateTrainerProfileThrowsWhenAlreadyInactive() {
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", false, TrainingType.FITNESS);
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(trainer));

        assertThrows(BadRequestException.class, () -> trainerService.deactivateTrainerProfile("trainer.user"));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    private TrainerRegistrationRequestDTO registrationRequest(
            String firstName,
            String lastName,
            String specialization
    ) {
        TrainerRegistrationRequestDTO request = new TrainerRegistrationRequestDTO();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setSpecialization(specialization);
        return request;
    }

    private UpdateTrainerProfileRequestDTO updateRequest(String firstName, String lastName, Boolean active) {
        UpdateTrainerProfileRequestDTO request = new UpdateTrainerProfileRequestDTO();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setIsActive(active);
        return request;
    }
}
