package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.AdminRegistrationRequestDTO;
import com.epam.gymmanagement.dto.response.AdminDashboardResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainerRepository;
import com.epam.gymmanagement.repository.TrainingRepository;
import com.epam.gymmanagement.repository.UserRepository;
import com.epam.gymmanagement.security.SecurityService;
import com.epam.gymmanagement.security.UserRoleResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserRoleResolver userRoleResolver;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AdminService adminService;

    @Test
    void registerAdminCreatesFirstAdminWithoutExistingAdminAuthorization() {
        AdminRegistrationRequestDTO request = adminRequest("root.admin", "RawPass1");

        UserEntity mappedUser = UserEntity.builder()
                .username("root.admin")
                .firstName("Root")
                .lastName("Admin")
                .build();

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsername("root.admin")).thenReturn(false);
        when(modelMapper.map(request, UserEntity.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode("RawPass1")).thenReturn("encoded-pass");

        MessageResponseDTO response = adminService.registerAdmin(request);

        assertEquals("Admin registered successfully", response.getMessage());
        assertEquals("encoded-pass", mappedUser.getPassword());
        assertTrue(mappedUser.getIsActive());
        assertEquals(UserRole.ADMIN, mappedUser.getRole());
        verify(securityService, never()).requireRole(UserRole.ADMIN);
        verify(userRepository).save(mappedUser);
    }

    @Test
    void registerAdminRequiresAdminWhenAnAdminAlreadyExists() {
        AdminRegistrationRequestDTO request = adminRequest("second.admin", "RawPass1");
        UserEntity mappedUser = UserEntity.builder()
                .username("second.admin")
                .build();

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.existsByUsername("second.admin")).thenReturn(false);
        when(modelMapper.map(request, UserEntity.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode("RawPass1")).thenReturn("encoded-pass");

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.existsByUsername("second.admin")).thenReturn(false);
        when(modelMapper.map(request, UserEntity.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode("RawPass1")).thenReturn("encoded-pass");

        adminService.registerAdmin(request);

        verify(securityService).requireRole(UserRole.ADMIN);
        verify(userRepository).save(mappedUser);
    }

    @Test
    void registerAdminRejectsDuplicateUsername() {
        AdminRegistrationRequestDTO request = adminRequest("taken.admin", "RawPass1");

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.existsByUsername("taken.admin")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> adminService.registerAdmin(request));

        verify(securityService).requireRole(UserRole.ADMIN);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void dashboardReturnsCountsAndSortedUserSummaries() {
        UserEntity admin = ServiceTestFixtures.user("admin.user", UserRole.ADMIN, true);
        UserEntity trainer = ServiceTestFixtures.user("trainer.user", UserRole.TRAINER, false);
        UserSummaryResponseDTO adminSummary = new UserSummaryResponseDTO();
        UserSummaryResponseDTO trainerSummary = new UserSummaryResponseDTO();

        when(userRepository.count()).thenReturn(3L);
        when(userRepository.countByIsActive(true)).thenReturn(2L);
        when(userRepository.countByIsActive(false)).thenReturn(1L);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);
        when(traineeRepository.count()).thenReturn(1L);
        when(trainerRepository.count()).thenReturn(1L);
        when(trainingRepository.count()).thenReturn(4L);
        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(admin, trainer));
        when(modelMapper.map(admin, UserSummaryResponseDTO.class)).thenReturn(adminSummary);
        when(modelMapper.map(trainer, UserSummaryResponseDTO.class)).thenReturn(trainerSummary);
        when(userRoleResolver.resolve(admin)).thenReturn(UserRole.ADMIN);
        when(userRoleResolver.resolve(trainer)).thenReturn(UserRole.TRAINER);

        AdminDashboardResponseDTO response = adminService.dashboard();

        assertEquals(3L, response.getTotalUsers());
        assertEquals(2L, response.getActiveUsers());
        assertEquals(1L, response.getInactiveUsers());
        assertEquals(1L, response.getAdmins());
        assertEquals(1L, response.getTrainees());
        assertEquals(1L, response.getTrainers());
        assertEquals(4L, response.getTrainings());
        assertEquals(List.of(adminSummary, trainerSummary), response.getUsers());
        assertTrue(adminSummary.getActive());
        assertEquals(UserRole.ADMIN, adminSummary.getRole());
        assertEquals(false, trainerSummary.getActive());
        assertEquals(UserRole.TRAINER, trainerSummary.getRole());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(userRepository).findAll(sortCaptor.capture());
        assertEquals(Sort.by("username"), sortCaptor.getValue());
        verify(securityService).requireRole(UserRole.ADMIN);
    }

    private AdminRegistrationRequestDTO adminRequest(String username, String password) {
        AdminRegistrationRequestDTO request = new AdminRegistrationRequestDTO();
        request.setFirstName("Root");
        request.setLastName("Admin");
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
