package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.entity.TrainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainerRepository extends JpaRepository<TrainerEntity, UUID> {
    Optional<TrainerEntity> findByUserUsername(String username);

    boolean existsByUserUsername(String username);

    List<TrainerEntity> findByUserIsActiveTrue();

    List<TrainerEntity> findByUserUsernameIn(List<String> usernames);
}
