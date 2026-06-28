package com.epam.gymmanagement.security;

import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.entity.UserSessionEntity;
import com.epam.gymmanagement.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSessionService {
    private static final Duration INACTIVITY_TIMEOUT = Duration.ofDays(15);
    private final UserSessionRepository userSessionRepository;

    @Transactional
    public void createSession(UserEntity user, String token) {
        Instant now = Instant.now();
        UserSessionEntity session = UserSessionEntity
                .builder()
                .user(user)
                .token(token)
                .createdAt(now)
                .lastActivityAt(now)
                .expiresAt(now.plus(INACTIVITY_TIMEOUT))
                .active(true)
                .build();
        userSessionRepository.save(session);
    }

    @Transactional
    public void updateLastActivity(String token) {
        userSessionRepository.findByTokenAndActiveTrue(token)
                .ifPresent(session -> {
                    Instant now = Instant.now();
                    session.setLastActivityAt(now);
                    session.setExpiresAt(now.plus(INACTIVITY_TIMEOUT));
                });
    }

    @Transactional
    public void signOut(String token) {
        userSessionRepository.findByTokenAndActiveTrue(token)
                .ifPresent(session -> session.setActive(false));
    }

    public boolean isSessionActive(String token) {
        return userSessionRepository.existsByTokenAndActiveTrue(token);
    }

    public boolean isInactive(String token) {
        return userSessionRepository.findByTokenAndActiveTrue(token)
                .map(session -> {
                    Instant now = Instant.now();
                    return session.getExpiresAt().isBefore(now)
                            || session.getLastActivityAt().plus(INACTIVITY_TIMEOUT).isBefore(now);
                })
                .orElse(false);
    }

    @Transactional
    public void signOutAll(UserEntity user) {

        List<UserSessionEntity> sessions =
                userSessionRepository.findAllByUserAndActiveTrue(user);

        for (UserSessionEntity session : sessions) {
            session.setActive(false);
        }

        userSessionRepository.saveAll(sessions);
    }
}
