package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.LoginRequestDTO;
import com.epam.gymmanagement.dto.request.update.ChangePasswordRequestDTO;
import com.epam.gymmanagement.dto.response.LoginResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.SessionResponseDTO;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.exception.NotFoundException;
import com.epam.gymmanagement.repository.UserRepository;
import com.epam.gymmanagement.security.JwtService;
import com.epam.gymmanagement.security.SecurityService;
import com.epam.gymmanagement.security.UserRoleResolver;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityService securityService;
    private final UserRoleResolver userRoleResolver;

    public LoginResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("User profile is inactive");
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new BadRequestException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        UserRole role = userRoleResolver.resolve(user);
        log.info("{} ID of user {} logged in with role {}", user.getId(), user.getUsername(), role);
        return new LoginResponseDTO("Login successful", token, role.name());
    }

    @Transactional
    public MessageResponseDTO changePassword(ChangePasswordRequestDTO request) {
        securityService.requireSameUserOrAdmin(request.getUsername());

        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Attempt to change password for inactive user: {}", user.getId());
            throw new BadRequestException("User profile is inactive");
        }

        boolean oldPasswordMatches = passwordEncoder.matches(
                request.getOldPassword(),
                user.getPassword()
        );

        if (!oldPasswordMatches) {
            log.warn("\"{}\" ID of user's old password doesn't match ", user.getId());
            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("{} ID of user password changed for username", user.getPassword());
        return new MessageResponseDTO("Password changed successfully");
    }

    @Transactional(readOnly = true)
    public SessionResponseDTO currentSession() {
        String username = securityService.currentUsername();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return new SessionResponseDTO(username, userRoleResolver.resolve(user).name());
    }
}
