package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.entity.TraineeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TraineeRepository extends JpaRepository<TraineeEntity, UUID> {
    Optional<TraineeEntity> findByUserEntity_Username(String username);

    boolean existsByUserEntity_Username(String username);
}
