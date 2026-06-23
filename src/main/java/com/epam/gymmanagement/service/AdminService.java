package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.ProfileStatus;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;
    private final UserRoleResolver userRoleResolver;
    private final ModelMapper modelMapper;

    @Transactional
    public MessageResponseDTO registerAdmin(AdminRegistrationRequestDTO request) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            securityService.requireRole(UserRole.ADMIN);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        UserEntity user = modelMapper.map(request, UserEntity.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setProfileStatus(ProfileStatus.ACTIVE);
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        return new MessageResponseDTO("Admin registered successfully");
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponseDTO dashboard() {
        securityService.requireRole(UserRole.ADMIN);

        List<UserSummaryResponseDTO> users = userRepository.findAll(Sort.by("username"))
                .stream()
                .map(this::toUserSummary)
                .toList();

        return new AdminDashboardResponseDTO(
                userRepository.count(),
                userRepository.countByIsActive(true),
                userRepository.countByIsActive(false),
                userRepository.countByRole(UserRole.ADMIN),
                traineeRepository.count(),
                trainerRepository.count(),
                trainingRepository.count(),
                users
        );
    }

    private UserSummaryResponseDTO toUserSummary(UserEntity user) {
        UserSummaryResponseDTO response = modelMapper.map(user, UserSummaryResponseDTO.class);

        response.setActive(user.getIsActive());
        response.setRole(userRoleResolver.resolve(user));

        return response;
    }
}
