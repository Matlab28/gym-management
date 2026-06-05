package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.entity.TrainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainerRepository extends JpaRepository<TrainerEntity, UUID> {
    Optional<TrainerEntity> findByUserEntity_Username(String username);

    boolean existsByUserEntity_Username(String username);

    List<TrainerEntity> findByUserEntity_IsActiveTrue();

    List<TrainerEntity> findByUserEntity_UsernameIn(List<String> usernames);
}
