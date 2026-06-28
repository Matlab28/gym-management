package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.AttemptType;
import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.request.*;
import com.epam.gymmanagement.dto.response.AuthResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.exception.BadRequestException;
import com.epam.gymmanagement.exception.NotFoundException;
import com.epam.gymmanagement.repository.UserRepository;
import com.epam.gymmanagement.security.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private static final long CONFIRMATION_CODE_EXPIRATION_SECONDS = 300; // 5 minutes
    private static final long CONFIRMATION_RESEND_COOLDOWN_SECONDS = 30;
    private static final String EMPTY_PROFILE_VALUE = "";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityService securityService;
    private final UserRoleResolver userRoleResolver;
    private final UserSessionService userSessionService;
    private final JavaMailSender javaMailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final BruteForceProtectionService bruteForceProtectionService;

    @Value("${spring.mail.from}")
    private String mailFrom;

    private String generateConfirmationCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = normalizeEmail(request.getEmail());

        if (!Objects.equals(request.getPassword(), request.getPassConfirm())) {
            throw new BadRequestException("Password and password confirmation do not match.");
        }

        String confirmationCode = generateConfirmationCode();
        Instant now = Instant.now();
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .map(existingUser -> prepareExistingPendingUser(
                        existingUser,
                        request.getPassword(),
                        confirmationCode,
                        now
                ))
                .orElseGet(() -> createPendingUser(email, request.getPassword(), confirmationCode, now));

        userRepository.save(user);
        sendEmail(email, confirmationCode);
        log.info("User registered with email: \"{}\" and waiting for email confirmation", email);
        return AuthResponseDTO.register(
                "Registration successful. Please confirm your email.",
                null
        );
    }

    private UserEntity createPendingUser(
            String email,
            String rawPassword,
            String confirmationCode,
            Instant sentAt
    ) {
        return UserEntity
                .builder()
                .email(email)
                .username(email)
                .firstName(EMPTY_PROFILE_VALUE)
                .lastName(EMPTY_PROFILE_VALUE)
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.TRAINEE)
                .profileStatus(ProfileStatus.PENDING)
                .isActive(false)
                .confirmationCode(passwordEncoder.encode(confirmationCode))
                .confirmationCodeExpiresAt(
                        sentAt.plusSeconds(CONFIRMATION_CODE_EXPIRATION_SECONDS)
                )
                .confirmationCodeSentAt(sentAt)
                .build();
    }

    private UserEntity prepareExistingPendingUser(
            UserEntity user,
            String rawPassword,
            String confirmationCode,
            Instant sentAt
    ) {
        if (user.getProfileStatus() == ProfileStatus.ACTIVE) {
            throw new BadRequestException("This email is already registered. Please log in.");
        }

        if (user.getProfileStatus() != ProfileStatus.PENDING) {
            throw new BadRequestException("This email cannot be registered again.");
        }

        ensureConfirmationRequestAllowed(user, sentAt);

        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setIsActive(false);
        user.setConfirmationCode(passwordEncoder.encode(confirmationCode));
        user.setConfirmationCodeExpiresAt(
                sentAt.plusSeconds(CONFIRMATION_CODE_EXPIRATION_SECONDS)
        );
        user.setConfirmationCodeSentAt(sentAt);

        return user;
    }

    @Transactional
    public AuthResponseDTO resendConfirmationCode(ResendConfirmationRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getProfileStatus() == ProfileStatus.ACTIVE) {
            throw new BadRequestException("Email is already confirmed.");
        }

        if (user.getProfileStatus() != ProfileStatus.PENDING) {
            throw new BadRequestException("Confirmation code cannot be sent for this user.");
        }

        Instant now = Instant.now();
        ensureConfirmationRequestAllowed(user, now);

        String confirmationCode = generateConfirmationCode();
        user.setConfirmationCode(passwordEncoder.encode(confirmationCode));
        user.setConfirmationCodeExpiresAt(now.plusSeconds(CONFIRMATION_CODE_EXPIRATION_SECONDS));
        user.setConfirmationCodeSentAt(now);
        userRepository.save(user);

        sendEmail(email, confirmationCode);
        log.info("Confirmation code resent to email={}", email);

        return AuthResponseDTO.register(
                "Confirmation code sent. Please check your email.",
                null
        );
    }

    private void ensureConfirmationRequestAllowed(UserEntity user, Instant now) {
        Instant lastSentAt = user.getConfirmationCodeSentAt();

        if (lastSentAt == null) {
            return;
        }

        Instant nextAllowedAt = lastSentAt.plusSeconds(CONFIRMATION_RESEND_COOLDOWN_SECONDS);

        if (nextAllowedAt.isAfter(now)) {
            throw new BadRequestException("Please wait 30 seconds before requesting another confirmation code.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required.");
        }

        return email.trim().toLowerCase();
    }

    private String normalizeLogin(LoginRequestDTO request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return normalizeEmail(request.getEmail());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            return request.getUsername().trim();
        }

        throw new BadRequestException("Email or username is required.");
    }

    private Optional<UserEntity> findByEmailOrUsername(String login) {
        if (login.contains("@")) {
            Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(login);

            if (user.isPresent()) {
                return user;
            }
        }

        return userRepository.findByUsernameIgnoreCaseContains(login);
    }

    private UserEntity findPasswordChangeUser(ChangePasswordRequestDTO request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return userRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                    .orElseThrow(() -> new NotFoundException("User not found"));
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            return userRepository.findByUsernameIgnoreCaseContains(request.getUsername().trim())
                    .orElseThrow(() -> new NotFoundException("User not found"));
        }

        throw new BadRequestException("Email or username is required.");
    }

    private String tokenSubject(UserEntity user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail().trim().toLowerCase();
        }

        return user.getUsername();
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        String login = normalizeLogin(request);

        bruteForceProtectionService.checkNotBlocked(
                AttemptType.PASSWORD_LOGIN,
                login
        );

        UserEntity user = findByEmailOrUsername(login)
                .orElseThrow(() -> {
                    bruteForceProtectionService.registerFailure(
                            AttemptType.PASSWORD_LOGIN,
                            login
                    );

                    return new BadRequestException("Invalid email or password");
                });

        if (user.getProfileStatus() != ProfileStatus.ACTIVE) {
            throw new BadRequestException("Please confirm your email before logging in.");
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            bruteForceProtectionService.registerFailure(
                    AttemptType.PASSWORD_LOGIN,
                    login
            );

            throw new BadRequestException("Invalid email or password");
        }

        bruteForceProtectionService.registerSuccess(
                AttemptType.PASSWORD_LOGIN,
                login
        );

        String token = jwtService.generateToken(tokenSubject(user));
        userSessionService.createSession(user, token);
        UserRole role = userRoleResolver.resolve(user);

        log.info("User with email={} logged in with role {}", user.getEmail(), role);

        return AuthResponseDTO.login("Login successful", token, role.name());
    }

    @Transactional
    public MessageResponseDTO changePassword(ChangePasswordRequestDTO request) {
        UserEntity user = findPasswordChangeUser(request);

        securityService.requireSameUserOrAdmin(user.getUsername());

        if (user.getProfileStatus() == ProfileStatus.INACTIVE) {
            throw new BadRequestException("User profile is inactive");
        } else if (user.getProfileStatus() == ProfileStatus.PENDING) {
            throw new BadRequestException("User profile is pending approval");
        } else if (user.getProfileStatus() == ProfileStatus.SUSPENDED) {
            throw new BadRequestException("User profile is suspended");
        } else if (user.getProfileStatus() == ProfileStatus.DELETED) {
            throw new BadRequestException("User profile is deleted");
        }

        if (!passwordEncoder.matches(
                request.getOldPassword(),
                user.getPassword())) {

            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        userSessionService.signOutAll(user);

        log.info("Password changed successfully for username={}", user.getUsername());

        return new MessageResponseDTO(
                "Password changed successfully. Please log in again."
        );
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO currentSession() {
        String username = securityService.currentUsername();
        UserEntity user = userRepository.findByUsernameIgnoreCaseContains(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return AuthResponseDTO.session(username, userRoleResolver.resolve(user).name());
    }

    private String extractBearerToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BadRequestException("Authorization token is required.");
        }

        return header.substring(7).trim();
    }

    @Transactional
    public MessageResponseDTO signOut(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        userSessionService.signOut(token);
        SecurityContextHolder.clearContext();
        log.info("User signed out.");
        return new MessageResponseDTO("Signed out successfully.");
    }

    @Transactional
    public AuthResponseDTO confirm(ConfirmRequestDto dto) {
        String email = normalizeEmail(dto.getEmail());

        bruteForceProtectionService.checkNotBlocked(
                AttemptType.EMAIL_VERIFICATION,
                email
        );

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getProfileStatus() == ProfileStatus.ACTIVE) {
            throw new BadRequestException("Email is already confirmed.");
        }

        if (user.getConfirmationCode() == null
                || user.getConfirmationCodeExpiresAt() == null) {

            bruteForceProtectionService.registerFailure(
                    AttemptType.EMAIL_VERIFICATION,
                    email
            );

            throw new BadRequestException(
                    "Confirmation code was not generated. Please request a new code."
            );
        }

        if (user.getConfirmationCodeExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException(
                    "Confirmation code expired. Please request a new code."
            );
        }

        boolean confirmationMatches = passwordEncoder.matches(
                dto.getConfirmation(),
                user.getConfirmationCode()
        );

        if (!confirmationMatches) {

            bruteForceProtectionService.registerFailure(
                    AttemptType.EMAIL_VERIFICATION,
                    email
            );

            throw new BadRequestException("Invalid confirmation code.");
        }

        bruteForceProtectionService.registerSuccess(
                AttemptType.EMAIL_VERIFICATION,
                email
        );

        user.setProfileStatus(ProfileStatus.ACTIVE);
        user.setIsActive(true);
        user.setConfirmationCode(null);
        user.setConfirmationCodeExpiresAt(null);
        user.setConfirmationCodeSentAt(null);

        userRepository.save(user);

        log.info("Email confirmed successfully for email={}", user.getEmail());
        return AuthResponseDTO.register(
                "Email confirmed successfully. Please log in.",
                null
        );
    }

    private void sendEmail(String email, String confirmation) {
        if (email == null || email.isBlank()) {
            log.error("Email address is null or blank from user. Cannot send an email.");
            return;
        }

        String toEmail = normalizeEmail(email);

        try {
            String subject = "Email Confirmation";
            String htmlContent = html(confirmation);

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            log.info("Confirmation email sent to - {}", toEmail);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send confirmation email to {}", toEmail, e);
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }

    private String html(String confirmation) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Email Confirmation</title>
                </head>
                
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color:#f4f6f8; padding:40px 0;">
                        <tr>
                            <td align="center">
                
                                <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                                       style="max-width:600px; background-color:#ffffff; border-radius:14px; overflow:hidden; box-shadow:0 8px 24px rgba(0,0,0,0.08);">
                
                                    <tr>
                                        <td align="center" style="background-color:#111827; padding:32px 24px;">
                                            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/Effective_Programming_for_America_logo.svg/3840px-Effective_Programming_for_America_logo.svg.png"
                                                 alt="EPAM Logo"
                                                 width="150"
                                                 style="display:block; margin:0 auto 18px auto; max-width:150px; height:auto;">
                
                                            <h1 style="margin:0; color:#ffffff; font-size:24px; font-weight:700; letter-spacing:0.3px;">
                                                EPAM Gym Management
                                            </h1>
                
                                            <p style="margin:8px 0 0 0; color:#d1d5db; font-size:14px;">
                                                Email Confirmation
                                            </p>
                                        </td>
                                    </tr>
                
                                    <tr>
                                        <td style="padding:36px 32px 28px 32px;">
                
                                            <h2 style="margin:0 0 18px 0; color:#111827; font-size:22px; font-weight:700;">
                                                Confirm Your Registration
                                            </h2>
                
                                            <p style="margin:0 0 18px 0; color:#374151; font-size:16px; line-height:1.6;">
                                                Hi,
                                            </p>
                
                                            <p style="margin:0 0 18px 0; color:#374151; font-size:16px; line-height:1.6;">
                                                Thank you for your <strong>EPAM Gym Management</strong> registration.
                                                Please use the confirmation code below to complete your email verification.
                                            </p>
                
                                            <div style="margin:32px 0; text-align:center;">
                                                <div style="display:inline-block; background-color:#f3f4f6; border:1px solid #d1d5db; border-radius:10px; padding:18px 34px;">
                                                    <span style="color:#111827; font-size:30px; font-weight:700; letter-spacing:5px;">
                                                        %s
                                                    </span>
                                                </div>
                                            </div>
                
                                            <p style="margin:0 0 16px 0; color:#374151; font-size:15px; line-height:1.6;">
                                                Enter this code in the application to activate your account.
                                            </p>
                
                                            <p style="margin:24px 0 0 0; padding:14px 16px; background-color:#fff7ed; border-left:4px solid #f97316; color:#9a3412; font-size:14px; line-height:1.6;">
                                                <strong>Security notice:</strong> Do not share this code with anyone.
                                            </p>
                
                                            <p style="margin:28px 0 0 0; color:#6b7280; font-size:14px; line-height:1.6;">
                                                If you did not request this registration, you can safely ignore this email.
                                            </p>
                
                                        </td>
                                    </tr>
                
                                    <tr>
                                        <td align="center" style="background-color:#f9fafb; padding:22px 24px; border-top:1px solid #e5e7eb;">
                                            <p style="margin:0; color:#9ca3af; font-size:12px; line-height:1.5;">
                                                © 2026 EPAM Gym Management. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                
                                </table>
                
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(confirmation);
    }
}
