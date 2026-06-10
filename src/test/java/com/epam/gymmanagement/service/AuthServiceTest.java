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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserRoleResolver userRoleResolver;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsTokenAndResolvedRoleForActiveUserWithMatchingPassword() {
        LoginRequestDTO request = loginRequest("trainer.user", "secret");
        UserEntity user = ServiceTestFixtures.user("trainer.user", UserRole.TRAINER, true);

        when(userRepository.findByUsernameIgnoreCaseContains("trainer.user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken("trainer.user")).thenReturn("jwt-token");
        when(userRoleResolver.resolve(user)).thenReturn(UserRole.TRAINER);

        LoginResponseDTO response = authService.login(request);

        assertEquals("Login successful", response.getMessage());
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("TRAINER", response.getRole());
    }

    @Test
    void loginThrowsWhenUserDoesNotExist() {
        LoginRequestDTO request = loginRequest("missing.user", "secret");
        when(userRepository.findByUsernameIgnoreCaseContains("missing.user")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(request));

        verifyNoInteractions(passwordEncoder, jwtService, userRoleResolver);
    }

    @Test
    void loginThrowsWhenProfileIsInactive() {
        LoginRequestDTO request = loginRequest("inactive.user", "secret");
        UserEntity user = ServiceTestFixtures.user("inactive.user", UserRole.TRAINEE, false);
        when(userRepository.findByUsernameIgnoreCaseContains("inactive.user")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.login(request));

        verifyNoInteractions(passwordEncoder, jwtService, userRoleResolver);
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        LoginRequestDTO request = loginRequest("trainee.user", "wrong");
        UserEntity user = ServiceTestFixtures.user("trainee.user", UserRole.TRAINEE, true);

        when(userRepository.findByUsernameIgnoreCaseContains("trainee.user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken("trainee.user");
    }

    @Test
    void changePasswordSavesEncodedNewPasswordWhenOldPasswordMatches() {
        ChangePasswordRequestDTO request = changePasswordRequest("trainee.user", "old", "newStrong1");
        UserEntity user = ServiceTestFixtures.user("trainee.user", UserRole.TRAINEE, true);

        when(userRepository.findByUsernameIgnoreCaseContains("trainee.user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newStrong1")).thenReturn("encoded-new");

        MessageResponseDTO response = authService.changePassword(request);

        assertEquals("Password changed successfully", response.getMessage());
        assertEquals("encoded-new", user.getPassword());
        verify(securityService).requireSameUserOrAdmin("trainee.user");
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordThrowsWhenUserDoesNotExist() {
        ChangePasswordRequestDTO request = changePasswordRequest("missing.user", "old", "newStrong1");
        when(userRepository.findByUsernameIgnoreCaseContains("missing.user")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.changePassword(request));

        verify(securityService).requireSameUserOrAdmin("missing.user");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void changePasswordThrowsWhenOldPasswordDoesNotMatch() {
        ChangePasswordRequestDTO request = changePasswordRequest("trainee.user", "wrong", "newStrong1");
        UserEntity user = ServiceTestFixtures.user("trainee.user", UserRole.TRAINEE, true);

        when(userRepository.findByUsernameIgnoreCaseContains("trainee.user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.changePassword(request));

        verify(passwordEncoder, never()).encode("newStrong1");
        verify(userRepository, never()).save(user);
    }

    @Test
    void currentSessionReturnsCurrentUsernameAndResolvedRole() {
        UserEntity user = ServiceTestFixtures.user("admin.user", UserRole.ADMIN, true);

        when(securityService.currentUsername()).thenReturn("admin.user");
        when(userRepository.findByUsernameIgnoreCaseContains("admin.user")).thenReturn(Optional.of(user));
        when(userRoleResolver.resolve(user)).thenReturn(UserRole.ADMIN);

        SessionResponseDTO response = authService.currentSession();

        assertEquals("admin.user", response.getUsername());
        assertEquals("ADMIN", response.getRole());
    }

    private LoginRequestDTO loginRequest(String username, String password) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private ChangePasswordRequestDTO changePasswordRequest(
            String username,
            String oldPassword,
            String newPassword
    ) {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername(username);
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);
        return request;
    }
}
