package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.ChangePasswordRequestDTO;
import com.epam.gymmanagement.dto.request.ConfirmRequestDto;
import com.epam.gymmanagement.dto.request.LoginRequestDTO;
import com.epam.gymmanagement.dto.request.RegisterRequestDTO;
import com.epam.gymmanagement.dto.request.ResendConfirmationRequestDTO;
import com.epam.gymmanagement.dto.response.AuthResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.exception.NotFoundException;
import com.epam.gymmanagement.repository.UserRepository;
import com.epam.gymmanagement.security.JwtService;
import com.epam.gymmanagement.security.SecurityService;
import com.epam.gymmanagement.security.UserRoleResolver;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
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
    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesPendingUserAndSendsConfirmationEmail() {
        RegisterRequestDTO request = registerRequest("New.User@Gmail.com", "Strong1", "Strong1");
        MimeMessage message = new MimeMessage((Session) null);

        when(userRepository.findByEmailIgnoreCase("new.user@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        ReflectionTestUtils.setField(authService, "mailFrom", "no-reply@gym.test");

        AuthResponseDTO response = authService.register(request);

        assertEquals("Registration successful. Please confirm your email.", response.getMessage());
        assertNull(response.getToken());
        assertNull(response.getTokenType());
        assertNull(response.getRole());

        verify(userRepository).save(any(UserEntity.class));
        verify(javaMailSender).send(same(message));
    }

    @Test
    void registerRefreshesPendingUserAndResendsConfirmationEmail() {
        RegisterRequestDTO request = registerRequest("pending.user@gmail.com", "Fresh1", "Fresh1");
        UserEntity pendingUser = ServiceTestFixtures.user("pending.user@gmail.com", UserRole.TRAINEE, false);
        pendingUser.setEmail("pending.user@gmail.com");
        pendingUser.setProfileStatus(ProfileStatus.PENDING);
        pendingUser.setConfirmationCode("old-code");
        pendingUser.setConfirmationCodeExpiresAt(Instant.now().minusSeconds(60));
        MimeMessage message = new MimeMessage((Session) null);

        when(userRepository.findByEmailIgnoreCase("pending.user@gmail.com")).thenReturn(Optional.of(pendingUser));
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        ReflectionTestUtils.setField(authService, "mailFrom", "no-reply@gym.test");

        AuthResponseDTO response = authService.register(request);

        assertEquals("Registration successful. Please confirm your email.", response.getMessage());
        assertEquals("encoded-Fresh1", pendingUser.getPassword());
        assertEquals(ProfileStatus.PENDING, pendingUser.getProfileStatus());
        assertTrue(pendingUser.getConfirmationCode().startsWith("encoded-"));
        assertTrue(pendingUser.getConfirmationCodeExpiresAt().isAfter(Instant.now()));
        assertNull(response.getToken());
        verify(userRepository).save(pendingUser);
        verify(javaMailSender).send(same(message));
    }

    @Test
    void registerRejectsAlreadyActiveEmail() {
        RegisterRequestDTO request = registerRequest("active.user@gmail.com", "Strong1", "Strong1");
        UserEntity activeUser = ServiceTestFixtures.user("active.user@gmail.com", UserRole.TRAINEE, true);
        activeUser.setEmail("active.user@gmail.com");

        when(userRepository.findByEmailIgnoreCase("active.user@gmail.com")).thenReturn(Optional.of(activeUser));

        assertThrows(BadRequestException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(javaMailSender, jwtService, userRoleResolver);
    }

    @Test
    void resendConfirmationCodeUpdatesCodeWhenCooldownPassed() {
        ResendConfirmationRequestDTO request = resendRequest("Pending.User@Gmail.com");
        UserEntity pendingUser = ServiceTestFixtures.user("pending.user@gmail.com", UserRole.TRAINEE, false);
        pendingUser.setEmail("pending.user@gmail.com");
        pendingUser.setProfileStatus(ProfileStatus.PENDING);
        pendingUser.setConfirmationCode("old-code");
        pendingUser.setConfirmationCodeExpiresAt(Instant.now().minusSeconds(60));
        pendingUser.setConfirmationCodeSentAt(Instant.now().minusSeconds(31));
        MimeMessage message = new MimeMessage((Session) null);

        when(userRepository.findByEmailIgnoreCase("pending.user@gmail.com")).thenReturn(Optional.of(pendingUser));
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        ReflectionTestUtils.setField(authService, "mailFrom", "no-reply@gym.test");

        AuthResponseDTO response = authService.resendConfirmationCode(request);

        assertEquals("Confirmation code sent. Please check your email.", response.getMessage());
        assertNull(response.getToken());
        assertTrue(pendingUser.getConfirmationCode().startsWith("encoded-"));
        assertTrue(pendingUser.getConfirmationCodeExpiresAt().isAfter(Instant.now()));
        assertTrue(pendingUser.getConfirmationCodeSentAt().isAfter(Instant.now().minusSeconds(5)));
        verify(userRepository).save(pendingUser);
        verify(javaMailSender).send(same(message));
    }

    @Test
    void resendConfirmationCodeRejectsRequestsInsideCooldown() {
        ResendConfirmationRequestDTO request = resendRequest("pending.user@gmail.com");
        UserEntity pendingUser = ServiceTestFixtures.user("pending.user@gmail.com", UserRole.TRAINEE, false);
        pendingUser.setEmail("pending.user@gmail.com");
        pendingUser.setProfileStatus(ProfileStatus.PENDING);
        pendingUser.setConfirmationCodeSentAt(Instant.now().minusSeconds(10));

        when(userRepository.findByEmailIgnoreCase("pending.user@gmail.com")).thenReturn(Optional.of(pendingUser));

        assertThrows(BadRequestException.class, () -> authService.resendConfirmationCode(request));

        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(javaMailSender);
    }

    @Test
    void registerRejectsPasswordConfirmationMismatch() {
        RegisterRequestDTO request = registerRequest("new.user@gmail.com", "Strong1", "Different1");

        assertThrows(BadRequestException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(javaMailSender, jwtService, userRoleResolver);
    }

    @Test
    void loginReturnsTokenAndResolvedRoleForActiveUserWithMatchingPassword() {
        LoginRequestDTO request = loginRequest("trainer.user", "secret");
        UserEntity user = ServiceTestFixtures.user("trainer.user", UserRole.TRAINER, true);

        when(userRepository.findByUsernameIgnoreCaseContains("trainer.user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken("trainer.user@example.com")).thenReturn("jwt-token");
        when(userRoleResolver.resolve(user)).thenReturn(UserRole.TRAINER);

        AuthResponseDTO response = authService.login(request);

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

        verify(jwtService, never()).generateToken(anyString());
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

        verify(securityService, never()).requireSameUserOrAdmin(anyString());
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

        AuthResponseDTO response = authService.currentSession();

        assertEquals("admin.user", response.getUsername());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void confirmActivatesUserAndReturnsEmailSubjectJwt() {
        ConfirmRequestDto request = confirmRequest("Pending.User@Gmail.com", "123456");
        UserEntity user = ServiceTestFixtures.user("pending.user", UserRole.TRAINEE, false);
        user.setEmail("pending.user@gmail.com");
        user.setProfileStatus(com.epam.gymmanagement.constant.ProfileStatus.PENDING);
        user.setConfirmationCode("encoded-code");
        user.setConfirmationCodeExpiresAt(Instant.now().plusSeconds(60));
        user.setConfirmationCodeSentAt(Instant.now());

        when(userRepository.findByEmailIgnoreCase("pending.user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded-code")).thenReturn(true);
        when(jwtService.generateToken("pending.user@gmail.com")).thenReturn("jwt-token");

        AuthResponseDTO response = authService.confirm(request);

        assertEquals("Email confirmed successfully! Thank you for your registration.", response.getMessage());
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertNull(response.getRole());
        assertTrue(user.getIsActive());
        assertEquals(com.epam.gymmanagement.constant.ProfileStatus.ACTIVE, user.getProfileStatus());
        assertNull(user.getConfirmationCode());
        assertNull(user.getConfirmationCodeExpiresAt());
        assertNull(user.getConfirmationCodeSentAt());
        verify(userRepository).save(user);
    }

    private LoginRequestDTO loginRequest(String username, String password) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private RegisterRequestDTO registerRequest(String email, String password, String passConfirm) {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail(email);
        request.setPassword(password);
        request.setPassConfirm(passConfirm);
        return request;
    }

    private ConfirmRequestDto confirmRequest(String email, String confirmation) {
        ConfirmRequestDto request = new ConfirmRequestDto();
        request.setEmail(email);
        request.setConfirmation(confirmation);
        return request;
    }

    private ResendConfirmationRequestDTO resendRequest(String email) {
        ResendConfirmationRequestDTO request = new ResendConfirmationRequestDTO();
        request.setEmail(email);
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
