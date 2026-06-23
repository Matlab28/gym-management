package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsernameIgnoreCaseContains(String username);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByRole(UserRole role);

    long countByIsActive(boolean isActive);

    long countByRole(UserRole role);

    UUID id(UUID id);
}
