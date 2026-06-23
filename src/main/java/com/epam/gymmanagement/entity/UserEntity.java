package com.epam.gymmanagement.entity;

import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.UserRole;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_table")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status", nullable = false)
    private ProfileStatus profileStatus;

    private Boolean isActive;

    private String confirmationCode;
    private Instant confirmationCodeExpiresAt;
    private Instant confirmationCodeSentAt;

    @PrePersist
    @PreUpdate
    void applyDefaults() {
        if (role == null) {
            role = UserRole.TRAINEE;
        }
        if (username == null || username.isBlank()) {
            username = email;
        }
        if (email == null || email.isBlank()) {
            email = username;
        }
        if (firstName == null) {
            firstName = "";
        }
        if (lastName == null) {
            lastName = "";
        }
        if (profileStatus == null) {
            profileStatus = Boolean.FALSE.equals(isActive)
                    ? ProfileStatus.INACTIVE
                    : ProfileStatus.ACTIVE;
        }
        if (isActive == null) {
            isActive = profileStatus == ProfileStatus.ACTIVE;
        }
    }
}
