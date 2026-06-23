//package com.epam.gymmanagement.service;
//
//import com.epam.gymmanagement.constant.TrainingType;
//import com.epam.gymmanagement.constant.UserRole;
//import com.epam.gymmanagement.dto.request.TraineeRegistrationRequestDTO;
//import com.epam.gymmanagement.dto.request.UpdateTraineeProfileRequestDTO;
//import com.epam.gymmanagement.dto.request.UpdateTraineeTrainersRequestDTO;
//import com.epam.gymmanagement.dto.response.AuthResponseDTO;
//import com.epam.gymmanagement.dto.response.MessageResponseDTO;
//import com.epam.gymmanagement.dto.response.UserProfileResponseDTO;
//import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
//import com.epam.gymmanagement.entity.TraineeEntity;
//import com.epam.gymmanagement.entity.TrainerEntity;
//import com.epam.gymmanagement.entity.UserEntity;
//import com.epam.gymmanagement.exception.BadRequestException;
//import com.epam.gymmanagement.exception.NotFoundException;
//import com.epam.gymmanagement.metrics.TraineeMetrics;
//import com.epam.gymmanagement.repository.TraineeRepository;
//import com.epam.gymmanagement.repository.TrainerRepository;
//import com.epam.gymmanagement.repository.UserRepository;
//import com.epam.gymmanagement.security.SecurityService;
//import com.epam.gymmanagement.util.PasswordGenerator;
//import com.epam.gymmanagement.util.UsernameGenerator;
//import io.micrometer.core.instrument.Counter;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.modelmapper.ModelMapper;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TraineeServiceTest {
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private TraineeRepository traineeRepository;
//    @Mock
//    private TrainerRepository trainerRepository;
//    @Mock
//    private UsernameGenerator usernameGenerator;
//    @Mock
//    private PasswordGenerator passwordGenerator;
//    @Mock
//    private PasswordEncoder passwordEncoder;
//    @Mock
//    private GymMapper gymMapper;
//    @Mock
//    private SecurityService securityService;
//    @Mock
//    private ModelMapper modelMapper;
//    @Mock
//    private TraineeMetrics traineeMetrics;
//    @Mock
//    private Counter traineeMetricCounter;
//
//    @InjectMocks
//    private TraineeService traineeService;
//
//    @BeforeEach
//    void setUpMetrics() {
//        lenient().when(traineeMetrics.getTraineeCreatedCounter()).thenReturn(traineeMetricCounter);
//        lenient().when(traineeMetrics.getTraineeUpdatedCounter()).thenReturn(traineeMetricCounter);
//        lenient().when(traineeMetrics.getTraineeDeletedCounter()).thenReturn(traineeMetricCounter);
//        lenient().when(traineeMetrics.getTraineeActivatedCounter()).thenReturn(traineeMetricCounter);
//        lenient().when(traineeMetrics.getTraineeDeactivatedCounter()).thenReturn(traineeMetricCounter);
//        lenient().when(traineeMetrics.getTraineeTrainerAssignmentUpdatedCounter()).thenReturn(traineeMetricCounter);
//    }
//
//    @Test
//    void registerTraineeCreatesUserAndTraineeProfile() {
//        TraineeRegistrationRequestDTO request = registrationRequest();
//        UserEntity mappedUser = new UserEntity();
//        mappedUser.setFirstName("Tina");
//        mappedUser.setLastName("Trainee");
//
//        when(usernameGenerator.generate("Tina", "Trainee")).thenReturn("tina.trainee");
//        when(passwordGenerator.generate()).thenReturn("RawPass1");
//        when(modelMapper.map(request, UserEntity.class)).thenReturn(mappedUser);
//        when(passwordEncoder.encode("RawPass1")).thenReturn("encoded-pass");
//        when(userRepository.save(mappedUser)).thenReturn(mappedUser);
//
//        AuthResponseDTO response = traineeService.registerTrainee(request);
//
//        assertEquals("tina.trainee", response.getUsername());
//        assertEquals("RawPass1", response.getPassword());
//        assertEquals("tina.trainee", mappedUser.getUsername());
//        assertEquals("encoded-pass", mappedUser.getPassword());
//        assertTrue(mappedUser.getIsActive());
//        assertEquals(UserRole.TRAINEE, mappedUser.getRole());
//
//        ArgumentCaptor<TraineeEntity> traineeCaptor = ArgumentCaptor.forClass(TraineeEntity.class);
//        verify(traineeRepository).save(traineeCaptor.capture());
//        TraineeEntity savedTrainee = traineeCaptor.getValue();
//        assertEquals(mappedUser, savedTrainee.getUserEntity());
//        assertEquals(LocalDate.of(2000, 2, 3), savedTrainee.getDateOfBirth());
//        assertEquals("Main street", savedTrainee.getAddress());
//    }
//
//    @Test
//    void getTraineeProfileRequiresAccessAndMapsProfile() {
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
//        UserProfileResponseDTO mapped = new UserProfileResponseDTO();
//
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//        when(gymMapper.toTraineeProfileResponse(trainee)).thenReturn(mapped);
//
//        UserProfileResponseDTO response = traineeService.getTraineeProfile("trainee.user");
//
//        assertEquals(mapped, response);
//        verify(securityService).requireSelfOrAdmin("trainee.user", UserRole.TRAINEE);
//    }
//
//    @Test
//    void updateTraineeProfilePersistsUserAndTraineeChanges() {
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
//        UpdateTraineeProfileRequestDTO request = updateRequest();
//        UserProfileResponseDTO mapped = new UserProfileResponseDTO();
//
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//        when(traineeRepository.save(trainee)).thenReturn(trainee);
//        when(gymMapper.toTraineeProfileResponse(trainee)).thenReturn(mapped);
//
//        UserProfileResponseDTO response = traineeService.updateTraineeProfile("trainee.user", request);
//
//        assertEquals(mapped, response);
//        assertEquals("Updated", trainee.getUserEntity().getFirstName());
//        assertEquals("Trainee", trainee.getUserEntity().getLastName());
//        assertFalse(trainee.getUserEntity().getIsActive());
//        assertEquals(LocalDate.of(1999, 4, 5), trainee.getDateOfBirth());
//        assertEquals("Updated address", trainee.getAddress());
//        verify(userRepository).save(trainee.getUserEntity());
//        verify(traineeRepository).save(trainee);
//    }
//
//    @Test
//    void updateTraineeProfileThrowsWhenTraineeDoesNotExist() {
//        when(traineeRepository.findByUserEntity_Username("missing.trainee")).thenReturn(Optional.empty());
//
//        assertThrows(
//                NotFoundException.class,
//                () -> traineeService.updateTraineeProfile("missing.trainee", updateRequest())
//        );
//
//        verify(userRepository, never()).save(any(UserEntity.class));
//    }
//
//    @Test
//    void deleteTraineeProfileHardDeletesTraineeAndUser() {
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//
//        MessageResponseDTO response = traineeService.deleteTraineeProfile("trainee.user");
//
//        assertEquals("Trainee profile deleted successfully", response.getMessage());
//        verify(traineeRepository).delete(trainee);
//        verify(userRepository).delete(trainee.getUserEntity());
//    }
//
//    @Test
//    void activateTraineeProfileChangesInactiveProfileToActive() {
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", false);
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//
//        MessageResponseDTO response = traineeService.activateTraineeProfile("trainee.user");
//
//        assertEquals("Trainee profile activated successfully", response.getMessage());
//        assertTrue(trainee.getUserEntity().getIsActive());
//        verify(userRepository).save(trainee.getUserEntity());
//        verify(securityService).requireSelfOrAdmin("trainee.user", UserRole.TRAINEE);
//    }
//
//    @Test
//    void activateTraineeProfileThrowsWhenAlreadyActive() {
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//
//        assertThrows(BadRequestException.class, () -> traineeService.activateTraineeProfile("trainee.user"));
//
//        verify(userRepository, never()).save(any(UserEntity.class));
//    }
//
//    @Test
//    void deactivateTraineeProfileChangesActiveProfileToInactive() {
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//
//        MessageResponseDTO response = traineeService.deactivateTraineeProfile("trainee.user");
//
//        assertEquals("Trainee profile deactivated successfully", response.getMessage());
//        assertFalse(trainee.getUserEntity().getIsActive());
//        verify(userRepository).save(trainee.getUserEntity());
//    }
//
//    @Test
//    void deactivateTraineeProfileThrowsWhenAlreadyInactive() {
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", false);
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//
//        assertThrows(BadRequestException.class, () -> traineeService.deactivateTraineeProfile("trainee.user"));
//
//        verify(userRepository, never()).save(any(UserEntity.class));
//    }
//
//    @Test
//    void getNotAssignedActiveTrainersFiltersAssignedTrainerAndMapsRemainingTrainers() {
//        TrainerEntity assignedTrainer = ServiceTestFixtures.trainer("assigned.trainer", true, TrainingType.YOGA);
//        TrainerEntity availableTrainer = ServiceTestFixtures.trainer("available.trainer", true, TrainingType.FITNESS);
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true, assignedTrainer);
//        UserSummaryResponseDTO mappedAvailableTrainer = new UserSummaryResponseDTO();
//
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//        when(trainerRepository.findByUserEntity_IsActiveTrue()).thenReturn(List.of(assignedTrainer, availableTrainer));
//        when(gymMapper.toTrainerSummaryResponse(availableTrainer)).thenReturn(mappedAvailableTrainer);
//
//        List<UserSummaryResponseDTO> response = traineeService.getNotAssignedActiveTrainers("trainee.user");
//
//        assertEquals(List.of(mappedAvailableTrainer), response);
//        verify(securityService).requireSelfOrAdmin("trainee.user", UserRole.TRAINEE);
//        verify(gymMapper, never()).toTrainerSummaryResponse(assignedTrainer);
//    }
//
//    @Test
//    void updateTraineeTrainersReplacesAssignmentsWithActiveTrainers() {
//        TrainerEntity firstTrainer = ServiceTestFixtures.trainer("first.trainer", true, TrainingType.YOGA);
//        TrainerEntity secondTrainer = ServiceTestFixtures.trainer("second.trainer", true, TrainingType.FITNESS);
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
//        UpdateTraineeTrainersRequestDTO request = trainerAssignmentRequest("first.trainer", "second.trainer");
//        UserSummaryResponseDTO firstResponse = new UserSummaryResponseDTO();
//        UserSummaryResponseDTO secondResponse = new UserSummaryResponseDTO();
//
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//        when(trainerRepository.findByUserEntity_UsernameIn(List.of("first.trainer", "second.trainer")))
//                .thenReturn(List.of(firstTrainer, secondTrainer));
//        when(traineeRepository.save(trainee)).thenReturn(trainee);
//        when(gymMapper.toTrainerSummaryResponse(firstTrainer)).thenReturn(firstResponse);
//        when(gymMapper.toTrainerSummaryResponse(secondTrainer)).thenReturn(secondResponse);
//
//        List<UserSummaryResponseDTO> response = traineeService.updateTraineeTrainers("trainee.user", request);
//
//        assertEquals(List.of(firstTrainer, secondTrainer), trainee.getTrainers());
//        assertEquals(List.of(firstResponse, secondResponse), response);
//        verify(securityService).requireSelfOrAdmin("trainee.user", UserRole.TRAINEE);
//    }
//
//    @Test
//    void updateTraineeTrainersThrowsWhenAnyTrainerIsMissing() {
//        TrainerEntity firstTrainer = ServiceTestFixtures.trainer("first.trainer", true, TrainingType.YOGA);
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
//        UpdateTraineeTrainersRequestDTO request = trainerAssignmentRequest("first.trainer", "missing.trainer");
//
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//        when(trainerRepository.findByUserEntity_UsernameIn(List.of("first.trainer", "missing.trainer")))
//                .thenReturn(List.of(firstTrainer));
//
//        assertThrows(
//                NotFoundException.class,
//                () -> traineeService.updateTraineeTrainers("trainee.user", request)
//        );
//
//        verify(traineeRepository, never()).save(any(TraineeEntity.class));
//    }
//
//    @Test
//    void updateTraineeTrainersThrowsWhenAnyTrainerIsInactive() {
//        TrainerEntity activeTrainer = ServiceTestFixtures.trainer("active.trainer", true, TrainingType.YOGA);
//        TrainerEntity inactiveTrainer = ServiceTestFixtures.trainer("inactive.trainer", false, TrainingType.FITNESS);
//        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
//        UpdateTraineeTrainersRequestDTO request = trainerAssignmentRequest("active.trainer", "inactive.trainer");
//
//        when(traineeRepository.findByUserEntity_Username("trainee.user")).thenReturn(Optional.of(trainee));
//        when(trainerRepository.findByUserEntity_UsernameIn(List.of("active.trainer", "inactive.trainer")))
//                .thenReturn(List.of(activeTrainer, inactiveTrainer));
//
//        assertThrows(
//                BadRequestException.class,
//                () -> traineeService.updateTraineeTrainers("trainee.user", request)
//        );
//
//        verify(traineeRepository, never()).save(any(TraineeEntity.class));
//    }
//
//    private TraineeRegistrationRequestDTO registrationRequest() {
//        return TraineeRegistrationRequestDTO.builder()
//                .firstName("Tina")
//                .lastName("Trainee")
//                .dateOfBirth(LocalDate.of(2000, 2, 3))
//                .address("Main street")
//                .build();
//    }
//
//    private UpdateTraineeProfileRequestDTO updateRequest() {
//        return UpdateTraineeProfileRequestDTO.builder()
//                .firstName("Updated")
//                .lastName("Trainee")
//                .dateOfBirth(LocalDate.of(1999, 4, 5))
//                .address("Updated address")
//                .isActive(false)
//                .build();
//    }
//
//    private UpdateTraineeTrainersRequestDTO trainerAssignmentRequest(String... usernames) {
//        UpdateTraineeTrainersRequestDTO request = new UpdateTraineeTrainersRequestDTO();
//        request.setTrainers(List.of(usernames));
//        return request;
//    }
//}
