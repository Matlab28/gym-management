package com.epam.gymmanagement.security;

import com.epam.gymmanagement.constant.AttemptType;
import com.epam.gymmanagement.security.bruteForce.BruteForceProperties;
import com.epam.gymmanagement.security.bruteForce.TooManyAttemptsException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {

    private final Map<AttemptKey, AttemptState> attempts = new ConcurrentHashMap<>();
    private final BruteForceProperties bruteForceProperties;
    private final Clock clock;

    public BruteForceProtectionService(BruteForceProperties bruteForceProperties) {
        this.bruteForceProperties = bruteForceProperties;
        this.clock = Clock.systemUTC();
    }

    public void checkNotBlocked(AttemptType type, String identifier) {
        AttemptKey key = new AttemptKey(type, normalize(identifier));
        AttemptState state = attempts.get(key);

        if (state == null) {
            return;
        }

        Instant now = clock.instant();
        BruteForceProperties.AttemptPolicy policy = bruteForceProperties.getPolicy(type);

        if (state.lockedUntil != null) {
            if (state.lockedUntil.isAfter(now)) {
                long retryAfterSeconds = Duration.between(now, state.lockedUntil).toSeconds();

                throw new TooManyAttemptsException(
                        "Too many failed attempts. Please try again later.",
                        Math.max(retryAfterSeconds, 1)
                );
            }

            attempts.remove(key);
            return;
        }

        if (isAttemptWindowExpired(state, policy, now)) {
            attempts.remove(key);
        }
    }

    public void registerFailure(AttemptType type, String identifier) {
        AttemptKey key = new AttemptKey(type, normalize(identifier));
        BruteForceProperties.AttemptPolicy policy = bruteForceProperties.getPolicy(type);
        Instant now = clock.instant();

        attempts.compute(key, (ignored, currentState) -> {
            if (currentState == null || isAttemptWindowExpired(currentState, policy, now)) {
                return new AttemptState(1, now, now, null);
            }

            int updatedFailedAttempts = currentState.failedAttempts + 1;
            Instant lockedUntil = currentState.lockedUntil;

            if (updatedFailedAttempts >= policy.getMaxAttempts()) {
                lockedUntil = now.plus(policy.getLockDuration());
            }

            return new AttemptState(
                    updatedFailedAttempts,
                    currentState.firstFailedAt,
                    now,
                    lockedUntil
            );
        });
    }

    public void registerSuccess(AttemptType type, String identifier) {
        AttemptKey key = new AttemptKey(type, normalize(identifier));
        attempts.remove(key);
    }

    public int getRemainingAttempts(AttemptType type, String identifier) {
        AttemptKey key = new AttemptKey(type, normalize(identifier));
        AttemptState state = attempts.get(key);

        if (state == null) {
            return bruteForceProperties.getPolicy(type).getMaxAttempts();
        }

        int maxAttempts = bruteForceProperties.getPolicy(type).getMaxAttempts();
        return Math.max(maxAttempts - state.failedAttempts, 0);
    }

    @Scheduled(fixedDelayString = "${security.brute-force.cleanup-delay-ms:600000}")
    public void cleanExpiredAttempts() {
        Instant now = clock.instant();

        attempts.entrySet().removeIf(entry -> {
            AttemptType type = entry.getKey().type();
            AttemptState state = entry.getValue();
            BruteForceProperties.AttemptPolicy policy = bruteForceProperties.getPolicy(type);

            boolean lockExpired = state.lockedUntil != null && state.lockedUntil.isBefore(now);
            boolean windowExpired = isAttemptWindowExpired(state, policy, now);

            return lockExpired || windowExpired;
        });
    }

    private boolean isAttemptWindowExpired(
            AttemptState state,
            BruteForceProperties.AttemptPolicy policy,
            Instant now
    ) {
        return state.firstFailedAt
                .plus(policy.getAttemptWindow())
                .isBefore(now);
    }

    private String normalize(String identifier) {
        if (identifier == null) {
            return "";
        }

        return identifier.trim().toLowerCase(Locale.ROOT);
    }

    private record AttemptKey(
            AttemptType type,
            String identifier
    ) {
    }

    private record AttemptState(
            int failedAttempts,
            Instant firstFailedAt,
            Instant lastFailedAt,
            Instant lockedUntil
    ) {
    }
}