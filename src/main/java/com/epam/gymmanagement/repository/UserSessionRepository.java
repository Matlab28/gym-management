package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {
    Optional<UserSessionEntity> findByTokenAndActiveTrue(String token);

    List<UserSessionEntity> findAllByUserAndActiveTrue(UserEntity user);

    boolean existsByTokenAndActiveTrue(String token);
}
