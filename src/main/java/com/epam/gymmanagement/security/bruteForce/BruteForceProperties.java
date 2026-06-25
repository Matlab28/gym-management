package com.epam.gymmanagement.security.bruteForce;

import com.epam.gymmanagement.constant.AttemptType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "security.brute-force")
public class BruteForceProperties {

    private AttemptPolicy passwordLogin = new AttemptPolicy(
            5,
            Duration.ofMinutes(15),
            Duration.ofMinutes(15)
    );

    private AttemptPolicy emailVerification = new AttemptPolicy(
            5,
            Duration.ofMinutes(10),
            Duration.ofMinutes(10)
    );

    public AttemptPolicy getPasswordLogin() {
        return passwordLogin;
    }

    public void setPasswordLogin(AttemptPolicy passwordLogin) {
        this.passwordLogin = passwordLogin;
    }

    public AttemptPolicy getEmailVerification() {
        return emailVerification;
    }

    public void setEmailVerification(AttemptPolicy emailVerification) {
        this.emailVerification = emailVerification;
    }

    public AttemptPolicy getPolicy(AttemptType type) {
        return switch (type) {
            case PASSWORD_LOGIN -> passwordLogin;
            case EMAIL_VERIFICATION -> emailVerification;
        };
    }

    public static class AttemptPolicy {

        private int maxAttempts;
        private Duration attemptWindow;
        private Duration lockDuration;

        public AttemptPolicy() {
        }

        public AttemptPolicy(int maxAttempts, Duration attemptWindow, Duration lockDuration) {
            this.maxAttempts = maxAttempts;
            this.attemptWindow = attemptWindow;
            this.lockDuration = lockDuration;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getAttemptWindow() {
            return attemptWindow;
        }

        public void setAttemptWindow(Duration attemptWindow) {
            this.attemptWindow = attemptWindow;
        }

        public Duration getLockDuration() {
            return lockDuration;
        }

        public void setLockDuration(Duration lockDuration) {
            this.lockDuration = lockDuration;
        }
    }
}